<script setup lang="ts">
/**
 * 活动管理列表页面
 *
 * 功能说明：
 * - 展示秒杀活动列表（分页）
 * - 支持按状态筛选
 * - 新增/编辑/删除/发布活动
 */
import { ref, onMounted } from 'vue'
import { seckillApi } from '@/api/seckill'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const activityList = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

async function loadActivities() {
  loading.value = true
  try {
    const res = await seckillApi.list()
    activityList.value = res.data || []
    total.value = res.data?.total || res.data?.length || 0
  } catch (e) {
    ElMessage.error('加载活动列表失败')
  } finally {
    loading.value = false
  }
}

function getStatusText(status: number) {
  const map: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return map[status] || '未知'
}

function getStatusType(status: number) {
  const map: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return map[status] || 'info'
}

function formatTime(time: string) {
  return new Date(time).toLocaleString()
}

function handleAdd() {
  router.push('/seckill/activity/add')
}

function handleEdit(id: number) {
  router.push(`/seckill/activity/${id}`)
}

async function handlePublish(id: number) {
  try {
    await ElMessageBox.confirm('发布活动将自动预热数据到Redis，确定发布吗？', '提示', {
      type: 'warning'
    })
    await seckillApi.start(id)
    ElMessage.success('发布成功')
    loadActivities()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('发布失败')
    }
  }
}

async function handleEnd(id: number) {
  try {
    await ElMessageBox.confirm('确定结束该活动吗？', '提示', {
      type: 'warning'
    })
    await seckillApi.end(id)
    ElMessage.success('结束成功')
    loadActivities()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('结束失败')
    }
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该活动吗？', '提示', {
      type: 'warning'
    })
    await seckillApi.delete(id)
    ElMessage.success('删除成功')
    loadActivities()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadActivities()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadActivities()
}

onMounted(() => {
  loadActivities()
})
</script>

<template>
  <div class="activity-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <el-button type="primary" @click="handleAdd">新增活动</el-button>
        </div>
      </template>

      <el-table :data="activityList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="description" label="活动描述" show-overflow-tooltip />
        <el-table-column label="活动时间" width="220">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="限购" width="80">
          <template #default="{ row }">
            {{ row.perLimit }} 件/人
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row.id!)">编辑</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handlePublish(row.id!)"
            >
              发布
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleEnd(row.id!)"
            >
              结束
            </el-button>
            <el-button type="danger" link @click="handleDelete(row.id!)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.activity-list {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
