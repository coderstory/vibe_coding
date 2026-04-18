<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserDetail } from '@/api/user'
import type { UserVO } from '@/api/types'

const route = useRoute()
const router = useRouter()
const user = ref<UserVO | null>(null)
const loading = ref(false)

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

function goBack() {
  router.push('/system/user')
}

function goToEdit() {
  router.push({ path: '/system/user', query: { editId: route.params.id as string } })
}

onMounted(() => {
  loadUser()
})
</script>

<template>
  <div class="page-container" v-loading="loading">
    <h2 class="page-title">用户详情</h2>
    <el-button @click="goBack">返回列表</el-button>
    <el-button type="primary" @click="goToEdit">编辑</el-button>
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
