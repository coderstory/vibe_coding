<script lang="ts">
/**
 * 系统信息展示卡片。标签-值对展示 OS/运行时间/进程数。
 */
</script>

<script lang="ts" setup>
import type { SystemInfoDTO } from '@/api/modules/hardware'

defineProps<{
  systemData: SystemInfoDTO | null
}>()
</script>

<template>
  <div class="system-info">
    <el-card shadow="never">
      <template #header><span class="section-title">系统信息</span></template>
      <div v-if="!systemData" class="empty-state"><span>暂无系统信息</span></div>
      <div v-else class="info-grid">
        <div class="info-row"><span class="info-label">操作系统</span><span class="info-value">{{ systemData.osFamily }} {{ systemData.osVersion }} {{ systemData.osVersionInfo || '' }}</span></div>
        <div class="info-row"><span class="info-label">运行时间</span><span class="info-value">{{ systemData.systemUptimeFormatted || '-' }}</span></div>
        <div class="info-row"><span class="info-label">进程数</span><span class="info-value">{{ systemData.processCount ?? '-' }} 个进程</span></div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.system-info { margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; color: var(--ocean-deep, #1e3a8a); }
.info-grid { display: flex; flex-direction: column; gap: 12px; }
.info-row { display: flex; align-items: center; }
.info-label { width: 100px; font-size: 14px; color: var(--el-text-color-secondary, #64748b); flex-shrink: 0; }
.info-value { font-size: 14px; font-weight: 500; color: var(--el-text-color-primary, #1e293b); }
.empty-state { display: flex; align-items: center; justify-content: center; height: 100px; color: var(--el-text-color-placeholder, #c0c4cc); }
</style>
