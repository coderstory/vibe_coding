<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import type {Order} from '@/api/modules/order'
import {orderApi} from '@/api/modules/order'
import {ElMessage} from 'element-plus'

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
  const map = {0: '待支付', 1: '已支付', 2: '已取消', 3: '超时取消'}
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
        <el-table-column label="订单号" prop="orderNo" width="200"/>
        <el-table-column label="商品ID" prop="goodsId" width="100"/>
        <el-table-column label="数量" prop="quantity" width="80"/>
        <el-table-column label="价格" prop="price" width="100"/>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="createTime" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              size="small"
              type="primary"
              @click="handlePay(row.orderNo)"
            >
              支付
            </el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="danger"
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
