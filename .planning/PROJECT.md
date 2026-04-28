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

## Current Milestone: v1.3 RocketMQ 管理功能

**Goal:** 在现有管理后台中集成 RocketMQ 管理和监控功能，支持开发和生产环境使用

**Target features:**
- Topic 管理：创建、删除、配置查看
- Consumer Group 管理：消费进度、状态查看
- 消息管理：查看消息内容
- 监控面板：连接数、延迟、堆积量等

### Active

<!-- 当前里程碑正在实现的需求 -->

## Current Milestone: v1.2 用户管理

**Goal:** 开发独立用户管理模块，包含用户列表页（筛选+分页）和用户详情页

**Target features:**
- 用户列表页：支持按用户名/手机号/状态筛选，分页展示
- 用户详情页：查看用户详细信息
- 用户 CRUD：新增、编辑、删除用户
- 用户状态管理：启用/禁用用户

## Current Milestone: v1.3 RocketMQ 管理功能

**Goal:** 在现有管理后台中集成 RocketMQ 管理和监控功能，支持开发和生产环境使用

**Target features:**
- Topic 管理：创建、删除、配置查看
- Consumer Group 管理：消费进度、状态查看
- 消息管理：查看消息内容
- 监控面板：连接数、延迟、堆积量等

### Out of Scope

- 深色模式切换（已移除）
- 移动端响应式布局
- 暗色主题适配

---

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
*Last updated: 2026-04-28 after milestone v1.3 started*
