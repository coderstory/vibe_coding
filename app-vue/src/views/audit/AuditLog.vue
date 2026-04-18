<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogs } from '@/api/audit'
import type { AuditLog, AuditLogQueryParams } from '@/api/types'

// 查询表单
const searchForm = reactive({
  operator: '',
  operationType: null as string | null,
  startTime: null as Date | null,
  endTime: null as Date | null
})

// 审计日志列表
const auditList = ref<AuditLog[]>([])
const total = ref(0)
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 操作类型选项
const operationTypeOptions = [
  { value: null, label: '全部' },
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '登出' },
  { value: '新增', label: '新增' },
  { value: '编辑', label: '编辑' },
  { value: '删除', label: '删除' }
]

// 加载审计日志列表
async function loadAuditLogs() {
  loading.value = true
  try {
    const params: AuditLogQueryParams = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.operator) params.username = searchForm.operator
    if (searchForm.operationType) params.action = searchForm.operationType
    if (searchForm.startTime) params.startDate = formatDateTime(searchForm.startTime)
    if (searchForm.endTime) params.endDate = formatDateTime(searchForm.endTime)

    const res = await getAuditLogs(params)
    auditList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载审计日志失败', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
function formatDateTime(date: Date | null): string | null {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadAuditLogs()
}

// 重置搜索
function handleReset() {
  searchForm.operator = ''
  searchForm.operationType = null
  searchForm.startTime = null
  searchForm.endTime = null
  pagination.page = 1
  loadAuditLogs()
}

// 分页变化
function handlePageChange(page: number) {
  pagination.page = page
  loadAuditLogs()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadAuditLogs()
}

// 格式化操作类型
function formatOperationType(type: string): string {
  const typeMap: Record<string, string> = {
    'LOGIN': '登录',
    'LOGOUT': '登出',
    '新增': '新增',
    '编辑': '编辑',
    '删除': '删除'
  }
  return typeMap[type] || type
}

// 获取最近7天的日期范围
function getDefaultDateRange(): [Date, Date] {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 7)
  return [start, end]
}

// 初始化
onMounted(() => {
  // 默认查询最近7天
  const [start, end] = getDefaultDateRange()
  searchForm.startTime = start
  searchForm.endTime = end
  loadAuditLogs()
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">审计日志</h2>
    
    <!-- 查询区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.startTime"
            type="datetime"
            placeholder="开始时间"
            style="width: 180px"
          />
          <span style="margin: 0 8px;">至</span>
          <el-date-picker
            v-model="searchForm.endTime"
            type="datetime"
            placeholder="结束时间"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operator" placeholder="请输入操作人" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in operationTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 审计日志列表 -->
    <el-table :data="auditList" stripe v-loading="loading" class="audit-table">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="username" label="操作人" min-width="120" />
      <el-table-column prop="createTime" label="操作时间" min-width="160" />
      <el-table-column prop="operation" label="操作类型" width="100" align="center">
        <template #default="{ row }">
          {{ formatOperationType(row.operation) }}
        </template>
      </el-table-column>
      <el-table-column prop="targetType" label="目标" min-width="120">
        <template #default="{ row }">
          {{ row.targetType }} {{ row.targetId ? '#' + row.targetId : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" min-width="140" />
    </el-table>
    
    <!-- 空状态 -->
    <el-empty v-if="!loading && auditList.length === 0" description="暂无审计日志" />
    
    <!-- 分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page-container {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.audit-table {
  margin-bottom: 16px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
}
</style>
