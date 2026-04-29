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

## Current Milestone: v1.4 后端 Maven 到 Gradle 迁移 + Spring Boot 4.1.0-RC1 升级

**Goal:** 将后端从 Maven 迁移到 Gradle，同时升级 Spring Boot 到 4.1.0-RC1 并更新所有兼容依赖

**Target features:**
- Maven → Gradle 完整迁移（Gradle 9.4 + JDK 26）
- Spring Boot 4.0.5 → 4.1.0-RC1 升级
- 所有依赖升级到与 Spring Boot 4.1 兼容的最新版本
- 构建脚本优化（Groovy DSL）
- 保持现有功能完全正常工作
- 修复 pom.xml 中硬编码的数据库凭证

### Technical Notes (from research)

**版本兼容性矩阵：**

| 依赖 | 当前版本 | 升级目标 | 证据 |
|------|----------|----------|------|
| Spring Boot | 4.0.5 | 4.1.0-RC1 | [Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1.0-RC1-Release-Notes) |
| Gradle Plugin | - | 4.1.0-RC1 | [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.springframework.boot) |
| MyBatis-Plus | 3.5.16 | 3.5.16 + boot4-starter | [v3.5.16](https://github.com/baomidou/mybatis-plus/releases/tag/v3.5.16) |
| Flyway | 4.0.6 | 12.x (12.4.0) | [Issue #50079](https://github.com/spring-projects/spring-boot/issues/50079) |
| RocketMQ spring-starter | 2.3.5 | 2.3.5 | [v2.3.5](https://github.com/apache/rocketmq-spring/releases/tag/rocketmq-spring-all-2.3.5) |
| Redisson | 4.3.1 | 4.2.0 | [v4.2.0](https://github.com/redisson/redisson/releases/tag/redisson-4.2.0) |
| JJWT | 0.13.0 | 0.13.0 | [v0.13.0](https://github.com/jwtk/jjwt/releases/tag/0.13.0) |
| MySQL Connector | 9.6.0 | 9.7.0 | [v9.7.0](https://dev.mysql.com/doc/relnotes/connector-j/en/news-9-7-0.html) |
| ZSTD | 1.5.7-7 | 1.5.7-7 | [tags](https://github.com/luben/zstd-jni/tags) |

**Breaking Changes 注意事项：**
- `ReactorClientHttpRequestFactoryBuilder` 默认值变更（需检查 HTTP 客户端配置）
- `management.httpexchanges.recording.include` 默认值变更
- Java 最低要求 17+（部分特性需要 21+）

**特殊 Maven 配置需翻译：**
- `--enable-preview` compiler flag → `options.compilerArgs += "--enable-preview"`
- `annotationProcessorPaths` → Gradle `annotationProcessor` 配置
- Spring AOP 3.5.13 版本覆盖
- `spring-boot-maven-plugin` 的 Lombok exclude

### Previous Milestone

**v1.3 RocketMQ 管理功能 — ✅ 已完成 (2026-04-29)**

**Delivered:**
- Topic 管理：创建、删除、配置查看
- Consumer Group 管理：消费进度、状态查看、位点重置
- 消息管理：查看消息内容、轨迹追踪
- 监控面板：集群概览、Broker状态、Topic堆积量

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
| Gradle 9.4 + JDK 26 | Gradle 9.4 原生支持 JDK 26，Spring Boot 4.1 兼容 | 2026-04-29 |
| Spring Boot 4.1.0-RC1 | 最新 RC 版本，依赖升级目标 | 2026-04-29 |
| Spring Boot Gradle Plugin 4.1.0-RC1 | 与 Spring Boot 4.1.0-RC1 版本对齐 | 2026-04-29 |
| Groovy DSL | 简洁直观，迁移自 Maven POM | 2026-04-29 |
| 依赖全面升级 | MyBatis-Plus/Flyway/RocketMQ/Redisson 等升级到最新兼容版 | 2026-04-29 |

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
*Last updated: 2026-04-29 after v1.4 scope expanded (Maven→Gradle + SB 4.1.0-RC1 upgrade + dependency upgrades)*
