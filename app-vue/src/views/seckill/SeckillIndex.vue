<script setup lang="ts">
/**
 * 秒杀首页组件
 *
 * 功能说明：
 * - 展示秒杀活动列表
 * - 显示活动状态（未开始/进行中/已结束）
 * - 支持跳转到活动详情页
 *
 * @description 秒杀活动入口页面
 */
import { ref, onMounted } from 'vue'
import { activityApi, type Activity } from '@/api/seckill'

/** 活动列表数据 */
const activities = ref<Activity[]>([])
/** 加载状态 */
const loading = ref(false)

/**
 * 组件挂载时加载活动列表
 */
onMounted(async () => {
  loading.value = true
  try {
    const res = await activityApi.list()
    activities.value = res.data?.records || []
  } catch {
    activities.value = []
  } finally {
    loading.value = false
  }
})

/**
 * 格式化时间显示
 * @param time - ISO 时间字符串
 * @returns 本地化的时间字符串
 */
function formatTime(time: string) {
  return new Date(time).toLocaleString()
}

/**
 * 获取活动状态文本
 * @param status - 活动状态码
 * @returns 状态描述文本
 */
function getStatusText(status: number) {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status as keyof typeof map] || '未知'
}
</script>

<template>
  <div class="seckill-index">
    <h1>秒杀活动</h1>
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>活动列表</span>
        </div>
      </template>
      <!-- 无活动时显示空提示 -->
      <div v-if="activities.length === 0" class="empty-tip">
        暂无秒杀活动
      </div>
      <!-- 活动列表 -->
      <el-row v-else :gutter="20">
        <el-col v-for="activity in activities" :key="activity.id" :span="8">
          <el-card class="activity-card">
            <template #header>
              <div class="activity-header">
                <span>{{ activity.name }}</span>
                <!-- 根据活动状态显示不同颜色的标签 -->
                <el-tag :type="activity.status === 1 ? 'success' : 'info'">
                  {{ getStatusText(activity.status) }}
                </el-tag>
              </div>
            </template>
            <!-- 活动详情信息 -->
            <div class="activity-info">
              <p>{{ activity.description }}</p>
              <p>开始时间: {{ formatTime(activity.startTime) }}</p>
              <p>结束时间: {{ formatTime(activity.endTime) }}</p>
              <p>限购: {{ activity.perLimit }} 件/人</p>
            </div>
            <!-- 跳转到活动详情 -->
            <router-link :to="`/seckill/detail/${activity.id}`">
              <el-button type="primary" class="detail-btn">查看详情</el-button>
            </router-link>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.seckill-index {
  padding: 20px;
}
.card-header {
  font-size: 18px;
  font-weight: bold;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}
.activity-card {
  margin-bottom: 20px;
}
.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.activity-info {
  margin-bottom: 15px;
}
.activity-info p {
  margin: 5px 0;
  color: #666;
}
.detail-btn {
  width: 100%;
}
</style>
