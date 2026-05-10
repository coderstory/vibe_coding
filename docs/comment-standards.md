# 注释规范

> 本文是 CLAUDE.md 注释规范的详细展开。CLAUDE.md 仅保留 L0-L3 速览。

## 目录

- [1. 注释语言策略](#1-注释语言策略)
- [2. L0-L3 注释层级](#2-l0-l3-注释层级)
- [3. Javadoc 模板](#3-javadoc-模板)
- [4. TSDoc 模板](#4-tsdoc-模板)
- [5. YAML 注释规范](#5-yaml-注释规范)
- [6. TODO 管理规则](#6-todo-管理规则)
- [7. PR Review 检查清单](#7-pr-review-检查清单)
- [8. 注释维护机制](#8-注释维护机制)

## 1. 注释语言策略

- **Javadoc 描述语言：** 纯中文，技术术语（token、cache、JWT、RocketMQ、Redis、MyBatis、DTO、VO）保留英文。禁止中英混用（如"获取 user by id"）。
- **@param/@return 语言：** 中文描述。例如 `@param userId 用户 ID`，而非 `@param userId user ID`。
- **行内注释策略：** 仅复杂逻辑处添加。包括：算法实现、多线程/并发同步、RocketMQ 事务消息状态机、复杂日期计算、正则表达式、SQL 动态拼接。一般代码不写行内注释。
- **书写风格：** 陈述句，说明方法"做什么"而非"是什么"。例如"根据用户 ID 查询用户信息"，而非"查询"或"Query user"。

示例（正反例）：

```java
// 正面：陈述句 + 技术术语保留英文
/** 根据用户 ID 查询用户信息，包含角色和权限列表。 */

// 反面：中英混用
/** 根据 user id 获取 user info */

// 反面：祈使句
/** 查询用户信息 */
```

## 2. L0-L3 注释层级

每个 Java 类/接口/文件和 Vue 组件按以下层级确定注释要求：

| 层级 | 约束 | 适用范围 | 说明 |
|------|------|----------|------|
| L0 | 不强制 | Entity/Model 类全部 | 字段命名自解释度高，getter/setter 无业务逻辑 |
| L1 | 建议 | Mapper 接口 + 工具类私有方法 | 仅在 SQL 逻辑非直观或私有方法有业务含义时添加 |
| L2 | 鼓励 | Service 接口 + Vue 组件 | 接口职责和方法说明 + 组件职责和 Props/Emits 说明 |
| L3 | 强制 | Config 全部 + Controller 全部 + 异常体系（BusinessException）+ JWT 相关类（JwtTokenProvider、JwtAuthenticationFilter）+ AOP 切面（AuditAspect）+ YAML 配置文件段头 | 类级+方法级 Javadoc 必须完整 |

**判断规则：** 为每个文件确定层级归属，按层级要求编写注释。L3 层文件缺少 Javadoc 视为 PR 阻塞项。

## 3. Javadoc 模板

### 3.1 类级 Javadoc

格式：

```java
/**
 * 类职责说明（中文陈述句）。
 * <p>
 * 详细描述类的核心功能和职责边界（可选，职责足够清晰时可省略此段）。
 *
 * @since x.x.x
 */
```

规则：

- 必须包含：职责说明 + `@since` 版本号
- 禁止 `@author`（改用 git blame/log 追溯作者）
- `@since` 版本号参考 CLAUDE.md 演变历史中的版本号（当前最新 v1.5）

示例（Config 类）：

```java
/**
 * 数据源配置属性。
 * <p>
 * 映射 spring.datasource 前缀的配置项，包含主数据源和只读数据源的连接参数。
 *
 * @since 1.4.0
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {
    /** JDBC 连接 URL（必填） */
    private String url;
    /** 数据库用户名（必填） */
    private String username;
}
```

示例（Controller 类）：

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
public class UserController {
    // ...
}
```

### 3.2 方法级 Javadoc

格式：

```java
/**
 * 方法说明（中文陈述句，说明做什么而非如何做）。
 *
 * @param paramName 参数说明（中文，逐个参数）
 * @return 返回值说明（有返回值时必填）
 */
```

规则：

- 必须包含：方法说明
- L3 层方法必须包含 `@param`（有参数时）和 `@return`（有返回值时）
- 禁止在 `@param`/`@return` 后留空（禁止空骨架）

示例：

```java
/**
 * 根据用户 ID 获取用户详情。
 * <p>
 * 返回完整的用户信息，包括角色和权限列表。
 *
 * @param userId 用户 ID（必须为正整数）
 * @return 用户详情 DTO，包含角色和权限信息
 */
public UserDTO getUser(Long userId) { ... }
```

### 3.3 禁止行为

1. **空骨架注释：** 只有标签没有描述内容的 Javadoc
   ```java
   // 禁止：@param 后无描述
   /**
    * @param name
    * @return
    */
   ```
2. **注释掉的代码块：** 残留在源码中的注释代码
   ```java
   // 禁止：被注释掉的残留代码
   // public void oldMethod() { ... }
   ```
3. **getter/setter 注释：** Entity 字段无需注释（字段名已自解释）
4. **实现细节：** 注释说"what"不说"how"——避免 `/** 调用 userMapper.selectById(id) 返回 */`

## 4. TSDoc 模板

### 4.1 Vue 组件注释

用单独 `<script lang="ts">` 块（非 setup）放置组件级文档（解决 Vue 3 `<script setup>` 编译时丢弃顶层 JSDoc），`defineProps`/`defineEmits` 用行内 `/** */` JSDoc。

示例：

```vue
<script lang="ts">
/**
 * 用户编辑弹窗组件。
 *
 * 支持新增和编辑两种模式，通过 userId prop 区分：
 * - userId 为空 → 新增模式
 * - userId 有值 → 编辑模式（自动回填数据）
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

### 4.2 API 模块函数注释

用 `/** */` JSDoc 块注释，包含 `@param`。

示例：

```typescript
/**
 * 获取用户列表。
 * @param params 查询参数（分页 + 筛选条件）
 */
export function getUsers(params: UserQuery): Promise<PageResult<UserVO>>
```

### 4.3 Pinia Store 注释

用 `//` 行注释说明 Store 职责。

示例：

```typescript
// 用户认证状态管理。
// 管理 JWT token 的存储、刷新和用户会话信息。
// 应用启动时自动从 localStorage 恢复登录状态。
export const useAuthStore = defineStore('auth', () => {
  // ...
})
```

### 4.4 Router 注释

Vue Router 路由表中每条路由配置添加行注释说明：

```typescript
// 用户管理页面（需要 ADMIN 角色）
path: '/system/users',
component: () => import('@/views/system/users/UserListPage.vue'),
meta: { roles: ['ADMIN'] },
```

## 5. YAML 注释规范

段头注释格式：

```yaml
# ==================== 段落标题 ====================
# 段落说明（该段落的核心配置目的）
# ---------------------------------------------------
```

行内注释格式（确保注释前至少 2 空格）：

```yaml
key: value  # 行内说明
```

示例：

```yaml
# ==================== 数据源配置 ====================
# 主数据源（读写）和从数据源（只读）的连接参数。
# 使用 HikariCP 连接池，默认超时 30 秒。
# ---------------------------------------------------
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/admin_system
    username: root
    # 密码通过环境变量 DB_PASSWORD 注入，不在配置文件中明文保存
```

## 6. TODO 管理规则

格式：

```
// TODO(#ISSUE_NUMBER): 简短描述
```

规则：

1. 每个 TODO **必须** 关联 Issue 编号
2. Issue 编号用 `#N` 格式（GitHub 自动渲染超链接）
3. 描述中文，技术术语保留英文
4. 多行 TODO：首行 Issue 引用，后续行缩进说明背景
5. 禁止无 Issue 编号的裸 TODO，如 `// TODO: fix this`

示例：

```java
// TODO(#42): 实现指数退避重试逻辑
//  当前在 RocketMQ 消费失败时直接抛出异常，
//  需要改为 ExponentialBackOff 策略提高成功率。
```

## 7. PR Review 检查清单

PR 评审时逐项检查：

| # | 检查项 | 层级 | 严重度 | 判定方法 |
|---|--------|------|--------|----------|
| 1 | L3 层文件是否有类级职责 Javadoc？ | L3 | blocking | 检查类声明上方是否有 `/**` 注释块 |
| 2 | L3 层方法级是否有 `@param`/`@return`（有返回值时）？ | L3 | blocking | 检查 L3 层 service/controller 方法签名上方 |
| 3 | 是否存在空骨架注释（有标签无内容）？ | L3 | blocking | 搜索 `@param \w+\s*\*/` 或 `@return\s*\*/` 模式 |
| 4 | 是否存在被注释掉的代码块？ | L3 | blocking | 搜索以 `//` 或 `/*` 开头但内容是代码而非说明的行 |
| 5 | TODO 是否都关联了 Issue 编号？ | L3 | blocking | 搜索 `TODO:` 检查是否包含 `(#N)` |
| 6 | 中英文混用？ | L2 | non-blocking | 检查中文描述中是否有未转义的英文技术描述 |
| 7 | 是否存在 `@author` 标签？ | L2 | non-blocking | 搜索 `@author` |
| 8 | L2 层 Vue 组件是否有职责说明？ | L2 | non-blocking | 检查组件的 `<script lang="ts">` 块 |
| 9 | L2 层 Service 接口是否有类级 Javadoc？ | L2 | non-blocking | 检查 Service 接口文件上方 |

### 判定标准

各检查项通过/未通过标准：

**1. L3 层文件是否有类级职责 Javadoc？**
- 通过：类声明正上方存在 `/** ... */`，包含至少 10 中文字符职责描述，包含 `@since x.x.x`
- 未通过：无 Javadoc；或 Javadoc 仅 `@since` 无职责描述；或描述不足 10 中文字符

**2. L3 层方法级是否有 @param/@return（有返回值时）？**
- 通过：方法声明上方 `/** ... */`，每个参数有 `@param` 且描述非空；有返回值时包含 `@return` 且描述非空
- 未通过：缺少 `@param`（参数 > 0 时）；有返回值但缺少 `@return`；标签后描述为空（空骨架）

**3. 是否存在空骨架注释（有标签无内容）？**
- 通过：所有 Javadoc 标签（@param/@return/@throws）描述字段均有实际内容（至少 2 中文字符或英文术语）
- 未通过：存在 `@param name` 后直接换行或 `@param name */`；存在 `@return` 后直接换行或 `@return */`
- 判定：`grep -rn '@param\s\+\w\+\s*\*/'` 和 `grep -rn '@return\s*\*/'` 扫描目标文件

**4. 是否存在被注释掉的代码块？**
- 通过：源文件中无被注释的 Java/TS 代码行
- 未通过：存在被注释掉的方法调用、变量声明、逻辑代码（含 `public`、`private`、`function`、`if`、`for`、`import`、`const`、`let`、`var` 等关键字）
- 例外：TODO 中包含的代码片段引用不属于注释掉的代码块
- 判定：`grep -rn '^\s*//\s*\(public\|private\|protected\|import\|const\|let\|var\|function\)'`

**5. TODO 是否都关联了 Issue 编号？**
- 通过：每个 `TODO` 后紧跟 `(#数字):` 或 `(#TODO-数字):` 格式
- 未通过：存在 `TODO:` 或 `TODO(描述)` 不包含 `(#数字)`
- 判定：`grep -rn 'TODO[^#]'` 并手动检查

**6. 中英文混用？**
- 通过：Javadoc 描述纯中文（仅技术术语保留英文，如 JWT、RocketMQ、token、cache、DTO、VO、API）；或纯英文描述
- 未通过：中英文混写，如"获取 user by id"、"根据 Id 查询"
- 判定：目视检查

**7. 是否存在 @author 标签？**
- 通过：无 `@author` 标签
- 未通过：任何文件包含 `@author`
- 判定：`grep -rn '@author'`

**8. L2 层 Vue 组件是否有职责说明？**
- 通过：组件文件包含 `<script lang="ts">` 块（非 setup）并在 `/** */` 中描述职责；或文件顶部 `//` 行注释说明职责
- 未通过：无任何形式的职责说明
- 例外：极简组件（仅 1 标签的包装组件）可通过 `/^<template>/` 豁免

**9. L2 层 Service 接口是否有类级 Javadoc？**
- 通过：接口声明上方存在 `/** ... */` 块注释，包含职责描述
- 未通过：接口声明上方无 Javadoc，或仅有单行 `//` 注释

### 验证示例

真实代码抽样验证展示判定过程：

**样本 1：`config/CorsConfig.java`**（Config 类，L3 层）
- 检查项 1（类级 Javadoc）：**未通过** — 无 Javadoc，仅 `@Configuration`
- 检查项 3（空骨架注释）：**通过**
- 检查项 4（注释代码块）：**通过**
- 检查项 5（TODO 编号）：**通过**
- 检查项 7（@author）：**通过**

**样本 2：`config/JwtTokenProvider.java`**（JWT 核心类，L3 层）
- 检查项 1（类级 Javadoc）：**未通过** — 无 Javadoc，仅 `@Component`
- 检查项 3（空骨架注释）：**通过**
- 检查项 4（注释代码块）：**通过**
- 检查项 5（TODO 编号）：**通过**
- 检查项 7（@author）：**通过**

**样本 3：`controller/auth/AuthController.java`**（Controller 类，L3 层）
- 检查项 1（类级 Javadoc）：**通过**
- 检查项 3（空骨架注释）：**通过**
- 检查项 4（注释代码块）：**通过**
- 检查项 5（TODO 编号）：**通过**
- 检查项 7（@author）：**通过**

## 8. 注释维护机制

### 8.1 维护流程

注释质量遵循"先同步、后清理、再抽查"三层维护策略：

| 层级 | 频率 | 触发条件 | 执行人 | 输出 |
|------|------|----------|--------|------|
| L1 同步 | 每次 PR | PR 提交时 | PR 作者 + Review 者 | PR Review 检查清单 |
| L2 清理 | 每月 | 每月首个工作日 | 值班开发者 | TODO 清理报告 |
| L3 抽查 | 每季度 | 季度末 | 指定审查人 | 漂移分析报告 |

各层级详细操作见 `docs/comment-maintenance-guide.md`。

### 8.2 L2 TODO 清理流程

每月执行一次：

1. **扫描**：搜索所有 TODO
   - 后端：`grep -rn 'TODO' springboot/src/main/java/ --include='*.java'`
   - 前端：`grep -rn 'TODO' app-vue/src/ --include='*.{ts,vue}'`
2. **分类**：
   - `TODO(#N):` — 有效项，保留
   - `TODO:` 无编号 — 评估后补 Issue 编号或删除
   - 已完成 TODO — 移除注释
3. **更新**：检查关联 Issue 状态，已关闭的 TODO 同步移除
4. **报告**：项目周会通报清理结果

### 8.3 L3 季度抽查机制

每季度末抽查一次，轮流覆盖不同模块：

1. **抽取样本**：
   - 后端：L3 层文件（Controller/Config/异常体系/JWT/AOP）中随机 5 个
   - 前端：Vue 组件中随机 5 个
2. **检查项**：
   - 空骨架注释（`@param`/`@return` 无描述）
   - 注释掉的代码块
   - 类级 Javadoc 与代码实际行为一致性（漂移检测）
   - L0-L3 层级要求满足度
3. **评分**：每文件通过/未通过，计算通过率
4. **修复**：通过率 < 80% 时创建 Issue 安排专项修复

**首次抽查范围**：v1.7 阶段 28 补充注释的 Controller 和 Config 类。
**首次抽查时间**：2026-08-01（下季度首日）。
