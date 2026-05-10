<script lang="ts">
/**
 * 网络吞吐量实时折线图。ECharts LineChart 展示上下行速率。
 * 从 SSE 数据流缓冲最近 60 个采样点（约 2 分钟窗口）。
 */
</script>

<script lang="ts" setup>
import { ref, watch, computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import type { HardwareMetricsDTO } from '@/api/modules/hardware'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps<{
  metrics: HardwareMetricsDTO | null
  connected: boolean
}>()

const MAX_POINTS = 60
const timeLabels = ref<string[]>([])
const sentRates = ref<number[]>([])
const recvRates = ref<number[]>([])

watch(() => props.metrics, (val) => {
  if (!val?.network || val.network.length === 0) return

  const now = new Date(val.timestamp).toLocaleTimeString()
  timeLabels.value.push(now)

  const totalSent = val.network.reduce((sum, n) => sum + (n.bytesSentPerSec?.value ?? 0), 0)
  const totalRecv = val.network.reduce((sum, n) => sum + (n.bytesRecvPerSec?.value ?? 0), 0)

  sentRates.value.push(+(totalSent / 1024 / 1024).toFixed(3))
  recvRates.value.push(+(totalRecv / 1024 / 1024).toFixed(3))

  if (timeLabels.value.length > MAX_POINTS) {
    timeLabels.value.shift()
    sentRates.value.shift()
    recvRates.value.shift()
  }
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(255,255,255,0.95)',
    borderColor: '#93c5fd',
    textStyle: { fontSize: 12 }
  },
  legend: {
    data: ['上行速率', '下行速率'],
    bottom: 0,
    textStyle: { fontSize: 12, color: '#64748b' }
  },
  grid: { left: 40, right: 16, top: 10, bottom: 32 },
  xAxis: {
    type: 'category',
    data: timeLabels.value,
    axisLabel: { fontSize: 10, color: '#94a3b8' },
    axisLine: { lineStyle: { color: '#e2e8f0' } }
  },
  yAxis: {
    type: 'value',
    name: 'MB/s',
    nameTextStyle: { fontSize: 11, color: '#64748b' },
    axisLabel: { fontSize: 10, color: '#94a3b8' },
    splitLine: { lineStyle: { color: '#f1f5f9' } }
  },
  series: [
    {
      name: '下行速率',
      type: 'line',
      data: recvRates.value,
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: '#3b82f6' },
      areaStyle: { color: 'rgba(59,130,246,0.1)' },
      animationDuration: 300
    },
    {
      name: '上行速率',
      type: 'line',
      data: sentRates.value,
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: '#f59e0b' },
      areaStyle: { color: 'rgba(245,158,11,0.1)' },
      animationDuration: 300
    }
  ]
}))
</script>

<template>
  <div class="network-chart">
    <el-card shadow="never">
      <template #header><span class="section-title">网络吞吐量（{{ connected ? '实时' : '离线' }}）</span></template>
      <div class="chart-box">
        <v-chart :option="chartOption" autoresize style="height: 220px; width: 100%;" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.network-chart { margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ocean-deep, #1e3a8a); }
.chart-box { padding: 8px 0; }
</style>
