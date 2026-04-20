<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order'
import type { Order } from '@/api/order'

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

function formatTime(time: string) {
  return new Date(time).toLocaleString()
}

function getStatusText(status: number) {
  const map = { 0: '待支付', 1: '已支付', 2: '已取消', 3: '超时取消' }
  return map[status as keyof typeof map] || '未知'
}
</script>

<template>
  <div class="seckill-record">
    <el-card v-loading="loading">
      <template #header>
        <span>抢购记录</span>
      </template>
      <div v-if="orders.length === 0" class="empty-tip">
        暂无抢购记录
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
        <el-table-column prop="createTime" label="下单时间">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.seckill-record {
  padding: 20px;
}
.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}
</style>