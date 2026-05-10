<script lang="ts">
/**
 * 内存使用率趋势图。ECharts LineChart 展示近 1 小时内存使用率趋势。
 * 通过 REST trend API 获取数据，每 30s 自动刷新。
 */
</script>

<script lang="ts" setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { fetchTrend } from '@/api/modules/hardware'
import type { TrendDataPoint } from '@/api/modules/hardware'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

defineProps<{
  connected: boolean
}>()

const trendData = ref<TrendDataPoint[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
let pollTimer: ReturnType<typeof setTimeout> | null = null

async function loadTrend() {
  loading.value = true
  error.value = null
  try {
    const res = await fetchTrend('memory', 360)
    trendData.value = res.data.data || []
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '获取趋势数据失败'
  } finally {
    loading.value = false
  }
}

function startPoll() {
  pollTimer = setTimeout(async () => {
    await loadTrend()
    startPoll()
  }, 30000)
}

onMounted(() => {
  loadTrend().then(() => startPoll())
})

onUnmounted(() => {
  if (pollTimer) { clearTimeout(pollTimer); pollTimer = null }
})

const chartOption = computed(() => {
  const data = trendData.value
  if (data.length === 0) return {}
  const times = data.map(d => new Date(d.timestamp).toLocaleTimeString())
  const values = data.map(d => +(d.value).toFixed(1))

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#a78bfa',
      textStyle: { fontSize: 12 }
    },
    grid: { left: 40, right: 16, top: 10, bottom: 24 },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: { fontSize: 10, color: '#94a3b8', interval: 30 },
      axisLine: { lineStyle: { color: '#e2e8f0' } }
    },
    yAxis: {
      type: 'value',
      name: '%',
      min: 0,
      max: 100,
      nameTextStyle: { fontSize: 11, color: '#64748b' },
      axisLabel: { fontSize: 10, color: '#94a3b8' },
      splitLine: { lineStyle: { color: '#f1f5f9' } }
    },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: '#8b5cf6' },
      areaStyle: { color: 'rgba(139,92,246,0.12)' },
      animationDuration: 500
    }]
  }
})
</script>

<template>
  <div class="memory-trend-chart">
    <el-card shadow="never">
      <template #header>
        <span class="section-title">内存使用率趋势（近 1 小时）</span>
        <span v-if="loading" class="trend-loading">刷新中...</span>
      </template>
      <div v-if="error" class="trend-empty">加载失败：{{ error }}</div>
      <div v-else-if="trendData.length === 0" class="trend-empty">暂无趋势数据</div>
      <div v-else class="chart-box">
        <v-chart :option="chartOption" autoresize style="height: 200px; width: 100%;" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.memory-trend-chart { margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ocean-deep, #1e3a8a); }
.trend-loading { font-size: 12px; color: var(--el-text-color-secondary, #64748b); margin-left: 8px; }
.trend-empty { display: flex; align-items: center; justify-content: center; height: 100px; font-size: 13px; color: var(--el-text-color-placeholder, #c0c4cc); }
.chart-box { padding: 8px 0; }
</style>
