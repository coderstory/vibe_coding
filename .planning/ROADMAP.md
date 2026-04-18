# Ocean Breeze Admin - 路线图

> **创建日期:** 2026-04-03
> **更新日期:** 2026-04-18
> **当前里程碑:** v1.2 用户管理模块
> **目标:** 完成用户管理模块的完整前后端功能

---

## Phases

- [x] **Phase 5: 后端API基础设施** - 实现用户管理RESTful API
- [x] **Phase 6: 用户列表与状态功能** - 用户列表页、筛选、分页、状态切换
- [ ] **Phase 7: 用户详情页** - 用户详情展示、角色信息
- [ ] **Phase 8: 用户增删改表单** - 新增/编辑/删除用户、表单验证

---

## Phase Details

### Phase 5: 后端API基础设施

**Goal:** 用户管理模块的RESTful API后端实现

**Depends on:** 无

**Requirements:** API-01, API-02, API-03, API-04, API-05, API-06

**Success Criteria** (what must be TRUE):
1. 前端能通过GET /api/users获取分页用户列表（支持筛选参数）
2. 前端能通过GET /api/users/{id}获取单个用户详情
3. 前端能通过POST /api/users创建新用户
4. 前端能通过PUT /api/users/{id}更新用户信息
5. 前端能通过DELETE /api/users/{id}删除用户
6. 前端能通过PATCH /api/users/{id}/status切换用户状态

**Plans:** 1 plan
- [x] 05-01-PLAN.md — 用户管理RESTful API实现

---

### Phase 6: 用户列表与状态功能

**Goal:** 用户列表页面完整功能，包括筛选、分页、状态切换

**Depends on:** Phase 5

**Requirements:** LIST-01, LIST-02, LIST-03, LIST-04, LIST-05, LIST-06, STATUS-03

**Success Criteria** (what must be TRUE):
1. 用户可以在表格中看到所有用户的用户名、手机、状态、创建时间
2. 用户可以输入用户名进行模糊搜索
3. 用户可以输入手机号精确筛选
4. 用户可以按下拉筛选启用/禁用状态
5. 用户可以切换每页显示条数并翻页
6. 用户点击表格行能导航到用户详情页
7. 用户切换用户状态后能看到明确的成功/失败反馈

**Plans:** 1 plan
- [x] 06-01-PLAN.md — 用户列表页面增强

**UI hint:** yes

---

### Phase 7: 用户详情页

**Goal:** 用户详情页面完整展示

**Depends on:** Phase 5

**Requirements:** DETAIL-01, DETAIL-02, DETAIL-03, DETAIL-04

**Success Criteria** (what must be TRUE):
1. 用户访问 /users/:id 能看到详情页面
2. 用户能看到用户名、手机号、邮箱、状态、创建时间
3. 用户能看到该用户的角色信息列表
4. 用户能点击返回按钮回到用户列表页

**Plans:** TBD

**UI hint:** yes

---

### Phase 8: 用户增删改表单

**Goal:** 用户创建、编辑、删除功能完整可用

**Depends on:** Phase 5, Phase 6, Phase 7

**Requirements:** CRUD-01, CRUD-02, CRUD-03, CRUD-04, STATUS-01, STATUS-02

**Success Criteria** (what must be TRUE):
1. 用户可以填写表单创建新用户（用户名必填、手机号格式正确、邮箱格式正确）
2. 用户可以编辑已有用户信息，表单预填充现有数据
3. 用户删除用户前会看到确认提示
4. 用户可以启用被禁用的用户
5. 用户可以禁用已启用的用户
6. 表单验证失败时显示清晰的错误提示

**Plans:** TBD

**UI hint:** yes

---

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 5. Backend API | 1/1 | Completed | 2026-04-18 |
| 6. User List | 1/1 | Completed | 2026-04-18 |
| 7. User Detail | 0/1 | Not started | - |
| 8. User CRUD | 0/1 | Not started | - |

---

## Coverage Map

```
API-01 → Phase 5
API-02 → Phase 5
API-03 → Phase 5
API-04 → Phase 5
API-05 → Phase 5
API-06 → Phase 5
LIST-01 → Phase 6
LIST-02 → Phase 6
LIST-03 → Phase 6
LIST-04 → Phase 6
LIST-05 → Phase 6
LIST-06 → Phase 6
STATUS-03 → Phase 6
DETAIL-01 → Phase 7
DETAIL-02 → Phase 7
DETAIL-03 → Phase 7
DETAIL-04 → Phase 7
CRUD-01 → Phase 8
CRUD-02 → Phase 8
CRUD-03 → Phase 8
CRUD-04 → Phase 8
STATUS-01 → Phase 8
STATUS-02 → Phase 8

Mapped: 23/23 ✓
```

---

## v1.2 执行顺序

```
Phase 5 (API) → Phase 6 (List) ─┬─→ Phase 7 (Detail)
                                 │          ↓
                                 └────────→ Phase 8 (CRUD)
```

**说明:**
- Phase 5 独立，是后续所有阶段的基础
- Phase 6、7、8 可并行开发（都依赖 Phase 5）
- Phase 8 依赖 Phase 6 和 Phase 7（需要列表和详情的上下文）

---

*路线图创建完成: 2026-04-18*
*等待用户批准后开始执行*