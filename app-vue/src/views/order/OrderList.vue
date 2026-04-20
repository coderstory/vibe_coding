<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order'
import type { Order } from '@/api/order'
import { ElMessage } from 'element-plus'

const orders = ref<Order[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await orderApi.getMyOrders()
    orders.value = res.data || []
  } finally {
    loading.value = false
  }
})

async function handlePay(orderNo: string) {
  try {
    await orderApi.payOrder(orderNo)
    ElMessage.success('支付成功')
    const res = await orderApi.getMyOrders()
    orders.value = res.data || []
  } catch {
    ElMessage.error('支付失败')
  }
}

async function handleCancel(orderNo: string) {
  try {
    await orderApi.cancelOrder(orderNo)
    ElMessage.success('取消成功')
    const res = await orderApi.getMyOrders()
    orders.value = res.data || []
  } catch {
    ElMessage.error('取消失败')
  }
}

function getStatusText(status: number) {
  const map = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '超时取消' }
  return map[status as keyof typeof map] || '未知'
}
</script>

<template>
  <div class="order-list">
    <el-card v-loading="loading">
      <template #header>
        <span>我的订单</span>
      </template>
      <div v-if="orders.length === 0" class="empty-tip">
        暂无订单
      </div>
      <el-table v-else :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="goodsId" label="商品ID" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="handlePay(row.orderNo)"
            >
              支付
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(row.orderNo)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.order-list {
  padding: 20px;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>