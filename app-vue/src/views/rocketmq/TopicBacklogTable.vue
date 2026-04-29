<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElTable, ElTableColumn } from 'element-plus'
import { getTopicBacklogList, type TopicBacklogVO } from '@/api/rocketmq'

const loading = ref(false)
const topics = ref<TopicBacklogVO[]>([])

async function loadTopics() {
  loading.value = true
  try {
    const res = await getTopicBacklogList()
    if (res.code === 200) {
      topics.value = res.data.records || []
    }
  } catch (e) {
    console.error('加载 Topic 堆积量失败', e)
  } finally {
    loading.value = false
  }
}

const formatDiff = (diff: number): string => {
  if (diff >= 10000) {
    return (diff / 10000).toFixed(1) + '万'
  }
  return diff.toString()
}

onMounted(() => {
  loadTopics()
})
</script>

<template>
  <el-table :data="topics" v-loading="loading" stripe border size="small" max-height="300">
    <el-table-column prop="topicName" label="Topic 名称" min-width="150" show-overflow-tooltip />
    <el-table-column prop="diff" label="堆积量" width="100" align="center">
      <template #default="{ row }">
        <span :class="{ 'text-danger': row.diff > 10000 }">
          {{ formatDiff(row.diff) }}
        </span>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>