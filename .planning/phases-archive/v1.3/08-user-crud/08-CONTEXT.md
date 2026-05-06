# Phase 8: 用户增删改表单 - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning

<domain>
## Phase Boundary

用户创建、编辑、删除功能完整可用，包括表单验证、密码修改逻辑、详情页编辑入口。

</domain>

<decisions>
## Implementation Decisions

### 密码修改逻辑
- **D-01:** 编辑用户时默认不显示密码字段，需要用户勾选「修改密码」后才显示密码输入框
- **D-02:** 勾选修改密码后，密码字段为必填；不勾选则密码字段不提交

### 编辑入口
- **D-03:** UserDetail.vue 详情页添加「编辑」按钮，点击后跳转到 UserManagement 的编辑对话框

### 表单验证增强 (CRUD-04)
- **D-04:** 用户名必填（已有）
- **D-05:** 手机号格式验证（11位数字，可选字段）
- **D-06:** 邮箱格式验证（标准邮箱格式，可选字段）

### OpenCode's Discretion
- 手机号正则表达式：`/^1[3-9]\d{9}$/`
- 邮箱正则表达式：`/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/`
- 编辑时密码留空则后端不更新密码

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Files
- `.planning/REQUIREMENTS.md` — v1 Requirements 定义
- `.planning/ROADMAP.md` — Phase 8 定义

### Code References
- `app-vue/src/views/system/UserManagement.vue` — 现有 CRUD 实现（需增强）
- `app-vue/src/views/system/UserDetail.vue` — 详情页（需添加编辑按钮）
- `app-vue/src/api/user.js` — 前端 API

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Element Plus 表单验证规则模式
- ElMessageBox.confirm 确认对话框
- el-dialog 弹窗组件

### Established Patterns
- 表单使用 `reactive()` 管理数据
- 表单验证使用 `rules` 对象
- 提交前调用 `validate()` 验证

### Integration Points
- 详情页编辑按钮 → 路由跳转或事件emit到列表页打开编辑对话框
- 编辑模式 → 显示/隐藏密码字段

</code_context>

<specifics>
## Specific Ideas

- 编辑用户时密码字段默认隐藏，勾选「修改密码」才显示
- 详情页添加编辑按钮，与「返回列表」按钮并列

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 08-user-crud*
*Context gathered: 2026-04-18*