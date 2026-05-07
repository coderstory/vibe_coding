# Stack Research

**Domain:** 代码注释与文档工具 (Code Comments & Documentation Tools)
**Project:** Vue 3 + Spring Boot 管理后台 (v1.7 注释与文档工程)
**Researched:** 2026-05-07
**Confidence:** HIGH

## 推荐工具链

### 核心注释标准

| 标准 | 适用范围 | 说明 | 为什么选择 |
|---|---|---|---|
| **Javadoc** (`/** */`) | Java 后端 | JDK 原生标准，所有 Java 开发者熟悉。Checkstyle、IDE、Smart-Doc 均原生支持 | 项目存量为 Java，无需引入额外库，生态最成熟 |
| **TSDoc** (`/** */` 含 TSDoc 标签) | TypeScript 前端 | 微软维护的 TS 生态现代标准。与 JSDoc 不同：类型由 TS 类型系统提供，`@param` 只写名称和描述 | Vue 3 + TS 项目首选，与 eslint-plugin-tsdoc 配合最佳 |
| **KDoc** (`/** */` + Markdown) | Kotlin DSL (Gradle) | Kotlin 标准注释格式，支持 Markdown 内联标记 | `build.gradle.kts` 使用 Kotlin DSL，KDoc 是原生标准 |
| **YAML `#` 注释** | 配置文件 | YAML 仅支持单行 `#` 注释，无多行语法 | 配置文件标准，搭配 yamllint 校验格式 |
| **Markdown** | 文档文件 | README、文档页面 | 通用文档格式，搭配 markdownlint 校验 |

### 注释格式校验工具

| 工具 | 版本 | 用途 | 配置建议 |
|---|---|---|---|
| **Checkstyle Javadoc 模块** | 10.21.4 (已在项目中) | 校验 Java Javadoc 的完整性和格式 | 当前关闭了所有 Javadoc 检查。建议按需启用轻量检查（见下方配置策略） |
| **eslint-plugin-tsdoc** | 0.5.2 | 校验 TypeScript 文件中的 TSDoc 注释格式 | 当已有注释时校验格式正确性，不强制要求添加注释。兼容 ESLint 10 flat config |
| **markdownlint-cli2** | 最新 | 校验 Markdown 文件格式 | 检查文档目录下的 `.md` 文件格式一致性 |

### API 文档生成工具（下游阶段，非 v1.7 范围）

| 工具 | 版本 | 用途 | 使用场景 |
|---|---|---|---|
| **Smart-Doc Gradle Plugin** | 3.1.2 | 从 Javadoc 零注解生成 API 文档 | 构建器生成 HTML/Markdown/OpenAPI 3.0/Postman。无需运行时注解，适合"先写注释再出文档"策略 |
| **SpringDoc OpenAPI** | 3.0.2 | 运行时注解驱动的 Swagger UI | 如需在线调试 (Try-it-out)，需在 Controller 添加 `@Operation` 注解 |
| **vue-component-meta** | vuejs/language-tools 3.x | 从 Vue SFC 提取组件元数据 | 提取 Props/Slots/Events 文档，可配合自定义站点 |

### Javadoc 检查配置策略

Checkstyle 的 Javadoc 模块分为 **"格式校验"** 和 **"强制存在"** 两类：

**推荐启用（格式校验，无强制）：**

```xml
<!-- Javadoc 块标签顺序 -->
<module name="AtclauseOrder">
    <property name="tagOrder" value="@param, @return, @throws, @see, @since"/>
</module>

<!-- 块标签必须有描述内容 -->
<module name="NonEmptyAtclauseDescription"/>

<!-- Javadoc 段落结构 -->
<module name="JavadocParagraph">
    <property name="allowNewlineParagraph" value="false"/>
</module>

<!-- 单行 Javadoc 检查（没有块标签时要使用简洁格式） -->
<module name="SingleLineJavadoc"/>

<!-- Summary 首句质量 -->
<module name="SummaryJavadoc"/>

<!-- Javadoc 内容位置 -->
<module name="JavadocContentLocation"/>

<!-- 函数名和注释首句重复检查 (v10.15.0+) -->
<module name="SummaryJavadoc"/>
```

**审慎启用（强制存在，需额外配置豁免）：**

```xml
<!-- 对 public 方法要求 Javadoc，但豁免 Override 和 Controller 注解 -->
<module name="MissingJavadocMethod">
    <property name="scope" value="public"/>
    <property name="allowMissingPropertyJavadoc" value="true"/>
    <property name="allowedAnnotations" value="Override,GetMapping,PostMapping,PutMapping,DeleteMapping,PatchMapping,RequestMapping"/>
    <property name="minLineCount" value="4"/>  <!-- 少于4行的方法不强制 -->
</module>

<!-- public 类要求 Javadoc -->
<module name="MissingJavadocType">
    <property name="scope" value="public"/>
</module>
```

**核心原则：** Javadoc 检查的目的是发现"明显遗漏"而非强制每行都写。合理的豁免配置（allowedAnnotations/minLineCount）是消除误报的关键。Spring Boot 官方自身的 Checkstyle 配置也采用类似的实用主义策略。

## 不使用

| 工具 | 为什么不 | 替代方案 |
|---|---|---|
| **Springfox** (io.springfox) | 项目已停止维护，不支持 Spring Boot 4.x Jakarta 命名空间 | SpringDoc OpenAPI 3.0.2 |
| **Swagger 3.x 注解** (`@ApiOperation`/`@ApiParam`) | v1.7 专注注释本身，注解引入运行时依赖和代码侵入。如需 API 文档，Smart-Doc 可从 Javadoc 生成 | Smart-Doc 3.1.2 |
| **Knife4j** | 强绑定 Swagger 注解，对 Spring Boot 4.x 支持不明确 | Smart-Doc 或 SpringDoc |
| **eslint-plugin-jsdoc** (v62.x) | 主要用于 JS 项目。本项目是 TypeScript + Vue 3，应使用 TSDoc 规范 | eslint-plugin-tsdoc 0.5.2 |
| **vuese** | 使用私有注释语法，非标准 JSDoc/TSDoc | vue-component-meta |
| **`@author` / `@since` 标签** | Git blame 和 Changelog 是更准确的作者和日期信息来源。手动维护易过时 | Git 原生工具 |
| **代码注释中的 PlantUML/Mermaid 图** | 维护困难，在 IDE 中预览体验不佳 | 放在 `docs/` 目录以独立文件管理 |
| **PMD CommentRequired** | 规则不如 Checkstyle 全面，且已有 Checkstyle | Checkstyle Javadoc 模块 |
| **Prettier 的 Markdown 格式化** | Prettier 对 Markdown 的规则支持有限 | markdownlint-cli2 |

## Javadoc vs Swagger 注解的互补关系

根据 2025-2026 行业共识，两者应互补而非对立：

| 层次 | 使用 | 描述内容 | 目标读者 |
|---|---|---|---|
| **Controller 层** (端点) | Javadoc | 业务含义：端点做什么、何时调用、前置条件 | 后端开发者 |
| **Controller 层** (API 合同) | `@Operation`/`@Parameter`/`@Schema` | 请求/响应结构、状态码、数据约束 | 前端/第三方调用者 |
| **Service 层** | Javadoc | 业务逻辑意图、算法说明、设计决策 | 后端维护者 |
| **DTO/Entity 层** | Javadoc + `@Schema` | 字段含义 (Javadoc) + 数据约束描述 (Schema) | 两端开发者 |
| **Config/Utils 层** | Javadoc | 配置项作用、工具方法前置条件和行为 | 后端维护者 |

**核心原则：不要重复。** Javadoc 描述"为什么"（实现层），`@Operation` 描述"是什么"（API 合同层）。

## Smart-Doc vs SpringDoc 对比

| 维度 | Smart-Doc 3.1.2 | SpringDoc 3.0.2 |
|---|---|---|
| **工作方式** | 构建时静态分析源码 | 运行时反射扫描注解 |
| **代码侵入** | 零注解，完全基于 Javadoc | 需要添加 `@Operation` 等注解 |
| **输出** | HTML/Markdown/OpenAPI JSON/Word/Postman | Swagger UI (在线) + OpenAPI JSON |
| **在线调试** | 生成 HTML 调试页面（`createDebugPage: true`） | 原生 Swagger UI 调试支持 |
| **Gradle 集成** | 专用插件 `com.ly.smart-doc` | 标准依赖 + 自动配置 |
| **多模块** | 支持 subprojects 统一配置 | 支持，需额外配置 GroupedOpenApi |
| **适合场景** | 注释优先、不想要注解侵入的团队 | 需要交互式 API 调试、前端频繁调用 |

## 注释约定速查

### Java Javadoc 示例

```java
/**
 * 根据用户 ID 获取用户信息及其关联角色
 *
 * <p>查询用户基本信息，同时加载用户角色和权限列表。
 * 返回的用户对象包含完整的角色树。</p>
 *
 * @param userId 用户唯一标识，不能为空
 * @return 带角色信息的用户对象，不存在则返回 null
 * @throws IllegalArgumentException 如果 userId 为空
 * @see UserRoleService#getUserRoles
 */
public UserVO getUserWithRoles(Long userId) {
    // ...
}
```

### TypeScript TSDoc 示例

```typescript
/**
 * 用户登录服务
 *
 * 处理用户认证流程，包括密码验证和令牌生成。
 * 支持记住我功能延长 token 有效期。
 *
 * @param credentials - 登录凭据（用户名 + 密码）
 * @param rememberMe - 是否记住登录状态
 * @returns 认证结果，包含 token 和用户信息
 * @throws AuthenticationError 用户名或密码错误时
 */
async function login(credentials: LoginDto, rememberMe?: boolean): Promise<LoginResult>
```

### Vue 3 组件注释

```vue
<script lang="ts">
/**
 * 用户表单组件
 *
 * 支持用户信息的创建和编辑，包含表单验证和异步提交。
 * 通过 `mode` prop 切换新建/编辑模式。
 *
 * @example
 * <UserForm mode="create" @submit="handleSubmit" />
 */
</script>

<script setup lang="ts">
interface Props {
  /** 表单模式：create 新建 / edit 编辑 */
  mode: 'create' | 'edit'
  /** 编辑模式下的用户 ID（create 模式下忽略） */
  userId?: number
  /** 表单初始数据（可选，用于预填充） */
  initialData?: Partial<UserInfo>
}
const props = defineProps<Props>()

/**
 * 提交表单时触发
 * @param formData - 表单数据对象
 */
const emit = defineEmits<{
  submit: [formData: UserInfo]
}>()
</script>
```

### Gradle KDoc 示例

```kotlin
/**
 * 生产环境构建配置
 *
 * 启用所有优化选项，禁用调试信息。
 * 此配置文件应用于 CI/CD 生产部署流程。
 *
 * @since 1.4.0
 */
plugins {
    id("org.springframework.boot") version "4.1.0-RC1"
}
```

### YAML 注释示例

```yaml
# ---------------------------------------------------------------------------
# 数据源配置
# ---------------------------------------------------------------------------
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/admin_system
    # 连接池初始大小（根据服务启动时的并发量调整）
    hikari:
      initial-size: 5
```

## 安装

```bash
# eslint-plugin-tsdoc（前端 TSDoc 格式校验）
cd app-vue
npm install -D eslint-plugin-tsdoc@^0.5.2

# markdownlint-cli2（Markdown 格式校验）
npm install -D markdownlint-cli2@latest

# Smart-Doc（下游阶段，API 文档生成）
# 在 springboot/build.gradle.kts 添加：
# plugins { id("com.ly.smart-doc") version "3.1.2" }

# SpringDoc OpenAPI（下游阶段，交互式 API 文档）
# 在 springboot/build.gradle.kts 添加依赖：
# implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
```

## 版本兼容

| 包 | 兼容环境 | 注意事项 |
|---|---|---|
| `eslint-plugin-tsdoc@0.5.2` | ESLint 9.x / 10.x flat config | v0.5.0+ 支持 flat config。v0.5.2 修复了 ESLint 10 的 API 变更 |
| `springdoc-openapi-starter-webmvc-ui:3.0.2` | Spring Boot 4.1.x | v3.x 专为 Spring Boot 4.x / Spring Framework 7.x 设计。Spring Boot 3.x 用户使用 v2.8.x |
| `com.ly.smart-doc:smart-doc-gradle-plugin:3.1.2` | JDK 17+, Gradle 8.4+ | 本项目 JDK 26 + Gradle 9.5 完全兼容 |
| `Checkstyle 10.21.4` | JDK 23+ | 如 JDK 26 遇到兼容性问题，升级到 Checkstyle 13.x |

## 来源

- [Smart-Doc 官方文档](https://smart-doc-group.github.io/#/zh-cn/) — Gradle 插件配置、3.x 版本变更
- [Smart-Doc Gradle Plugin GitHub](https://github.com/TongchengOpenSource/smart-doc-gradle-plugin) — 3.1.2 版本信息
- [SpringDoc OpenAPI Releases](https://github.com/springdoc/springdoc-openapi/releases) — v3.0.2 兼容性
- [Checkstyle Javadoc Checks 官方文档](https://checkstyle.org/checks/javadoc/index.html) — 所有 Javadoc 检查模块说明
- [Checkstyle Release Notes](https://checkstyle.org/releasenotes.html) — 10.21.4 最新发布说明
- [eslint-plugin-tsdoc npm](https://www.npmjs.com/package/eslint-plugin-tsdoc) — v0.5.2 ESLint 10 兼容性
- [tsdoc.org](https://tsdoc.org/) — TypeScript 注释标准规范
- [KDoc 官方文档](https://kotlinlang.org/docs/kotlin-doc.html) — Kotlin 文档注释语法
- [YAML 注释最佳实践](https://devgex.com/en/article/00001216) — YAML 注释约定
- [Vue.js 3 组件中 JSDoc 注释最佳实践](https://blog.gitcode.com/76e939cf67d58be05074740c174da061.html) — Vue 3 组件注释方案
- [Spring Boot 官方 Checkstyle 配置](https://gitee.com/mirrors/spring-boot/blob/main/buildSrc/config/checkstyle/checkstyle.xml) — Spring Boot 项目自身的 Checkstyle 参考

---
*Stack research for: 代码注释与文档工具*
*Researched: 2026-05-07*
