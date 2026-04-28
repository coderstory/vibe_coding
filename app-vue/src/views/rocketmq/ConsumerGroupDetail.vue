<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getConsumerGroupDetail, type ConsumerGroupDetailVO } from '@/api/rocketmq'
import ResetOffsetDialog from './ResetOffsetDialog.vue'

const props = defineProps<{
  modelValue: boolean
  groupName: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'refresh': []
}>()

const dialogVisible = ref(props.modelValue)
const loading = ref(false)
const detailData = ref<ConsumerGroupDetailVO | null>(null)
const activeTab = ref('progress')

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
})
watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val)
})

// 加载详情
async function loadDetail() {
  if (!props.groupName) return
  loading.value = true
  try {
    const res = await getConsumerGroupDetail(props.groupName)
    detailData.value = res.data
  } catch (error: any) {
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    loading.value = false
  }
}

watch(() => props.groupName, () => {
  if (dialogVisible.value) {
    loadDetail()
  }
}, { immediate: true })

// 重置位点弹窗
const resetDialogVisible = ref(false)

function openResetDialog() {
  resetDialogVisible.value = true
}

function onResetSuccess() {
  resetDialogVisible.value = false
  emit('refresh')
  loadDetail()
}

const isClustering = (type: string): boolean => type === 'CLUSTERING'

// 堆积量格式化
const formatDiff = (diff: number): string => {
  return diff > 0 ? (diff / 10000).toFixed(1) + '万' : '0'
}

// 类型文本
const groupTypeText = (type: string): string => {
  return type === 'BROADCASTING' ? '广播' : '集群'
}

// 状态文本
const statusText = (status: string): string => {
  switch (status) {
    case 'OK': return '正常'
    case 'REBALANCE_NOT_INIT': return '重试中'
    case 'OFFLINE': return '离线'
    default: return '未知'
  }
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="Consumer Group 详情"
    width="800px"
    @close="emit('update:modelValue', false)"
  >
    <div v-loading="loading">
      <!-- 基本信息 -->
      <el-descriptions v-if="detailData" :column="3" border style="margin-bottom: 20px">
        <el-descriptions-item label="Group 名称">{{ detailData.group }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="detailData.groupType === 'BROADCASTING' ? 'primary' : 'info'" size="small">
            {{ groupTypeText(detailData.groupType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="detailData.status === 'OK' ? 'success' : detailData.status === 'REBALANCE_NOT_INIT' ? 'warning' : 'danger'"
            size="small"
          >
            {{ statusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消费者数">{{ detailData.consumerCount }}</el-descriptions-item>
        <el-descriptions-item label="堆积量">{{ formatDiff(detailData.totalDiff) }}</el-descriptions-item>
      </el-descriptions>

      <!-- Tabs -->
      <el-tabs v-model="activeTab">
        <el-tab-pane label="消费进度" name="progress">
          <el-table :data="[]" max-height="300">
            <el-table-column prop="topic" label="Topic" />
            <el-table-column prop="queueId" label="队列ID" width="100" />
            <el-table-column prop="consumerOffset" label="消费位点" width="120" />
            <el-table-column prop="brokerOffset" label="存储位点" width="120" />
            <el-table-column prop="diff" label="堆积量" width="100" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="订阅关系" name="subscription">
          <el-table :data="detailData?.subscriptions || []" max-height="300">
            <el-table-column prop="topic" label="Topic" />
            <el-table-column prop="filterExpression" label="过滤表达式" />
            <el-table-column prop="startPosition" label="起始位置" width="100" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="位点信息" name="offset">
          <div v-if="detailData?.offsetTable">
            <el-card v-for="(offset, key) in detailData.offsetTable" :key="key" style="margin-bottom: 8px">
              <template #header>{{ key }}</template>
              <span>消费位点: {{ offset }}</span>
            </el-card>
          </div>
          <el-empty v-else description="暂无位点信息" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button
        v-if="detailData && isClustering(detailData.groupType)"
        type="primary"
        @click="openResetDialog"
      >
        重置位点
      </el-button>
    </template>
  </el-dialog>

  <!-- 重置位点弹窗 -->
  <ResetOffsetDialog
    v-model="resetDialogVisible"
    :group-name="props.groupName"
    @success="onResetSuccess"
  />
</template>