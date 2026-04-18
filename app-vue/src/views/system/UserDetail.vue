<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getUserDetail, updateUser, getAllRoles } from '@/api/user'
import type { UserVO, Role, UpdateUserParams } from '@/api/types'

const route = useRoute()
const router = useRouter()

const user = ref<UserVO | null>(null)
const loading = ref(false)
const saving = ref(false)

// 编辑对话框
const dialogVisible = ref(false)
const dialogTitle = ref('编辑用户')
const userFormRef = ref<FormInstance | null>(null)
const roles = ref<Role[]>([])

const userForm = reactive({
  name: '',
  gender: 1 as number,
  email: '',
  department: '',
  position: '',
  roleId: null as number | null,
  enabled: 1 as number,
  phone: ''
})

const userFormRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function loadUser() {
  loading.value = true
  try {
    const res = await getUserDetail(Number(route.params.id))
    user.value = res.data
  } catch {
    ElMessage.error('加载用户详情失败')
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    const res = await getAllRoles()
    roles.value = res.data || []
  } catch {
    ElMessage.error('加载角色列表失败')
  }
}

function goBack() {
  router.push('/system/user')
}

function openEditDialog() {
  if (!user.value) return
  dialogTitle.value = '编辑用户'
  userForm.name = user.value.name || ''
  userForm.gender = user.value.gender || 1
  userForm.email = user.value.email || ''
  userForm.department = user.value.department || ''
  userForm.position = user.value.position || ''
  userForm.roleId = user.value.roleId || null
  userForm.enabled = user.value.enabled || 1
  userForm.phone = user.value.phone || ''
  dialogVisible.value = true
}

async function handleSave() {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      const params: UpdateUserParams = {
        name: userForm.name,
        gender: userForm.gender,
        email: userForm.email,
        department: userForm.department,
        position: userForm.position,
        roleId: userForm.roleId ?? undefined,
        enabled: userForm.enabled,
        phone: userForm.phone
      }
      await updateUser(Number(route.params.id), params)
      ElMessage.success('用户更新成功')
      dialogVisible.value = false
      loadUser()
    } catch {
      ElMessage.error('用户更新失败')
    } finally {
      saving.value = false
    }
  })
}

onMounted(() => {
  loadUser()
  loadRoles()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <h2 class="page-title">用户详情</h2>
    <el-button @click="goBack">返回列表</el-button>
    <el-button type="primary" @click="openEditDialog">编辑</el-button>
    <el-descriptions v-if="user" :column="2" border style="margin-top: 20px">
      <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user.name }}</el-descriptions-item>
      <el-descriptions-item label="手机">{{ user.phone }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ user.email }}</el-descriptions-item>
      <el-descriptions-item label="部门">{{ user.department }}</el-descriptions-item>
      <el-descriptions-item label="岗位">{{ user.position }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user.roleName }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="user.enabled === 1 ? 'success' : 'info'">
          {{ user.enabled === 1 ? '启用' : '禁用' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ user.createTime }}</el-descriptions-item>
    </el-descriptions>

    <!-- 编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" :close-on-click-modal="false">
      <el-form ref="userFormRef" :model="userForm" :rules="userFormRules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
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
              v-for="item in roles"
              :key="item.id"
              :label="item.roleName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-radio-group v-model="userForm.enabled">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
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
</style>
