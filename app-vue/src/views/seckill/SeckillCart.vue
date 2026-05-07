<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import type { Cart } from '@/api/modules/cart'
import { cartApi } from '@/api/modules/cart'
import { ElMessage } from 'element-plus'

const carts = ref<Cart[]>([])
const loading = ref(false)

onMounted(async () => {
  await loadCart()
})

async function loadCart() {
  loading.value = true
  try {
    const res = await cartApi.getMyCart()
    carts.value = res.data || []
  }
  finally {
    loading.value = false
  }
}

async function handleRemove(goodsId: number) {
  await cartApi.removeFromCart(goodsId)
  ElMessage.success('移除成功')
  await loadCart()
}

async function handleClear() {
  await cartApi.clearCart()
  ElMessage.success('清空成功')
  carts.value = []
}
</script>

<template>
  <div class="seckill-cart">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>秒杀购物车</span>
          <el-button v-if="carts.length > 0" size="small" type="danger" @click="handleClear">
            清空购物车
          </el-button>
        </div>
      </template>
      <div v-if="carts.length === 0" class="empty-tip">
        购物车是空的
      </div>
      <el-table v-else :data="carts" style="width: 100%">
        <el-table-column label="商品ID" prop="goodsId" width="200"/>
        <el-table-column label="数量" prop="quantity" width="100"/>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleRemove(row.goodsId)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.seckill-cart {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>
