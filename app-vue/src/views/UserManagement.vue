<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, resetUserPassword, getAllRoles } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  department: '',
  enabled: null
})

// 用户列表数据
const userList = ref([])
const total = ref(0)
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 20
})

// 角色列表
const roleList = ref([])

// 新建/编辑用户对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建用户')
const userForm = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  gender: 1,
  email: '',
  department: '',
  position: '',
  roleId: null,
  enabled: 1,
  avatar: ''
})
const userFormRef = ref(null)
const userFormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}
const isEdit = ref(false)

// 重置密码对话框
const passwordDialogVisible = ref(false)
const passwordForm = reactive({
  id: null,
  username: '',
  password: '',
  confirmPassword: ''
})
const passwordRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }]
}

// 状态选项
const statusOptions = [
  { value: null, label: '全部' },
  { value: 1, label: '启用' },
  { value: 0, label: '禁用' }
]

// 性别选项
const genderOptions = [
  { value: 1, label: '男' },
  { value: 0, label: '女' }
]

// 加载用户列表
async function loadUserList() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.username) params.username = searchForm.username
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.department) params.department = searchForm.department
    if (searchForm.enabled !== null) params.enabled = searchForm.enabled
    
    const res = await getUserList(params)
    userList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载角色列表
async function loadRoles() {
  try {
    const res = await getAllRoles()
    roleList.value = res.data
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadUserList()
}

// 重置搜索
function handleReset() {
  searchForm.username = ''
  searchForm.name = ''
  searchForm.department = ''
  searchForm.enabled = null
  pagination.page = 1
  loadUserList()
}

// 分页变化
function handlePageChange(page) {
  pagination.page = page
  loadUserList()
}

function handleSizeChange(size) {
  pagination.size = size
  pagination.page = 1
  loadUserList()
}

// 新建用户
function handleCreate() {
  isEdit.value = false
  dialogTitle.value = '新建用户'
  resetUserForm()
  dialogVisible.value = true
}

// 编辑用户
function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  userForm.id = row.id
  userForm.username = row.username
  userForm.password = ''
  userForm.name = row.name
  userForm.gender = row.gender
  userForm.email = row.email
  userForm.department = row.department
  userForm.position = row.position
  userForm.roleId = row.roleId
  userForm.enabled = row.enabled
  userForm.avatar = row.avatar
  dialogVisible.value = true
}

// 重置密码
function handleResetPassword(row) {
  passwordForm.id = row.id
  passwordForm.username = row.username
  passwordForm.password = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

// 确认重置密码
async function confirmResetPassword() {
  if (passwordForm.password !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入密码不一致')
    return
  }
  try {
    await resetUserPassword(passwordForm.id, passwordForm.password)
    ElMessage.success('密码重置成功')
    passwordDialogVisible.value = false
  } catch (error) {
    ElMessage.error('密码重置失败')
  }
}

// 删除用户
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除用户「${row.username}」吗？删除后不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('用户删除成功')
      loadUserList()
    } catch (error) {
      ElMessage.error('用户删除失败')
    }
  }).catch(() => {})
}

// 保存用户
async function handleSaveUser() {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (isEdit.value) {
        await updateUser(userForm.id, {
          name: userForm.name,
          gender: userForm.gender,
          email: userForm.email,
          department: userForm.department,
          position: userForm.position,
          roleId: userForm.roleId,
          enabled: userForm.enabled,
          avatar: userForm.avatar
        })
        ElMessage.success('用户更新成功')
      } else {
        await createUser({
          username: userForm.username,
          password: userForm.password,
          name: userForm.name,
          gender: userForm.gender,
          email: userForm.email,
          department: userForm.department,
          position: userForm.position,
          roleId: userForm.roleId,
          enabled: userForm.enabled,
          avatar: userForm.avatar
        })
        ElMessage.success('用户创建成功')
      }
      dialogVisible.value = false
      loadUserList()
    } catch (error) {
      ElMessage.error(isEdit.value ? '用户更新失败' : '用户创建失败')
    }
  })
}

// 重置用户表单
function resetUserForm() {
  userForm.id = null
  userForm.username = ''
  userForm.password = ''
  userForm.name = ''
  userForm.gender = 1
  userForm.email = ''
  userForm.department = ''
  userForm.position = ''
  userForm.roleId = null
  userForm.enabled = 1
  userForm.avatar = ''
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

// 格式化性别
function formatGender(gender) {
  return gender === 1 ? '男' : '女'
}

// 格式化状态
function formatStatus(enabled) {
  return enabled === 1 ? '启用' : '禁用'
}

// 初始化
onMounted(() => {
  loadUserList()
  loadRoles()
})
</script>

<template>
  <div class="page-container">
    <h2 class="page-title">用户管理</h2>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="searchForm.department" placeholder="请输入部门" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.enabled" placeholder="全部" clearable style="width: 120px">
            <el-option
              v-for="item in statusOptions"
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
    
    <!-- 操作按钮 -->
    <div class="action-section">
      <el-button type="primary" @click="handleCreate">新建用户</el-button>
    </div>
    
    <!-- 用户列表 -->
    <el-table :data="userList" stripe border v-loading="loading" class="user-table">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="gender" label="性别" width="80" align="center">
        <template #default="{ row }">
          {{ formatGender(row.gender) }}
        </template>
      </el-table-column>
      <el-table-column prop="department" label="部门" min-width="120" />
      <el-table-column prop="position" label="岗位" min-width="100" />
      <el-table-column prop="email" label="邮箱" min-width="150" />
      <el-table-column prop="enabled" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.enabled === 1 ? 'success' : 'info'" size="small">
            {{ formatStatus(row.enabled) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          <el-button link type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
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
    
    <!-- 新建/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="userFormRef" :model="userForm" :rules="userFormRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio v-for="item in genderOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="userForm.department" placeholder="请输入部门" />
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-input v-model="userForm.position" placeholder="请输入岗位" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleList"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="userForm.enabled"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveUser">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 重置密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="重置密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="用户">
          <span>{{ passwordForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResetPassword">确认重置</el-button>
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

.user-table {
  margin-bottom: 16px;
}

.pagination-section {
  display: flex;
  justify-content: flex-end;
}
</style>
