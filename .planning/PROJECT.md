# 后台管理系统

## What This Is

一个面向企业内部使用的后台管理系统，提供用户认证、数据审计、业务数据管理等功能。前端采用 Vue 3 构建，后端采用 Spring Boot + MySQL 技术栈。

## Core Value

为运营人员提供一个安全、高效、易用的后台管理平台，能够安全地管理用户、审计数据、操作业务数据。

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] 用户认证：安全的登录/登出功能
- [ ] 用户管理：用户的增删改查、角色分配
- [ ] 数据审计：记录关键操作日志、查询审计数据
- [ ] 业务数据管理：业务数据的增删改查、导入导出
- [ ] 精美的 UI：专业的登录页面、管理后台主界面
- [ ] 管理界面：左侧菜单导航、页签式多任务界面

### Out of Scope

- 移动端适配 — Web 后台管理系统优先
- 微信/钉钉等第三方登录 — 账户密码登录满足需求
- 多租户隔离 — 单租户架构
- 数据可视化/报表 — 基础管理功能优先

## Context

**技术栈背景：**
- 前端：`app-vue/` — Vue 3.5 + Vite 8 空项目
- 后端：`springboot/` — Spring Boot 4.0.5 + MySQL 框架
- 项目组织：`cn.coderstory`

**已具备的基础：**
- Vue 3 项目结构已初始化
- Spring Boot 项目结构已初始化
- Vite 配置已完成（@ 别名）
- Maven Wrapper 已配置

**用户画像：**
- 目标用户：企业内部运营人员、行政管理人员
- 技术水平：非技术人员为主，需要简洁易懂的界面
- 使用场景：日常数据管理、用户管理、审计查询

## Constraints

- **技术栈**: Vue 3 + Spring Boot 4.0.5 — 现有架构约束
- **数据库**: MySQL — 需适配 MySQL 语法
- **安全**: 用户密码加密存储、会话管理、CORS 配置
- **兼容性**: 现代浏览器（Chrome、Firefox、Edge 最新版本）

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 前端 UI 框架待选 | 需要精美的 UI 但未确定具体框架 | — Pending |
| 认证机制待定 | Session 还是 JWT | — Pending |
| 菜单结构待定 | 需要根据业务需求设计 | — Pending |

---

*Last updated: 2026-04-02 after initialization*

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state
