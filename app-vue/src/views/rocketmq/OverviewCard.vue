<script lang="ts" setup>
import {onMounted, ref} from 'vue'
import {ElCard, ElStatistic} from 'element-plus'
import {type ClusterOverviewVO, getClusterOverview} from '@/api/modules/rocketmq'

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
      <el-card v-loading="loading" shadow="hover">
        <el-statistic :value="overview?.clusterName || '-'" title="集群名称"/>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card v-loading="loading" shadow="hover">
        <el-statistic :value="overview?.brokerCount || 0" title="Broker 数量"/>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card v-loading="loading" shadow="hover">
        <el-statistic :value="overview?.topicCount || 0" title="Topic 数量"/>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card v-loading="loading" shadow="hover">
        <el-statistic :value="overview?.consumerGroupCount || 0" title="Consumer Group"/>
      </el-card>
    </el-col>
  </el-row>
</template>
