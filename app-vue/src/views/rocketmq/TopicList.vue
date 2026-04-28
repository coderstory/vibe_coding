<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTopicList, createTopic, deleteTopic, type TopicVO, type CreateTopicParams } from '@/api/rocketmq'

// 状态
const loading = ref(false)
const topicList = ref<TopicVO[]>([])
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 创建对话框
const createDialogVisible = ref(false)
const createForm = reactive<CreateTopicParams>({
  topicName: '',
  queueCount: 8,
  perm: 'READ'
})
const createLoading = ref(false)
const createFormRef = ref()

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref<TopicVO | null>(null)

// 状态标签颜色
const statusTagType = (status: string): '' | 'success' | 'warning' | 'info' => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'SUSPEND':
      return 'warning'
    default:
      return 'info'
  }
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await getTopicList(searchForm.keyword || undefined)
    topicList.value = res.data.records
    total.value = res.data.total
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  loadData()
}

// 重置
function handleReset() {
  searchForm.keyword = ''
  loadData()
}

// 打开创建对话框
function openCreateDialog() {
  createForm.topicName = ''
  createForm.queueCount = 8
  createForm.perm = 'READ'
  createDialogVisible.value = true
}

// 创建 Topic (D-05, D-06, D-07)
async function handleCreate() {
  // 表单验证 (D-07)
  if (!createForm.topicName) {
    ElMessage.error('Topic 名称不能为空')
    return
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(createForm.topicName)) {
    ElMessage.error('Topic 名称不能包含特殊字符')
    return
  }

  createLoading.value = true
  try {
    await createTopic({
      topicName: createForm.topicName,
      queueCount: createForm.queueCount,
      perm: createForm.perm
    })
    ElMessage.success('Topic 创建成功')
    createDialogVisible.value = false
    loadData()
  } catch {
    // 错误已在 request.ts 的响应拦截器中处理
  } finally {
    createLoading.value = false
  }
}

// 查看详情
async function handleView(row: TopicVO) {
  detailData.value = row
  detailDialogVisible.value = true
}

// 删除 Topic (D-08, D-09, D-10)
async function handleDelete(row: TopicVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除 Topic「${row.topicName}」吗？删除前请确保没有活跃的 Consumer Group 订阅该 Topic。删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await deleteTopic(row.topicName)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 如果用户取消，ElMessageBox 会抛出 'cancel' 字符串
    // 如果是其他错误，错误消息已在 request.ts 中处理
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <h2 class="page-title">Topic 管理</h2>

    <!-- 搜索区域 (D-04) -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="Topic 名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="输入 Topic 名称搜索"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-button type="primary" @click="openCreateDialog">
        {{ '新建 Topic' }}
      </el-button>
    </div>

    <!-- 表格 (D-03) -->
    <el-table
      v-loading="loading"
      :data="topicList"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="topicName" label="Topic 名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="queueCount" label="队列数" width="100" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="messageCount" label="消息数量" width="120" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">
            查看
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && topicList.length === 0" description="暂无 Topic">
      <el-button type="primary" @click="openCreateDialog">新建 Topic</el-button>
    </el-empty>

    <!-- 创建对话框 (D-05, D-06, D-07) -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建 Topic"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="Topic 名称" required>
          <el-input
            v-model="createForm.topicName"
            placeholder="请输入 Topic 名称"
            :disabled="createLoading"
          />
        </el-form-item>
        <el-form-item label="队列数">
          <el-input-number
            v-model="createForm.queueCount"
            :min="1"
            :max="16"
            :disabled="createLoading"
          />
          <span class="form-tip">1-16，默认 8</span>
        </el-form-item>
        <el-form-item label="权限">
          <el-select v-model="createForm.perm" :disabled="createLoading" style="width: 100%">
            <el-option label="READ" value="READ" />
            <el-option label="WRITE" value="WRITE" />
            <el-option label="READ_WRITE" value="READ_WRITE" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="Topic 详情"
      width="600px"
    >
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="Topic 名称">{{ detailData.topicName }}</el-descriptions-item>
        <el-descriptions-item label="队列数">{{ detailData.queueCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailData.status)" size="small">
            {{ detailData.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="消息数量">{{ detailData.messageCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
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

.action-section {
  margin-bottom: 16px;
}

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
