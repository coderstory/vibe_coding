# Requirements: Vue + Spring Boot 管理后台

**Defined:** 2026-04-29
**Core Value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## v1 Requirements

### 迁移准备 (Migration Prep)

- [ ] **MIGR-01**: 分析现有 pom.xml 依赖和插件，生成完整依赖清单
- [ ] **MIGR-02**: 创建 Gradle Wrapper 配置（Gradle 9.4）
- [ ] **MIGR-03**: 创建初始 build.gradle 文件，等效翻译 pom.xml 依赖
- [ ] **MIGR-04**: 配置 Java Toolchain (JDK 26)
- [ ] **MIGR-05**: 配置 annotationProcessor (Lombok + spring-boot-configuration-processor)

### Spring Boot 升级 (Boot Upgrade)

- [ ] **BOOT-01**: 升级 Spring Boot 4.0.5 → 4.1.0-RC1 (spring-boot-starter-parent)
- [ ] **BOOT-02**: 升级 Spring Boot Gradle Plugin → 4.1.0-RC1
- [ ] **BOOT-03**: 升级 Spring AOP 版本覆盖（3.5.13 → 兼容 4.1 版本）
- [ ] **BOOT-04**: 验证 Spring Boot 4.1 兼容性测试通过

### 依赖升级 (Dependency Upgrades)

- [ ] **DEPS-01**: 升级 Flyway 4.0.6 → 12.x (12.4.0)
- [ ] **DEPS-02**: 升级 Redisson 4.3.1 → 4.2.0
- [ ] **DEPS-03**: 升级 MySQL Connector 9.6.0 → 9.7.0
- [ ] **DEPS-04**: MyBatis-Plus 3.5.16 + mybatis-plus-spring-boot4-starter
- [ ] **DEPS-05**: 验证 RocketMQ 2.3.5 与 Spring Boot 4.1 兼容性
- [ ] **DEPS-06**: 验证 JJWT 0.13.0 与 Spring Boot 4.1 兼容性
- [ ] **DEPS-07**: 验证 ZSTD 1.5.7-7 与 Spring Boot 4.1 兼容性

### 配置迁移 (Config Migration)

- [ ] **CONF-01**: 迁移 maven-compiler-plugin --enable-preview 到 Gradle options
- [ ] **CONF-02**: 迁移 spring-boot-maven-plugin 的 Lombok exclude 配置
- [ ] **CONF-03**: 迁移 Flyway Maven Plugin 配置到 Gradle Flyway 插件
- [ ] **CONF-04**: 修复硬编码数据库凭证（pom.xml 中的 root/123456）
- [ ] **CONF-05**: 检查 ReactorClientHttpRequestFactoryBuilder 默认值变更影响
- [ ] **CONF-06**: 检查 management.httpexchanges.recording.include 默认值变更

### 构建验证 (Build Verification)

- [ ] **BUILD-01**: gradlew bootRun 成功启动应用
- [ ] **BUILD-02**: gradlew build 成功打包（跳过测试）
- [ ] **BUILD-03**: gradlew test 测试通过
- [ ] **BUILD-04**: 验证 Flyway 数据库迁移正常工作
- [ ] **BUILD-05**: 验证 Redis 连接正常工作
- [ ] **BUILD-06**: 验证 RocketMQ 生产者/消费者正常工作
- [ ] **BUILD-07**: 验证 JWT 认证流程正常工作

### 功能回归 (Regression Testing)

- [ ] **REGR-01**: 用户登录/登出功能正常
- [ ] **REGR-02**: 用户管理 CRUD 功能正常
- [ ] **REGR-03**: 角色管理功能正常
- [ ] **REGR-04**: 菜单管理功能正常
- [ ] **REGR-05**: 审计日志功能正常
- [ ] **REGR-06**: 知识库功能正常
- [ ] **REGR-07**: 秒杀系统功能正常

## v2 Requirements

暂未定义，待 v1.4 完成后根据需要添加。

## Out of Scope

| Feature | Reason |
|---------|--------|
| 前端技术栈变更 | 保持 Vue 3 + Vite 不变 |
| 移动端响应式 | 桌面端优先 |
| Spring Boot 4.1 正式版发布前生产使用 | RC 版本仅用于评估 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| MIGR-01 | Phase 1 | Pending |
| MIGR-02 | Phase 1 | Pending |
| MIGR-03 | Phase 1 | Pending |
| MIGR-04 | Phase 1 | Pending |
| MIGR-05 | Phase 1 | Pending |
| BOOT-01 | Phase 2 | Pending |
| BOOT-02 | Phase 2 | Pending |
| BOOT-03 | Phase 2 | Pending |
| BOOT-04 | Phase 2 | Pending |
| DEPS-01 | Phase 2 | Pending |
| DEPS-02 | Phase 2 | Pending |
| DEPS-03 | Phase 2 | Pending |
| DEPS-04 | Phase 2 | Pending |
| DEPS-05 | Phase 2 | Pending |
| DEPS-06 | Phase 2 | Pending |
| DEPS-07 | Phase 2 | Pending |
| CONF-01 | Phase 3 | Pending |
| CONF-02 | Phase 3 | Pending |
| CONF-03 | Phase 3 | Pending |
| CONF-04 | Phase 3 | Pending |
| CONF-05 | Phase 3 | Pending |
| CONF-06 | Phase 3 | Pending |
| BUILD-01 | Phase 4 | Pending |
| BUILD-02 | Phase 4 | Pending |
| BUILD-03 | Phase 4 | Pending |
| BUILD-04 | Phase 4 | Pending |
| BUILD-05 | Phase 4 | Pending |
| BUILD-06 | Phase 4 | Pending |
| BUILD-07 | Phase 4 | Pending |
| REGR-01 | Phase 4 | Pending |
| REGR-02 | Phase 4 | Pending |
| REGR-03 | Phase 4 | Pending |
| REGR-04 | Phase 4 | Pending |
| REGR-05 | Phase 4 | Pending |
| REGR-06 | Phase 4 | Pending |
| REGR-07 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 34 total
- Mapped to phases: 34
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-29*
*Last updated: 2026-04-29 after initial definition*
