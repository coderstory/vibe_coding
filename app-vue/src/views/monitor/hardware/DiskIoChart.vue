<script lang="ts">
/**
 * 磁盘 IO 实时折线图。ECharts LineChart 展示磁盘读写速率和 IOPS。
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
const readSpeeds = ref<number[]>([])
const writeSpeeds = ref<number[]>([])
const totalIops = ref<number[]>([])

watch(() => props.metrics, (val) => {
  if (!val?.disks || val.disks.length === 0) return

  const now = new Date(val.timestamp).toLocaleTimeString()
  timeLabels.value.push(now)

  const totalRead = val.disks.reduce((sum, d) => sum + (d.readBytesPerSec?.value ?? 0), 0)
  const totalWrite = val.disks.reduce((sum, d) => sum + (d.writeBytesPerSec?.value ?? 0), 0)
  const iops = val.disks.reduce((sum, d) => sum + (d.readsPerSec?.value ?? 0) + (d.writesPerSec?.value ?? 0), 0)

  readSpeeds.value.push(+(totalRead / 1024 / 1024).toFixed(2))
  writeSpeeds.value.push(+(totalWrite / 1024 / 1024).toFixed(2))
  totalIops.value.push(Math.round(iops))

  if (timeLabels.value.length > MAX_POINTS) {
    timeLabels.value.shift()
    readSpeeds.value.shift()
    writeSpeeds.value.shift()
    totalIops.value.shift()
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
    data: ['读取速度', '写入速度', 'IOPS'],
    bottom: 0,
    textStyle: { fontSize: 12, color: '#64748b' }
  },
  grid: { left: 40, right: 50, top: 10, bottom: 32 },
  xAxis: {
    type: 'category',
    data: timeLabels.value,
    axisLabel: { fontSize: 10, color: '#94a3b8' },
    axisLine: { lineStyle: { color: '#e2e8f0' } }
  },
  yAxis: [
    {
      type: 'value',
      name: 'MB/s',
      nameTextStyle: { fontSize: 11, color: '#64748b' },
      axisLabel: { fontSize: 10, color: '#94a3b8' },
      splitLine: { lineStyle: { color: '#f1f5f9' } }
    },
    {
      type: 'value',
      name: 'IOPS',
      nameTextStyle: { fontSize: 11, color: '#64748b' },
      axisLabel: { fontSize: 10, color: '#94a3b8' },
      splitLine: { show: false }
    }
  ],
  series: [
    {
      name: '读取速度',
      type: 'line',
      data: readSpeeds.value,
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: '#3b82f6' },
      areaStyle: { color: 'rgba(59,130,246,0.1)' },
      animationDuration: 300
    },
    {
      name: '写入速度',
      type: 'line',
      data: writeSpeeds.value,
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 2, color: '#f59e0b' },
      areaStyle: { color: 'rgba(245,158,11,0.1)' },
      animationDuration: 300
    },
    {
      name: 'IOPS',
      type: 'line',
      yAxisIndex: 1,
      data: totalIops.value,
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 1.5, color: '#10b981', type: 'dashed' },
      animationDuration: 300
    }
  ]
}))
</script>

<template>
  <div class="disk-io-chart">
    <el-card shadow="never">
      <template #header><span class="section-title">磁盘 IO（{{ connected ? '实时' : '离线' }}）</span></template>
      <div class="chart-box">
        <v-chart :option="chartOption" autoresize style="height: 220px; width: 100%;" />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.disk-io-chart { margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ocean-deep, #1e3a8a); }
.chart-box { padding: 8px 0; }
</style>
