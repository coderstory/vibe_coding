# Phase 2: 用户与权限管理 - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

管理员可以完整管理用户、角色和查看审计日志。包括用户 CRUD、角色 CRUD、权限分配、审计日志查询。
</domain>

<decisions>
## Implementation Decisions

### 用户管理字段
- **D-01:** 用户表字段：用户名、密码、姓名、性别、头像、创建日期、是否启用、邮箱、部门、岗位
- **D-02:** 手机号字段不在用户表中

### 权限模型
- **D-03:** 页面级权限（不是按钮级权限）
- **D-04:** 用户分配角色，角色分配菜单访问权限

### 角色权限分配
- **D-05:** 树形菜单结构，勾选分配权限

### 密码重置
- **D-06:** 管理员直接输入新密码（不是随机生成或默认密码）

### 审计日志字段
- **D-07:** 核心4字段：操作人、时间、操作类型、目标
- **D-08:** 审计日志查询支持：时间范围 + 操作人 + 操作类型

### OpenCode's Discretion
- 列表分页大小、排序规则等细节由 OpenCode 决定
- 前端表格组件具体实现由 OpenCode 决定（遵循 Element Plus 规范）

### Folded Todos
无

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

No external specs — requirements fully captured in decisions above.

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- `app-vue/src/views/AuditLog.vue` — 审计日志页面框架已存在
- `app-vue/src/components/AppMenu.vue` — 菜单组件，权限控制需集成
- `springboot/src/entity/` — 实体目录，User 实体已存在
- `springboot/src/mapper/` — MyBatis-Plus Mapper

### Established Patterns
- JWT 认证（Phase 1 决策）
- Element Plus UI 组件（Phase 1 决策）
- RESTful API 设计
- MyBatis-Plus CRUD

### Integration Points
- 菜单权限控制需在 `AppMenu.vue` 集成
- 用户登录后需加载用户角色和菜单权限
- 审计日志拦截器需在 Service 层统一处理

</codebase_context>

<specifics>
## Specific Ideas

无特定参考要求 — 遵循 Element Plus 标准实现
</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope
</deferred>

---

*Phase: 02-user-permissions*
*Context gathered: 2026-04-02*
