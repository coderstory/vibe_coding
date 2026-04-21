<script setup lang="ts">
/**
 * 商品管理列表页面
 *
 * 功能说明：
 * - 展示秒杀商品列表（分页）
 * - 支持按活动筛选
 * - 支持搜索
 * - 新增/编辑/删除商品
 */
import { ref, onMounted, onActivated } from 'vue'
import { goodsApi, type SeckillGoods } from '@/api/goods'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const goodsList = ref<SeckillGoods[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const activityId = ref<number | undefined>(undefined)

async function loadGoods() {
  loading.value = true
  try {
    const res = await goodsApi.getGoodsPage(currentPage.value, pageSize.value, activityId.value)
    goodsList.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  router.push('/seckill/goods/add')
}

function handleEdit(id: number) {
  router.push(`/seckill/goods/${id}`)
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该商品吗？', '提示', {
      type: 'warning'
    })
    await goodsApi.deleteGoods(id)
    ElMessage.success('删除成功')
    loadGoods()
  } catch (e) {
    // 错误消息已由 axios 拦截器通过 ElMessage 显示，此处无需重复处理
    if (e === 'cancel') {
      // 用户取消确认对话框，不做任何处理
    }
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  loadGoods()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  loadGoods()
}

onMounted(() => {
  loadGoods()
})

// keep-alive 缓存激活时刷新数据
onActivated(() => {
  loadGoods()
})
</script>

<template>
  <div class="goods-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <el-button type="primary" @click="handleAdd">新增商品</el-button>
        </div>
      </template>

      <el-table :data="goodsList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="originalPrice" label="原价" width="100">
          <template #default="{ row }">
            ¥{{ row.originalPrice }}
          </template>
        </el-table-column>
        <el-table-column prop="seckillPrice" label="秒杀价" width="100">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.seckillPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="sold" label="已售" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row.id!)">编辑</el-button>
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
.goods-list {
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
