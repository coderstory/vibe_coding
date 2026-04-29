# Phase 5: 后端API基础设施 - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning

<domain>
## Phase Boundary

实现用户管理模块的 RESTful API 后端实现，为 Phase 6/7/8 提供数据接口。

</domain>

<decisions>
## Implementation Decisions

### 列表筛选参数
- **D-01:** GET /api/users 支持 username 模糊筛选（原有）
- **D-02:** GET /api/users 支持 phone 模糊筛选（新增）
- **D-03:** GET /api/users 支持 enabled 精确筛选（原有）
- **D-04:** GET /api/users 支持分页（page, size 参数）

### 状态更新
- **D-05:** 复用 PUT /api/users/{id} 更新 enabled 字段，不单独创建 status API
- **D-06:** PATCH /api/users/{id}/status 需求废弃

### 用户详情
- **D-07:** GET /api/users/{id} 只返回用户基本信息
- **D-08:** 角色信息不在此 API 返回（Phase 7 详情页自行关联查询）

### OpenCode's Discretion
- 分页默认参数（page=1, size=20）
- 列表排序规则（createTime 倒序）
- 错误响应格式

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Existing Backend Code
- `springboot/src/main/java/cn/coderstory/springboot/entity/User.java` — User 实体定义
- `springboot/src/main/java/cn/coderstory/springboot/controller/UserController.java` — 现有 API 实现
- `springboot/src/main/java/cn/coderstory/springboot/service/UserService.java` — Service 接口
- `springboot/src/main/java/cn/coderstory/springboot/service/impl/UserServiceImpl.java` — Service 实现

### Requirements
- `.planning/REQUIREMENTS.md` — USR-API 需求定义

</canonical_refs>

<codebase_context>
## Existing Code Insights

### Reusable Assets
- User 实体：已定义，包含 id/username/name/phone/email/enabled/createTime 等字段
- MyBatis Plus：已集成，IPage/Page/LambdaQueryWrapper 使用模式已建立
- PasswordEncoder：已存在，用于密码加密

### Established Patterns
- Controller 返回格式：`{code: 200, message: "success", data: {...}}`
- Service 实现：使用 MyBatis Plus 的 Mapper 层
- 分页模式：Page<User> + LambdaQueryWrapper

### Integration Points
- 新 API 需要复用到现有 UserController
- 筛选逻辑在 UserServiceImpl.getUserPage() 中扩展

</codebase_context>

<specifics>
## Specific Ideas

- phone 筛选：使用 `wrapper.like(User::getPhone, phone)` 而非精确匹配

</specifics>

<deferred>
## Deferred Ideas

- PATCH /api/users/{id}/status 独立 API — 用户选择复用 update API
- 角色信息返回 — 用户选择只在详情页自行关联

</deferred>

---

*Phase: 05-backend-api*
*Context gathered: 2026-04-18*