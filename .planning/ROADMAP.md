# Ocean Breeze Admin - 路线图

> **创建日期:** 2026-04-03
> **更新日期:** 2026-04-30
> **当前里程碑:** v1.4 后端 Maven 到 Gradle 迁移 + Spring Boot 4.1 升级
> **目标:** 将后端从 Maven 迁移到 Gradle，同时升级 Spring Boot 到 4.1.0-RC1 并更新所有兼容依赖

---

## Phases

### ✅ v1.3 RocketMQ 管理功能 (2026-04-03 → 2026-04-29)

- [x] **Phase 9: Topic 管理** — completed 2026-04-20
- [x] **Phase 10: Consumer Group 管理** — completed 2026-04-28
- [x] **Phase 11: 消息管理** — completed 2026-04-29
- [x] **Phase 12: 监控面板** — completed 2026-04-29

### 🚧 v1.4 Maven→Gradle + Spring Boot 4.1 升级

| Phase | Name | Goal | Requirements | Success Criteria |
|-------|------|------|--------------|------------------|
| 13 | 迁移准备 | 创建 Gradle 项目结构，配置 Wrapper 和基础构建文件 | MIGR-01~05 | 1. Gradle Wrapper 生成成功<br>2. build.gradle 基础配置完成<br>3. Java Toolchain 配置为 JDK 26 |
| 14 | 依赖升级 | 升级 Spring Boot 到 4.1.0-RC1，所有依赖升级到最新兼容版本 | BOOT-01~04, DEPS-01~07 | 1. Spring Boot 版本显示为 4.1.0-RC1<br>2. 所有依赖版本已更新<br>3. Gradle 依赖解析无冲突 |
| 15 | 配置迁移 | 迁移 Maven 特殊配置到 Gradle，修复硬编码凭证 | CONF-01~06 | 1. --enable-preview 编译选项生效<br>2. Flyway 配置迁移完成<br>3. 硬编码数据库凭证已移除 |
| 16 | 构建验证与回归测试 | 验证构建成功，所有功能回归测试通过 | BUILD-01~07, REGR-01~07 | 1. bootRun 成功启动<br>2. 所有测试通过<br>3. 功能回归测试 100% 通过 |

---

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 9. Topic 管理 | 1/1 | Complete | 2026-04-20 |
| 10. Consumer Group | 1/1 | Complete | 2026-04-28 |
| 11. 消息管理 | 1/1 | Complete | 2026-04-29 |
| 12. 监控面板 | 1/1 | Complete | 2026-04-29 |
| 13. 迁移准备 | 0/5 | Pending | - |
| 14. 依赖升级 | 1/1 | In Planning | - |
| 15. 配置迁移 | 0/6 | Pending | - |
| 16. 构建验证 | 0/14 | Pending | - |

---

### Phase 14 Plans

- [ ] 14-01-PLAN.md — 升级 Spring Boot 4.1.0-RC1 及所有依赖

*路线图更新: 2026-04-30*
