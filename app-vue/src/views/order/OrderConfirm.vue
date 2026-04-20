<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

function handleConfirm() {
  loading.value = true
  ElMessage.success('订单已确认')
  router.push('/order/list')
}
</script>

<template>
  <div class="order-confirm">
    <el-card>
      <template #header>
        <span>订单确认</span>
      </template>
      <div class="confirm-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商品">秒杀商品</el-descriptions-item>
          <el-descriptions-item label="数量">1</el-descriptions-item>
          <el-descriptions-item label="配送地址">
            <el-input placeholder="请输入收货地址" />
          </el-descriptions-item>
        </el-descriptions>
        <div class="action-area">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :loading="loading" @click="handleConfirm">
            确认订单
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.order-confirm {
  padding: 20px;
}
.confirm-content {
  padding: 20px 0;
}
.action-area {
  margin-top: 20px;
  text-align: right;
}
</style>