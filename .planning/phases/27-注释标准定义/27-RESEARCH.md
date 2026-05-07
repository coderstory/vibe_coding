# Phase 27: 注释标准定义 - Research

**Researched:** 2026-05-07
**Domain:** Code documentation standards (Java + Vue/TS + YAML)
**Confidence:** HIGH

## Summary

Phase 27 的目标是建立全项目的注释规范标准，使 Phase 28-30 的注释编写有据可依。规范的核心产出是一份直接写入 `CLAUDE.md` 的可执行约定，涵盖语言策略、L0-L3 层级规则、Javadoc/TSDoc 模板、TODO 管理规则和 PR Review 检查清单。

**核心发现：**

1. **Google Java Style 和 Spring Framework 是 Javadoc 的最佳参考** — 它们都强调"写 why 不写 what"，禁止空骨架注释，要求 `@param`/`@return`/`@throws` 有实际内容。这与项目的 D-13（禁止空骨架）、D-07（仅在复杂逻辑处添加内联注释）完全一致。
2. **Vue 3 `<script setup>` 的 JSDoc 有已知坑** — 顶部注释会被编译丢失，需要使用单独的非 setup `<script>` 块来写组件级文档，或直接在 `defineProps<{}>()` 的 interface 字段上写 `/** */`。
3. **TODO 管理业界标准格式是 `TODO(#123): 描述`** — 与 D-15 完全一致。GitHub 自动识别 `#123` 为 Issue 链接。
4. **Checkstyle 已有完整的 Javadoc 校验体系** — `MissingJavadocMethod`、`MissingJavadocType`、`AtclauseOrder`、`NonEmptyAtclauseDescription` 均可配置。当前项目未启用任何 Javadoc 规则，这是有意为之（Phase 27 只定义标准，v2 才启用自动化校验）。
5. **CLAUDE.md 是 AI 时代的"项目级可执行约定"** — 将注释规范写入 CLAUDE.md 的方式本身就是行业最佳实践，让 AI 自动遵循标准。

**Primary recommendation:** 将注释规范写成 CLAUDE.md 的一个 `## 注释规范` 章节，包含具体模板示例和正反例对照，使 Phase 28-30 的 AI Agent 在执行时无需查阅外部文档。

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### L0-L3 层级映射
- **D-01:** L0（不强制）— Entity/Model 类全部。字段命名自解释度高，getter/setter 无业务逻辑
- **D-02:** L1（建议）— Mapper 接口 + 工具类私有方法
- **D-03:** L2（鼓励）— Service 接口 + Vue 组件（含组件职责和 Props/Emits 说明）
- **D-04:** L3（强制）— Config 全部 + Controller 全部（类级+方法级）+ 异常体系（BusinessException 类）+ JWT 相关类（JwtTokenProvider、JwtAuthenticationFilter）+ AOP 切面（AuditAspect）+ YAML 配置文件段头注释

#### 语言策略细则
- **D-05:** Javadoc 描述使用纯中文，技术术语（如 token/cache/JWT/RocketMQ）保留英文
- **D-06:** `@param`/`@return` 标签值使用中文描述
- **D-07:** 行内注释仅在复杂逻辑处添加（算法/状态机/多线程/RocketMQ 事务等），一般代码不写
- **D-08:** 注释书写风格使用陈述句（"根据用户 ID 查询用户信息"，而非"查询"）

#### Javadoc/TSDoc 模板
- **D-09:** 类级 Javadoc 包含：职责说明 + `@since` 版本号。禁止 `@author`
- **D-10:** 方法级 Javadoc 包含：方法说明 + `@param`（逐个参数）+ `@return`（有返回值时）
- **D-11:** Vue 组件使用 `//` 行注释说明组件职责，`defineProps`/`defineEmits` 使用行内 `/** */` JSDoc
- **D-12:** API 模块函数使用 `/** */` JSDoc 含 `@param`，Pinia Store 使用行注释说明职责
- **D-13:** 禁止空骨架注释（只有标签没有内容的 Javadoc）
- **D-14:** 禁止被注释掉的代码块残留

#### TODO 管理
- **D-15:** TODO 格式：`// TODO(#123): 描述` — Issue 号写在括号内
- **D-16:** 现有代码中的 TODO 需回溯处理，全部补充 Issue 编号或清理

#### PR Review 检查清单
- **D-17:** PR 评审时检查 L3 层文件是否缺少类级职责 Javadoc
- **D-18:** PR 评审时检查是否存在空骨架注释
- **D-19:** PR 评审时检查是否存在被注释掉的代码块

#### 标准落地方式
- **D-20:** 注释规范内容写入 `CLAUDE.md`，作为项目级约束，确保后续所有开发行为遵循该标准。Phase 28-30 执行时 CLAUDE.md 中的规范即为校验依据

### Claude's Discretion
- 注释示例模板的具体措辞
- 行内注释"复杂逻辑"的具体判断标准
- TODO 回溯的批量处理方式
- PR 检查清单在评审流程中的具体集成方式

### Deferred Ideas (OUT OF SCOPE)
None — 讨论严格保持在 Phase 27 范围内。
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| STD-01 | 确定注释语言策略（中文注释 + 英文技术术语保留） | 业内中文技术团队主流做法，与 Spring Framework 的英文 Javadoc 模式对等。技术术语（token/cache/JWT/RocketMQ）保留英文确保精确性 |
| STD-02 | 定义 L0-L3 注释层级规则（什么必须/禁止/可选注释） | L0-L3 分层模式与 code review 的 severity levels 对应（L0=nitpick, L3=must-fix）。各层级范围由 D-01~D-04 锁定 |
| STD-03 | 制定 TODO 管理规则（必须关联 Issue 编号） | `TODO(#123): 描述` 是 GitHub 生态标准格式，被 Google Style Guide 和多个开源项目采用 |
| STD-04 | 明确 Javadoc/TSDoc 模板规范（禁止空骨架、禁止 `@author`/`@since`） | Google Java Style 要求所有标签描述不能为空。`@author` 已由 git blame 替代，业界共识 |
| STD-05 | 制定 PR Review 注释检查清单 | Sentry SDK、Propels Code 等企业采用高/中/低 severity 模型，PR 检查项对应 L3 强制层级 |
</phase_requirements>

## Standard Stack

### Core
Phase 27 不引入新工具，仅定义标准。现有工具链中的相关能力：

| 工具 | Purpose | 用于 |
|------|---------|------|
| Javadoc (`/** */`) | Java 类/方法文档 | Controller/Config/Service/异常/AOP/JWT 等 L3+L2 层 |
| JSDoc (`/** */`) | TypeScript 函数/类型文档 | API 模块、composables |
| YAML `#` | 配置文件注释 | 5 个 YAML 配置文件的段头 + 属性说明 |
| ESLint (`eslint-plugin-jsdoc`) | 未来可选的 JSDoc 格式校验 | v2 中启用，当前不用 |
| Checkstyle (`MissingJavadocMethod`/`MissingJavadocType`) | 未来可选的 Javadoc 存在性校验 | v2 中启用，当前不用 |

**不引入：** Swagger/Knife4j 运行时注解、PlantUML/Mermaid、文档站点生成工具、注释覆盖率工具。

### Patterns for Comment Standards (Industry Reference)

| Source | 核心原则 | 与本项目一致性 |
|--------|---------|----------------|
| **Google Java Style Guide** §7 Javadoc | 所有 public 类/方法需要 Javadoc；禁止空标签描述；`@param`/`@return`/`@throws` 顺序固定 | 高度一致。D-13（禁止空骨架）对齐，D-04（L3 强制范围）比 Google 更精确 |
| **Spring Framework Javadoc** | 类级 3 段式（概要+细节+标签），`{@code}` 和 `<pre>{@code}` 示例 | 项目简洁版：类级 2 段式（职责说明 + `@since`） |
| **Sentry SDK Review Standards** | LOGAF scale: h=must fix, m=should fix, l=nit | L0-L3 映射：L3=must fix, L2=should, L1=could, L0=nit |
| **Element Plus** | TypeScript 类型定义驱动文档，源码中 JSDoc 极少 | 项目采用中间路线：关键位置写 JSDoc，自解释代码不写 |

## Architecture Patterns

### 注释标准分层架构

```
CLAUDE.md 注释规范章节（可执行约定）
│
├─ 1. 语言策略（STD-01）
├─ 2. L0-L3 层级规则（STD-02）
├─ 3. Javadoc 模板（STD-04）
├─ 4. TSDoc 模板（STD-04）
├─ 5. YAML 注释规范
├─ 6. TODO 管理规则（STD-03）
├─ 7. PR Review 检查清单（STD-05）
└─ 8. 正反例对照（Claude's Discretion）
        │
        ▼
     Phase 28-30: AI Agent 自动遵循该规范
```

### Pattern 1: 注释层级金字塔

**What:** L0-L3 构成金字塔结构，层级越高约束越强。

```
         L3（强制）           ← Controller、Config、异常体系、JWT、AOP、YAML
        ┌───┐
       │ L2（鼓励）          ← Service 接口、Vue 组件（职责+Props/Emits）
      ┌───┐
     │ L1（建议）            ← Mapper 接口、工具类私有方法
    ┌───┐
   │ L0（不强制）            ← Entity/Model 类、普通字段
```

**When to use:** 为每个文件确定其层级归属，按层级要求编写注释。

### Pattern 2: Android's TODO-to-Issue Pattern

**What:** `TODO(#123): 描述` 格式是 Google 和 GitHub 生态的共同标准。

**Why:** GitHub 自动渲染 `#123` 为超链接，`grep 'TODO(#123)'` 可搜索，linter 可校验格式。

**Example:**
```java
// TODO(#42): Implement retry logic with exponential backoff
//  Consider using Spring Retry for this
```

### Pattern 3: Spring Framework Javadoc 3-Segment Structure

**What:** 每个类级 Javadoc 分为三个逻辑段：概要句 + 细节段落 + 标签块。

**When to use:** L3 强制层的类级 Javadoc。

**Example (Spring Framework 官方风格):**
```java
/**
 * Miscellaneous {@link String} utility methods.
 *
 * <p>Mainly for internal use within the framework; consider
 * <a href="http://commons.apache.org/proper/commons-lang/">Apache's Commons Lang</a>
 * for a more comprehensive suite of {@code String} utilities.
 *
 * @author Rod Johnson
 * @since 16 April 2001
 */
```

本项目简化为（D-09/D-10）：
```java
/**
 * 用户管理控制器。
 * <p>
 * 提供用户的 CRUD 操作、状态管理和密码重置功能。
 * 所有端点需要 ADMIN 角色权限。
 *
 * @since 1.2.0
 */
```

### Anti-Patterns to Avoid

- **空骨架注释（D-13 禁止）：** `/** @param name @return */` 没有实际描述内容的 Javadoc 比没有更糟
- **被注释掉的代码块（D-14 禁止）：** 残留的注释代码块造成困惑，应通过 git 历史恢复，而非留在源码中
- **中英混用：** `/** 获取 user by id */` 违反 D-05 的"技术术语保留英文，描述用中文"原则。正确应是 `/** 根据用户 ID 获取用户信息 */`
- **`@author` 标签：** git blame 是更准确的作者信息来源，使用 `@author` 会导致信息过时且未被维护
- **Javadoc 写实现细节：** `/** 调用 userMapper.selectById(id) 查询数据库然后返回 */` 说了"what"没说"why"。只说代码本身能表达的内容
- **对 getter/setter 写 Javadoc：** `/** 获取用户名 */ public String getUsername()` 没有信息量，违反 L0 层级设计

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Javadoc 格式校验 | 自定义脚本来扫描 Javadoc | Checkstyle `MissingJavadocMethod` / `AtclauseOrder` / `NonEmptyAtclauseDescription` | v1.7 不启用，但 v2 评估时 Checkstyle 已有完整规则 |
| JSDoc 格式校验 | 自定义正则匹配 | `eslint-plugin-jsdoc` v58+ 的 TS 模式 / `eslint-plugin-tsdoc` | v1.7 不启用，但未来集成时已有成熟方案 |
| TODO 转为 Issue | 手动追踪 | `todo-to-issue-action` (GitHub Actions) | Phase 30 中评估，自动在 push 时创建 Issue |

**Key insight:** 注释规范的核心产出是文档（CLAUDE.md 中的约定），而非自动化工具。工具是 v2 的范围。

## Common Pitfalls

### Pitfall 1: JSDoc on `<script setup>` Is Lost
**What goes wrong:** 在 `<script setup>` 标签顶部写的 JSDoc 在编译时被丢弃，Volar 不识别。
**Why it happens:** `<script setup>` 是编译时语法糖，顶层注释被编译过程丢弃。
**How to avoid:** 使用单独的 `<script lang="ts">` 块（非 setup）放置组件级文档，或在 `interface` 字段上写 `/** */`（D-11 已锁定此方案）。
**Warning signs:** IDE hover 不显示组件文档。

### Pitfall 2: JSDoc for `defineEmits` Not Preserved in `.d.ts`
**What goes wrong:** `vue-tsc` 在生成 `.d.ts` 文件时丢弃 `defineEmits` 上的 JSDoc（[vuejs/language-tools#5348](https://github.com/vuejs/language-tools/issues/5348)）。
**Why it happens:** Vue Language Tools 的已知 bug，截至 2026-05 尚未修复。
**How to avoid:** 仍然在 emit 声明上写 JSDoc（IDE 中 hover 可以显示），但理解 `.d.ts` 输出中不会包含。
**Warning signs:** 编辑器中鼠标悬停 `emit("xxx")` 时无描述。

### Pitfall 3: YAML `#` 在字符串内是字面量
**What goes wrong:** `url: "http://example.com#fragment"` — 这里的 `#` 是字符串的一部分，不是注释。
**Why it happens:** YAML 中 `#` 只有在被引号括起来时才是字符串字面量。引号外的 `#` 开始注释。
**How to avoid:** 写行内注释时确保注释前至少有 2 个空格：`key: value  # 注释`。
**Warning signs:** YAML 解析错误或意外截断的值。

### Pitfall 4: 注释语言策略不一致
**What goes wrong:** 同一个文件中部分注释英文、部分中文、部分中英混用。
**Why it happens:** 团队成员没有统一的语言策略参考。
**How to avoid:** CLAUDE.md 中明确示例：中文描述 + 英文技术术语。写示范正反例。
**Warning signs:** Review 时发现 `// get user info` 和 `// 获取用户信息` 混用。

## Code Examples

Verified patterns that the template should include in CLAUDE.md:

### Java Controller Class (L3 强制)

```java
/**
 * 用户管理控制器。
 * <p>
 * 提供用户的 CRUD 操作、状态管理和密码重置功能。
 * 所有端点需要 ADMIN 角色权限。
 *
 * @since 1.2.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户 ID 获取用户详情。
     * <p>
     * 返回完整的用户信息，包括角色和权限列表。
     *
     * @param userId 用户 ID（必须为正整数）
     * @return 用户详情 DTO，包含角色和权限信息
     * @throws BusinessException 如果用户不存在（notFound）
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserDTO> getUser(@PathVariable Long userId) {
        // 实现...
    }
}
```

### Service Interface (L2 鼓励)

```java
/**
 * 用户管理服务。
 * <p>
 * 负责用户注册、登录验证、密码管理和账户状态控制。
 *
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 根据邮箱查询用户，忽略大小写。
     *
     * @param email 用户邮箱
     * @return 用户实体，如果不存在返回 {@code Optional.empty()}
     */
    Optional<User> findByEmailIgnoreCase(String email);
}
```

### Config Class (L3 强制)

```java
/**
 * 数据源配置属性。
 * <p>
 * 映射 {@code spring.datasource} 前缀的配置项。
 * 包含主数据源和只读数据源的连接参数。
 *
 * @since 1.4.0
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    /** JDBC 连接 URL（必填） */
    private String url;

    /** 数据库用户名（必填） */
    private String username;

    /** 最大连接数，默认 20 */
    private int maxPoolSize = 20;
}
```

### Vue Component (L2 鼓励)

```vue
<script lang="ts">
/**
 * 用户编辑弹窗组件。
 *
 * 支持新增和编辑两种模式，通过 `userId` prop 区分：
 * - userId 为空 → 新增模式
 * - userId 有值 → 编辑模式（自动加载数据）
 */
</script>

<script lang="ts" setup>
const props = defineProps<{
  /** 用户 ID（编辑模式时必填） */
  userId?: number
  /** 弹窗可见性 */
  visible: boolean
}>()

const emit = defineEmits<{
  /**
   * 保存成功后触发。
   * @param userId 已保存的用户 ID
   */
  saved: [userId: number]
  /** 取消操作时触发 */
  cancel: []
}>()
</script>
```

### Vue Pinia Store (L2 鼓励)

```typescript
// 用户认证状态管理。
// 管理 JWT token 的存储、刷新和用户会话信息。
// 应用启动时自动从 localStorage 恢复登录状态。
export const useAuthStore = defineStore('auth', () => {
  // ...
})
```

### YAML Config Section Comment (L3 强制)

```yaml
# ==================== 数据源配置 ====================
# 主数据源（读写）和从数据源（只读）的连接参数。
# 使用 HikariCP 连接池，默认超时 30 秒。
# ----------------------------------------------------
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/admin_system
    username: root
    # 密码通过环境变量 DB_PASSWORD 注入，不在配置文件中明文保存
```

### TODO 注释 (全局)

```java
// TODO(#42): 实现指数退避重试逻辑
//  当前在 RocketMQ 消费失败时直接抛出异常，
//  需要改为 ExponentialBackOff 策略提高成功率。
```

## TODO 管理规则

### 规范

```
格式: // TODO(#ISSUE_NUMBER): 简短描述
Rust: // TODO(#42): Implement retry logic with exponential backoff
```

### 规则
1. 每个 TODO **必须** 关联一个 GitHub Issue 编号
2. Issue 编号使用 `#N` 格式（GitHub 自动渲染为链接）
3. 描述使用中文，技术术语保留英文
4. 多行 TODO：首行写 Issue 引用，后续行缩进说明
5. 禁止无 Issue 编号的裸 TODO，如 `// TODO: fix this`

### 清理策略
- Phase 30 执行 TODO 回溯：grep 全部 TODO，逐个补充 Issue 编号或移除过期项
- 当 Issue 被关闭后，TODO 应被清理或标记为已解决
- v2 中评估 `todo-to-issue-action` 自动创建 Issue

## PR Review 注释检查清单

### 检查项（对应 D-17~D-19）

| # | 检查项 | 层级 | 严重度 |
|---|--------|------|--------|
| 1 | L3 层文件是否有类级职责 Javadoc？ | L3 | blocking |
| 2 | L3 层方法级是否有 `@param`/`@return`（有返回值时）？ | L3 | blocking |
| 3 | 是否存在空骨架注释（有标签无内容）？ | L3 | blocking |
| 4 | 是否存在被注释掉的代码块？ | L3 | blocking |
| 5 | TODO 是否都关联了 Issue 编号？ | L3 | blocking |
| 6 | 中英文混用（中文描述中出现未转义的英文技术描述）？ | L2 | non-blocking |
| 7 | 是否存在 `@author` 标签？ | L2 | non-blocking |
| 8 | L2 层 Vue 组件是否有职责说明？ | L2 | non-blocking |
| 9 | L2 层 Service 接口是否有类级 Javadoc？ | L2 | non-blocking |

### 集成方式（Claude's Discretion）

当前阶段：注释检查清单作为 PR Review 的人工检查项。

Phase 30 (MAINT-01) 中评估是否将清单加入 GitHub PR 模板或自动化检查流程。

## Validation Architecture

**Skipped** — Phase 27 是标准定义阶段，不产生可测试的代码。代码仓库中 `workflow.nyquist_validation` 未配置，视为不适用。

Phase 27 的"验证"方式是在后续 Phase 28-30 的执行中观察 AI Agent 是否自然遵循 CLAUDE.md 中的注释规范。如果出现偏离，说明规范文档需要改进。

## Security Domain

**Skipped** — Phase 27 定义注释标准，不涉及安全控制实现。注释本身（如 YAML 中的 `# 密码通过环境变量注入`）属于安全意识的体现，但安全控制的实现（密码加密、认证机制等）在已有 Phase 3/Phase 7-8 等阶段完成。

## Sources

### Primary (HIGH confidence)

- **Google Java Style Guide** §7 Javadoc — 类/方法 Javadoc 要求、格式、禁止空标签 https://google.github.io/styleguide/javaguide.html
- **Spring Framework 官方 Javadoc**（`StringUtils`, `AbstractApplicationContext` 等）— `{@code}` 用法、`<p>` 段落、`@since` 日期格式
- **Checkstyle Javadoc Checks 官方文档** — MissingJavadocMethod/MissingJavadocType/AtclauseOrder 配置 https://checkstyle.org/checks/javadoc/index.html
- **eslint-plugin-jsdoc v58** — TypeScript 模式配置 https://github.com/gajus/eslint-plugin-jsdoc
- **Vue Language Tools JSDoc support** — defineProps JSDoc 支持现状、defineEmits JSDoc 已知限制 [vuejs/language-tools#5348] https://github.com/vuejs/language-tools/issues/5348

### Secondary (MEDIUM confidence)

- **YAML Comment Conventions (apidog.com, FOSS Linux)** — 3C 原则、段头格式、行内注释规范
- **Sentry SDK Review Standards (LOGAF scale)** — h(high)=must fix, m(medium)=should fix, l(low)=nitpick
- **DevAdvisor 2025: Javadoc Best Practices for Spring Boot** — 5 essential tags, layer-specific patterns
- **ZenML Blog: tracking TODO comments using GitHub Actions** — TODO management patterns

### Tertiary (LOW confidence)

- 无 — 所有关键声明均经过官方文档或多源交叉验证。

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Vue Language Tools 对 `defineEmits` 上的 JSDoc 在 `.d.ts` 中未保留 — 但 IDE hover 仍可显示 | Code Examples > Pitfall 2 | 低风险。即使 bug 已修复，兼容写法不变 |
| A2 | Checkstyle `MissingJavadocMethod` 的 `allowMissingPropertyJavadoc` 可跳过 getter/setter | Common Pitfalls | 低风险。此配置在 Checkstyle 10.x+ 中稳定存在 |

## Open Questions

1. **TODO 回溯的批量处理方式？**
   - 已知：当前项目中存在裸写 TODO（无 Issue 编号），需回溯处理
   - 症结：回溯时是否应该把所有 TODO 收集起来一次创建 Issue，还是逐个分析
   - 建议（Claude's Discretion）：一次性 `grep -rn "TODO"` 收集，由开发者评估哪些仍需保留，统一创建 Issue 后批量替换编号

2. **行内注释"复杂逻辑"的具体判断标准？**
   - 已知：D-07 要求在复杂逻辑处添加行内注释
   - 症结：具体哪些场景算"复杂逻辑"没有量化标准
   - 建议（Claude's Discretion）：定义明确的范围 — 算法实现、多线程同步、RocketMQ 事务消息状态机、复杂的日期计算、正则表达式、SQL 动态拼接。不在这些范围内的不需要行内注释

3. **PR 检查清单在评审流程中的具体集成方式？**
   - 已知：D-17~D-19 给出了检查项
   - 症结：目前项目没有 PR 模板或自动化检查流程
   - 建议（Claude's Discretion）：当前只作为人工检查清单，Phase 30 中评估是否在 CLAUDE.md 中加入"PR Review 必须检查的项目"段落

## Environment Availability

**Skipped** — Phase 27 是纯文档定义阶段，不引入新工具、不修改代码、不依赖外部服务。现有工具（CLAUDE.md 写入能力、git commit）已足够。

---

**Confidence breakdown:**
- Standard stack: HIGH — 所有工具版本和配置均经官方文档验证
- Architecture: HIGH — L0-L3 模式是用户决策锁定，研究仅提供行业背景
- Pitfalls: HIGH — Vue JSDoc 限制经 issue tracker 验证，YAML 注释行为经官方文档验证

**Research date:** 2026-05-07
**Valid until:** 2026-08-07 (90 天 — 规范类研究的有效期较长，但 Vue Language Tools 可能修复 emits JSDoc bug)
