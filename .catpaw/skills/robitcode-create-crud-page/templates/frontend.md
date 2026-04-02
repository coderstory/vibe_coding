# 前端代码模板

## TypeScript 类型定义

文件路径：`app/src/types/{entity}.ts`

```typescript
/** {中文名称}VO - 表格展示 */
export interface {EntityName}VO {
  id: number
  name: string
  code?: string
  sort?: number
  status: number
  remark?: string
  // 关联字段
  {refEntity}Id?: number
  {refEntity}Name?: string
  // 标准字段
  createTime: string
  updateTime?: string
}

/** {中文名称}DTO - 创建/更新 */
export interface {EntityName}DTO {
  id?: number
  name: string
  code?: string
  sort?: number
  status?: number
  remark?: string
  {refEntity}Id?: number
}

/** 查询参数 */
export interface {EntityName}QueryParams {
  page: number
  size: number
  keyword?: string
  status?: number
  {refEntity}Id?: number
}

/** 下拉选项 */
export interface OptionVO {
  value: number
  label: string
}

/** 导入结果 */
export interface ImportResult {
  total: number
  success: number
  failed: number
  errors: Array<{ row: number; field: string; message: string }>
}

/** 导出参数 */
export interface ExportParams {
  keyword?: string
  status?: number
  {refEntity}Id?: number
  ids?: number[]
  format?: 'excel' | 'csv'
}
```

---

## API 封装

文件路径：`app/src/api/{entity}.ts`

```typescript
import { get, post, put, del, download } from '@/utils/request'
import type { {EntityName}VO, {EntityName}DTO, {EntityName}QueryParams, OptionVO, ImportResult, ExportParams } from '@/types/{entity}'

/** 分页查询 */
export function get{EntityName}Page(params: {EntityName}QueryParams) {
  return get<{ records: {EntityName}VO[]; total: number }>('/{path}/page', { params })
}

/** 获取详情 */
export function get{EntityName}ById(id: number) {
  return get<{EntityName}VO>(`/{path}/${id}`)
}

/** 创建 */
export function create{EntityName}(data: {EntityName}DTO) {
  return post<{EntityName}VO>('/{path}', data)
}

/** 更新 */
export function update{EntityName}(id: number, data: {EntityName}DTO) {
  return put<{EntityName}VO>(`/{path}/${id}`, data)
}

/** 删除 */
export function delete{EntityName}(id: number) {
  return del(`/{path}/${id}`)
}

/** 批量删除 */
export function batchDelete{EntityName}(ids: number[]) {
  return del('/{path}/batch', ids)
}

/** 获取下拉选项 */
export function get{EntityName}Options() {
  return get<OptionVO[]>('/{path}/options')
}

/** 导出数据 */
export function export{EntityName}(params: ExportParams) {
  return download('/{path}/export', params, `{中文名称}.xlsx`)
}

/** 下载导入模板 */
export function downloadTemplate() {
  return download('/{path}/template', {}, `{中文名称}导入模板.xlsx`)
}

/** 导入数据 */
export function import{EntityName}(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<ImportResult>('/{path}/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 导入预览 */
export function previewImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<{EntityName}VO[]>('/{path}/import/preview', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
```

---

## Vue 组件（完整移动端适配版）

文件路径：`app/src/views/{EntityName}Manage.vue`

```vue
<template>
  <div class="crud-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryParams" inline :class="{ 'mobile-search-form': isMobile }">
        <el-form-item label="名称">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入名称搜索"
            clearable
            :style="{ width: isMobile ? '100%' : '180px' }"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择"
            clearable
            :style="{ width: isMobile ? '100%' : '120px' }"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="{关联实体中文名}">
          <el-select
            v-model="queryParams.{refEntity}Id"
            placeholder="请选择"
            clearable
            filterable
            :style="{ width: isMobile ? '100%' : '140px' }"
          >
            <el-option
              v-for="item in {refEntity}Options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isMobile">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
      <!-- 移动端搜索按钮 -->
      <div v-if="isMobile" class="mobile-search-buttons">
        <el-button v-if="!searchExpanded" type="primary" link @click="searchExpanded = true">
          <el-icon><ArrowDown /></el-icon>展开筛选
        </el-button>
        <el-button v-if="searchExpanded" link @click="searchExpanded = false">
          <el-icon><ArrowUp /></el-icon>收起筛选
        </el-button>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshRight /></el-icon>重置
        </el-button>
      </div>
    </el-card>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span v-if="!isMobile">新增</span>
      </el-button>
      <el-button
        type="danger"
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        <el-icon><Delete /></el-icon>
        <span v-if="!isMobile">批量删除</span>
      </el-button>
      <el-dropdown v-if="!isMobile" @command="handleExportCommand" style="margin-left: 8px">
        <el-button>
          <el-icon><Download /></el-icon>导出
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="current">导出当前页</el-dropdown-item>
            <el-dropdown-item command="all">导出全部</el-dropdown-item>
            <el-dropdown-item command="selected" :disabled="selectedIds.length === 0">
              导出选中 ({{ selectedIds.length }})
            </el-dropdown-item>
            <el-dropdown-item divided command="template">下载导入模板</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-upload
        v-if="!isMobile"
        :show-file-list="false"
        :before-upload="handleImport"
        accept=".xlsx,.xls,.csv"
        style="margin-left: 8px"
      >
        <el-button>
          <el-icon><Upload /></el-icon>导入
        </el-button>
      </el-upload>
    </div>

    <!-- PC端表格 -->
    <el-card v-if="!isMobile" class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="编码" width="120" show-overflow-tooltip />
        <el-table-column prop="{refEntity}Name" label="{关联实体中文名}" width="120" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val: number) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- PC端分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 移动端卡片列表 -->
    <div v-else class="mobile-card-list">
      <div v-if="loading" class="loading-wrapper">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      
      <template v-else>
        <el-card
          v-for="item in tableData"
          :key="item.id"
          class="mobile-card"
          shadow="hover"
        >
          <div class="card-header">
            <span class="card-title">{{ item.name }}</span>
            <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
              {{ item.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="card-row">
              <span class="label">编码：</span>
              <span class="value">{{ item.code || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="label">{关联实体中文名}：</span>
              <span class="value">{{ item.{refEntity}Name || '-' }}</span>
            </div>
            <div class="card-row">
              <span class="label">排序：</span>
              <span class="value">{{ item.sort ?? '-' }}</span>
            </div>
            <div class="card-row">
              <span class="label">创建时间：</span>
              <span class="value">{{ item.createTime }}</span>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
          </div>
        </el-card>

        <!-- 移动端分页 -->
        <div class="mobile-pagination">
          <el-pagination
            v-model:current-page="queryParams.page"
            :page-size="queryParams.size"
            :total="total"
            layout="prev, pager, next"
            :small="true"
            @current-change="fetchData"
          />
          <div class="total-info">共 {{ total }} 条</div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="tableData.length === 0" description="暂无数据" />
      </template>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="isMobile ? '100%' : '500px'"
      :fullscreen="isMobile"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-width="isMobile ? '100%' : '80px'"
        :label-position="isMobile ? 'top' : 'right'"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入编码" />
        </el-form-item>
        <el-form-item label="{关联实体中文名}" prop="{refEntity}Id">
          <el-select
            v-model="formData.{refEntity}Id"
            placeholder="请选择{关联实体中文名}"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in {refEntity}Options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 导入结果弹窗 -->
    <el-dialog v-model="importResultVisible" title="导入结果" width="500px">
      <el-result
        :icon="importResult.failed === 0 ? 'success' : 'warning'"
        :title="`成功导入 ${importResult.success} 条数据`"
      >
        <template #sub-title>
          <span v-if="importResult.failed > 0">
            失败 {{ importResult.failed }} 条，共 {{ importResult.total }} 条
          </span>
          <span v-else>共 {{ importResult.total }} 条</span>
        </template>
        <template #extra>
          <el-table v-if="importResult.errors?.length" :data="importResult.errors" max-height="200">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="field" label="字段" width="100" />
            <el-table-column prop="message" label="错误信息" />
          </el-table>
        </template>
      </el-result>
      <template #footer>
        <el-button type="primary" @click="importResultVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, RefreshRight, Plus, Delete, Edit, Download, Upload, ArrowDown, ArrowUp, Loading } from '@element-plus/icons-vue'
import { useResponsive } from '@/composables/useResponsive'
import * as api from '@/api/{entity}'
import type { {EntityName}VO, {EntityName}DTO, {EntityName}QueryParams, OptionVO, ImportResult } from '@/types/{entity}'

// ========== 响应式 ==========
const { isMobile } = useResponsive()
const searchExpanded = ref(false)

// ========== 数据状态 ==========
const loading = ref(false)
const tableData = ref<{EntityName}VO[]>([])
const total = ref(0)
const selectedIds = ref<number[]>([])

// ========== 查询参数 ==========
const queryParams = reactive<{EntityName}QueryParams>({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined,
  {refEntity}Id: undefined
})

// ========== 关联选项 ==========
const {refEntity}Options = ref<OptionVO[]>([])

// ========== 弹窗状态 ==========
const dialogVisible = ref(false)
const dialogTitle = computed(() => (formData.id ? '编辑' : '新增'))
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

// ========== 导入结果 ==========
const importResultVisible = ref(false)
const importResult = ref<ImportResult>({ total: 0, success: 0, failed: 0, errors: [] })

// ========== 表单数据 ==========
const formData = reactive<{EntityName}DTO>({
  id: undefined,
  name: '',
  code: '',
  sort: 0,
  status: 1,
  remark: '',
  {refEntity}Id: undefined
})

// ========== 表单校验 ==========
const formRules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

// ========== 数据加载 ==========

/** 加载表格数据 */
const fetchData = async () => {
  loading.value = true
  try {
    const res = await api.get{EntityName}Page(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/** 加载关联选项 */
const loadOptions = async () => {
  // 加载{关联实体}选项
  // const res = await api.get{RefEntity}Options()
  // {refEntity}Options.value = res
}

// ========== 搜索操作 ==========

const handleSearch = () => {
  queryParams.page = 1
  fetchData()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.status = undefined
  queryParams.{refEntity}Id = undefined
  searchExpanded.value = false
  handleSearch()
}

// ========== 表格操作 ==========

const handleSelectionChange = (rows: {EntityName}VO[]) => {
  selectedIds.value = rows.map((r) => r.id)
}

const handleStatusChange = async (row: {EntityName}VO, status: number) => {
  try {
    await api.update{EntityName}(row.id, { ...row, status })
    ElMessage.success('状态更新成功')
  } catch {
    row.status = status === 1 ? 0 : 1
  }
}

// ========== 弹窗操作 ==========

const handleAdd = () => {
  Object.assign(formData, {
    id: undefined,
    name: '',
    code: '',
    sort: 0,
    status: 1,
    remark: '',
    {refEntity}Id: undefined
  })
  dialogVisible.value = true
}

const handleEdit = (row: {EntityName}VO) => {
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    sort: row.sort,
    status: row.status,
    remark: row.remark,
    {refEntity}Id: row.{refEntity}Id
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    if (formData.id) {
      await api.update{EntityName}(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await api.create{EntityName}(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

// ========== 删除操作 ==========

const handleDelete = async (row: {EntityName}VO) => {
  await ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' })
  await api.delete{EntityName}(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const handleBatchDelete = async () => {
  await ElMessageBox.confirm(
    `确定删除选中的 ${selectedIds.value.length} 条记录？`,
    '提示',
    { type: 'warning' }
  )
  await api.batchDelete{EntityName}(selectedIds.value)
  ElMessage.success('批量删除成功')
  fetchData()
}

// ========== 导入导出操作 ==========

const handleExportCommand = async (command: string) => {
  switch (command) {
    case 'current':
      await api.export{EntityName}({ ...queryParams, ids: tableData.value.map(r => r.id) })
      break
    case 'all':
      await api.export{EntityName}(queryParams)
      break
    case 'selected':
      await api.export{EntityName}({ ids: selectedIds.value })
      break
    case 'template':
      await api.downloadTemplate()
      break
  }
}

const handleImport = async (file: File) => {
  try {
    const res = await api.import{EntityName}(file)
    importResult.value = res
    importResultVisible.value = true
    fetchData()
  } catch (error) {
    ElMessage.error('导入失败')
  }
  return false // 阻止默认上传行为
}

// ========== 初始化 ==========

onMounted(() => {
  fetchData()
  loadOptions()
})
</script>

<style lang="scss" scoped>
// 容器样式
.crud-container {
  @media screen and (max-width: 768px) {
    padding: 12px;
    padding-bottom: 80px;
  }
}

// 搜索表单样式
.search-card {
  margin-bottom: 12px;

  :deep(.el-card__body) {
    padding-bottom: 0;
  }
}

// 移动端搜索表单样式
.mobile-search-form {
  :deep(.el-form-item) {
    width: 100%;
    margin-bottom: 12px;

    .el-form-item__content {
      flex: 1;
    }
  }
}

// 移动端搜索按钮
.mobile-search-buttons {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  padding: 12px 0 0;
  border-top: 1px solid #ebeef5;
  margin-top: 8px;

  .el-button {
    flex: 1;
    max-width: 120px;
  }
}

// 工具栏样式
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

// 表格卡片
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

// 分页样式
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 15px 20px;
}

// ============================================
// 移动端卡片列表样式
// ============================================
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0;
  padding-bottom: 16px;

  .loading-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    color: #909399;

    .el-icon {
      font-size: 32px;
      margin-bottom: 8px;
    }
  }
}

.mobile-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .card-title {
      font-weight: 600;
      font-size: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
      margin-right: 8px;
    }
  }

  .card-body {
    .card-row {
      display: flex;
      margin-bottom: 8px;
      font-size: 14px;
      line-height: 1.5;

      .label {
        color: #909399;
        min-width: 80px;
        flex-shrink: 0;
      }

      .value {
        color: #303133;
        flex: 1;
        word-break: break-all;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
  }
}

// 移动端分页样式
.mobile-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0;

  .total-info {
    font-size: 12px;
    color: #909399;
  }
}

// 对话框底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

// ============================================
// 响应式断点样式
// ============================================
@media screen and (max-width: 768px) {
  .search-card {
    :deep(.el-card__body) {
      padding: 12px;
    }
  }

  .mobile-search-form {
    :deep(.el-form-item) {
      margin-bottom: 12px;
      width: 100%;
      display: flex;
      flex-direction: row;
      align-items: center;
      flex-wrap: nowrap;

      .el-form-item__label {
        flex-shrink: 0;
        width: auto !important;
        max-width: 80px;
        min-width: 60px;
        text-align: left;
        padding-right: 8px;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .el-form-item__content {
        flex: 1;
        margin-left: 0 !important;
      }
    }
  }

  .toolbar {
    flex-wrap: wrap;
    gap: 8px;

    .el-button {
      flex: 1;
      min-width: 80px;
    }
  }

  .table-card {
    display: none;
  }

  .mobile-pagination {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 12px 16px;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
    z-index: 100;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;

    .total-info {
      font-size: 12px;
      color: #909399;
    }
  }

  .dialog-footer {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}

@media screen and (min-width: 769px) {
  .mobile-card-list {
    display: none;
  }
}
</style>
```

---

## 路由注册

文件路径：`app/src/router/index.ts`

在 `dashboard` 的 `children` 数组末尾添加：

```typescript
{
  path: '{EntityName}Manage',
  name: '{EntityName}Manage',
  component: () => import('@/views/{EntityName}Manage.vue'),
  meta: {
    title: 'menu.{entityName}Manage',
    icon: '{图标名}',
    requiresAuth: true
  }
}
```

---

## 多语言配置

### 中文语言包

文件路径：`app/src/locales/zh-CN.ts`

在 `menu` 对象末尾添加：

```typescript
{entityName}Manage: '{中文名称}管理',
```

### 英文语言包

文件路径：`app/src/locales/en-US.ts`

在 `menu` 对象末尾添加：

```typescript
{entityName}Manage: '{EntityName} Management',
```

**示例（办公用品库存 office-supply）：**
```typescript
// zh-CN.ts
menu: {
  // ... 其他菜单项
  officeSupplyManage: '办公用品库存管理',
}

// en-US.ts
menu: {
  // ... 其他菜单项
  officeSupplyManage: 'Office Supply Management',
}
```

---

## 关联实体选项加载

如果关联实体已存在，需要在 `loadOptions` 中调用其 options 接口：

```typescript
import { get{RefEntity}Options } from '@/api/{ref-entity}'

const loadOptions = async () => {
  const res = await get{RefEntity}Options()
  {refEntity}Options.value = res
}
```

---

## useResponsive 工具函数

确保 `app/src/composables/useResponsive.ts` 存在，不存在则创建：

```typescript
import { ref, computed, onMounted, onUnmounted } from 'vue'

export function useResponsive() {
  const windowWidth = ref(window.innerWidth)
  
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }
  
  onMounted(() => {
    window.addEventListener('resize', updateWidth)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })
  
  const isMobile = computed(() => windowWidth.value < 768)
  const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 992)
  const isDesktop = computed(() => windowWidth.value >= 992)
  
  return {
    windowWidth,
    isMobile,
    isTablet,
    isDesktop
  }
}
```

---

## request.ts 工具函数补充

确保 `app/src/utils/request.ts` 包含 download 方法：

```typescript
export function download(url: string, params?: any, filename?: string) {
  return request({
    url,
    method: 'get',
    params,
    responseType: 'blob'
  }).then((response: any) => {
    const blob = new Blob([response])
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = filename || 'download.xlsx'
    link.click()
    window.URL.revokeObjectURL(link.href)
  })
}
```
