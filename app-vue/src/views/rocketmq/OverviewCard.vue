<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElStatistic, ElCard } from 'element-plus'
import { getClusterOverview, type ClusterOverviewVO } from '@/api/rocketmq'

const overview = ref<ClusterOverviewVO | null>(null)
const loading = ref(false)

async function loadOverview() {
  loading.value = true
  try {
    const res = await getClusterOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (e) {
    console.error('加载集群概览失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <el-row :gutter="16">
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic title="集群名称" :value="overview?.clusterName || '-'" />
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic title="Broker 数量" :value="overview?.brokerCount || 0" />
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic title="Topic 数量" :value="overview?.topicCount || 0" />
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card shadow="hover" v-loading="loading">
        <el-statistic title="Consumer Group" :value="overview?.consumerGroupCount || 0" />
      </el-card>
    </el-col>
  </el-row>
</template>