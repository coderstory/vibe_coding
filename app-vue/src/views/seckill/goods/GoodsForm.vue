<script setup lang="ts">
/**
 * 商品表单页面
 *
 * 功能说明：
 * - 新增/编辑商品
 * - 表单验证
 * - 保存后返回列表
 */
import { ref, onMounted } from 'vue'
import { goodsApi, type SeckillGoods } from '@/api/goods'
import { activityApi } from '@/api/seckill'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isEdit = ref(false)
const loading = ref(false)
const form = ref<SeckillGoods>({
  activityId: null as any,
  name: '',
  originalPrice: 0,
  seckillPrice: 0,
  stock: 0,
  imageUrl: ''
})

const activityList = ref<any[]>([])

async function loadActivities() {
  try {
    const res = await activityApi.list()
    activityList.value = res.data?.records || []
  } catch (e) {
    console.error('加载活动列表失败', e)
  }
}

async function loadGoods(id: number) {
  loading.value = true
  try {
    const res = await goodsApi.getGoods(id)
    form.value = res.data
    // 如果商品关联的活动在列表中不存在，保留原活动ID供后续处理
    if (form.value.activityId) {
      const activityExists = activityList.value.some(a => a.id === form.value.activityId)
      if (!activityExists) {
        ElMessage.warning('该商品关联的活动已不存在或已下架')
      }
    }
  } catch (e) {
    ElMessage.error('加载商品失败，但您仍可以编辑表单')
    // 不关闭页面，让用户可以继续编辑
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!form.value.name) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (form.value.originalPrice <= 0) {
    ElMessage.warning('请输入正确的原价')
    return
  }
  if (form.value.seckillPrice <= 0) {
    ElMessage.warning('请输入正确的秒杀价')
    return
  }
  if (form.value.stock <= 0) {
    ElMessage.warning('请输入正确的库存')
    return
  }

  loading.value = true
  try {
    if (isEdit.value) {
      await goodsApi.updateGoods(form.value.id!, form.value)
      ElMessage.success('更新成功')
    } else {
      await goodsApi.createGoods(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/seckill/goods')
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.back()
}

onMounted(async () => {
  await loadActivities()
  const id = route.params.id
  if (id) {
    isEdit.value = true
    await loadGoods(Number(id))
  }
})
</script>

<template>
  <div class="goods-form">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑商品' : '新增商品' }}</span>
      </template>

      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="所属活动" required>
          <el-select v-model="form.activityId" placeholder="请选择活动" style="width: 300px;">
            <el-option
              v-for="activity in activityList"
              :key="activity.id"
              :label="activity.name"
              :value="activity.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" placeholder="请输入商品名称" style="width: 300px;" />
        </el-form-item>

        <el-form-item label="商品图片">
          <el-input v-model="form.imageUrl" placeholder="请输入图片URL" style="width: 400px;" />
        </el-form-item>

        <el-form-item label="原价" required>
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 200px;" />
        </el-form-item>

        <el-form-item label="秒杀价" required>
          <el-input-number v-model="form.seckillPrice" :min="0" :precision="2" style="width: 200px;" />
        </el-form-item>

        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" style="width: 200px;" />
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
.goods-form {
  padding: 20px;
  max-width: 800px;
}
</style>
