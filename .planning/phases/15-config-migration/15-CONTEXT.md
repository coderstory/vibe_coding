# Phase 15: 配置迁移 - Context

**Gathered:** 2026-04-30
**Status:** Ready for planning
**Source:** Manual analysis from pom.xml and build.gradle.kts

<domain>
## Phase Boundary

迁移 Maven 特殊配置到 Gradle 配置，修复硬编码数据库凭证。

</domain>

<decisions>
## Implementation Decisions

### CONF-01: --enable-preview 编译选项
- **Source:** pom.xml maven-compiler-plugin `<compilerArgs>--enable-preview</compilerArgs>`
- **Target:** build.gradle.kts `tasks.withType<JavaCompile>().configureEach { options.compilerArgs.add("--enable-preview") }`
- **Status:** ✓ 已迁移 (build.gradle.kts:61-63)

### CONF-02: spring-boot-maven-plugin Lombok exclude
- **Source:** pom.xml spring-boot-maven-plugin exclude configuration
- **Target:** Gradle 不需要此配置，annotation processor 配置方式不同
- **Status:** ✓ N/A (Gradle 默认行为已正确)

### CONF-03: Flyway Maven Plugin 配置
- **Source:** pom.xml flyway-maven-plugin `<url>`, `<user>`, `<password>`
- **Target:** build.gradle.kts flyway { url, user, password, driver, locations, baselineOnMigrate }
- **Status:** ✓ 已迁移 (build.gradle.kts:73-79)

### CONF-04: 硬编码数据库凭证 [CRITICAL]
- **Hardcoded in:** application.yaml:53-54
  - `username: root`
  - `password: 123456`
- **Also hardcoded:** application.yaml:46 (URL 中的 localhost)
- **Target:** 使用环境变量 `DB_USER`, `DB_PASSWORD`, `DB_HOST`
- **Required fix:** 将硬编码值替换为 `${DB_USER:-root}` 格式

### CONF-05: ReactorClientHttpRequestFactoryBuilder 默认值
- **Context:** Spring Boot 4.x 可能更改了默认值
- **Action:** 检查 `spring.httpclient.reactor.*` 相关配置是否存在
- **Status:** 待验证

### CONF-06: management.httpexchanges.recording.include 默认值
- **Context:** Spring Boot 4.x 可能更改了默认值
- **Action:** 检查 `management.httpexchanges` 配置
- **Status:** 待验证

</decisions>

<canonical_refs>
## Canonical References

- `springboot/build.gradle.kts` — Gradle 构建配置
- `springboot/pom.xml` — Maven 配置（参考）
- `springboot/src/main/resources/application.yaml` — 应用配置
- `springboot/gradle/libs.versions.toml` — 版本目录

</canonical_refs>

<specifics>
## Specific Items to Migrate

1. **CONF-04 [CRITICAL]:** application.yaml 中的硬编码凭证
   - `username: root` → `username: ${DB_USER:-root}`
   - `password: 123456` → `password: ${DB_PASSWORD:-}`
   - URL localhost → `${DB_HOST:-localhost}`

2. **CONF-05:** ReactorClientHttpRequestFactoryBuilder
   - 检查 WebClient/RestTemplate 配置变更

3. **CONF-06:** management.httpexchanges
   - 检查 HTTP exchanges 记录配置

</specifics>

<deferred>
## Deferred Ideas

- Flyway Maven Plugin 的 url/user/password 可考虑移到 application.yaml 中统一管理
- 当前 build.gradle.kts 中的 flyway 块可能与 application.yaml 重复配置

</deferred>

---

*Phase: 15-config-migration*
*Context gathered: 2026-04-30*
