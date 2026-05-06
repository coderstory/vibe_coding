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
- ✓ Topic 管理（列表、详情、创建、删除）— v1.3
- ✓ Consumer Group 管理（列表、详情、位点重置）— v1.3
- ✓ 消息管理（查询、详情、轨迹追踪）— v1.3
- ✓ 监控面板（集群概览、Broker状态、堆积量）— v1.3

- ✓ Maven → Gradle 完整迁移（Gradle 9.5 + JDK 26）— v1.4
- ✓ Spring Boot 4.0.5 → 4.1.0-RC1 升级 — v1.4
- ✓ 所有依赖升级到最新兼容版本 — v1.4
- ✓ 构建脚本优化（Kotlin DSL）— v1.4
- ✓ 修复 Gradle 测试中文路径 ClassNotFoundException — v1.4
- ✓ 修复 pom.xml 中硬编码的数据库凭证 — v1.4

## Current Milestone: v1.6 代码深度清理与优化

**Goal:** 全面清理前后端代码，删除无用方法、无用依赖、无用组件，合并拆分代码类，优化代码结构

**Target features:**
- 后端 Java 代码清理（无用方法/类删除、类合并拆分、代码精简）
- 后端依赖清理（pom/gradle 无用依赖移除）
- 前端 Vue/TS 代码清理（冗余组件删除、API 层瘦身）
- 前端依赖清理（npm 无用包移除）
- 配置文件与环境变量清理

### Previous Milestones

**v1.5 前后端代码重构与目录整理 — ✅ 已完成 (2026-05-07)**

**Delivered:**
- 代码质量工具链（ESLint 10.x/Stylelint/Checkstyle/ArchUnit/PMD/SpotBugs/JaCoCo）
- 后端 10 个业务域分包 + shared 通用层
- 配置文件拆分为 5 个关注点（datasource/cache/mq/security/business）
- 前端 components/api/router 按域拆分
- 代码规范统一（命名/PascalCase/import 清理）

**v1.4 Maven→Gradle + Spring Boot 4.1 升级 — ✅ 已完成 (2026-05-06)**

**Delivered:**
- Maven → Gradle 完整迁移（Gradle 9.5 + JDK 26）
- Spring Boot 4.0.5 → 4.1.0-RC1 升级
- 所有依赖升级到最新兼容版本
- 修复 Gradle 测试中文路径 ClassNotFoundException（file.encoding=GBK）
- 修复 pom.xml 中硬编码的数据库凭证
- 保持现有功能完全正常工作

### Out of Scope

- 深色模式切换（已移除）
- 移动端响应式布局
- 暗色主题适配

---

## Constraints

- **Tech Stack**: Vue 3 + Element Plus — 不更换技术栈
- **主题风格**: 夏日海滩风 — 保持海洋蓝主调
- **兼容性**: 桌面端优先 — 移动端暂不考虑
- **Java 版本**: JDK 26 — Spring Boot 4.1 要求 17+，秒杀等功能需要 21+
- **Spring Boot**: 4.1.0-RC1 — RC 版本用于评估，正式环境需等正式版

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 使用 CSS 变量实现主题 | 便于统一管理和动态切换 | ✓ Good |
| 移除暗色模式 | 用户反馈不需要，简化复杂度 | ✓ Good |
| 海洋蓝渐变侧边栏 | 形成视觉焦点，区分导航区 | ✓ Good |
| 沙滩色悬停效果 | 呼应海滩主题，增强交互反馈 | ✓ Good |
| Gradle 9.5 + JDK 26 | Gradle 9.5 原生支持 JDK 26，Spring Boot 4.1 兼容 | 2026-04-29 |
| Spring Boot 4.1.0-RC1 | 最新 RC 版本，依赖升级目标 | 2026-04-29 |
| Spring Boot Gradle Plugin 4.1.0-RC1 | 与 Spring Boot 4.1.0-RC1 版本对齐 | 2026-04-29 |
| Kotlin DSL | 类型安全，IDE 支持好 | 2026-04-29 |
| 依赖全面升级 | MyBatis-Plus/Flyway/RocketMQ/Redisson 等升级到最新兼容版 | 2026-04-29 |
| -Dfile.encoding=GBK | 修复中文路径下 Gradle 测试 ClassNotFoundException | 2026-05-06 |

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
*Last updated: 2026-05-07 after v1.5 completed, v1.6 initialized*
