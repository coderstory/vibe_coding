<script lang="ts">
/**
 * CPU 使用率环形图组件。ECharts PieChart 展示总体使用率 + 处理器信息卡片。
 */
</script>

<script lang="ts" setup>
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { computed } from 'vue'
import type { CpuMetricsDTO } from '@/api/modules/hardware'

use([CanvasRenderer, PieChart])

const props = defineProps<{
  cpuData: CpuMetricsDTO | null
  connected: boolean
}>()

const usagePercent = computed(() => {
  if (!props.cpuData) return 0
  return Math.round(props.cpuData.systemLoad * 10) / 10
})

const chartOption = computed(() => ({
  series: [{
    type: 'pie',
    radius: ['55%', '75%'],
    avoidLabelOverlap: false,
    animationDurationUpdate: 500,
    label: { show: true, position: 'center', formatter: () => `${usagePercent.value}%`, fontSize: 28, fontWeight: 700, color: '#1e3a8a' },
    emphasis: { label: { show: true } },
    data: [
      { value: usagePercent.value, name: '已使用', itemStyle: { color: '#3b82f6' } },
      { value: Math.max(100 - usagePercent.value, 0), name: '剩余', itemStyle: { color: '#f0f9ff' } }
    ]
  }]
}))

const frequencyFormatted = computed(() => {
  if (!props.cpuData) return '-'
  const freq = props.cpuData.processorFrequency
  if (freq >= 1_000_000_000) return (freq / 1_000_000_000).toFixed(2) + ' GHz'
  if (freq >= 1_000_000) return (freq / 1_000_000).toFixed(0) + ' MHz'
  return freq + ' Hz'
})
</script>

<template>
  <div class="cpu-gauge">
    <el-card shadow="never">
      <template #header><span class="section-title">CPU</span></template>
      <el-row :gutter="24" align="middle">
        <el-col :span="8">
          <div class="chart-container" style="height: 180px; width: 180px; margin: 0 auto;">
            <v-chart :option="chartOption" autoresize style="height: 100%; width: 100%;" />
          </div>
        </el-col>
        <el-col :span="16">
          <div class="info-list">
            <div class="info-item"><span class="info-label">型号</span><span class="info-value">{{ cpuData?.processorName || '暂无数据' }}</span></div>
            <div class="info-item"><span class="info-label">物理核心</span><span class="info-value">{{ cpuData?.physicalCores ?? '-' }}</span></div>
            <div class="info-item"><span class="info-label">逻辑核心</span><span class="info-value">{{ cpuData?.logicalCores ?? '-' }}</span></div>
            <div class="info-item"><span class="info-label">频率</span><span class="info-value">{{ frequencyFormatted }}</span></div>
            <div class="info-item"><span class="info-label">架构</span><span class="info-value">{{ cpuData?.cpu64bit ? '64-bit' : '32-bit' }}</span></div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped>
.cpu-gauge { margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ocean-deep, #1e3a8a); }
.info-list { display: flex; flex-direction: column; gap: 12px; }
.info-item { display: flex; align-items: center; }
.info-label { width: 100px; font-size: 13px; color: var(--el-text-color-secondary, #64748b); flex-shrink: 0; }
.info-value { font-size: 14px; font-weight: 600; color: var(--el-text-color-primary, #1e293b); }
</style>
