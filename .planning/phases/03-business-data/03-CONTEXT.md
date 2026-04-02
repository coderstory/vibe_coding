# Phase 3: 业务数据管理 - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段交付完整知识库系统：
- 树形分类管理（一对多：分类 → 知识表单）
- 知识表单（标题、正文、标签、附件）
- 富文本编辑器（Tiptap，支持 Markdown）
- 文件管理模块（zstd 压缩存储）
- 全文检索功能（MySQL 全文索引）
- 响应式布局（支持移动端）
</domain>

<decisions>
## Implementation Decisions

### 知识库架构
- **D-01:** 知识库采用树形分类 + 知识表单结构（一对多）
- **D-02:** 每个知识表单可以有多个 tag（多对多关系）

### 富文本编辑器
- **D-03:** 使用 Tiptap 作为富文本编辑器
- **D-04:** 支持 Markdown 语法
- **D-05:** 支持图片和文件上传

### 文件存储
- **D-06:** 文件使用 zstd 压缩后存储到数据库
- **D-07:** 通用文件管理模块，支持预览/下载/删除

### 分类树交互
- **D-08:** 左侧树形结构（可折叠），右侧知识列表
- **D-09:** 点击知识项在当前页签打开内容
- **D-10:** 提供全屏编辑模式（弹窗）

### 检索功能
- **D-11:** 使用 MySQL 全文索引实现知识检索
- **D-12:** 支持按标题、内容、标签搜索

### 响应式布局
- **D-13:** 支持移动端适配

### OpenCode's Discretion
- 树形组件具体实现（可折叠细节）
- 分页大小、排序规则
- Tiptap 具体插件配置
- tag 选择组件实现

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 项目配置
- `./AGENTS.md` — 项目规范和代码风格指南
- `.planning/REQUIREMENTS.md` — 完整需求列表（BIZ-01~05, UI-06）

### Phase 1 & 2 上下文
- `.planning/phases/01-foundation/01-foundation-CONTEXT.md` — Element Plus UI 框架、JWT 认证
- `.planning/phases/02-user-permissions/02-CONTEXT.md` — 用户字段、页面级权限

### 技术参考
- Tiptap 官方文档: https://tiptap.dev/
- Vue 3 Tiptap 集成指南

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- `app-vue/src/views/business/BusinessData.vue` — 现有占位页面，需完全重写
- `app-vue/src/components/AppMenu.vue` — 左侧菜单组件
- `springboot/src/entity/` — 实体目录，User/Menu/Role 实体已存在

### Established Patterns
- Element Plus UI 组件
- JWT 认证
- RESTful API 设计
- MyBatis-Plus CRUD
- Flyway 数据库迁移

### Integration Points
- 菜单系统（AppMenu.vue）需集成知识库入口
- 现有路由 `/dashboard/business` 指向 BusinessData.vue
- 审计日志（已实现）需记录知识库操作

</code_context>

<specifics>
## Specific Ideas

- 分类树可折叠
- 知识列表点击覆盖当前页签内容
- 全屏弹窗编辑知识
- 图片/文件使用 zstd 压缩存储到数据库
</specifics>

<deferred>
## Deferred Ideas

None — expanded Phase 3 scope covers all mentioned features

</deferred>

---

*Phase: 03-business-data*
*Context gathered: 2026-04-02*
