# Roadmap: 后台管理系统

## Overview

从零构建一个企业级后台管理系统。首先建立认证基础设施和基础 UI 框架，然后逐步添加用户权限管理和业务数据管理功能。整体采用前后端分离架构，前端 Vue 3 + Element Plus，后端 Spring Boot + MySQL。

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work

- [ ] **Phase 1: 认证与基础框架** - 用户登录登出 + 管理后台基础布局
- [ ] **Phase 2: 用户与权限管理** - 用户 CRUD、角色管理、审计日志查询
- [ ] **Phase 3: 业务数据管理** - 业务数据 CRUD + 响应式布局`n- [ ] **Phase 4: UI 增强与主题系统** - 动态效果、液态玻璃、主题切换

## Phase Details

### Phase 1: 认证与基础框架
**Goal**: 用户可以安全登录系统并使用基础管理界面
**Depends on**: Nothing (first phase)
**Requirements**: AUTH-01, AUTH-02, AUTH-03, AUTH-04, MENU-01, MENU-03, MENU-04, MENU-05, AUDIT-04, UI-01, UI-02
**Success Criteria** (what must be TRUE):
  1. 用户可以使用用户名和密码成功登录系统
  2. 用户登录后在有效期内无需重复登录
  3. 用户可以安全登出系统
  4. 未登录用户访问受保护页面时自动跳转登录页
  5. 系统左侧显示菜单导航，支持多级展开
  6. 页面支持页签式多任务切换
  7. 顶部显示当前登录用户信息和登出按钮
  8. 登录页面设计精美，符合企业级审美
  9. 管理后台主界面布局合理（左侧菜单 + 顶部用户栏 + 内容区）
  10. 审计日志记录操作用户、操作时间、操作类型、目标对象、IP地址
**Plans**: 2 plans
- [x] 01-foundation-01-PLAN.md — 后端认证 API 和数据库层
- [x] 01-foundation-02-PLAN.md — 前端认证 UI 和管理后台布局

### Phase 2: 用户与权限管理
**Goal**: 管理员可以完整管理用户、角色和查看审计日志
**Depends on**: Phase 1
**Requirements**: USER-01, USER-02, USER-03, USER-04, USER-05, ROLE-01, ROLE-02, ROLE-03, ROLE-04, MENU-02, AUDIT-01, AUDIT-02, AUDIT-03, UI-03, UI-04, UI-05
**Success Criteria** (what must be TRUE):
  1. 管理员可以查看用户列表（分页、搜索）
  2. 管理员可以新增用户（用户名、密码、姓名、手机号、角色）
  3. 管理员可以编辑用户信息
  4. 管理员可以删除用户（逻辑删除）
  5. 管理员可以重置用户密码
  6. 管理员可以查看角色列表
  7. 管理员可以新增角色（角色名、描述、权限）
  8. 管理员可以编辑角色
  9. 管理员可以删除角色
  10. 用户只能看到有权限访问的菜单项
  11. 系统自动记录用户登录/登出事件
  12. 系统自动记录关键数据操作（新增/编辑/删除）
  13. 管理员可以查询审计日志（按用户、操作类型、时间范围）
  14. 列表页面统一使用表格组件，支持分页
  15. 表单页面统一使用 Element Plus 表单组件
  16. 操作反馈使用 Message 消息提示
**Plans**: 3 plans
- [x] 02-01-PLAN.md — 角色与菜单管理后端基础设施
- [x] 02-02-PLAN.md — 用户管理API + 审计日志功能
- [x] 02-03-PLAN.md — 角色管理前端 + 动态菜单权限

### Phase 3: 业务数据管理
**Goal**: 用户可以完整管理业务数据
**Depends on**: Phase 2
**Requirements**: BIZ-01, BIZ-02, BIZ-03, BIZ-04, BIZ-05, UI-06
**Success Criteria** (what must be TRUE):
  1. 用户可以查看业务数据列表（分页、排序、搜索）
  2. 用户可以新增业务数据（表单录入）
  3. 用户可以编辑业务数据
  4. 用户可以删除业务数据
  5. 用户可以查看业务数据详情
  6. 响应式布局，适配不同屏幕尺寸
**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. 认证与基础框架 | 2/2 | Planned | - |
| 2. 用户与权限管理 | 0/3 | Planned | - |
| 3. 业务数据管理 | 0/? | Not started | - |
