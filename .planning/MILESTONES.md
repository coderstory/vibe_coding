# Milestones

## v1.0 — 基础框架搭建

**Date:** 2026-04-02

**Goal:** 完成管理后台基础框架搭建

**Delivered:**
- Vue 3 + Vite 项目初始化
- Element Plus 组件库集成
- 登录页面
- 布局框架（侧边栏、顶栏、标签页）
- 夏日海滩风主题基础样式
- 用户管理、角色管理页面
- 审计日志页面
- Spring Boot 后端基础

---

## v1.1 — 夏日海滩风主题修复与完善

**Date:** 2026-04-03

**Goal:** 修复当前主题的显示问题，添加动态海浪效果，完善组件样式

**Target features:**
- UI-01: 添加动态海浪/气泡动画效果
- UI-02: 修复弹窗 (dialog) 显示问题
- UI-03: 完善所有 Element Plus 组件样式
- UI-04: 调整配色（阳光金→琥珀色）
- UI-05: 确保视觉风格统一

**Status:** Completed

---

## v1.2 — 用户管理模块

**Date:** 2026-04-18

**Goal:** 开发独立用户管理模块，包含用户列表页和用户详情页

**Delivered:**
- 用户列表页（筛选+分页+状态切换）
- 用户详情页
- 用户 CRUD API

**Status:** Completed

---

## v1.3 — RocketMQ 管理功能

**Date:** 2026-04-28

**Goal:** 在现有管理后台中集成 RocketMQ 管理和监控功能

**Target features:**
- Topic 管理：创建、删除、配置查看
- Consumer Group 管理：消费进度、状态查看
- 消息管理：查看消息内容
- 监控面板：连接数、延迟、堆积量等

**Status:** Completed

---

## v1.4 — Maven→Gradle + Spring Boot 4.1 升级

**Date:** 2026-04-29 → 2026-05-06

**Goal:** 将后端从 Maven 迁移到 Gradle，同时升级 Spring Boot 到 4.1.0-RC1 并更新所有兼容依赖

**Delivered:**
- Maven → Gradle 完整迁移（Gradle 9.5 + JDK 26）
- Spring Boot 4.0.5 → 4.1.0-RC1 升级
- 所有依赖升级到最新兼容版本
- 构建脚本优化（Kotlin DSL）
- 修复 Gradle 测试中文路径 ClassNotFoundException（file.encoding=GBK）
- 保持现有功能完全正常工作

**Status:** ✅ Completed

---

## v1.5 — 前后端代码重构与目录整理

**Date:** 2026-05-06 → 2026-05-07

**Goal:** 重构前后端代码结构，整理代码和配置文件目录，消除技术债务

**Delivered:**
- Phase 17: 工具链搭建 — EditorConfig/ESLint 10.x/Stylelint/Checkstyle/ArchUnit/PMD/SpotBugs/JaCoCo/Error Prone
- Phase 18: 后端包结构重组 — shared 通用层 + 10 个业务域包，71 个文件迁移
- Phase 19: 配置文件整理 — application.yaml 拆分为 datasource/cache/mq/security/business 5 个关注点文件
- Phase 20: 前端目录重组 — components/api/router 按域拆分
- Phase 21: 代码规范统一 — PascalCase/Page 后缀，interface vs type 收敛，lint 零 warning

**Status:** ✅ Completed

---

## v1.6 — 代码深度清理与优化

**Date:** 2026-05-07

**Goal:** 全面清理前后端死代码（未使用方法/字段/导入/组件/API），清理无用依赖和冗余配置，提升代码库整洁度

**Delivered:**
- 后端 Java 死代码清理：删除未用方法/字段/导入/注释块
- 后端结构体优化：DTO/VO 精简，RocketMQAdminService Facade 拆分，type-first 目录重构
- 前端代码清理：删除无用组件/API/类型/CSS，lint 零 warning
- 依赖清理：修正 Gradle AOP 依赖为 BOM 管理，npm 包作用域修正（@playwright/test→devDependencies），移除冗余 vue-test-utils
- 配置清理：移除 application-test.yaml 中与主配置重复的属性（mybatis-plus、seckill.limiter、seckill.order.timeout）

**Status:** ✅ Completed

---

*Last updated: 2026-05-07*
