# Phase 14: 依赖升级 - Context

**Gathered:** 2026-04-30
**Status:** Ready for planning

<domain>
## Phase Boundary

升级 Spring Boot 到 4.1.0-RC1，所有依赖升级到最新兼容版本。保持项目功能不变，仅更新依赖版本。

</domain>

<decisions>
## Implementation Decisions

### Spring Boot 版本
- **D-01:** 升级 Spring Boot 从 4.0.5 → 4.1.0-RC1
- **D-02:** 更新 spring-boot-gradle-plugin 版本匹配

### 依赖版本策略
- **D-03:** 使用 Spring Boot 4.1 兼容的最新依赖版本
- **D-04:** Flyway 升级到 12.x（Spring Boot 4.1 要求）
- **D-05:** 保持 Java 26 toolchain

### 版本对照表

| 依赖 | 当前版本 | 目标版本 |
|------|---------|---------|
| spring-boot | 4.0.5 | 4.1.0-RC1 |
| spring-boot-starter-aop | 3.5.13 | 4.1 兼容版本 |
| flyway | 4.0.6 | 12.x |
| mybatis-plus | 3.5.16 | 最新兼容版本 |
| rocketmq-client | 5.3.2 | 5.3.x 最新 |
| rocketmq-spring-boot-starter | 2.3.5 | 2.4.x 最新 |
| redisson | 4.3.1 | 最新兼容版本 |
| jjwt | 0.13.0 | 0.13.x 最新 |
| lombok | 1.18.44 | 最新稳定版 |

</decisions>

<specifics>
## Specific Ideas

- Spring Boot 4.1 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1.0-RC1-Release-Notes
- 需要检查 breaking changes
- MyBatis Plus 需要确认 Spring Boot 4.1 兼容性
- RocketMQ 客户端版本需要与 Broker 版本匹配

</specifics>

<canonical_refs>
## Canonical References

### 构建配置
- `springboot/pom.xml` — 当前 Maven 配置（参考）
- `springboot/build.gradle.kts` — 当前 Gradle 配置
- `springboot/gradle/libs.versions.toml` — 版本目录

### Spring Boot 4.1 升级
- Spring Boot 4.1.0-RC1 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1.0-RC1-Release-Notes
- Spring Boot Gradle Plugin 文档: https://docs.spring.io/spring-boot/gradle-plugin/

</canonical_refs>

<codebase_context>
## Existing Code Insights

### 依赖结构
- Spring Boot Starter 依赖通过 spring-boot-dependencies BOM 管理
- Flyway 作为独立插件配置
- Redisson 通过 starter 集成
- JWT 使用 JJWT 0.13.x

### 特殊配置
- annotationProcessor 配置 Lombok + spring-boot-configuration-processor
- --enable-preview 编译选项
- Flyway 数据库迁移配置

</codebase_context>

<deferred>
## Deferred Ideas

- 依赖版本锁定策略（可选：使用精确版本 vs 范围版本）

</deferred>

---

*Phase: 14-dependency-upgrade*
*Context gathered: 2026-04-30*