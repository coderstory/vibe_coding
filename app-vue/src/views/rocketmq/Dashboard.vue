<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElCard, ElRow, ElCol } from 'element-plus'
import OverviewCard from './OverviewCard.vue'
import BrokerStatusTable from './BrokerStatusTable.vue'
import TopicBacklogTable from './TopicBacklogTable.vue'
import QpsChart from './QpsChart.vue'

// 刷新间隔（毫秒）
const REFRESH_INTERVAL = 30000
let refreshTimer: number | null = null

onMounted(() => {
  // 启动定时刷新
  refreshTimer = window.setInterval(() => {
    // 刷新所有数据
    location.reload()
  }, REFRESH_INTERVAL)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<template>
  <div class="dashboard-container">
    <h2 class="page-title">RocketMQ 监控面板</h2>

    <!-- 概览卡片 -->
    <OverviewCard />

    <!-- Broker QPS 图表 -->
    <el-card shadow="hover" style="margin-top: 16px">
      <template #header>
        <span>Broker TPS 监控</span>
      </template>
      <QpsChart />
    </el-card>

    <!-- Broker 状态和 Topic 堆积 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>Broker 状态</span>
          </template>
          <BrokerStatusTable />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>Topic 堆积量</span>
          </template>
          <TopicBacklogTable />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100%;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}
</style>