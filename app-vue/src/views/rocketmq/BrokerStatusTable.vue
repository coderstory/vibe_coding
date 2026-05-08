<script lang="ts">
/**
 * Broker 状态表格组件。展示 RocketMQ Broker 的运行状态、角色和负载信息。
 */
</script>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { ElTable, ElTableColumn, ElTag } from 'element-plus'
import { type BrokerStatusVO, getBrokerStatusList } from '@/api/modules/rocketmq'

const loading = ref(false)
const brokers = ref<BrokerStatusVO[]>([])

async function loadBrokers() {
  loading.value = true
  try {
    const res = await getBrokerStatusList()
    if (res.code === 200) {
      brokers.value = res.data.records || []
    }
  }
  catch (e) {
    console.error('加载 Broker 状态失败', e)
  }
  finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBrokers()
})
</script>

<template>
  <el-table v-loading="loading" :data="brokers" border max-height="300" size="small" stripe>
    <el-table-column label="Broker 名称" min-width="130" prop="brokerName" show-overflow-tooltip/>
    <el-table-column label="地址" min-width="120" prop="brokerAddr" show-overflow-tooltip/>
    <el-table-column align="center" label="状态" prop="status" width="100">
      <template #default="{ row }">
        <el-tag :type="row.status === 'ONLINE' ? 'success' : 'danger'" size="small">
          {{ row.status }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column align="center" label="版本" min-width="80" prop="version"/>
  </el-table>
</template>
