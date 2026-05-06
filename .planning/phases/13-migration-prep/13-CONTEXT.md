# Phase 13: 迁移准备 - Context

**Gathered:** 2026-04-29
**Status:** Ready for planning

<domain>
## Phase Boundary

创建 Gradle 项目结构，配置 Gradle Wrapper 和基础构建文件，为 Spring Boot 4.1.0-RC1 迁移和依赖升级做准备。

</domain>

<decisions>
## Implementation Decisions

### DSL 选择
- **D-01:** 使用 **Kotlin DSL** (build.gradle.kts)
- 理由: 类型安全，IDE 支持更好

### 版本管理策略
- **D-02:** 使用 **Version Catalog** (libs.versions.toml)
- 理由: Gradle 最佳实践，集中管理版本，便于依赖升级

### Gradle Wrapper 生成
- **D-03:** 使用 `gradle wrapper --gradle-version 9.4` 命令生成
- 要求: 先安装 Gradle 9.4 或使用 sdkman

### 构建配置结构
- **D-04:** 目录结构:
  ```
  springboot/
  ├── build.gradle.kts          # 根构建脚本
  ├── settings.gradle.kts        # 项目设置
  ├── gradle.properties          # Gradle 属性
  ├── libs.versions.toml         # 版本目录
  ├── gradlew                   # Unix Wrapper 脚本
  ├── gradlew.bat               # Windows Wrapper 脚本
  └── gradle/wrapper/           # Wrapper 文件
  ```

### 关键配置项
- **D-05:** Java Toolchain 配置为 JDK 26
- **D-06:** annotationProcessor 配置 Lombok + spring-boot-configuration-processor

</decisions>

<specifics>
## Specific Ideas

- 从 pom.xml 翻译依赖时，保持 groupId/artifactId 结构
- 版本号从 pom.xml properties 移到 libs.versions.toml
- Spring Boot 插件版本: 4.1.0-RC1

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 构建迁移
- `springboot/pom.xml` — 当前 Maven 配置，包含所有依赖和插件
- `springboot/src/main/resources/application.yaml` — 运行时配置

### 研究文档
- Spring Boot 4.1.0-RC1 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1.0-RC1-Release-Notes
- Spring Boot Gradle Plugin 文档: https://docs.spring.io/spring-boot/gradle-plugin/index.html
- Gradle Kotlin DSL 文档: https://docs.gradle.org/current/userguide/kotlin_dsl.html
- Version Catalog 文档: https://docs.gradle.org/current/userguide/platforms.html

</canonical_refs>

<code_context>
## Existing Code Insights

### pom.xml 关键依赖 (来自分析)
- Spring Boot Parent: 4.0.5 (目标升级到 4.1.0-RC1)
- Java Version: 26
- MyBatis-Plus: 3.5.16
- Flyway: 4.0.6 (目标升级到 12.x)
- RocketMQ: 5.3.2 client, 2.3.5 spring-starter
- Redisson: 4.3.1
- JJWT: 0.13.0
- MySQL Connector: 9.6.0
- ZSTD: 1.5.7-7

### 特殊配置
- `--enable-preview` compiler flag
- annotationProcessorPaths: Lombok + spring-boot-configuration-processor
- spring-boot-maven-plugin excludes Lombok

</code_context>

<deferred>
## Deferred Ideas

- 删除 pom.xml 和 mvnw.cmd — 迁移完成后在 Phase 16 处理
- Gradle 缓存配置优化 — 可后续添加

</deferred>

---

*Phase: 13-migration-prep*
*Context gathered: 2026-04-29*
