<script setup lang="ts">
/**
 * 秒杀详情页面
 *
 * 功能说明：
 * 1. 显示商品详细信息
 * 2. 显示活动倒计时
 * 3. 执行秒杀抢购
 * 4. SSE 实时接收结果
 */
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { seckillApi, activityApi, type Activity, type SeckillResponse } from '@/api/seckill'

const route = useRoute()
const router = useRouter()

/** 活动详情 */
const activity = ref<Activity | null>(null)
/** 商品库存 */
const stock = ref(0)
/** 加载状态 */
const loading = ref(false)
/** 抢购中状态 */
const seckilling = ref(false)
/** SSE 连接 */
let eventSource: EventSource | null = null

/**
 * 活动状态文本
 */
const statusText = computed(() => {
  if (!activity.value) return '加载中'
  const map: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[activity.value.status] || '未知'
})

/**
 * 活动状态类型（用于标签颜色）
 */
const statusType = computed(() => {
  if (!activity.value) return 'info'
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[activity.value.status] || 'info'
})

/**
 * 格式化时间
 */
function formatTime(time: string) {
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 加载活动详情
 */
async function loadActivity() {
  loading.value = true
  try {
    const id = Number(route.params.id)
    const res = await activityApi.get(id)
    if (res.code === 200) {
      activity.value = res.data
      await loadStock()
    }
  } catch (error) {
    ElMessage.error('加载活动详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载商品库存
 */
async function loadStock() {
  if (!activity.value?.id) return
  try {
    const res = await seckillApi.getStock(activity.value.id)
    if (res.code === 200) {
      stock.value = res.data.stock
    }
  } catch (error) {
    console.error('加载库存失败', error)
  }
}

/**
 * 执行秒杀抢购
 */
async function handleSeckill() {
  if (!activity.value) {
    ElMessage.warning('活动信息加载中，请稍后')
    return
  }

  if (activity.value.status !== 1) {
    ElMessage.warning('活动未开始或已结束')
    return
  }

  seckilling.value = true

  try {
    // 生成幂等键
    const idempotentKey = `user_${Date.now()}_${activity.value.id}`

    // 获取签名（可选）
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

    // 调用秒杀接口
    const res = await seckillApi.buy({
      goodsId: activity.value.id,
      activityId: activity.value.id,
      sign,
      timestamp,
      idempotentKey
    })

    if (res.code === 200 && res.data.queueId) {
      // 抢购请求成功，开始 SSE 订阅
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
 * 订阅秒杀结果（SSE）
 *
 * @param queueId 队列ID
 */
function subscribeSeckillResult(queueId: string) {
  // 关闭已有连接
  if (eventSource) {
    eventSource.close()
  }

  // 建立 SSE 连接
  eventSource = seckillApi.subscribeSeckillResult(
    queueId,
    (data: SeckillResponse) => {
      if (data.status === 1) {
        // 抢购成功
        ElMessage.success('恭喜！抢购成功！')
        // 跳转到购物车或订单页面
        router.push('/order/confirm')
      } else if (data.status === 2) {
        // 抢购失败
        ElMessage.error(data.message || '抢购失败')
      } else {
        // 其他状态
        ElMessage.info(data.message || '处理中...')
      }

      // 关闭 SSE 连接
      eventSource?.close()
      eventSource = null
    },
    (error: Event) => {
      console.error('SSE 连接错误', error)
      ElMessage.warning('实时通知连接中断，请刷新页面')
    }
  )
}

/**
 * 预约活动
 */
async function handleReserve() {
  if (!activity.value) return

  try {
    await activityApi.reserve(activity.value.id)
    ElMessage.success('预约成功，活动开始前会通知您')
  } catch (error) {
    ElMessage.error('预约失败')
  }
}

/**
 * 组件挂载时
 */
onMounted(() => {
  loadActivity()
})

/**
 * 组件卸载时
 */
onUnmounted(() => {
  // 关闭 SSE 连接
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

        <!-- 库存信息 -->
        <div class="stock-info">
          <div class="stock-label">当前库存</div>
          <div class="stock-value">{{ stock }}</div>
          <el-button link type="primary" @click="loadStock">刷新库存</el-button>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
            v-if="activity.status === 1"
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
