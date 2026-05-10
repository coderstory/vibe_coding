<script lang="ts">
/**
 * 磁盘分区容量进度条列表组件。El-Progress 展示每个分区使用率。
 * >=90% 红色, >=80% 橙色。
 */
</script>

<script lang="ts" setup>
import type { DiskMetricsDTO } from '@/api/modules/hardware'

defineProps<{
  disks: DiskMetricsDTO[] | null
  connected: boolean
}>()

function diskPercentage(disk: DiskMetricsDTO): number {
  if (!disk.totalSpace?.value || !disk.usedSpace?.value) return 0
  return Math.round((disk.usedSpace.value / disk.totalSpace.value) * 100)
}

function diskStatus(pct: number): 'exception' | 'warning' | '' {
  if (pct >= 90) return 'exception'
  if (pct >= 80) return 'warning'
  return ''
}
</script>

<template>
  <div class="disk-partitions">
    <el-card shadow="never">
      <template #header><span class="section-title">磁盘分区</span></template>
      <div v-if="!disks || disks.length === 0" class="empty-state"><span>暂无磁盘信息</span></div>
      <div v-else v-for="disk in disks" :key="disk.name" class="disk-card">
        <div class="disk-header">
          <span class="disk-name">{{ disk.name || disk.mount }}</span>
          <span class="disk-model">{{ disk.model || '' }}</span>
        </div>
        <el-progress :percentage="diskPercentage(disk)" :text-inside="true" :stroke-width="22" :status="diskStatus(diskPercentage(disk))" />
        <div class="disk-details">
          <span>已用: {{ disk.usedSpace?.format || '-' }}</span>
          <span>可用: {{ disk.usableSpace?.format || '-' }}</span>
          <span>总量: {{ disk.totalSpace?.format || '-' }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.disk-partitions { margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ocean-deep, #1e3a8a); }
.disk-card { margin-bottom: 16px; }
.disk-card:last-child { margin-bottom: 0; }
.disk-header { display: flex; align-items: baseline; gap: 8px; margin-bottom: 8px; }
.disk-name { font-size: 14px; font-weight: 600; color: var(--el-text-color-primary, #1e293b); }
.disk-model { font-size: 12px; color: var(--el-text-color-secondary, #64748b); }
.disk-details { display: flex; gap: 16px; margin-top: 6px; font-size: 13px; color: var(--el-text-color-secondary, #64748b); }
.empty-state { display: flex; align-items: center; justify-content: center; height: 100px; color: var(--el-text-color-placeholder, #c0c4cc); }
</style>
