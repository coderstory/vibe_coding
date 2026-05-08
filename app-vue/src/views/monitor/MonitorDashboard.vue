<script lang="ts">
/**
 * 监控大盘页面组件。展示系统运行状态、性能指标和资源使用情况的实时监控面板。
 */
</script>

<script lang="ts" setup>
import { onMounted, onUnmounted, ref } from 'vue'
import type { MonitorMetrics } from '@/api/modules/monitor'
import { monitorApi } from '@/api/modules/monitor'

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
  }
  finally {
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
          <el-statistic :value="metrics?.concurrentCount || 0" title="当前并发数"/>
        </el-col>
        <el-col :span="8">
          <el-statistic :value="metrics?.qpsKeys || 0" title="QPS Key数量"/>
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
