<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { resetConsumerOffset, getTopicList } from '@/api/rocketmq'

const props = defineProps<{
  modelValue: boolean
  groupName: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const dialogVisible = ref(props.modelValue)
const loading = ref(false)
const topicList = ref<{ topicName: string }[]>([])

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val) {
    loadTopics()
  }
})
watch(() => dialogVisible.value, (val) => {
  emit('update:modelValue', val)
})

const form = reactive({
  topic: '',
  timestamp: null as Date | null,
  force: false
})

async function loadTopics() {
  try {
    const res = await getTopicList()
    topicList.value = res.data.records
  } catch {
    // 忽略错误
  }
}

async function handleSubmit() {
  if (!form.topic) {
    ElMessage.warning('请选择 Topic')
    return
  }
  if (!form.timestamp) {
    ElMessage.warning('请选择时间')
    return
  }

  loading.value = true
  try {
    const timestamp = form.timestamp.getTime()
    await resetConsumerOffset(props.groupName, {
      topic: form.topic,
      timestamp
    })
    ElMessage.success('位点重置成功')
    emit('success')
    emit('update:modelValue', false)
  } catch (error: any) {
    ElMessage.error(error.message || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="重置消费位点"
    width="500px"
    @close="emit('update:modelValue', false)"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="Topic" required>
        <el-select v-model="form.topic" placeholder="选择 Topic" style="width: 100%">
          <el-option
            v-for="t in topicList"
            :key="t.topicName"
            :label="t.topicName"
            :value="t.topicName"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="重置时间" required>
        <el-date-picker
          v-model="form.timestamp"
          type="datetime"
          placeholder="选择日期和时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.force">强制重置</el-checkbox>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>