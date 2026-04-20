<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { monitorApi } from '@/api/monitor'
import type { MonitorMetrics } from '@/api/monitor'

const metrics = ref<MonitorMetrics | null>(null)
const loading = ref(false)
let interval: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  await loadMetrics()
  interval = setInterval(loadMetrics, 5000)
})

onUnmounted(() => {
  if (interval) {
    clearInterval(interval)
  }
})

async function loadMetrics() {
  loading.value = true
  try {
    const res = await monitorApi.getMetrics()
    metrics.value = res.data
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="monitor-dashboard">
    <el-card v-loading="loading">
      <template #header>
        <span>监控大盘</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-statistic title="当前并发数" :value="metrics?.concurrentCount || 0" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="QPS Key数量" :value="metrics?.qpsKeys || 0" />
        </el-col>
        <el-col :span="8">
          <el-statistic title="更新时间">
            <template #default>
              {{ metrics?.timestamp ? new Date(metrics.timestamp).toLocaleTimeString() : '-' }}
            </template>
          </el-statistic>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.monitor-dashboard {
  padding: 20px;
}
</style>