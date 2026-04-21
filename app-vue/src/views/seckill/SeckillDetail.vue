<script setup lang="ts">
/**
 * 秒杀详情页面
 *
 * 页面功能：
 * 1. 展示秒杀活动的基本信息（名称、描述、时间、状态等）
 * 2. 展示活动关联的商品列表（用户选择要抢购的商品）
 * 3. 展示商品库存（从 Redis 实时读取）
 * 4. 提供抢购按钮，参与秒杀活动
 * 5. 支持 SSE 实时接收抢购结果
 * 6. 支持活动预约（活动未开始时）
 *
 * 数据流向：
 * - 活动详情: activityApi.getDetail(id) -> 后端 ActivityService.getActivityDetail()
 * - 活动库存: seckillApi.getStock(id) -> 后端 PreheatService.getActivityStock()
 * - 抢购结果: seckillApi.subscribeSeckillResult() -> SSE 推送
 *
 * 状态机：
 * - status=0 (未开始): 显示"预约提醒"按钮
 * - status=1 (进行中): 显示"立即抢购"按钮
 * - status=2 (已结束): 按钮禁用，显示"活动已结束"
 */
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { seckillApi, activityApi, type ActivityDetail, type Goods, type SeckillResponse } from '@/api/seckill'

const route = useRoute()
const router = useRouter()

// ==================== 响应式数据 ====================

/** 活动详情（从后端获取，包含商品列表） */
const activity = ref<ActivityDetail | null>(null)

/** 当前选中的商品 */
const selectedGoods = ref<Goods | null>(null)

/** 商品库存（从 Redis 获取，显示实时库存） */
const stock = ref(0)

/** 页面加载状态（控制 v-loading） */
const loading = ref(false)

/** 抢购按钮加载状态（防止重复点击） */
const seckilling = ref(false)

/** SSE 连接实例（用于取消连接） */
let eventSource: EventSource | null = null

// ==================== 计算属性 ====================

/**
 * 活动状态文本
 * 用于页面展示，如"未开始"、"进行中"、"已结束"
 */
const statusText = computed(() => {
  if (!activity.value) return '加载中'
  const map: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[activity.value.status] || '未知'
})

/**
 * 活动状态类型
 * 用于 Element Plus 标签组件的颜色映射
 * - warning: 未开始（黄色）
 * - success: 进行中（绿色）
 * - info: 已结束（灰色）
 */
const statusType = computed(() => {
  if (!activity.value) return 'info'
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[activity.value.status] || 'info'
})

// ==================== 工具函数 ====================

/**
 * 格式化时间字符串为可读格式
 *
 * @param time ISO 格式时间字符串（如 "2024-01-15T10:30:00"）
 * @returns 中文格式时间字符串（如 "2024/1/15 上午10:30:00"）
 */
function formatTime(time: string) {
  return new Date(time).toLocaleString('zh-CN')
}

// ==================== 数据加载 ====================

/**
 * 加载活动详情
 *
 * 流程：
 * 1. 从 URL 参数获取活动 ID
 * 2. 调用 activityApi.getDetail() 获取活动信息（含商品列表）
 * 3. 活动信息加载成功后，默认选中第一个商品
 * 4. 调用 loadStock() 获取库存
 */
async function loadActivity() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await activityApi.getDetail(id)
    if (res.code === 200) {
      activity.value = res.data
      // 默认选中第一个商品
      if (res.data.goodsList && res.data.goodsList.length > 0) {
        selectedGoods.value = res.data.goodsList[0]
      }
      // 活动信息加载成功后，获取库存
      await loadStock()
    } else {
      ElMessage.error(res.message || '加载活动详情失败')
    }
  } catch (error) {
    console.error('加载活动详情失败', error)
    ElMessage.error('加载活动详情失败，请重试')
  } finally {
    loading.value = false
  }
}

/**
 * 加载活动库存
 *
 * 库存数据来自 Redis（预热时写入），不是数据库
 * 这样可以支撑高并发的库存查询
 *
 * 响应数据格式：直接是数字，不是 { stock: number }
 */
async function loadStock() {
  // 使用选中的商品ID查询库存
  if (!selectedGoods.value?.id) return
  try {
    const res = await seckillApi.getStock(selectedGoods.value.id)
    if (res.code === 200) {
      stock.value = res.data // 直接是库存数字
    }
  } catch (error) {
    console.error('加载库存失败', error)
  }
}

/**
 * 选择商品时更新库存显示
 */
function selectGoods(goods: Goods) {
  selectedGoods.value = goods
  loadStock()
}

// ==================== 抢购流程 ====================

/**
 * 执行秒杀抢购
 *
 * 完整流程：
 * 1. 前端生成幂等键（防止重复提交）
 * 2. 获取活动签名（可选，用于后端验证请求合法性）
 * 3. 调用抢购接口 seckillApi.buy()
 * 4. 如果返回 queueId，建立 SSE 连接监听结果
 * 5. 根据 SSE 推送的结果显示成功/失败提示
 */
async function handleSeckill() {
  if (!activity.value) {
    ElMessage.warning('活动信息加载中，请稍后')
    return
  }

  if (!selectedGoods.value) {
    ElMessage.warning('请先选择要抢购的商品')
    return
  }

  if (activity.value.status !== 1) {
    ElMessage.warning('活动未开始或已结束')
    return
  }

  seckilling.value = true

  try {
    // 1. 生成幂等键
    // 格式: user_{userId}_{activityId}_{timestamp}
    // 后端用这个键做幂等校验，防止用户重复提交
    const idempotentKey = `user_${Date.now()}_${activity.value.id}`

    // 2. 获取签名（可选增强安全性）
    // 签名 = HMAC(activityId + userId + timestamp, signKey)
    // 后端会验证签名是否正确、是否过期
    let sign: string | undefined
    let timestamp: number | undefined
    try {
      const signRes = await seckillApi.getSign(activity.value.id)
      if (signRes.code === 200) {
        sign = signRes.data.sign
        timestamp = signRes.data.timestamp
      }
    } catch (e) {
      console.warn('获取签名失败，使用无签名模式', e)
    }

    // 3. 调用抢购接口（使用用户选择的商品ID）
    const res = await seckillApi.buy({
      goodsId: selectedGoods.value.id,   // 使用选中的商品ID
      activityId: activity.value.id,   // 活动ID
      sign,
      timestamp,
      idempotentKey
    })

    // 4. 处理响应
    if (res.code === 200 && res.data.queueId) {
      // 抢购请求入队成功，开始 SSE 订阅等待结果
      ElMessage.info('正在处理您的请求，请稍候...')
      subscribeSeckillResult(res.data.queueId)
    } else {
      ElMessage.error(res.message || '抢购失败')
    }
  } catch (error) {
    console.error('抢购失败', error)
    ElMessage.error('抢购失败，请稍后重试')
  } finally {
    seckilling.value = false
  }
}

/**
 * 订阅秒杀结果（SSE - Server-Sent Events）
 *
 * 为什么用 SSE 而不是轮询？
 * - SSE 是服务端推送，实时性更好
 * - 比轮询更省资源，不需要频繁发请求
 *
 * SSE 事件类型：
 * - seckill_result: 最终结果（成功/失败）
 * - seckill_status: 状态更新（如排队中、处理中）
 * - heartbeat: 心跳（保持连接）
 * - completed: 连接完成
 *
 * @param queueId 队列ID（从抢购接口返回）
 */
function subscribeSeckillResult(queueId: string) {
  // 关闭已有连接（如果用户重复点击）
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  // 建立 SSE 连接
  // 格式: /api/seckill/subscribe/{queueId}
  eventSource = seckillApi.subscribeSeckillResult(
    queueId,
    // 消息回调：处理秒杀结果
    (data: SeckillResponse) => {
      if (data.status === 1) {
        // status=1: 抢购成功
        ElMessage.success('恭喜！抢购成功！')
        // 跳转到订单确认页面
        router.push('/order/confirm')
      } else if (data.status === 2) {
        // status=2: 抢购失败
        ElMessage.error(data.message || '抢购失败')
      } else {
        // status=0: 排队中或其他状态
        ElMessage.info(data.message || '处理中...')
      }

      // 处理完毕，关闭连接
      eventSource?.close()
      eventSource = null
    },
    // 错误回调：处理连接异常
    (error: Event) => {
      console.error('SSE 连接错误', error)
      ElMessage.warning('实时通知连接中断，请刷新页面重试')
    }
  )
}

/**
 * 预约活动（活动未开始时）
 *
 * 用户预约后，活动开始前会收到通知
 * 预约信息存储在 Redis Set 中（seckill:reservation:{activityId}）
 */
async function handleReserve() {
  if (!activity.value) return

  try {
    await activityApi.reserve(activity.value.id)
    ElMessage.success('预约成功，活动开始前会通知您')
  } catch (error) {
    console.error('预约失败', error)
    ElMessage.error('预约失败，请重试')
  }
}

// ==================== 生命周期 ====================

/**
 * 组件挂载时：加载活动详情
 */
onMounted(() => {
  loadActivity()
})

/**
 * 组件卸载时：清理 SSE 连接
 * 防止用户切换页面后连接仍在，造成资源浪费
 */
onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
})
</script>

<template>
  <div class="seckill-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>秒杀详情</span>
          <el-button text @click="router.back()">返回</el-button>
        </div>
      </template>

      <div v-if="activity" class="activity-content">
        <!-- 活动基本信息 -->
        <div class="activity-info">
          <h2 class="activity-name">{{ activity.name }}</h2>

          <div class="activity-meta">
            <el-tag :type="statusType">{{ statusText }}</el-tag>
            <span class="per-limit">限购：{{ activity.perLimit }} 件/人</span>
          </div>

          <p class="activity-desc">{{ activity.description }}</p>

          <div class="activity-time">
            <p>开始时间：{{ formatTime(activity.startTime) }}</p>
            <p>结束时间：{{ formatTime(activity.endTime) }}</p>
          </div>
        </div>

        <!-- 商品列表 -->
        <div v-if="activity.goodsList && activity.goodsList.length > 0" class="goods-section">
          <h3>秒杀商品</h3>
          <div class="goods-list">
            <div
              v-for="goods in activity.goodsList"
              :key="goods.id"
              class="goods-item"
              :class="{ selected: selectedGoods?.id === goods.id }"
              @click="selectGoods(goods)"
            >
              <img v-if="goods.imageUrl" :src="goods.imageUrl" class="goods-image" />
              <div v-else class="goods-image goods-image-placeholder">暂无图片</div>
              <div class="goods-info">
                <div class="goods-name">{{ goods.name }}</div>
                <div class="goods-price">
                  <span class="seckill-price">￥{{ goods.seckillPrice }}</span>
                  <span class="original-price">￥{{ goods.originalPrice }}</span>
                </div>
                <div class="goods-stock">库存：{{ goods.stock }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 库存信息 -->
        <div class="stock-info">
          <div class="stock-label">当前库存</div>
          <div class="stock-value">{{ stock }}</div>
          <el-button link type="primary" @click="loadStock">刷新库存</el-button>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
            v-if="activity.status === 1 && selectedGoods"
            type="danger"
            size="large"
            :loading="seckilling"
            @click="handleSeckill"
          >
            {{ seckilling ? '正在抢购...' : '立即抢购' }}
          </el-button>

          <el-button
            v-else-if="activity.status === 0"
            type="warning"
            size="large"
            @click="handleReserve"
          >
            预约提醒
          </el-button>

          <el-button
            v-else
            type="info"
            size="large"
            disabled
          >
            活动已结束
          </el-button>
        </div>

        <!-- 提示信息 -->
        <div class="tips">
          <h4>抢购须知：</h4>
          <ul>
            <li>每个用户每个活动限购 {{ activity.perLimit }} 件</li>
            <li>抢购成功后请在 15 分钟内完成支付</li>
            <li>超时未支付订单将自动取消</li>
            <li>库存有限，抢完即止</li>
          </ul>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <p>活动信息加载失败</p>
        <el-button type="primary" @click="loadActivity">重新加载</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.seckill-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-name {
  margin: 0;
  font-size: 24px;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.per-limit {
  color: #666;
}

.activity-desc {
  margin: 0;
  color: #333;
  line-height: 1.6;
}

.activity-time {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 4px;
}

.activity-time p {
  margin: 4px 0;
  color: #666;
}

.goods-section {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.goods-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.goods-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.goods-item {
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.goods-item:hover {
  border-color: #ddd;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.goods-item.selected {
  border-color: #f56c6c;
  background: #fff5f5;
}

.goods-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 8px;
}

.goods-image-placeholder {
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
}

.goods-info {
  text-align: center;
}

.goods-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  margin-bottom: 4px;
}

.seckill-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 8px;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.goods-stock {
  font-size: 12px;
  color: #666;
}

.stock-info {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.stock-label {
  font-size: 14px;
  opacity: 0.9;
}

.stock-value {
  font-size: 48px;
  font-weight: bold;
  margin: 8px 0;
}

.action-buttons {
  text-align: center;
}

.action-buttons .el-button {
  width: 200px;
  height: 50px;
  font-size: 18px;
}

.tips {
  background: #fffbe6;
  padding: 16px;
  border-radius: 4px;
  border-left: 4px solid #faad14;
}

.tips h4 {
  margin: 0 0 8px 0;
  color: #d48806;
}

.tips ul {
  margin: 0;
  padding-left: 20px;
  color: #8c8c8c;
}

.tips li {
  margin: 4px 0;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
