<script setup lang="ts">
/**
 * 活动表单页面
 *
 * 功能说明：
 * - 新增/编辑秒杀活动
 * - 表单验证
 * - 保存后返回列表
 */
import { ref, onMounted, watch } from 'vue'
import { activityApi } from '@/api/seckill'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isEdit = ref(false)
const loading = ref(false)
const form = ref({
  name: '',
  description: '',
  startTime: '',
  endTime: '',
  status: 0,
  perLimit: 1,
  enableCaptcha: true,
  enableIpLimit: true
})

async function loadActivity(id: number) {
  loading.value = true
  try {
    const res = await activityApi.get(id)
    const data = res.data
    form.value = {
      name: data.name,
      description: data.description,
      startTime: data.startTime,
      endTime: data.endTime,
      status: data.status,
      perLimit: data.perLimit,
      enableCaptcha: data.enableCaptcha,
      enableIpLimit: data.enableIpLimit
    }
  } catch (e) {
    ElMessage.error('加载活动失败')
    router.back()
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.name) {
    ElMessage.warning('请输入活动名称')
    return
  }
  if (!form.value.startTime || !form.value.endTime) {
    ElMessage.warning('请选择活动时间')
    return
  }
  if (new Date(form.value.startTime) >= new Date(form.value.endTime)) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  if (form.value.perLimit <= 0) {
    ElMessage.warning('请输入正确的限购数量')
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await activityApi.update(Number(route.params.id), form.value)
      ElMessage.success('更新成功')
    } else {
      await activityApi.create(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/seckill/activity')
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.back()
}

onMounted(() => {
  checkRouteAndInit()
})

// 监听路由变化，解决新增页面保留编辑数据的问题
watch(
  () => route.params.id,
  () => {
    checkRouteAndInit()
  }
)

function checkRouteAndInit() {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    loadActivity(Number(id))
  } else {
    // 新增模式，重置表单
    isEdit.value = false
    form.value = {
      name: '',
      description: '',
      startTime: '',
      endTime: '',
      status: 0,
      perLimit: 1,
      enableCaptcha: true,
      enableIpLimit: true
    }
  }
}
</script>

<template>
  <div class="activity-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑活动' : '新增活动' }}</span>
      </template>

      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" placeholder="请输入活动名称" style="width: 300px;" />
        </el-form-item>

        <el-form-item label="活动描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="请输入活动描述"
            :rows="3"
            style="width: 400px;"
          />
        </el-form-item>

        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 220px;"
          />
        </el-form-item>

        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 220px;"
          />
        </el-form-item>

        <el-form-item label="每人限购">
          <el-input-number v-model="form.perLimit" :min="1" :max="10" style="width: 200px;" />
          <span style="margin-left: 10px; color: #999;">件/人</span>
        </el-form-item>

        <el-form-item label="安全设置">
          <el-checkbox v-model="form.enableCaptcha">启用验证码</el-checkbox>
          <el-checkbox v-model="form.enableIpLimit">启用IP限制</el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.activity-form {
  padding: 20px;
  max-width: 800px;
}
</style>
