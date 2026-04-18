<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus, getMenuTree } from '@/api/role'
import type { Role, MenuTree, CreateRoleParams, UpdateRoleParams } from '@/api/types'
import type { ElTree } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  roleName: ''
})

// 角色列表数据
const roleList = ref<Role[]>([])
const total = ref(0)
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 新建/编辑角色对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建角色')
const roleForm = reactive({
  id: null as number | null,
  roleName: '',
  roleCode: '',
  description: ''
})
const roleFormRef = ref<FormInstance | null>(null)
const roleFormRules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}
const isEdit = ref(false)

// 分配权限对话框
const permissionDialogVisible = ref(false)
const permissionDialogTitle = ref('分配权限')
const menuTreeRef = ref<InstanceType<typeof ElTree> | null>(null)
const menuTreeData = ref<MenuTree[]>([])
const checkedMenuIds = ref<number[]>([])
const currentRoleId = ref<number | null>(null)
const currentRoleName = ref('')

// el-tree配置
const treeProps = {
  label: 'name',
  children: 'children'
}

// 加载角色列表
async function loadRoleList() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.roleName) params.roleName = searchForm.roleName

    const res = await getRoleList(params)
    roleList.value = res.data.records
    total.value = res.data.total
  } catch {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadRoleList()
}

// 重置搜索
function handleReset() {
  searchForm.roleName = ''
  pagination.page = 1
  loadRoleList()
}

// 分页变化
function handlePageChange(page: number) {
  pagination.page = page
  loadRoleList()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  loadRoleList()
}

// 新建角色
function handleCreate() {
  isEdit.value = false
  dialogTitle.value = '新建角色'
  resetRoleForm()
  dialogVisible.value = true
}

// 编辑角色
function handleEdit(row: Role) {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  roleForm.id = row.id
  roleForm.roleName = row.roleName
  roleForm.roleCode = row.roleCode || ''
  roleForm.description = row.description || ''
  dialogVisible.value = true
}

// 删除角色
function handleDelete(row: Role) {
  ElMessageBox.confirm(
    `确定要删除角色「${row.roleName}」吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('角色删除成功')
      loadRoleList()
    } catch {
      ElMessage.error('角色删除失败')
    }
  }).catch(() => {})
}

// 分配权限
async function handleAssignPermission(row: Role) {
  currentRoleId.value = row.id
  currentRoleName.value = row.roleName
  permissionDialogTitle.value = `分配权限 - ${row.roleName}`

  try {
    // 获取菜单树
    const treeRes = await getMenuTree()
    menuTreeData.value = treeRes.data || []

    // 获取当前角色的菜单权限
    const roleMenuRes = await getRoleMenus(row.id)
    checkedMenuIds.value = roleMenuRes.data || []

    permissionDialogVisible.value = true
  } catch {
    ElMessage.error('加载权限数据失败')
  }
}

// 保存角色
async function handleSaveRole() {
  if (!roleFormRef.value) return

  await roleFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (isEdit.value) {
        const params: UpdateRoleParams = {
          roleName: roleForm.roleName,
          description: roleForm.description
        }
        await updateRole(roleForm.id as number, params)
        ElMessage.success('角色更新成功')
      } else {
        const params: CreateRoleParams = {
          roleName: roleForm.roleName,
          roleCode: roleForm.roleCode,
          description: roleForm.description
        }
        await createRole(params)
        ElMessage.success('角色创建成功')
      }
      dialogVisible.value = false
      loadRoleList()
    } catch {
      ElMessage.error(isEdit.value ? '角色更新失败' : '角色创建失败')
    }
  })
}

// 保存权限分配
async function handleSavePermission() {
  if (!menuTreeRef.value) return

  try {
    // 获取所有选中节点（包括半选状态的父节点）
    const checkedKeys = menuTreeRef.value.getCheckedKeys(false)
    const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
    const allSelected = [...checkedKeys, ...halfCheckedKeys]

    await assignRoleMenus(currentRoleId.value as number, allSelected)
    ElMessage.success('权限保存成功')
    permissionDialogVisible.value = false
  } catch {
    ElMessage.error('权限保存失败')
  }
}

// 重置角色表单
function resetRoleForm() {
  roleForm.id = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.description = ''
  if (roleFormRef.value) {
    roleFormRef.value.clearValidate()
  }
}

// 初始化
onMounted(() => {
  loadRoleList()
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">角色管理</h2>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button type="primary" @click="handleCreate">新建角色</el-button>
    </div>
    
    <!-- 角色列表 -->
    <el-table :data="roleList" stripe border v-loading="loading" class="role-table">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="roleName" label="角色名称" min-width="120" />
      <el-table-column prop="roleCode" label="角色编码" min-width="120" />
      <el-table-column prop="description" label="描述" min-width="200" />
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          <el-button link type="warning" size="small" @click="handleAssignPermission(row)">分配权限</el-button>
        </template>
      </el-table-column>
    </el-table>
    
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
    
    <!-- 新建/编辑角色对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleFormRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" :disabled="isEdit" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 分配权限对话框 -->
    <el-dialog v-model="permissionDialogVisible" :title="permissionDialogTitle" width="500px" :close-on-click-modal="false">
      <div class="permission-tree-container">
        <el-tree
          ref="menuTreeRef"
          :data="menuTreeData"
          :props="treeProps"
          node-key="id"
          show-checkbox
          :default-expand-all="true"
          :default-checked-keys="checkedMenuIds"
        />
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePermission">保存权限</el-button>
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

.action-section {
  margin-bottom: 16px;
}

.role-table {
  margin-bottom: 16px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
}

.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
}
</style>
