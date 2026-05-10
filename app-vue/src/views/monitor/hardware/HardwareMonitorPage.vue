<script lang="ts">
/**
 * 硬件监控页面组件。左导航 + 右内容区布局，SSE 实时接收硬件数据。
 * 包含环形图（CPU/内存）、进度条（磁盘）、折线图（趋势/IO/网络）。
 */
</script>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
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

const { connect, connected, metrics } = useHardwareMetrics()
const activeSection = ref('cpu')

const navItems = [
  { key: 'cpu', label: 'CPU', icon: Cpu },
  { key: 'memory', label: '内存', icon: DataBoard },
  { key: 'disk', label: '磁盘', icon: Folder },
  { key: 'network', label: '网络', icon: Monitor },
  { key: 'system', label: '系统信息', icon: InfoFilled }
]

function handleNavSelect(key: string) {
  activeSection.value = key
}

onMounted(() => { connect() })
// disconnect() handled by composable onUnmounted
</script>

<template>
  <div class="hardware-monitor-page">
    <div class="hw-layout">
      <div class="hw-nav">
        <el-menu :default-active="activeSection" @select="handleNavSelect" class="hw-nav-menu">
          <el-menu-item v-for="item in navItems" :key="item.key" :index="item.key">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div class="hw-content">
        <div class="hw-connection-indicator">
          <span class="hw-status-dot" :class="connected ? 'hw-status-dot--connected' : 'hw-status-dot--disconnected'" />
          <span class="hw-status-text">{{ connected ? '已连接' : '连接已断开' }}</span>
        </div>

        <div v-show="activeSection === 'cpu'" class="hw-section">
          <CpuGauge :cpu-data="metrics?.cpu ?? null" :connected="connected" />
          <CpuTrendChart :connected="connected" />
        </div>

        <div v-show="activeSection === 'memory'" class="hw-section">
          <MemoryGauge :memory-data="metrics?.memory ?? null" :connected="connected" />
          <MemoryTrendChart :connected="connected" />
        </div>

        <div v-show="activeSection === 'disk'" class="hw-section">
          <DiskPartitions :disks="metrics?.disks ?? null" :connected="connected" />
          <DiskIoChart :metrics="metrics" :connected="connected" />
        </div>

        <div v-show="activeSection === 'network'" class="hw-section">
          <NetworkChart :metrics="metrics" :connected="connected" />
        </div>

        <div v-show="activeSection === 'system'" class="hw-section">
          <SystemInfo :system-data="metrics?.system ?? null" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hardware-monitor-page { padding: 24px; height: 100%; }
.hw-layout { display: flex; gap: 24px; height: 100%; }
.hw-nav { flex: 0 0 180px; background: var(--ocean-pale, #dbeafe); border-radius: 8px; padding: 8px 0; }
.hw-nav-menu { border-right: none; background: transparent; }
.hw-nav-menu .el-menu-item { height: 44px; padding-left: 16px; border-radius: 6px; margin: 2px 8px; }
.hw-nav-menu .el-menu-item.is-active { background: rgba(253, 230, 138, 0.6); color: var(--ocean-deep, #1e3a8a); font-weight: 600; }
.hw-content { flex: 1; min-width: 0; }
.hw-connection-indicator { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; font-size: 13px; color: var(--el-text-color-secondary, #64748b); }
.hw-status-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.hw-status-dot--connected { background-color: var(--el-color-success, #10b981); }
.hw-status-dot--disconnected { background-color: var(--el-color-warning, #d97706); }
.hw-status-text { vertical-align: middle; }
.hw-section { animation: hw-fade-in 0.3s ease; }
@keyframes hw-fade-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>
