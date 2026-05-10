# Technology Stack

**Analysis Date:** 2026-05-10

## Languages

**Primary:**
- **Java 26** (toolchain via `JavaLanguageVersion.of(26)`) - Backend Spring Boot application. Configured in `springboot/build.gradle.kts` line 23-29. Preview features enabled: `--enable-preview` in `JavaCompile` tasks.
- **TypeScript 6.0.3** (`^6.0.3`) - Frontend application. Configured in `app-vue/package.json`. Target ES2020, strict mode, `@` path alias to `./src`.

**Secondary:**
- **CSS** - Vue component styles, scoped via `<style scoped>`. CSS checked by Stylelint 17.x.
- **SQL** - MyBatis Plus Mapper XMLs in `springboot/src/main/resources/mapper/`. Flyway migration scripts in `springboot/src/main/resources/db/migration/`.
- **YAML** - Application configuration split across `application.yaml` + `config/*.yaml`.

## Runtime

**Environment:**
- **Node.js** `^20.19.0 || >=22.12.0` - Frontend dev/build. Enforced in `app-vue/package.json` `engines` field.
- **Java 26** - Backend JDK toolchain. Preview features enabled for development.

**Package Manager:**
- **npm** (lockfile: `app-vue/package-lock.json`) - Frontend dependency management.
- **Gradle 9.5** (Kotlin DSL, version catalog) - Backend build system. Lockfile via `springboot/gradle/libs.versions.toml` (version catalog, not a lockfile per se). Wrapper at `springboot/gradle/wrapper/`.

## Frameworks

**Core — Frontend:**
- **Vue 3** `^3.5.31` — Composition API with `<script setup lang="ts">`. Core framework.
  - Location: `app-vue/node_modules/vue/`
  - Usage: All `.vue` components in `app-vue/src/views/` and `app-vue/src/components/`
- **Vite 8** `^8.0.3` — Build tool and dev server.
  - Config: `app-vue/vite.config.js`
  - Dev server port: `5173`
  - Plugin: `@vitejs/plugin-vue` v6.0.5
  - Proxy: `/api` -> `http://localhost:8080`
- **Element Plus** `^2.9.0` — UI component library.
  - Icons: `@element-plus/icons-vue` v2.3.1
  - Usage: Global registration in `app-vue/src/main.ts`
- **Pinia** `^2.3.0` — State management.
  - Usage: Store files in `app-vue/src/store/`
- **Vue Router** `^4.5.0` — Client-side routing.
  - Config: `app-vue/src/router/` with modular route files
- **Axios** `^1.7.9` — HTTP client.
  - Config: `app-vue/src/api/request.ts`
  - Base URL: `/api`
- **ECharts** `^6.0.0` + **vue-echarts** `^8.0.1` — Charting library.
- **wangEditor** `@wangeditor/editor` v5.1.23 + `@wangeditor/editor-for-vue` v5.1.12 — Rich text editor for knowledge base.

**Core — Backend:**
- **Spring Boot 4.1.0-RC1** — Application framework.
  - Version managed via `spring-boot-dependencies` BOM in `springboot/gradle/libs.versions.toml`
  - Starters: `webmvc`, `security`, `data-redis`, `aop` (v4.0.0-M2), `flyway`, `test`
  - Virtual threads enabled: `spring.threads.virtual.enabled: true`
- **MyBatis Plus 3.5.16** — ORM framework.
  - Starter: `mybatis-plus-spring-boot4-starter`
  - Config: `config/business.yaml` (`mybatis-plus` section)
  - Mapper XML location: `classpath:mapper/*.xml`
  - LambdaQueryWrapper, auto-fill, logical delete
- **Flyway 12.5.0** — Database migration.
  - Config: `config/datasource.yaml` (`spring.flyway.*`)
  - Migration scripts: `classpath:db/migration/`
  - Naming: `V{version}__{description}.sql` (e.g., `V10__seckill_queue.sql`)

**Messaging:**
- **RocketMQ 5.5.0** client + **2.3.5** Spring Boot starter — Message queue.
  - Producer group: `seckill_producer_group`
  - NameServer: `127.0.0.1:9876`
  - Config: `config/mq.yaml`
  - Admin client: `DefaultMQAdminExt` in `RocketMQConfig.java`
  - Modules: `springboot/src/main/java/cn/coderstory/springboot/mq/seckill/` (producer + consumer)

**Security:**
- **jjwt 0.13.0** — JWT token handling.
  - Libraries: `jjwt-api` (implementation), `jjwt-impl` (runtime), `jjwt-jackson` (runtime)
  - HMAC-SHA signing via `io.jsonwebtoken.security.Keys`
  - Config: `config/security.yaml` (`jwt.*`)

**Caching / Distributed Lock:**
- **Redisson 4.3.1** — Redis client with distributed lock support.
  - Starter: `redisson-spring-boot-starter`
  - Config: `RedissonConfig.java` (single server mode) + `config/cache.yaml`
  - Usage: distributed locks for seckill

**Hardware Monitoring:**
- **OSHI 7.1.0** (oshi-core-ffm) — Hardware info via Foreign Function & Memory API.
  - Entry: `SystemInfo` created as Bean in `MonitoringSchedulerConfig.java`
  - Usage: `HardwareMetricsServiceImpl` for CPU/memory/disk/network sampling
  - Sampling interval: 2s default

**Compression:**
- **Zstd 1.5.7-7** (`zstd-jni`) — Compression utility. Runtime dependency.

**Testing (Backend):**
- **JUnit 5** (via `spring-boot-starter-test`) — Test runner.
- **ArchUnit 1.4.2** — Architecture test. Test file: `ArchitectureTest.java` (currently disabled rules).
- **Mockito** (bundled with spring-boot-starter-test) — Mocking.
- Spring Security Test — Security test support.

**Testing (Frontend):**
- **Vitest** `^4.1.2` — Unit test runner. Config inline in `vite.config.js` + `app-vue/vitest.config.ts`.
- **@vue/test-utils** `^2.4.6` — Vue component mounting utilities.
- **jsdom** `^29.0.1` — DOM environment for tests.
- **@playwright/test** `^1.59.1` — E2E testing.

**Quality (Frontend):**
- **ESLint 10.3.0** — JavaScript/TypeScript lint with flat config.
  - Config: `app-vue/eslint.config.js` (flat config format)
  - Plugins: `@typescript-eslint/eslint-plugin` v8.58.2, `eslint-plugin-vue` v10.8.0, `@stylistic/eslint-plugin` v5.10.0
  - Parser: `vue-eslint-parser` + `@typescript-eslint/parser`
- **Stylelint 17.11.0** — CSS linting.
  - Config: `app-vue/stylelint.config.js`
  - Base: `stylelint-config-standard` v40.0.0
- **Prettier 3.8.1** — Code formatting.
  - Config: `app-vue/.prettierrc.json`
  - Settings: no semi, single quotes, 100 print width, no trailing commas, avoid arrow parens

**Quality (Backend):**
- **Checkstyle 10.21.4** — Code style checking. Integrated via Checkstyle Gradle plugin.
  - Config: `springboot/config/checkstyle/checkstyle.xml`
  - Mode: `isIgnoreFailures = true` (warnings only, does not fail build)
  - Rules: naming conventions, import rules, NeedBraces with single-line allowance
- **PMD** — Rules config file at `springboot/config/pmd/pmd-rules.xml`. Covers bestpractices, codestyle, design, errorprone categories. NOT currently wired into `build.gradle.kts` (config exists but plugin not applied).
- **SpotBugs** — Build output directory exists at `springboot/build/spotbugs/` but plugin not explicitly configured in `build.gradle.kts`.
- **Error Prone** — Mentioned in CLAUDE.md but no configuration files found in the codebase. NOT integrated.
- **JaCoCo** — Mentioned in CLAUDE.md but no configuration found in `build.gradle.kts`. NOT integrated.

## Dependency Management

**Frontend** (`app-vue/package.json`):
- Direct dependencies declared with `^` ranges.
- Dev dependencies separately declared.
- No monorepo structure, single `package.json`.

**Backend** (`springboot/gradle/libs.versions.toml`):
- Gradle Version Catalog: All dependency versions centralized in `[versions]` table.
- Spring Boot BOM managed: Most Spring starters inherit version from BOM.
- Non-BOM versions explicitly declared: `mybatis-plus`, `rocketmq`, `redisson`, `jjwt`, `oshi`, `flyway`, `mysql-connector`, etc.
- Lombok: `compileOnly` + `annotationProcessor` scope.
- Spring Boot Config Processor: `annotationProcessor` scope.
- zstd-jni: `runtimeOnly` scope.
- jjwt: `implementation` (api), `runtimeOnly` (impl + jackson).

**Key alignment concerns:**
- **`spring-boot-starter-aop`** pinned at `4.0.0-M2` separate from the BOM version `4.1.0-RC1` — potential version inconsistency risk.
- **RocketMQ 5.x API changes**: Class path migrations between versions (e.g., `TopicList` moved to `remoting.protocol.body`). Verified using `rocketmq-client` 5.5.0 and `rocketmq-spring` 2.3.5.
- **OSHI 7.x FFM API**: Uses `oshi-core-ffm` module (FFM-based), not the traditional `oshi-core`. Requires Java 22+ FFM preview. Java 26 toolchain has it enabled via `--enable-preview`.
- **Spring Boot 4.1.0-RC1**: Pre-release milestone, not a stable release. Bundle of breaking changes from Boot 3.x.

## Configuration

**Environment:**
- YAML configuration split across 5 files, imported via `spring.config.import`:
  - `springboot/src/main/resources/application.yaml` — Core config (app name, server port, virtual threads, multipart limits)
  - `springboot/src/main/resources/config/datasource.yaml` — MySQL + Flyway + HikariCP
  - `springboot/src/main/resources/config/cache.yaml` — Redis + Redisson
  - `springboot/src/main/resources/config/mq.yaml` — RocketMQ
  - `springboot/src/main/resources/config/security.yaml` — JWT + whitelist
  - `springboot/src/main/resources/config/business.yaml` — MyBatis Plus + seckill + logging + monitor
- `springboot/src/main/resources/application-test.yaml` — Test profile overrides
- `.env` file present (contains environment configuration — do not read contents)
- JWT secret hardcoded in `config/security.yaml` as placeholder — **sensitive information needs environment variable injection**

**Build:**
- Frontend: `app-vue/vite.config.js` + `app-vue/tsconfig.json` + `app-vue/tsconfig.node.json`
- Backend: `springboot/build.gradle.kts` + `springboot/settings.gradle.kts` + `springboot/gradle/libs.versions.toml`

## Platform Requirements

**Development:**
- Windows (primary dev platform) — Note: Gradle test encoding requires `-Dfile.encoding=GBK` for Chinese path compatibility on Windows.
- Node.js 20.19+ or 22.12+
- Java 26 JDK
- MySQL 9.x (port 3306)
- Redis 7.x (port 6379)
- RocketMQ 5.x (NameServer port 9876, Broker port 10911)

**Production:**
- Not explicitly documented. Assumed Linux-based deployment target.
- Server port: 8080
- Requires MySQL, Redis, RocketMQ as external dependencies.

---

*Stack analysis: 2026-05-10*
