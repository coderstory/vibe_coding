<script setup lang="ts">
/**
 * 文章编辑器组件
 *
 * 功能说明：
 * - 富文本编辑器（基于 wangEditor）
 * - 支持文章创建和编辑
 * - 支持标签管理和创建新标签
 * - 支持图片上传
 *
 * @module components/knowledge/ArticleEditor
 */
import { ref, watch, computed, shallowRef, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import { uploadFile } from '@/api/knowledge'
import { getArticleDetail, createArticle, updateArticle, getAllTags, createTag } from '@/api/knowledge'
import { ElMessage } from 'element-plus'
import type { KnowledgeTag, CreateArticleParams, UpdateArticleParams } from '@/api/types'

/** 文章表单数据结构 */
interface ArticleForm {
  /** 文章标题 */
  title: string
  /** 分类 ID */
  categoryId: number | null
  /** 标签 ID 列表 */
  tagIds: (string | number)[]
  /** 状态 */
  status: number
}

/** 组件Props定义 */
const props = defineProps<{
  /** 弹窗显示状态 */
  modelValue: boolean
  /** 文章 ID（编辑时传入） */
  articleId?: number | null
  /** 默认分类 ID */
  categoryId?: number | null
}>()

/** 组件事件定义 */
const emit = defineEmits<{
  /** 更新弹窗显示状态 */
  'update:modelValue': [value: boolean]
  /** 保存成功后触发 */
  'success': []
}>()

/** 弹窗显示状态（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 是否为编辑模式 */
const isEdit = computed(() => !!props.articleId)

/** 表单数据 */
const form = ref<ArticleForm>({
  title: '',
  categoryId: null,
  tagIds: [],
  status: 1
})

/** 所有可选标签列表 */
const allTags = ref<KnowledgeTag[]>([])

/** 富文本编辑器内容 */
const editorData = ref('')

/** 编辑器实例引用 */
const editorRef = shallowRef<InstanceType<typeof Editor> | null>(null)

/**
 * 编辑器创建回调
 * @param editor - 编辑器实例
 */
const handleCreated = (editor: InstanceType<typeof Editor>) => {
  editorRef.value = editor
}

/**
 * 自定义图片上传函数
 * @param file - 上传的文件
 * @param insertFn - 插入图片的回调函数
 */
const customUpload = (file: File, insertFn: (url: string, alt: string, href: string) => void) => {
  uploadFile(0, file.name, file).then(res => {
    insertFn(`/api/knowledge/files/${res.data.id}`, file.name, '')
  }).catch((err) => {
    ElMessage.error('图片上传失败')
    console.error('上传错误:', err)
  })
}

/**
 * 获取认证请求头
 * @returns 包含 Authorization 的请求头对象
 */
const getAuthHeaders = (): Record<string, string> => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

/** 富文本编辑器配置 */
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

/** 工具栏配置 */
const toolbarConfig = {
  excludeKeys: ['group-video']
}

/** 监听弹窗显示状态 */
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

/**
 * 加载标签列表
 */
async function loadTags() {
  try {
    const res = await getAllTags()
    allTags.value = res.data || []
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

/**
 * 加载文章详情
 */
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

/**
 * 重置表单
 */
function resetForm() {
  form.value = {
    title: '',
    categoryId: props.categoryId ?? null,
    tagIds: [],
    status: 1
  }
  editorData.value = ''
}

/**
 * 保存文章
 */
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

/** 组件卸载前销毁编辑器实例 */
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
        <!-- 标签选择器，支持创建新标签 -->
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
      <!-- 富文本编辑器区域 -->
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
