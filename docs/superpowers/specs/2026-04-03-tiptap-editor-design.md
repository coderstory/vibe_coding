# Tiptap 富文本编辑器集成设计

**日期：** 2026-04-03
**状态：** approved

---

## 背景

Phase 03 知识库系统需要集成富文本编辑器，当前 Tiptap v3.22.1 在 Vue 3 中无法正常渲染内容。问题根因是 CSS 样式冲突或组件配置问题。

---

## 功能需求

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 基础富文本 | P0 | 加粗、斜体、标题、列表、链接 |
| 图片插入 | P0 | 支持粘贴/拖拽上传 |
| Markdown 支持 | P0 | Markdown 语法快捷输入 |
| 代码块 | P0 | 代码高亮显示 |
| 文件上传 | P1 | 附件上传功能 |

---

## 技术方案

### 依赖

| 包 | 版本 | 用途 |
|----|------|------|
| `@tiptap/vue-3` | 3.22.1 | Vue 3 集成 |
| `@tiptap/starter-kit` | 3.22.1 | 基础富文本功能 |
| `@tiptap/extension-image` | 3.22.1 | 图片插入 |
| `@tiptap/markdown` | 3.22.1 | Markdown 支持 |
| `@tiptap/pm` | 3.22.1 | ProseMirror 核心 |

### 核心配置

```javascript
const editor = useEditor({
  content: '',
  extensions: [
    StarterKit.configure({
      heading: false,
      codeBlock: true
    }),
    Image.configure({
      inline: true,
      allowBase64: true
    }),
    Markdown.configure({
      html: false,
      transformCopiedText: true
    })
  ],
  onUpdate: ({ editor }) => {
    form.value.content = editor.getHTML()
  }
})
```

### CSS 隔离策略

```css
.tiptap-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 400px;
}

.tiptap-editor :deep(.ProseMirror) {
  min-height: 380px;
  padding: 16px;
  outline: none !important;
  box-sizing: border-box;
}

.tiptap-editor :deep(.ProseMirror p) {
  margin: 0 0 8px 0;
}
```

---

## 文件变更

| 文件 | 操作 |
|------|------|
| `app-vue/src/components/knowledge/ArticleEditor.vue` | 重写编辑器组件 |
| `app-vue/src/main.js` | 检查是否需要引入 Tiptap 样式 |

---

## 验证方式

1. 启动前端开发服务器
2. 打开知识库 -> 新建知识
3. 确认编辑器可编辑
4. 测试各项功能：
   - 输入文字并应用加粗/斜体
   - 插入图片
   - 使用 Markdown 语法（如 `**bold**`）
   - 插入代码块
   - 上传附件
5. 保存后重新编辑，确认内容回显正确
