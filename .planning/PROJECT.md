# Vue + Spring Boot 管理后台

## What This Is

Vue 3 + Element Plus 管理后台系统，前端使用夏日海滩风主题（海洋蓝 + 沙滩色 + 琥珀色），后端基于 Spring Boot。

## Core Value

提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## Requirements

### Validated

<!-- 已完成并验证的需求 -->

- ✓ Vue 3 + Vite 前端框架 — v1.0
- ✓ Element Plus 组件库集成 — v1.0
- ✓ 夏日海滩风主题基础样式 — v1.0
- ✓ 深色侧边栏（海洋蓝渐变）— v1.0
- ✓ 毛玻璃顶栏效果 — v1.0
- ✓ 菜单悬停/选中样式 — v1.0

### Active

<!-- 当前里程碑正在实现的需求 -->

- [ ] **UI-01**: 添加动态海浪/气泡动画效果
- [ ] **UI-02**: 修复弹窗 (dialog) 显示问题
- [ ] **UI-03**: 完善所有 Element Plus 组件样式（表格/表单/分页等）
- [ ] **UI-04**: 调整阳光金色为琥珀色 (#d97706)
- [ ] **UI-05**: 确保视觉风格统一、交互流畅

### Out of Scope

- 深色模式切换（已移除）
- 移动端响应式布局
- 暗色主题适配

## Context

**技术栈：**
- 前端：Vue 3.5 + Vite 8 + Element Plus 2.9 + Pinia
- 后端：Spring Boot 4.0.5 + Java 21
- 数据库：MySQL + Flyway 迁移

**已有页面：**
- 登录页 (Login)
- 布局框架 (Layout + 侧边栏 + 顶栏 + 标签页)
- 首页 (DashboardIndex)
- 系统管理 (UserManagement, RoleManage)
- 审计日志 (AuditLog)
- 业务数据 (BusinessData)

**主题配色（夏日海滩风）：**
- 深海蓝：`#1e3a8a`
- 中蓝：`#3b82f6`
- 浅蓝：`#93c5fd`
- 沙滩黄：`#fef3c7`
- 琥珀色：`#d97706`（替代原来的阳光金）
- 云朵白：`#f8fafc`

## Constraints

- **Tech Stack**: Vue 3 + Element Plus — 不更换技术栈
- **主题风格**: 夏日海滩风 — 保持海洋蓝主调
- **兼容性**: 桌面端优先 — 移动端暂不考虑

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 使用 CSS 变量实现主题 | 便于统一管理和动态切换 | ✓ Good |
| 移除暗色模式 | 用户反馈不需要，简化复杂度 | ✓ Good |
| 海洋蓝渐变侧边栏 | 形成视觉焦点，区分导航区 | ✓ Good |
| 沙滩色悬停效果 | 呼应海滩主题，增强交互反馈 | ✓ Good |

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

---
*Last updated: 2026-04-03 after milestone v1.1 started*
