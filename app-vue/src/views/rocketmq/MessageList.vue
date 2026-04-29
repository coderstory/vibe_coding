<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getTopicList, getMessageList, getMessageDetail, getMessageTrace, sendMessage, type TopicVO, type MessageVO, type MessageDetailVO, type MessageTraceVO } from '@/api/rocketmq'

// 状态
const loading = ref(false)
const messageLoading = ref(false)
const traceLoading = ref(false)
const sendLoading = ref(false)
const topicList = ref<TopicVO[]>([])
const messageList = ref<MessageVO[]>([])
const total = ref(0)

// 选中的 Topic
const selectedTopic = ref('')

// 搜索表单 - 默认查询当天
const getDefaultStartTime = () => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return now
}
const getDefaultEndTime = () => {
  const now = new Date()
  now.setHours(23, 59, 59, 999)
  return now
}
const searchForm = reactive({
  keyword: '',
  startTime: getDefaultStartTime(),
  endTime: getDefaultEndTime(),
  maxMsg: 100
})

// 快捷时间选择
const timeShortcuts = [
  { text: '最近 1 小时', value: () => Date.now() - 60 * 60 * 1000 },
  { text: '最近 6 小时', value: () => Date.now() - 6 * 60 * 60 * 1000 },
  { text: '最近 12 小时', value: () => Date.now() - 12 * 60 * 60 * 1000 },
  { text: '最近 24 小时', value: () => Date.now() - 24 * 60 * 60 * 1000 },
  { text: '最近 7 天', value: () => Date.now() - 7 * 24 * 60 * 60 * 1000 }
]

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref<MessageDetailVO | null>(null)

// 轨迹对话框
const traceDialogVisible = ref(false)
const traceList = ref<MessageTraceVO[]>([])

// 发送消息对话框
const sendDialogVisible = ref(false)
const sendForm = reactive({
  topic: '',
  tags: '',
  keys: '',
  body: ''
})

// 格式化时间
const formatTime = (timestamp: number) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 加载 Topic 列表
async function loadTopics() {
  try {
    const res = await getTopicList()
    topicList.value = res.data.records || []
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  }
}

// 加载消息列表
async function loadMessages() {
  if (!selectedTopic.value) {
    ElMessage.warning('请先选择 Topic')
    return
  }

  messageLoading.value = true
  try {
    const startTime = searchForm.startTime ? new Date(searchForm.startTime).getTime() : undefined
    const endTime = searchForm.endTime ? new Date(searchForm.endTime).getTime() : undefined

    const res = await getMessageList(
      selectedTopic.value,
      startTime,
      endTime,
      searchForm.maxMsg,
      searchForm.keyword || undefined
    )
    messageList.value = res.data.records || []
    total.value = res.data.total
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  } finally {
    messageLoading.value = false
  }
}

// 查看详情
async function handleViewDetail(row: MessageVO) {
  try {
    const res = await getMessageDetail(selectedTopic.value, row.msgId)
    detailData.value = res.data
    detailDialogVisible.value = true
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  }
}

// 查看轨迹
async function handleViewTrace(row: MessageVO) {
  traceLoading.value = true
  traceDialogVisible.value = true
  try {
    const res = await getMessageTrace(selectedTopic.value, row.msgId)
    traceList.value = res.data?.consumeTraceList || []
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  } finally {
    traceLoading.value = false
  }
}

// 状态标签颜色
const statusTagType = (status: string): '' | 'success' | 'warning' | 'info' | 'danger' => {
  switch (status) {
    case 'SUCCESS':
      return 'success'
    case 'FAILED':
      return 'danger'
    case 'PARTIAL_SUCCESS':
      return 'warning'
    default:
      return 'info'
  }
}

// 打开发送消息对话框
function openSendDialog() {
  sendForm.topic = selectedTopic.value
  sendForm.tags = ''
  sendForm.keys = ''
  sendForm.body = ''
  sendDialogVisible.value = true
}

// 发送消息
async function handleSend() {
  if (!sendForm.topic) {
    ElMessage.warning('请选择 Topic')
    return
  }
  if (!sendForm.body.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  sendLoading.value = true
  try {
    const res = await sendMessage(sendForm.topic, sendForm.body, sendForm.tags || undefined, sendForm.keys || undefined)
    ElMessage.success(`消息发送成功，MsgId: ${res.data.msgId}`)
    sendDialogVisible.value = false
    // 发送成功后不自动刷新列表，让用户自己决定是否刷新
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  } finally {
    sendLoading.value = false
  }
}

onMounted(() => {
  loadTopics()
})
</script>

<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <h2 class="page-title">消息管理</h2>

    <!-- Topic 选择和搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="选择 Topic">
          <el-select
            v-model="selectedTopic"
            placeholder="请选择 Topic"
            filterable
            clearable
            style="width: 240px"
            @change="loadMessages"
          >
            <el-option
              v-for="topic in topicList"
              :key="topic.topicName"
              :label="topic.topicName"
              :value="topic.topicName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="msgId/tag/key 模糊搜索"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="searchForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="searchForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="最大条数">
          <el-input-number
            v-model="searchForm.maxMsg"
            :min="1"
            :max="1000"
            style="width: 120px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="messageLoading" @click="loadMessages">
            查询
          </el-button>
          <el-button type="success" @click="openSendDialog">
            发送消息
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 提示信息 -->
    <div class="info-tip">
      <el-alert type="info" :closable="false">
        <template #title>
          <span>提示：消息查询默认返回最近 7 天的数据，最多返回 1000 条。查询时间范围越小，返回速度越快。</span>
        </template>
      </el-alert>
    </div>

    <!-- 表格 -->
    <el-table
      v-loading="messageLoading"
      :data="messageList"
      stripe
      border
      style="width: 100%; margin-top: 16px"
      empty-text="请先选择 Topic 后查询消息"
    >
      <el-table-column type="index" label="序号" width="80" align="center" />
      <el-table-column prop="msgId" label="消息 ID" min-width="180" show-overflow-tooltip />
      <el-table-column prop="tags" label="Tags" min-width="120" show-overflow-tooltip />
      <el-table-column prop="keys" label="Keys" min-width="120" show-overflow-tooltip />
      <el-table-column prop="timestamp" label="存储时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.timestamp) }}
        </template>
      </el-table-column>
      <el-table-column prop="queueId" label="队列 ID" width="100" align="center" />
      <el-table-column prop="queueOffset" label="队列 Offset" width="120" align="center" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleViewDetail(row)">
            详情
          </el-button>
          <el-button link type="success" size="small" @click="handleViewTrace(row)">
            轨迹
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!messageLoading && selectedTopic && messageList.length === 0" description="该 Topic 下没有消息">
      <el-button type="primary" @click="loadMessages">刷新</el-button>
    </el-empty>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="消息详情"
      width="700px"
    >
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="消息 ID" :span="2">{{ detailData.msgId }}</el-descriptions-item>
        <el-descriptions-item label="Topic">{{ detailData.topic }}</el-descriptions-item>
        <el-descriptions-item label="Tags">{{ detailData.tags || '-' }}</el-descriptions-item>
        <el-descriptions-item label="Keys">{{ detailData.keys || '-' }}</el-descriptions-item>
        <el-descriptions-item label="队列 ID">{{ detailData.queueId }}</el-descriptions-item>
        <el-descriptions-item label="队列 Offset">{{ detailData.queueOffset }}</el-descriptions-item>
        <el-descriptions-item label="存储时间" :span="2">{{ formatTime(detailData.timestamp) }}</el-descriptions-item>
        <el-descriptions-item label="消息内容" :span="2">
          <div class="message-body">{{ detailData.body }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="属性" :span="2">
          <div v-if="detailData.properties && Object.keys(detailData.properties).length > 0">
            <el-tag v-for="(value, key) in detailData.properties" :key="key" size="small" style="margin: 2px">
              {{ key }}: {{ value }}
            </el-tag>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 轨迹对话框 -->
    <el-dialog
      v-model="traceDialogVisible"
      title="消息轨迹"
      width="900px"
    >
      <el-table
        v-loading="traceLoading"
        :data="traceList"
        stripe
        border
        style="width: 100%"
        empty-text="暂无轨迹数据"
      >
        <el-table-column type="index" label="步骤" width="80" align="center" />
        <el-table-column prop="traceType" label="轨迹类型" width="120" />
        <el-table-column prop="traceTime" label="时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.traceTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="groupName" label="消费组" min-width="150" show-overflow-tooltip />
        <el-table-column prop="traceStatus" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.traceStatus)" size="small">
              {{ row.traceStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="clientHost" label="客户端" min-width="120" show-overflow-tooltip />
        <el-table-column prop="serverHost" label="服务端" min-width="120" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button @click="traceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 发送消息对话框 -->
    <el-dialog
      v-model="sendDialogVisible"
      title="发送消息"
      width="500px"
    >
      <el-form :model="sendForm" label-width="80px">
        <el-form-item label="Topic" required>
          <el-select
            v-model="sendForm.topic"
            placeholder="请选择 Topic"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="topic in topicList"
              :key="topic.topicName"
              :label="topic.topicName"
              :value="topic.topicName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Tags">
          <el-input v-model="sendForm.tags" placeholder="可选，如: order, payment" />
        </el-form-item>
        <el-form-item label="Keys">
          <el-input v-model="sendForm.keys" placeholder="可选，用于消息检索" />
        </el-form-item>
        <el-form-item label="消息内容" required>
          <el-input
            v-model="sendForm.body"
            type="textarea"
            :rows="4"
            placeholder="请输入消息内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="sendLoading" @click="handleSend">
          发送
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.search-section {
  background: #f5f7fa;
  padding: 20px;
  margin-bottom: 16px;
  border-radius: 4px;
}

.info-tip {
  margin-bottom: 0;
}

.message-body {
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 12px;
}
</style>
