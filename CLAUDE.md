# CLAUDE.md — AI 编程指南

本项目使用 Claude Code 进行 AI 辅助开发，本文件提供项目规范、沟通规则和常见陷阱指南。

## AI 语言配置

**默认语言：中文**
- 所有回复、注释、错误信息、文档、提交信息均使用中文
- 仅当用户明确要求、代码本身是英文、或技术术语无公认中文翻译时才使用英文
- 语言检测：发现即将输出英文时立即切换为中文（如 `"I'll..."` → `"我将..."`、`"Error:"` → `"错误："`）

## 交互要求

1. **全程中文，简洁直接** — 不总结已做的事，发现问题立即报告
2. **证据支撑** — 声称完成/修复前必须运行验证命令并提供输出
3. **先诊断再回滚** — build 失败先分析根因，不立即回滚
4. **直接执行不问意见** — 确认目标后直接执行，不询问"是否要执行"
5. **目录前置检查** — 执行命令和提交前确认当前目录正确
6. **禁止主动使用 emoji**
7. **禁止调用 gpt 系列模型**

## 工作规范

### Git
- commit 描述部分必须使用中文
- 提交前清理多余/缓存文件
- `.claude/projects/` 下的 memory 文件不提交

### 编辑
- 使用最小唯一字符串作为 `old_string`，避免跨段落匹配误删
- 编辑前先读文件确认精确定位，不确定时用 Write 重写整个文件

---

## 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + TypeScript | 3.5+ / 8+ |
| 后端 | Spring Boot + Java | 4.1.0-RC1 / 26 |
| 构建工具 | Gradle (Kotlin DSL) | 9.5 |
| ORM | MyBatis Plus | 3.5.x |
| 数据库 | MySQL + Flyway | admin_system |
| 缓存 | Redis + Redisson | 8.0+ |
| 消息队列 | RocketMQ | 5.3.2 |
| 认证 | JWT (jjwt) | 0.13.0 |
| 代码质量 | ArchUnit + Checkstyle + PMD + SpotBugs + Error Prone + JaCoCo | — |
| 前端 lint | ESLint 10.x flat config + Stylelint 17.x | — |

**端口**：前端 5173，后端 8080

---

## 项目架构

```
vibe_coding/
├── app-vue/                    # Vue 3 前端
│   └── src/
│       ├── api/modules/        # API 层按业务域拆分
│       ├── components/         # 组件（common/layout/business）
│       ├── router/modules/     # 路由按模块拆分 + guards.ts
│       ├── store/              # Pinia 状态管理
│       ├── views/              # 页面（auth/dashboard/system/seckill）
│       └── composables/        # 组合式函数
│
├── springboot/                 # Spring Boot 后端
│   └── src/main/java/cn/coderstory/springboot/
│       ├── shared/             # 通用层（config/security/aspect/exception/util/limiter）
│       └── {domain}/           # 业务域：user/role/menu/auth/audit/knowledge/seckill/rocketmq/order/monitor
│           ├── controller/
│           ├── service/impl/
│           ├── mapper/
│           ├── entity/
│           └── dto/
│
└── docs/
```

### 认证流程
1. 前端 `POST /api/auth/login` → 返回 `{token, refreshToken, user}`
2. Token 存 `localStorage`，请求携带 `Authorization: Bearer <token>`
3. 后端 `JwtAuthenticationFilter` 解析 token 设置 SecurityContext
4. Token 过期（401）→ 自动调用 `POST /api/auth/refresh` 刷新

### 秒杀系统架构
```
请求 → Redis原子预扣减 → RocketMQ事务消息 → 数据库乐观锁
         ↓                    ↓                    ↓
      第一层限流           削峰填谷              最终一致性
```

---

## 代码风格

### 前端

| 规范 | 规则 |
|------|------|
| 组件 | Composition API（`<script setup lang="ts">`），PascalCase 命名 |
| Props | camelCase，类型定义用 `defineProps<{...}>()` |
| CSS 类 | kebab-case，`<style scoped>` |
| Composables | camelCase + `use` 前缀 |
| 路径 | 优先使用 `@` 别名 |
| API 类型 | 集中在 `api/types.ts` |
| 弹窗 | 添加 `lock-scroll` 和 `append-to-body` |

### 后端

| 规范 | 规则 |
|------|------|
| Controller | `*Controller`，PascalCase 类名 |
| Service | `*Service` 接口 + `*ServiceImpl` 实现 |
| Mapper | `*Mapper`，复杂 SQL 写在 `mapper.xml` |
| 依赖注入 | `@RequiredArgsConstructor` + `private final`（不用 `@Autowired` 字段注入） |
| Lombok | `@Data`, `@Slf4j`, `@RequiredArgsConstructor` |
| 实体 | `@TableName` + `@TableId(type = IdType.AUTO)` + `@TableLogic` |
| 异常 | `BusinessException.notFound()/.badRequest()/.conflict()` |
| 响应 | `ApiResponse<T>` 封装 `{code, message, data}` |
| 审计日志 | AOP 切面 `cn.coderstory.springboot.service..*`，方法前缀推断 CRUD，`ThreadLocal` 防重复，`@Async` 异步写入 |

### 通用规则

- 新增表/字段必须创建 Flyway 迁移脚本
- 后端不用 `@SuppressWarnings`
- API 参数注意空值检查，避免 NPE
- 复用 MyBatis Plus 特性（`LambdaQueryWrapper` 等）

---

## 构建命令

### 前端
```powershell
cd app-vue
npm install
npm run dev     # 开发（端口 5173）
npm run build   # 生产构建
npm run test    # 单元测试
npm run lint    # ESLint + Stylelint
```

### 后端
```powershell
cd springboot
./gradlew.bat bootRun                   # 运行
./gradlew.bat build                     # 编译打包
./gradlew.bat test                      # 测试
./gradlew.bat test --tests "*ClassName" # 单个测试类
./gradlew.bat check                     # 全量代码质量检查
```

---

## 数据库迁移

Flyway 脚本位于 `springboot/src/main/resources/db/migration/`，命名 `V{版本号}__{描述}.sql`。

**规则**：每次变更创建新脚本，禁止修改已执行脚本；使用幂等语句。

配置拆分（`config/` 目录下）：
- `datasource.yaml` / `cache.yaml` / `mq.yaml` / `security.yaml` / `business.yaml`

---

## 常见陷阱

### 1. Windows 中文路径编码
**症状**：Gradle 测试报 `ClassNotFoundException`
**修复**：`build.gradle.kts` 添加 `-Dfile.encoding=GBK`

### 2. localhost DNS 解析失败
**症状**：`UnknownHostException: localhost`
**修复**：所有配置用 `127.0.0.1` 替代 `localhost`

### 3. RocketMQ 5.x API 包路径变更
**症状**：`ClassNotFoundException`，类路径变更（如 `TopicList` 移到 `remoting.protocol.body`）
**注意**：升级 RocketMQ 后检查 API 包路径

### 4. Edit 工具跨段落匹配误删
**规避**：用最小唯一字符串作为 `old_string`，不跨段落匹配

### 5. 依赖版本对齐
**常见**：Lombok/Spring AOP 版本需与 Spring Boot 对齐；`webmvc` vs `web` 一致性
**建议**：使用 `libs.versions.toml` 统一管理

### 6. GSD 命令语法
**注意**：使用冒号 `gsd:execute-phase`（不是 `gsd-execute-phase`）

### 7. Gradle 构建缓存
**修复**：`./gradlew.bat clean build` 或 `--no-build-cache`

### 8. SSE 连接时序（秒杀系统）
**问题**：SSE 连接与请求发送的时序竞争
**修复**：前端先生成 `queueId`，再建立 SSE 连接

---

## 注释规范

### 1. 注释语言策略

- **Javadoc 描述语言：** 描述使用纯中文，技术术语（如 token、cache、JWT、RocketMQ、Redis、MyBatis、DTO、VO）保留英文。禁止中英混用（如"获取 user by id"）。
- **@param/@return 语言：** 标签值使用中文描述。例如 `@param userId 用户 ID`，而非 `@param userId user ID`。
- **行内注释策略：** 仅在复杂逻辑处添加。包括：算法实现、多线程/并发同步、RocketMQ 事务消息状态机、复杂日期计算、正则表达式、SQL 动态拼接。一般代码不写行内注释。
- **书写风格：** 使用陈述句，说明方法"做什么"而非"是什么"。例如"根据用户 ID 查询用户信息"，而非"查询"或"Query user"。

示例（正反例）：
```java
// 正面：陈述句 + 技术术语保留英文
/** 根据用户 ID 查询用户信息，包含角色和权限列表。 */

// 反面：中英混用
/** 根据 user id 获取 user info */

// 反面：祈使句
/** 查询用户信息 */
```

### 2. L0-L3 注释层级

每个 Java 类/接口/文件和 Vue 组件按以下层级确定注释要求：

| 层级 | 约束 | 适用范围 | 说明 |
|------|------|----------|------|
| L0 | 不强制 | Entity/Model 类全部 | 字段命名自解释度高，getter/setter 无业务逻辑 |
| L1 | 建议 | Mapper 接口 + 工具类私有方法 | 仅在 SQL 逻辑非直观或私有方法有业务含义时添加 |
| L2 | 鼓励 | Service 接口 + Vue 组件 | 接口职责和方法说明 + 组件职责和 Props/Emits 说明 |
| L3 | 强制 | Config 全部 + Controller 全部 + 异常体系（BusinessException）+ JWT 相关类（JwtTokenProvider、JwtAuthenticationFilter）+ AOP 切面（AuditAspect）+ YAML 配置文件段头 | 类级+方法级 Javadoc 必须完整 |

判断规则：为每个文件确定层级归属，按层级要求编写注释。L3 层文件缺少 Javadoc 视为 PR 阻塞项。

### 3. Javadoc 模板

#### 类级 Javadoc

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
- `@since` 版本号参考 `CLAUDE.md` 演变历史中的版本号（当前最新 v1.5）

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

#### 方法级 Javadoc

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

#### 禁止行为

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

### 4. TSDoc 模板

#### Vue 组件注释

使用单独的 `<script lang="ts">` 块（非 setup）放置组件级文档（解决 Vue 3 `<script setup>` 编译时丢弃顶层 JSDoc 的问题），`defineProps`/`defineEmits` 使用行内 `/** */` JSDoc。

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

#### API 模块函数注释

使用 `/** */` JSDoc 块注释，包含 `@param`。

示例：
```typescript
/**
 * 获取用户列表。
 * @param params 查询参数（分页 + 筛选条件）
 */
export function getUsers(params: UserQuery): Promise<PageResult<UserVO>>
```

#### Pinia Store 注释

使用 `//` 行注释说明 Store 职责。

示例：
```typescript
// 用户认证状态管理。
// 管理 JWT token 的存储、刷新和用户会话信息。
// 应用启动时自动从 localStorage 恢复登录状态。
export const useAuthStore = defineStore('auth', () => {
  // ...
})
```

#### Router 注释

Vue Router 路由表中每条路由配置添加行注释说明：
```typescript
// 用户管理页面（需要 ADMIN 角色）
path: '/system/users',
component: () => import('@/views/system/users/UserListPage.vue'),
meta: { roles: ['ADMIN'] },
```

### 5. YAML 注释规范

段头注释格式：
```yaml
# ==================== 段落标题 ====================
# 段落说明（该段落的核心配置目的）
# ---------------------------------------------------
```

行内注释格式（确保注释前至少 2 个空格）：
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

### 6. TODO 管理规则

格式：
```
// TODO(#ISSUE_NUMBER): 简短描述
```

规则：
1. 每个 TODO **必须** 关联一个 Issue 编号
2. Issue 编号使用 `#N` 格式（GitHub 自动渲染为超链接）
3. 描述使用中文，技术术语保留英文
4. 多行 TODO：首行写 Issue 引用，后续行缩进说明背景
5. 禁止无 Issue 编号的裸 TODO，如 `// TODO: fix this`

示例：
```java
// TODO(#42): 实现指数退避重试逻辑
//  当前在 RocketMQ 消费失败时直接抛出异常，
//  需要改为 ExponentialBackOff 策略提高成功率。
```

### 7. PR Review 检查清单

在 PR 评审时逐项检查以下内容：

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

#### 判定标准

每条检查项的通过/未通过判定标准：

**1. L3 层文件是否有类级职责 Javadoc？**
- 通过：类声明正上方存在 `/** ... */` 块注释，包含至少 10 个中文字符的职责描述，且包含 `@since x.x.x` 版本号
- 未通过：类声明上方无 Javadoc；或 Javadoc 仅含 `@since` 无职责描述；或描述不足 10 个中文字符

**2. L3 层方法级是否有 @param/@return（有返回值时）？**
- 通过：方法声明上方存在 `/** ... */`，每个参数有 `@param` 标签且描述非空；有返回值时包含 `@return` 标签且描述非空
- 未通过：缺少 `@param`（参数 > 0 时）；有返回值但缺少 `@return`；标签后描述为空（空骨架）

**3. 是否存在空骨架注释（有标签无内容）？**
- 通过：所有 Javadoc 标签（@param/@return/@throws）的描述字段均有实际内容（至少 2 个中文字符或英文术语）
- 未通过：存在 `@param name` 后直接换行或 `@param name */` 的标签；存在 `@return` 后直接换行或 `@return */` 的标签
- 判定方法：执行 `grep -rn '@param\s\+\w\+\s*\*/'` 和 `grep -rn '@return\s*\*/'` 扫描目标文件

**4. 是否存在被注释掉的代码块？**
- 通过：源文件中无被注释的 Java/TS 代码行（以 `//` 开头但内容是代码而非说明的行；或以 `/* ... */` 包裹的代码块）
- 未通过：存在被注释掉的方法调用、变量声明、逻辑代码（判断标准：注释内容包含 Java/TS 关键字如 `public`、`private`、`function`、`if`、`for`、`import`、`const`、`let`、`var`）
- 例外：TODO 注释中包含的代码片段引用不属于注释掉的代码块
- 判定方法：执行 `grep -rn '^\s*//\s*\(public\|private\|protected\|import\|const\|let\|var\|function\)'` 扫描目标文件

**5. TODO 是否都关联了 Issue 编号？**
- 通过：每个 `TODO` 后紧跟 `(#数字):` 或 `(#TODO-数字):` 格式
- 未通过：存在 `TODO:` 或 `TODO(描述)` 而不包含 `(#数字)`
- 判定方法：执行 `grep -rn 'TODO[^#]'` 或 `grep -rn 'TODO'` 并手动检查不符合 `#N` 格式的项

**6. 中英文混用？**
- 通过：Javadoc 描述为纯中文（仅技术术语保留英文，如 JWT、RocketMQ、token、cache、DTO、VO、API）；或纯英文技术描述
- 未通过：中英文混写，如"获取 user by id"、"根据 Id 查询"
- 判定方法：目视检查 Javadoc 描述段落

**7. 是否存在 @author 标签？**
- 通过：无 `@author` 标签出现在任何文件中
- 未通过：任何文件包含 `@author` 标签
- 判定方法：执行 `grep -rn '@author'` 扫描

**8. L2 层 Vue 组件是否有职责说明？**
- 通过：组件文件包含 `<script lang="ts">` 块（非 setup）并在其 `/** */` 中描述组件职责；或文件顶部有 `//` 行注释说明职责
- 未通过：组件文件无任何形式的职责说明
- 例外：极简组件（如仅 1 个标签的包装组件）可通过 `/^<template>/` 规则豁免

**9. L2 层 Service 接口是否有类级 Javadoc？**
- 通过：接口声明上方存在 `/** ... */` 块注释，包含职责描述
- 未通过：接口声明上方无 Javadoc，或仅有单行 `//` 注释

#### 验证示例

以下通过对真实代码的抽样验证展示检查清单的判定过程：

**样本 1：`config/CorsConfig.java`**（Config 类，L3 层）
- 检查项 1（类级 Javadoc）：**未通过** — 类声明上方无任何 Javadoc，仅 `@Configuration` 注解
- 检查项 3（空骨架注释）：**通过** — 无空骨架注释
- 检查项 4（注释代码块）：**通过** — 无被注释掉的代码
- 检查项 5（TODO 编号）：**通过** — 无裸 TODO
- 检查项 7（@author）：**通过** — 无 `@author` 标签

**样本 2：`config/JwtTokenProvider.java`**（JWT 核心类，L3 层）
- 检查项 1（类级 Javadoc）：**未通过** — 类声明上方无 Javadoc，仅 `@Component` 注解
- 检查项 3（空骨架注释）：**通过** — 无空骨架注释
- 检查项 4（注释代码块）：**通过** — 无被注释掉的代码
- 检查项 5（TODO 编号）：**通过** — 无裸 TODO
- 检查项 7（@author）：**通过** — 无 `@author` 标签

**样本 3：`controller/auth/AuthController.java`**（Controller 类，L3 层）
- 检查项 1（类级 Javadoc）：**通过** — 存在类级 Javadoc 包含职责说明
- 检查项 3（空骨架注释）：**通过** — `@param` 和 `@return` 均有描述内容
- 检查项 4（注释代码块）：**通过** — 无被注释掉的代码
- 检查项 5（TODO 编号）：**通过** — 无裸 TODO
- 检查项 7（@author）：**通过** — 无 `@author` 标签

## 演变历史

| 版本 | 内容 | 日期 |
|------|------|------|
| v1.0 | 基础框架（Vue 3 + Element Plus + Spring Boot） | 2026-04-02 |
| v1.1 | 主题修复（海浪动画、弹窗修复、配色调整） | 2026-04-03 |
| v1.2 | 用户管理模块 | 2026-04-18 |
| v1.3 | RocketMQ 管理（Topic/Consumer Group/消息/监控） | 2026-04-29 |
| v1.4 | Maven→Gradle + Spring Boot 4.1 + 依赖升级 | 2026-05-06 |
| v1.5 | 代码重构（质量工具链/包结构重组/配置拆分/规范统一） | 2026-05-07 |

**滞留项**：敏感信息环境变量加固（JWT secret、DB 密码）

---

## 参考

- `docs/seckill/user-guide.md` — 秒杀系统手册
- `docs/browser-automation-guide.md` — 浏览器自动化指南
- `.planning/` — GSD 规划文件

<!-- superpowers-zh:begin (do not edit between these markers) -->
# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文代码审查规范——在保持专业严谨的同时，用符合国内团队文化的方式给出有效反馈
- **chinese-commit-conventions**: 中文 Git 提交规范 — 适配国内团队的 commit message 规范和 changelog 自动化
- **chinese-documentation**: 中文技术文档写作规范——排版、术语、结构一步到位，告别机翻味
- **chinese-git-workflow**: 适配国内 Git 平台和团队习惯的工作流规范——Gitee、Coding、极狐 GitLab、CNB 全覆盖
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发或执行实现计划之前使用——创建具有智能目录选择和安全验证的隔离 git 工作树
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
<!-- superpowers-zh:end -->
