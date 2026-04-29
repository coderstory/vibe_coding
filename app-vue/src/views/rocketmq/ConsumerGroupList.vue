<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConsumerGroupList, deleteConsumerGroup, type ConsumerGroupVO } from '@/api/rocketmq'
import ConsumerGroupDetail from './ConsumerGroupDetail.vue'

// 状态
const loading = ref(false)
const consumerGroupList = ref<ConsumerGroupVO[]>([])
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 详情弹窗
const detailDialogVisible = ref(false)
const currentGroup = ref('')

// 表格列定义
const columns = [
  { prop: 'index', label: '序号', width: 80, align: 'center' as const },
  { prop: 'group', label: 'Group 名称', minWidth: 120 },
  { prop: 'groupType', label: '类型', width: 100, align: 'center' as const },
  { prop: 'status', label: '状态', width: 100, align: 'center' as const },
  { prop: 'consumerCount', label: '消费者数', width: 100, align: 'center' as const },
  { prop: 'accumulatedDiff', label: '堆积量', width: 120, align: 'center' as const },
  { prop: 'actions', label: '操作', width: 120, fixed: 'right' as const }
]

// 类型标签
const groupTypeTagType = (type: string): '' | 'primary' | 'warning' | 'danger' | 'info' => {
  switch (type) {
    case 'BROADCASTING':
      return 'primary'
    case 'CLUSTERING':
      return 'info'
    default:
      return 'info'
  }
}

// 状态标签
const statusTagType = (status: string): '' | 'success' | 'warning' | 'danger' | 'info' => {
  switch (status) {
    case 'OK':
      return 'success'
    case 'REBALANCE_NOT_INIT':
      return 'warning'
    case 'OFFLINE':
      return 'danger'
    default:
      return 'info'
  }
}

// 类型文本
const groupTypeText = (type: string): string => {
  switch (type) {
    case 'BROADCASTING':
      return '广播'
    case 'CLUSTERING':
      return '集群'
    default:
      return '未知'
  }
}

// 状态文本
const statusText = (status: string): string => {
  switch (status) {
    case 'OK':
      return '正常'
    case 'REBALANCE_NOT_INIT':
      return '重试中'
    case 'OFFLINE':
      return '离线'
    default:
      return '未知'
  }
}

// 堆积量格式化
const formatDiff = (diff: number): string => {
  return diff > 0 ? (diff / 10000).toFixed(1) + '万' : '0'
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await getConsumerGroupList(searchForm.keyword || undefined)
    consumerGroupList.value = res.data.records
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

// 查看详情
function handleView(row: ConsumerGroupVO) {
  currentGroup.value = row.group
  detailDialogVisible.value = true
}

// 删除
async function handleDelete(row: ConsumerGroupVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除 Consumer Group "${row.group}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteConsumerGroup(row.group)
    ElMessage.success('删除成功')
    loadData()
  } catch {
    // 取消或错误已在 request.ts 拦截
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">Consumer Group 管理</h2>

    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="Group 名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="输入 Group 名称搜索"
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

    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="consumerGroupList"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="group" label="Group 名称" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">
            {{ row.group }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="groupType" label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="groupTypeTagType(row.groupType)" size="small">
            {{ groupTypeText(row.groupType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="consumerCount" label="消费者数" width="100" align="center" />
      <el-table-column prop="accumulatedDiff" label="堆积量" width="120" align="center">
        <template #default="{ row }">
          {{ formatDiff(row.accumulatedDiff) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
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
    <el-empty v-if="!loading && consumerGroupList.length === 0" description="暂无 Consumer Group" />

    <!-- 详情弹窗 -->
    <ConsumerGroupDetail
      v-model="detailDialogVisible"
      :group-name="currentGroup"
      @refresh="loadData"
    />
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
</style>