<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { ElTable, ElTableColumn } from 'element-plus'
import { getTopicBacklogList, type TopicBacklogVO } from '@/api/modules/rocketmq'

const loading = ref(false)
const topics = ref<TopicBacklogVO[]>([])

async function loadTopics() {
  loading.value = true
  try {
    const res = await getTopicBacklogList()
    if (res.code === 200) {
      topics.value = res.data.records || []
    }
  }
  catch (e) {
    console.error('加载 Topic 堆积量失败', e)
  }
  finally {
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
  <el-table v-loading="loading" :data="topics" border max-height="300" size="small" stripe>
    <el-table-column label="Topic 名称" min-width="150" prop="topicName" show-overflow-tooltip/>
    <el-table-column align="center" label="堆积量" prop="diff" width="100">
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
