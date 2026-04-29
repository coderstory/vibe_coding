<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { getBrokerMetrics, getBrokerStatusList, type BrokerStatusVO } from '@/api/rocketmq'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const loading = ref(false)
const chartOption = ref({})
const hasData = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

async function loadMetrics() {
  try {
    // 先获取 Broker 列表
    const brokerRes = await getBrokerStatusList()
    if (brokerRes.code !== 200 || !brokerRes.data.records?.length) {
      hasData.value = false
      return
    }
    const firstBroker: BrokerStatusVO = brokerRes.data.records[0]
    const res = await getBrokerMetrics(firstBroker.brokerName)
    if (res.code === 200 && res.data) {
      // 检查是否有真实 TPS 数据
      if (res.data.sendTps && res.data.consumeTps) {
        hasData.value = true
        updateChart(res.data)
      } else {
        hasData.value = false
      }
    }
  } catch (e) {
    console.error('加载 Broker 指标失败', e)
    hasData.value = false
  }
}

function updateChart(data: any) {
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
    <template v-if="!hasData">
      <div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #909399;">
        <span>暂无 TPS 数据（RocketMQ 5.x 运行时指标需通过其他方式获取）</span>
      </div>
    </template>
    <template v-else>
      <v-chart :option="chartOption" autoresize style="height: 100%" />
    </template>
  </div>
</template>