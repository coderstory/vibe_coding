<script setup>
import { ref, watch, computed, onBeforeUnmount, onMounted } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import { StarterKit } from '@tiptap/starter-kit'
import { getArticleDetail, createArticle, updateArticle, getAllTags, getArticleTags, uploadFile, createTag } from '@/api/knowledge'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean,
  articleId: Number,
  categoryId: Number
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.articleId)
const form = ref({
  title: '',
  categoryId: null,
  content: '',
  tagIds: [],
  status: 1
})
const allTags = ref([])
const fileList = ref([])
const uploadedFiles = ref([])

const editor = useEditor({
  content: '<p>输入知识内容...</p>',
  extensions: [
    StarterKit
  ],
  onUpdate: ({ editor }) => {
    form.value.content = editor.getHTML()
  }
})

watch(visible, async (val) => {
  if (val) {
    await loadTags()
    if (props.articleId) {
      await loadArticle()
    } else {
      resetForm()
    }
  }
})

async function loadTags() {
  try {
    const res = await getAllTags()
    allTags.value = res.data || []
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

async function loadArticle() {
  try {
    const res = await getArticleDetail(props.articleId)
    const article = res.data
    form.value = {
      title: article.title,
      categoryId: article.categoryId,
      content: article.content || '',
      tagIds: article.tags?.map(t => t.id || t) || [],
      status: article.status
    }
    editor.value?.commands.setContent(article.content || '')
  } catch (e) {
    ElMessage.error('加载文章失败')
  }
}

function resetForm() {
  form.value = {
    title: '',
    categoryId: props.categoryId,
    content: '',
    tagIds: [],
    status: 1
  }
  editor.value?.commands.clearContent()
  uploadedFiles.value = []
  fileList.value = []
}

async function handleSave() {
  if (!form.value.title) {
    ElMessage.warning('请输入标题')
    return
  }

  try {
    let tagIds = [...form.value.tagIds]
    const newTagNames = tagIds.filter(id => typeof id === 'string')
    for (const name of newTagNames) {
      const res = await createTag({ name, color: '#409EFF' })
      tagIds = tagIds.map(id => id === name ? res.data.id : id)
      allTags.value.push(res.data)
    }

    const data = {
      title: form.value.title,
      categoryId: form.value.categoryId,
      content: form.value.content,
      tagIds: tagIds.filter(id => typeof id === 'number'),
      status: form.value.status
    }

    if (isEdit.value) {
      await updateArticle(props.articleId, data)
      ElMessage.success('更新成功')
    } else {
      await createArticle(data)
      ElMessage.success('创建成功')
    }
    emit('success')
    visible.value = false
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

async function handleFileUpload(file) {
  try {
    const res = await uploadFile(0, file.name, file)
    uploadedFiles.value.push(res.data)
    fileList.value.push({ name: file.name, url: res.data.id })
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
  return false
}

function handleFileRemove(file) {
  const idx = fileList.value.findIndex(f => f.name === file.name)
  if (idx !== -1) {
    fileList.value.splice(idx, 1)
    uploadedFiles.value.splice(idx, 1)
  }
}

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑知识' : '新建知识'"
    width="90%"
    fullscreen
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="80px" class="form紧凑">
      <el-row :gutter="12">
        <el-col :xs="24" :sm="16" :md="8">
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="请输入标题" />
          </el-form-item>
        </el-col>
        <el-col :xs="24" :sm="8" :md="4">
          <el-form-item label="标签">
            <el-select
              v-model="form.tagIds"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="选择或输入新标签"
              style="width: 100%"
            >
              <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id">
                <span :style="{ color: tag.color }">{{ tag.name }}</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <div style="margin-bottom: 20px;">
        <label style="display: block; margin-bottom: 8px; font-weight: 500;">内容</label>
        <div class="editor-container">
          <div>editor ref: {{ editor }}</div>
          <editor-content :editor="editor" />
        </div>
      </div>
      <el-form-item label="附件">
        <el-upload
          :file-list="fileList"
          :before-upload="handleFileUpload"
          multiple
        >
          <el-button>上传文件</el-button>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 400px;
  padding: 0;
}
</style>
