<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { activityApi } from '@/api/activity'
import type { Activity } from '@/api/activity'

const activities = ref<Activity[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    activities.value = []
  } finally {
    loading.value = false
  }
})

function formatTime(time: string) {
  return new Date(time).toLocaleString()
}

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
      <div v-if="activities.length === 0" class="empty-tip">
        暂无秒杀活动
      </div>
      <el-row v-else :gutter="20">
        <el-col v-for="activity in activities" :key="activity.id" :span="8">
          <el-card class="activity-card">
            <template #header>
              <div class="activity-header">
                <span>{{ activity.name }}</span>
                <el-tag :type="activity.status === 1 ? 'success' : 'info'">
                  {{ getStatusText(activity.status) }}
                </el-tag>
              </div>
            </template>
            <div class="activity-info">
              <p>{{ activity.description }}</p>
              <p>开始时间: {{ formatTime(activity.startTime) }}</p>
              <p>结束时间: {{ formatTime(activity.endTime) }}</p>
              <p>限购: {{ activity.perLimit }} 件/人</p>
            </div>
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