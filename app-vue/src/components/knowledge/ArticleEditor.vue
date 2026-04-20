<script setup lang="ts">
import { ref, watch, computed, shallowRef, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { uploadFile } from '@/api/knowledge'
import { getArticleDetail, createArticle, updateArticle, getAllTags, createTag } from '@/api/knowledge'
import { ElMessage } from 'element-plus'
import type { KnowledgeTag, CreateArticleParams, UpdateArticleParams } from '@/api/types'

interface ArticleForm {
  title: string
  categoryId: number | null
  tagIds: (string | number)[]
  status: number
}

const props = defineProps<{
  modelValue: boolean
  articleId?: number | null
  categoryId?: number | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.articleId)
const form = ref<ArticleForm>({
  title: '',
  categoryId: null,
  tagIds: [],
  status: 1
})
const allTags = ref<KnowledgeTag[]>([])
const editorData = ref('')
const editorRef = shallowRef<InstanceType<typeof Editor> | null>(null)

const handleCreated = (editor: InstanceType<typeof Editor>) => {
  editorRef.value = editor
}

const customUpload = (file: File, insertFn: (url: string, alt: string, href: string) => void) => {
  uploadFile(0, file.name, file).then(res => {
    insertFn(`/api/knowledge/files/${res.data.id}`, file.name, '')
  }).catch((err) => {
    ElMessage.error('图片上传失败')
    console.error('上传错误:', err)
  })
}

const getAuthHeaders = (): Record<string, string> => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

const editorConfig = {
  placeholder: '请输入内容',
  MENU_CONF: {
    uploadImage: {
      customUpload,
      maxFileSize: 10 * 1024 * 1024,
      maxNumberOfFiles: 20,
      allowedFileTypes: ['image/*'],
      metaWithUrl: false,
      headers: getAuthHeaders()
    }
  }
}

const toolbarConfig = {
  excludeKeys: ['group-video']
}

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
    const res = await getArticleDetail(props.articleId as number)
    const article = res.data
    // article.tags 已经是标签 ID 数组，只保留已存在的标签
    const tagIds = (article.tags || []).filter((tagId: number) => {
      return allTags.value.some(t => t.id === tagId)
    })
    form.value = {
      title: article.title,
      categoryId: article.categoryId,
      tagIds,
      status: article.status
    }
    editorData.value = article.content || ''
  } catch {
    ElMessage.error('加载文章失败')
  }
}

function resetForm() {
  form.value = {
    title: '',
    categoryId: props.categoryId ?? null,
    tagIds: [],
    status: 1
  }
  editorData.value = ''
}

async function handleSave() {
  if (!form.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.value.categoryId) {
    ElMessage.warning('请先选择分类')
    return
  }

  try {
    // 将 tagIds 中的新标签名称（string）转为已创建的标签 ID
    let tagIds = [...form.value.tagIds]
    const newTagNames = tagIds.filter(id => typeof id === 'string') as string[]
    for (const name of newTagNames) {
      const res = await createTag({ name, color: '#409EFF' })
      tagIds = tagIds.map(id => id === name ? res.data.id : id)
      allTags.value.push(res.data)
    }

    // 只保留数字 ID
    const tags = tagIds.filter(id => typeof id === 'number') as number[]

    const data = {
      title: form.value.title,
      categoryId: form.value.categoryId,
      content: editorData.value,
      tags,
      status: form.value.status
    }

    if (isEdit.value) {
      await updateArticle(props.articleId as number, data as UpdateArticleParams)
      ElMessage.success('更新成功')
    } else {
      await createArticle(data as CreateArticleParams)
      ElMessage.success('创建成功')
    }
    emit('success')
    visible.value = false
  } catch {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑知识' : '新建知识'"
    width="80%"
    :close-on-click-modal="false"
    lock-scroll
    append-to-body
    class="article-editor-dialog"
  >
    <el-form :model="form" label-width="80px">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入标题" maxlength="200" show-word-limit style="width: 300px" />
        <span style="margin: 0 16px">标签</span>
        <el-select
          v-model="form.tagIds"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="选择或输入新标签"
          style="width: 300px"
        >
          <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id">
            <span :style="{ color: tag.color }">{{ tag.name }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item class="editor-form-item">
        <div class="editor-container">
          <Toolbar :editor="editorRef" :default-config="toolbarConfig" mode="default" />
          <Editor
            v-model="editorData"
            :default-config="editorConfig"
            mode="default"
            class="editor-content"
            @onCreated="handleCreated"
          />
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.article-editor-dialog :deep(.el-dialog) {
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  margin-top: 5vh !important;
}

.article-editor-dialog :deep(.el-dialog__body) {
  flex: 1;
  overflow-y: auto;
  padding-top: 20px;
  padding-bottom: 20px;
}

.editor-form-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
}

.editor-form-item :deep(.el-form-item__content) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 300px;
  max-height: 50vh;
}

.editor-content {
  flex: 1;
  overflow-y: auto;
}

.editor-content :deep(.w-e-text-area) {
  min-height: 200px;
}
</style>
