# Tiptap 富文本编辑器实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 集成 Tiptap 富文本编辑器到知识库系统，支持加粗、斜体、标题、列表、链接、图片、Markdown、代码块、文件上传功能。

**Architecture:** 使用 @tiptap/vue-3 的 useEditor 和 EditorContent 组件，通过 StarterKit 提供基础功能，Image 扩展支持图片，Markdown 扩展支持 Markdown 语法。样式使用 :deep() 确保穿透 Element Plus 样式隔离。

**Tech Stack:** Vue 3.5 + Vite 8 + Tiptap 3.22.1 + Element Plus 2.9

---

## 文件清单

**修改文件：**
- `app-vue/src/components/knowledge/ArticleEditor.vue` - 主编辑器组件

---

## 实现步骤

### Task 1: 验证基础渲染

**目标:** 确保 Tiptap 编辑器能正确渲染，验证 CSS 穿透生效

- [ ] **Step 1: 重写 ArticleEditor.vue 使用最小配置**

```vue
<script setup>
import { ref, watch, computed, onBeforeUnmount } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import { StarterKit } from '@tiptap/starter-kit'
import { getArticleDetail, createArticle, updateArticle, getAllTags, createTag } from '@/api/knowledge'
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
      <el-form-item label="内容">
        <div class="tiptap-editor">
          <editor-content :editor="editor" />
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
.form紧凑 {
  padding: 0 20px;
}
.tiptap-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 400px;
}
.tiptap-editor :deep(.ProseMirror) {
  min-height: 400px;
  padding: 16px;
  outline: none;
}
.tiptap-editor :deep(.ProseMirror p) {
  margin: 0 0 8px 0;
}
</style>
```

- [ ] **Step 2: 验证编辑器渲染**

启动前端 `npm run dev`，打开浏览器控制台，新建知识，确认：
1. 对话框弹出
2. 标题输入框正常
3. 内容区域显示 "输入知识内容..." 文字

---

### Task 2: 添加图片支持

**目标:** 支持在编辑器中插入图片

- [ ] **Step 1: 修改 ArticleEditor.vue 添加 Image 扩展**

在 `<script setup>` 中：
```javascript
import { StarterKit } from '@tiptap/starter-kit'
import TiptapImage from '@tiptap/extension-image'
```

在 `useEditor` 的 extensions 中添加：
```javascript
extensions: [
  StarterKit,
  TiptapImage.configure({
    inline: true,
    allowBase64: true
  })
]
```

- [ ] **Step 2: 添加图片上传处理**

添加方法：
```javascript
function handleImageUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = () => {
        editor.value?.chain().focus().setImage({ src: reader.result }).run()
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
}
```

在模板中添加图片按钮（在 el-form-item 内容后）：
```html
<el-form-item>
  <el-button @click="handleImageUpload">插入图片</el-button>
</el-form-item>
```

- [ ] **Step 3: 验证图片功能**

插入图片，检查图片是否正确显示。

---

### Task 3: 添加 Markdown 支持

**目标:** 支持 Markdown 语法快捷输入

- [ ] **Step 1: 添加 Markdown 扩展**

```javascript
import { Markdown } from '@tiptap/markdown'
```

在 useEditor extensions 中添加：
```javascript
Markdown.configure({
  html: false,
  transformCopiedText: true,
  transformPastedText: true
})
```

- [ ] **Step 2: 验证 Markdown 功能**

在编辑器中输入 `**粗体**`、`*斜体*`、`` ``` ``，验证渲染。

---

### Task 4: 添加文件上传功能

**目标:** 支持上传附件

- [ ] **Step 1: 添加文件上传逻辑**

```javascript
const fileList = ref([])
const uploadedFiles = ref([])

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
```

- [ ] **Step 2: 添加 el-upload 组件**

在模板中添加：
```html
<el-form-item label="附件">
  <el-upload
    :file-list="fileList"
    :before-upload="handleFileUpload"
    multiple
  >
    <el-button>上传文件</el-button>
  </el-upload>
</el-form-item>
```

- [ ] **Step 3: 验证文件上传**

上传文件，检查文件列表显示。

---

### Task 5: 完整测试

**目标:** 端到端测试所有功能

- [ ] **Step 1: 新建知识测试**
  - 选择分类
  - 输入标题
  - 使用加粗、斜体
  - 插入图片
  - 使用 Markdown 语法
  - 上传附件
  - 保存

- [ ] **Step 2: 编辑知识测试**
  - 打开已保存的知识
  - 验证内容正确回显
  - 修改内容并保存

- [ ] **Step 3: 验证数据完整性**
  - 检查数据库中的 content 字段
  - 验证富文本 HTML 正确存储
