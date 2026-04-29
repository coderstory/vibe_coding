<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn, ElTag } from 'element-plus'
import { getBrokerStatusList, type BrokerStatusVO } from '@/api/rocketmq'

const loading = ref(false)
const brokers = ref<BrokerStatusVO[]>([])

async function loadBrokers() {
  loading.value = true
  try {
    const res = await getBrokerStatusList()
    if (res.code === 200) {
      brokers.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载 Broker 状态失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBrokers()
})
</script>

<template>
  <el-table :data="brokers" v-loading="loading" stripe border size="small" max-height="300">
    <el-table-column prop="brokerName" label="Broker 名称" min-width="130" show-overflow-tooltip />
    <el-table-column prop="brokerAddr" label="地址" min-width="120" show-overflow-tooltip />
    <el-table-column prop="status" label="状态" width="100" align="center">
      <template #default="{ row }">
        <el-tag :type="row.status === 'ONLINE' ? 'success' : 'danger'" size="small">
          {{ row.status }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="version" label="版本" min-width="80" align="center" />
  </el-table>
</template>