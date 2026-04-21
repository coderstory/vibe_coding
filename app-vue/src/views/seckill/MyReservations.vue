<script setup lang="ts">
/**
 * 我的预约页面
 *
 * 功能：
 * 1. 显示用户已预约的秒杀活动列表
 * 2. 显示预约状态（预约中、已提醒、已过期）
 * 3. 提供取消预约功能（可选）
 *
 * 数据来源：
 * - GET /api/reservation/my - 获取当前用户的预约列表
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { activityApi, type Reservation } from '@/api/seckill'

/** 预约记录（包含活动详情） */
interface ReservationWithActivity extends Reservation {
  /** 活动名称 */
  activityName?: string
  /** 活动开始时间 */
  activityStartTime?: string
  /** 活动结束时间 */
  activityEndTime?: string
  /** 活动状态 */
  activityStatus?: number
}

/** 预约列表 */
const reservations = ref<ReservationWithActivity[]>([])
const loading = ref(false)

/**
 * 组件挂载时加载预约列表
 */
onMounted(async () => {
  loading.value = true
  try {
    // 调用 GET /api/reservation/my 获取当前用户的预约列表
    const res = await activityApi.getMyReservations()
    reservations.value = res.data || []

    // 补充活动详情（需要分别调用活动详情接口）
    await enrichActivityDetails()
  } catch (error) {
    console.error('加载预约列表失败', error)
    ElMessage.error('加载预约列表失败，请重试')
  } finally {
    loading.value = false
  }
})

/**
 * 补充活动详情信息
 *
 * 因为后端 /api/reservation/my 只返回预约记录，
 * 不包含活动详情，所以需要单独查询活动信息
 */
async function enrichActivityDetails() {
  for (const reservation of reservations.value) {
    try {
      const res = await activityApi.get(reservation.activityId)
      if (res.code === 200 && res.data) {
        reservation.activityName = res.data.name
        reservation.activityStartTime = res.data.startTime
        reservation.activityEndTime = res.data.endTime
        reservation.activityStatus = res.data.status
      }
    } catch (error) {
      console.error('加载活动详情失败', error)
    }
  }
}

/**
 * 格式化时间
 */
function formatTime(time: string | null | undefined) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 获取预约状态文本
 */
function getStatusText(status: number) {
  const map: Record<number, string> = {
    0: '预约中',
    1: '已提醒',
    2: '已过期'
  }
  return map[status] || '未知'
}

/**
 * 获取预约状态标签类型
 */
function getStatusType(status: number) {
  const map: Record<number, string> = {
    0: 'warning',  // 预约中 - 黄色
    1: 'success',  // 已提醒 - 绿色
    2: 'info'      // 已过期 - 灰色
  }
  return map[status] || 'info'
}

/**
 * 获取活动状态文本
 */
function getActivityStatusText(status: number | undefined) {
  if (status === undefined) return '-'
  const map: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return map[status] || '未知'
}

/**
 * 获取活动状态标签类型
 */
function getActivityStatusType(status: number | undefined) {
  if (status === undefined) return 'info'
  const map: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return map[status] || 'info'
}

/**
 * 跳转到活动详情页
 */
function goToDetail(activityId: number) {
  window.location.href = `/seckill/detail/${activityId}`
}
</script>

<template>
  <div class="my-reservations">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
          <el-button type="primary" link @click="$router.push('/seckill')">
            浏览秒杀活动
          </el-button>
        </div>
      </template>

      <!-- 空状态 -->
      <div v-if="reservations.length === 0" class="empty-tip">
        <p>您还没有预约任何活动</p>
        <el-button type="primary" @click="$router.push('/seckill')">
          去抢购
        </el-button>
      </div>

      <!-- 预约列表 -->
      <el-table v-else :data="reservations" style="width: 100%">
        <el-table-column label="活动信息" min-width="200">
          <template #default="{ row }">
            <div class="activity-info">
              <div class="activity-name">{{ row.activityName || `活动${row.activityId}` }}</div>
              <div class="activity-time">
                {{ formatTime(row.activityStartTime) }} ~ {{ formatTime(row.activityEndTime) }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="活动状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getActivityStatusType(row.activityStatus)">
              {{ getActivityStatusText(row.activityStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="预约状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="预约时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.reserveTime) }}
          </template>
        </el-table-column>

        <el-table-column label="提醒状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.notified ? 'success' : 'info'" size="small">
              {{ row.notified ? '已提醒' : '未提醒' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row.activityId)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.my-reservations {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 60px 20px;
}

.empty-tip p {
  margin-bottom: 20px;
  font-size: 16px;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activity-name {
  font-weight: 500;
  color: #333;
}

.activity-time {
  font-size: 12px;
  color: #999;
}
</style>
