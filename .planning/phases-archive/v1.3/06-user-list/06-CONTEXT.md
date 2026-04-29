# Phase 6: 用户列表与状态功能 - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning

<domain>
## Phase Boundary

用户列表页面完整功能，包括筛选、分页、状态切换。为 Phase 7/8 提供基础。

</domain>

<decisions>
## Implementation Decisions

### 筛选字段
- **D-01:** 搜索表单包含 username 模糊筛选（原有）
- **D-02:** 搜索表单包含 phone 精确筛选（新增）
- **D-03:** 搜索表单包含 department 筛选（原有）
- **D-04:** 搜索表单包含 enabled 状态筛选（原有）

### 状态切换
- **D-05:** 使用 el-switch 内联切换用户状态
- **D-06:** 状态切换后显示 ElMessage.success/failure 提示

### 分页
- **D-07:** 支持 page/size 参数，page-sizes 支持 [10, 20, 50, 100]

### 列表字段
- **D-08:** 表格显示：序号、用户名、姓名、性别、手机、部门、岗位、邮箱、状态、创建时间、操作列

### OpenCode's Discretion
- 表格列的精确宽度和顺序
- 搜索表单的布局和样式
- 状态 Tag 的颜色主题

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Frontend Code
- `app-vue/src/views/system/UserManagement.vue` — 现有用户管理页面实现

### Backend API
- `springboot/src/main/java/cn/coderstory/springboot/controller/UserController.java` — GET /api/users 支持 phone 筛选
- `springboot/src/main/java/cn/coderstory/springboot/service/UserService.java` — phone 参数

### Requirements
- `.planning/REQUIREMENTS.md` — LIST-01~06, STATUS-03 需求定义

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- UserManagement.vue: 已有完整CRUD页面，可扩展
- ElMessage: 已用于操作反馈
- el-table: 已用于数据展示
- el-switch: 已用于状态切换

### Established Patterns
- 搜索表单：el-form inline + el-input/el-select
- 表格：el-table stripe border + v-loading
- 分页：el-pagination background
- 对话框：el-dialog + el-form

### Integration Points
- loadUserList() 函数调用 GET /api/users
- 需扩展 API 调用支持 phone 参数

</codebase_context>

<specifics>
## Specific Ideas

- phone 筛选：精确匹配（LIKE 不太好，直接传参给后端）
- 状态切换：el-switch 放在状态列，inline 切换

</specifics>

<deferred>
## Deferred Ideas

- None — 讨论保持在阶段范围内

</deferred>

---

*Phase: 06-user-list*
*Context gathered: 2026-04-18*