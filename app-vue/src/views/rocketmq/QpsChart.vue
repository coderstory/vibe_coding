<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getBrokerMetrics, type BrokerMetricsVO } from '@/api/rocketmq'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const chartOption = ref({})
let timer: ReturnType<typeof setInterval> | null = null

const brokerNames = ref<string[]>([])

async function loadMetrics() {
  try {
    const res = await getBrokerMetrics(brokerNames.value[0] || '')
    if (res.code === 200 && res.data) {
      updateChart(res.data)
    }
  } catch (e) {
    console.error('加载 Broker 指标失败', e)
  }
}

function updateChart(data: BrokerMetricsVO) {
  const now = new Date().getTime()
  chartOption.value = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['发送TPS', '消费TPS', '读写TPS']
    },
    xAxis: {
      type: 'category',
      data: data.times || []
    },
    yAxis: {
      type: 'value',
      name: 'TPS',
      min: 0
    },
    series: [
      {
        name: '发送TPS',
        type: 'line',
        smooth: true,
        data: data.sendTps || []
      },
      {
        name: '消费TPS',
        type: 'line',
        smooth: true,
        data: data.consumeTps || []
      },
      {
        name: '读写TPS',
        type: 'line',
        smooth: true,
        data: data.getTransferedTps || []
      }
    ]
  }
}

async function init() {
  loading.value = true
  await loadMetrics()
  loading.value = false
}

onMounted(() => {
  init()
  timer = setInterval(loadMetrics, 10000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div v-loading="loading" style="height: 300px">
    <v-chart :option="chartOption" autoresize style="height: 100%" />
  </div>
</template>