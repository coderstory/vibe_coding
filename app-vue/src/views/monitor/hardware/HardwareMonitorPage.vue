<script lang="ts">
/**
 * 硬件监控页面组件。TAB 导航 + 内容区布局，SSE 实时接收硬件数据。
 * 包含环形图（CPU/内存）、进度条（磁盘）、折线图（趋势/IO/网络）。
 * 支持加载骨架态 / 空状态 / 错误状态 / SSE 断连提示。
 */
</script>

<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue'
import { useHardwareMetrics } from '@/composables/useHardwareMetrics'
import { Cpu, DataBoard, Folder, InfoFilled, Monitor } from '@element-plus/icons-vue'
import CpuGauge from './CpuGauge.vue'
import CpuTrendChart from './CpuTrendChart.vue'
import MemoryGauge from './MemoryGauge.vue'
import MemoryTrendChart from './MemoryTrendChart.vue'
import DiskPartitions from './DiskPartitions.vue'
import DiskIoChart from './DiskIoChart.vue'
import NetworkChart from './NetworkChart.vue'
import SystemInfo from './SystemInfo.vue'

const { connect, connected, metrics, error } = useHardwareMetrics()
const activeSection = ref('cpu')
const pageLoading = ref(true)

// 首次数据到达后取消加载态
watch(metrics, (val) => {
  if (val !== null) pageLoading.value = false
}, { once: true })

onMounted(() => { connect() })
// disconnect() handled by composable onUnmounted
</script>

<template>
  <div class="hardware-monitor-page">
    <!-- 连接指示器 -->
    <div class="hw-connection-indicator">
      <span class="hw-status-dot" :class="connected ? 'hw-status-dot--connected' : 'hw-status-dot--disconnected'" />
      <span class="hw-status-text">{{ connected ? '已连接' : '连接已断开' }}</span>
    </div>

    <!-- SSE 断连重连提示 -->
    <el-alert
      v-if="!connected && !pageLoading"
      title="连接已断开，正在重连..."
      type="warning"
      :closable="false"
      show-icon
      class="hw-reconnect-banner"
    />

    <!-- 错误提示 -->
    <el-alert
      v-if="error && connected"
      :title="error"
      type="error"
      :closable="true"
      show-icon
      class="hw-error-banner"
    />

    <!-- 加载骨架态 -->
    <div v-if="pageLoading" class="hw-skeleton">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 主内容区 -->
    <div v-else>
      <el-tabs v-model="activeSection" class="hw-tabs">
        <el-tab-pane label="CPU" name="cpu">
          <template #label>
            <el-icon><Cpu /></el-icon><span>CPU</span>
          </template>
          <CpuGauge :cpu-data="metrics?.cpu ?? null" :connected="connected" />
          <CpuTrendChart :connected="connected" />
        </el-tab-pane>

        <el-tab-pane label="内存" name="memory">
          <template #label>
            <el-icon><DataBoard /></el-icon><span>内存</span>
          </template>
          <MemoryGauge :memory-data="metrics?.memory ?? null" :connected="connected" />
          <MemoryTrendChart :connected="connected" />
        </el-tab-pane>

        <el-tab-pane label="磁盘" name="disk">
          <template #label>
            <el-icon><Folder /></el-icon><span>磁盘</span>
          </template>
          <DiskPartitions :disks="metrics?.disks ?? null" :connected="connected" />
          <DiskIoChart :metrics="metrics" :connected="connected" />
        </el-tab-pane>

        <el-tab-pane label="网络" name="network">
          <template #label>
            <el-icon><Monitor /></el-icon><span>网络</span>
          </template>
          <NetworkChart :metrics="metrics" :connected="connected" />
        </el-tab-pane>

        <el-tab-pane label="系统信息" name="system">
          <template #label>
            <el-icon><InfoFilled /></el-icon><span>系统信息</span>
          </template>
          <SystemInfo :system-data="metrics?.system ?? null" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.hardware-monitor-page { padding: 24px; height: 100%; }
.hw-connection-indicator { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; font-size: 13px; color: var(--el-text-color-secondary, #64748b); }
.hw-status-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.hw-status-dot--connected { background-color: var(--el-color-success, #10b981); }
.hw-status-dot--disconnected { background-color: var(--el-color-warning, #d97706); }
.hw-status-text { vertical-align: middle; }
.hw-reconnect-banner { margin-bottom: 12px; }
.hw-error-banner { margin-bottom: 12px; }
.hw-skeleton { padding: 24px; }
.hw-tabs .el-tab-pane { animation: hw-fade-in 0.3s ease; }
.hw-tabs .el-icon { margin-right: 4px; vertical-align: middle; }
@keyframes hw-fade-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>
