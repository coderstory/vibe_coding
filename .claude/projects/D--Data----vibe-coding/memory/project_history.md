---
name: project_history
description: 项目完整演变历史，从 v1.0 到 v1.5 的所有里程碑
type: project
originSessionId: a022f3ae-f37d-4f8a-ab92-32c0d326b4f6
---
## 项目概览

Vue 3 + Spring Boot 管理后台系统，夏日海滩风主题。单一开发者（coderstory），全部 424 次提交由 AI 辅助完成。

**当前状态**: v1.5 全部完成（2026-05-07），等待后续里程碑

## 里程碑历史

### v1.0 — 基础框架搭建（2026-04-02）
- Vue 3 + Vite 初始化，Element Plus 集成
- 登录页面、布局框架（侧边栏、顶栏、标签页）
- 夏日海滩风主题基础样式
- 用户管理、角色管理、审计日志页面
- Spring Boot 后端基础

**Why**: 从零搭建企业级管理后台

### v1.1 — 夏日海滩风主题修复（2026-04-03）
- 动态海浪/气泡动画效果
- 修复弹窗（dialog）显示问题
- 完善 Element Plus 组件样式
- 配色从阳光金调整为琥珀色

**Why**: 用户反馈视觉问题需要修复

### v1.2 — 用户管理模块（2026-04-18）
- 用户列表页（筛选 + 分页 + 状态切换）
- 用户详情页
- 用户 CRUD API

**Why**: 独立用户管理需求

### v1.3 — RocketMQ 管理功能（2026-04-28~29）
- **Phase 9**: Topic 管理（列表/创建/删除）
- **Phase 10**: Consumer Group 管理（消费进度/位点重置）
- **Phase 11**: 消息管理（查询/详情/轨迹追踪/发送）
- **Phase 12**: 监控面板（集群概览/Broker 状态/堆积量/TPS）

**关键经验**: RocketMQ 5.x API 包路径频繁变更，需关注 `remoting.protocol.body/route` 类路径

### v1.4 — Maven→Gradle + Spring Boot 4.1 升级（2026-04-29~05-06）
- **Phase 13**: Gradle 项目结构 + Wrapper 配置
- **Phase 14**: 依赖升级（Spring Boot 4.0.5 → 4.1.0-RC1）
- **Phase 15**: 配置迁移 + 修复硬编码凭证
- **Phase 16**: 构建验证 + 回归测试

**关键事件**:
1. Gradle 9.4 URL 404 → 升级到 9.5
2. Kotlin DSL 编译错误 → libs.versions.toml 配置修复
3. 中文路径 ClassNotFoundException → `file.encoding=GBK`
4. localhost DNS 解析失败 → 全局替换为 127.0.0.1
5. 多次 context 耗尽后继续会话

### v1.5 — 前后端代码重构与目录整理（2026-05-06~07）
- **Phase 17**: 代码质量工具链（EditorConfig/ESLint 10.x/Stylelint/Checkstyle/ArchUnit/PMD/SpotBugs/JaCoCo/Error Prone）
- **Phase 18**: 后端包结构重组（71 个文件从平面结构迁移到 shared + 10 个业务域包）
- **Phase 19**: 配置文件拆分（application.yaml → datasource/cache/mq/security/business）
- **Phase 20**: 前端目录重组（components/api/router 按域拆分）
- **Phase 21**: 代码规范统一（lint 清理、Checkstyle 治理、命名规范对齐）

**Why**: 消除技术债务，建立代码质量安全网，为后续开发奠定基础

## 滞留项

- 敏感信息环境变量加固（JWT secret、DB 密码）— 从 v1.4 延迟

## 技术演进

| 方面 | v1.3 时期 | v1.5 之后 |
|------|-----------|-----------|
| 构建工具 | Maven | Gradle 9.5 Kotlin DSL |
| Spring Boot | 4.0.5 | 4.1.0-RC1 |
| Java | 21 | 26 |
| 后端包结构 | 平面结构 | 10 业务域 + shared |
| 配置文件 | 单一 application.yaml | 5 个关注点拆分 |
| 前端目录 | 集中式 | 按域拆分 |
| 代码质量 | 无自动化 | ArchUnit/Checkstyle/PMD/ESLint/Stylelint |
