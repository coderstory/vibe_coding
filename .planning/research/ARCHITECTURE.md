# 注释体系结构

**项目：** Vue + Spring Boot 管理后台
**研究日期：** 2026-05-07
**模式：** 生态系统研究（注释体系结构）
**整体置信度：** 高

## 推荐注释架构

### 核心原则

```
注释价值金字塔（从高到低）：
┌─────────────────────────────────────────┐
│  公共 API / 对外接口    ← 强制性注释     │  ← 最高价值
├─────────────────────────────────────────┤
│  配置属性 / 配置类       ← 强制性注释     │
├─────────────────────────────────────────┤
│  复杂业务逻辑            ← 必须注释      │
├─────────────────────────────────────────┤
│  非显而易见的设计决策     ← 必须注释      │
├─────────────────────────────────────────┤
│  类/组件职责             ← 鼓励注释      │
├─────────────────────────────────────────┤
│  简单方法的用途           ← 可选          │
├─────────────────────────────────────────┤
│  Getter/Setter / 自解释代码 ← 不注释    │  ← 最低价值
└─────────────────────────────────────────┘
```

**两条铁律：**
1. **"为什么"优先于"是什么"** —— 代码本身说明"是什么"，注释说明"为什么这么写"
2. **不注释自解释代码** —— `String name` 不需要注释"用户姓名"，但需要注释为什么为 null 时回退到默认值

---

### 层注释定义

#### 第 1 层：配置类与配置文件（强制性注释）

| 文件类型 | 注释深度 | 示例 |
|----------|----------|------|
| YAML 配置 | 按块注释 | 已有良好模式：`# =====` 分区 + 行内说明 |
| `@ConfigurationProperties` | 类 Javadoc + 每个字段 Javadoc | `SeckillProperties` 是标杆（含 `<p>` 说明 + 默认值 + 用途） |
| `@Configuration` Bean 方法 | 方法 Javadoc（用途、返回值含义） | `RocketMQConfig` 是标杆 |
| Gradle build 文件 | 按注释块分组依赖 | `build.gradle.kts` 已有良好模式 |

**YAML 配置注释标准（现有模式保留）：**
```
# ===========================================
# [配置区块名称]
# [一句话说明该区块的用途]
# ===========================================
配置键: 值               # [行内说明：解释该值的作用、取值范围、是否可用环境变量覆盖]
```

**配置属性类注释标准（以 SeckillProperties 为蓝本）：**
```java
/**
 * [配置类作用的一句话说明]
 * <p>
 * 功能说明：
 * - [功能点 1]
 * - [功能点 2]
 * <p>
 * 配置项：
 * - [子配置项 1]: [说明]
 * - [子配置项 2]: [说明]
 */
@ConfigurationProperties(prefix = "xxx")
public class XxxProperties {
    /** [字段作用]，默认值: [值]，[额外说明] */
    private int field = defaultValue;
}
```

#### 第 2 层：公共 API / Controller 层（强制性注释）

**Controller 层注释标准（以 AuthController 为蓝本）：**
```java
/**
 * [控制器名称]
 * 提供 [资源] 的 CRUD 和 [额外操作] RESTful API
 */
@RestController
@RequestMapping("/api/xxx")
public class XxxController {

    // ==================== [子资源 1] 管理 ====================

    /**
     * [HTTP 动作] [资源]
     * [一句话说明做了什么]
     * [特殊说明：分页、筛选、排序等]
     * HTTP: [METHOD /api/xxx/yyy?params]
     */
    @GetMapping("/yyy")
    public ResponseEntity<ApiResponse<...>> method(...) { ... }
}
```

**要点：**
- 类注释：说明该控制器管理的资源和职责边界
- 方法注释：必须标注 HTTP 方法和路径，说明输入参数和返回数据
- 区块分隔符：`// =====` 用于区分不同子资源的管理接口
- **不注释**：显而易见的 Mapper 代理方法、自解释的 `return ResponseEntity.ok(...)`

#### 第 3 层：Service 接口/实现层（鼓励注释）

**Service 层注释策略：**

```java
/**
 * [接口职责的一句话说明]
 */
public interface XxxService {
    /**
     * [做什么] — 鼓励说明参数约束和返回值约定
     * @param param [参数说明，包括 null 安全性]
     * @return [返回值说明，包括 null 可能性]
     */
    ResultType method(ParamType param);
}
```

**Service 实现类注释要点：**
- **类注释**：可选（如果接口注释足够清晰）
- **公共方法**：复杂业务逻辑方法必须注释，标注关键步骤
- **私有方法**：非显而易见时注释
- **不需要注释**：简单的委托调用、参数透传

**示例（对现有 AuthService 的补全建议）：**
```java
/**
 * 认证服务
 * 处理用户登录验证、Token 生成和刷新
 */
@Slf4j
@Service
public class AuthService {
    
    /**
     * 用户密码登录
     * 验证流程：查用户 -> 校验密码 -> 检查禁用 -> 生成双 Token -> 记录审计
     * 注意：用户名不存在和密码错误返回相同消息（防用户枚举）
     */
    public Map<String, Object> login(String username, String password, String ipAddress) { ... }
}
```

#### 第 4 层：Entity / DTO / VO 层（选择性注释）

**注释策略：**
```java
/**
 * [实体类说明：对应的数据库表、业务含义]
 * 在数据库字段命名清晰时，可以省略大部分字段注释
 * 仅在以下情况必须添加字段注释：
 *   1. 字段含义与命名不一致（如 deleted 表示逻辑删除）
 *   2. 字段有特殊约束（如 status 的可选值）
 *   3. 数值字段需要说明单位/范围
 */
@Data
@TableName("sys_xxx")
public class XxxEntity {
    @TableId(type = IdType.AUTO)
    private Long id;               // 不注释 — 自解释

    private String name;           // 不注释 — 自解释

    private Integer status;        // 必须注释 — 需要说明可选值：0=启用, 1=禁用

    private Integer deleted;       // 必须注释 — 与命名不一致，表示逻辑删除
}
```

**DTO/VO 注释规则：**
- 类注释必须有（说明 DTO/VO 的用途和与实体的关系）
- 字段注释仅在有特殊含义时添加
- `ApiResponse` 现有注释是标杆

#### 第 5 层：Mapper / DAO 层（最低注释优先级）

**注释策略：**
- 接口方法签名即文档，通常不需要额外注释
- 仅在以下情况添加注释：
  - 自定义复杂 SQL 的方法（非 MyBatis Plus 自动生成）
  - 多表关联查询
  - 有特殊参数的查询

```java
@Mapper
public interface XxxMapper extends BaseMapper<XxxEntity> {

    /** 注意：返回 null 时上层应处理空值 */
    XxxVO selectUserWithRoleName(@Param("id") Long id);

    /** 仅查询未删除用户（deleted=0 已在 SQL 中过滤） */
    XxxEntity findByUsername(@Param("username") String username);
}
```

#### 第 6 层：前端组件（Vue SFC 注释策略）

```
Vue SFC 注释分布：
<script setup> 
  ├── 组件级别：顶部注释说明组件职责（必要）
  ├── Props / Emits 定义：可选（类型定义即文档，特殊行为需注释）
  ├── 响应式状态：按块注释分组（现有模式好）
  ├── 方法：仅复杂方法需 JSDoc
  └── 生命周期钩子：简单钩子不注释

<template>
  ├── 大区块模板：HTML 注释分隔（必须）
  └── 单行模板：不注释

<style scoped>
  — 不注释（CSS 类名应自解释）
</style>
```

**示例（对 TopicList.vue 的补全建议）：**
```vue
<script lang="ts" setup>
/**
 * Topic 列表管理组件
 * 提供 Topic 的查询、创建、删除操作
 * 依赖：getTopicList / createTopic / deleteTopic API
 */
import { ... }

// ===== 状态管理 =====
const loading = ref(false)
const topicList = ref<TopicVO[]>([])

// ===== 搜索表单 =====
const searchForm = reactive({ keyword: '' })

// ===== 创建对话框 =====
const createDialogVisible = ref(false)
const createForm = reactive<CreateTopicParams>({ ... })

/**
 * 加载 Topic 列表
 * 调用 /api/rocketmq/topics 接口
 */
async function loadData() { ... }

/**
 * 创建新 Topic
 * 先验证名称合法性，再调用创建接口
 * 成功后刷新列表
 */
async function handleCreate() { ... }
</script>
```

#### 第 7 层：前端 API 和 Store（现状保持）

**现有模式已经很好，只需统一：**
- 每个 API 模块文件顶部必须有注释说明该模块管理的资源
- 每个接口函数必须有 `@param` 和功能说明（现有 rocketmq.ts 是标杆）
- Store 的每个 action 必须有用途说明（现有 user.ts 是标杆）
- Router guards 需要有注释说明守卫策略（现有 guards.ts 是标杆）

---

### 文件头约定

**Java 文件头：**
```java
/*
 * 文件名: Xxx.java
 * 描述: [TODO - 只在首次创建时填写，不做长期维护]
 * 
 * 本文件属于 admin-system 项目
 */
package cn.coderstory.springboot.xxx;
```

**Vue/TS 文件头：**
```typescript
/**
 * 文件名: Xxx.vue
 * 描述: [TODO - 只在首次创建时填写]
 */

/**
 * 文件名: Xxx.ts
 * 描述: [TODO - 只在首次创建时填写]
 */
```

**注意：** 文件头注释不做长期维护。文件名和路径本身已经说明了大部分信息。文件头只在首次创建时填写"描述"字段，之后不做要求。

**不作要求的原因是：**
1. 文件名+包路径已经提供了足够的上下文
2. 文件头注释极易过时且无人维护
3. Git log 比文件头注释更能准确说明文件目的

---

### 配置文件的注释组织

**YAML 配置注释层级：**

```
# ===========================================
# [区块标题] — 一级标题（全等号包围）
# [区块说明]
# ===========================================
键:
  子键: 值               # 行内注释

  # ========== [子区块标题] ==========
  # [子区块说明]
  子键2:
    属性: 值             # 行内注释（说明作用、默认值、可选值）
```

**现有 YAML 注释已经很好，需补充的：**
1. 每个配置项注释说明是否需要环境变量覆盖（已有示例：`datasource.yaml` 的 `DB_USER` / `DB_PASSWORD`）
2. 数值配置项标注默认值和单位（已有示例：`expiration: 86400000 # Access Token 过期时间（毫秒），24 小时`）
3. 布尔配置项说明开放/关闭的效果

**Gradle 构建文件注释：**
```kotlin
// ==== 数据库 ====
implementation(libs.mysql.connector)  // MySQL JDBC 驱动
implementation(libs.mybatis.plus)      // MyBatis Plus ORM

// ==== 消息队列 ====
implementation(libs.rocketmq.client)   // RocketMQ 客户端核心库
```

**ESLint 配置注释：**
```javascript
// 仅启用基本规则，代码风格统一放在后续 Phase
```

---

### 跨引用策略

**什么时候需要跨文件/跨类引用注释：**

| 场景 | 引用方式 | 示例 |
|------|----------|------|
| Controller 引用 Service | `@see` 或不引用 | Controller 注释应自己说明职责 |
| Service 调用 Mapper | 不引用 | 依赖注入已说明 |
| 配置类引用 YAML | 在类注释中说明配置前缀 | `@ConfigurationProperties(prefix = "seckill")` |
| 各层 DTO 使用关系 | 在类注释中说明 | `用于 XxxController 的 Yyy 接口返回值` |
| 跨模块调用 | 在方法注释中说明 | `调用 authService.login() 完成认证` |
| AOP 切面 | 注释说明切点和逻辑 | `@Pointcut 表达式` + 类注释说明拦截策略 |

**关键规则：**
1. **不在注释中写文件名/行号** —— 重构时立即过时
2. **不在注释中写 Git 信息** —— Git blame 才是正确的做法
3. **可以写包名和类名** —— IDE 可点击跳转，但不需要 `@see` 标签（除非是外部库）
4. **不要在 Entity 注释中引用 Mapper** —— MyBatis Plus 框架约束已足够

**负示例（不要这样写）：**
```java
// BAD: 文件名会过时
// 参见 XxxController.java 第 42 行的 login() 方法

// BAD: Git 信息不属于注释
// Modified by Zhang San on 2026-01-01
```

**正示例（推荐这样写）：**
```java
// GOOD: 类引用比文件名稳定
// 返回数据格式见 AuthController.login() 的 ApiResponse 约定
```

---

### 注释维护策略

#### 自动化检查

**后端维护机制：**

| 工具 | 可以检查什么 | 是否适合本需求 |
|------|-------------|--------------|
| Checkstyle JavadocMethod | 公共方法缺少 Javadoc | 太严格，会报大量误报 |
| Checkstyle JavadocType | 公共类缺少 Javadoc | 太严格 |
| SpotBugs | 不检查注释 | 不适用 |
| PMD CommentRequired | 可配置哪些元素需要注释 | **建议启用（宽松模式）** |
| ArchUnit | 架构规则 | 不检查注释 |

**推荐方案：不强制自动化注释检查**

理由：
1. 该项目已启用 Checkstyle、PMD、SpotBugs，目前均未配置注释规则，说明团队选择不强制执行
2. 注释的"质量"无法用自动化工具衡量（写了不等于写好）
3. 强行启用会大量误报，降低团队对 lint 工具的信任

**可行的轻量方案：**
```xml
<!-- checkstyle.xml 可选添加，但设为忽略 -->
<!-- <module name="JavadocMethod">
  <property name="severity" value="ignore"/>
</module> -->
```

**前端维护机制：**

ESLint 的 `require-jsdoc` 规则：**不启用**，理由同上。

#### 人工维护机制

| 场景 | 维护方式 | 责任方 |
|------|----------|--------|
| 新增配置属性 | 必须同时写 Javadoc | 开发者 |
| 新增 Controller 接口 | 必须写方法注释 | 开发者 |
| 修改 API 行为 | 更新相关注释 | 开发者 |
| 新增复杂业务逻辑 | 添加关键步骤注释 | 开发者 |
| 清理过时注释 | Code Review 发现 | Reviewer |
| 重构后更新注释 | 重构完成前检查 | 开发者 |

#### PR Review 清单条目

在 `.github/PULL_REQUEST_TEMPLATE.md` 或 Review 清单中添加：

```
注释检查清单：
- [ ] 新增的 `@ConfigurationProperties` 字段是否有 Javadoc？
- [ ] 新增的 Controller 方法是否有 HTTP 方法和路径说明？
- [ ] 新增的复杂业务逻辑是否有关键步骤注释？
- [ ] 删除/修改功能时，同步删除了关联的过时注释？
- [ ] 没有对 Getter/Setter 或自解释代码写废话注释？
```

#### 过时注释治理

**发现过时注释的处理流程：**
1. 在对应代码旁添加 `// TODO(注释过期): [说明]` 
2. 或者在重构时一并清理
3. 不要在同一个 PR 中"只更新注释" —— 注释过时通常伴随着代码变更

---

### 各层文档级别矩阵

| 层 | 文档级别 | 必须注释 | 可选注释 | 不注释 |
|----|----------|----------|----------|--------|
| YAML 配置 | L3 — 详细文档 | 每个区块标题 + 关键属性行内说明 | 显而易见的属性 | — |
| Gradle 构建 | L1 — 关键点 | 依赖分组 | 版本号说明 | 标准配置 |
| `@ConfigurationProperties` | L3 — 详细文档 | 类 Javadoc, 每个字段 Javadoc | 内部静态类 Javadoc | 无 |
| `@Configuration` 类 | L2 — 方法级 | Bean 方法 Javadoc | 类 Javadoc | 自解释方法 |
| Controller | L2 — API 级 | 类+方法 Javadoc | 私有帮助方法 | 自解释小方法 |
| Service Interface | L2 — 接口级 | 公共方法 Javadoc | 类 Javadoc | 参数/返回值自解释 |
| Service Impl | L1 — 关键点 | 复杂逻辑注释 | 类 Javadoc | 简单委托方法 |
| Entity | L0-L1 — 仅特殊字段 | 字段含义模糊时 | 字段有约束/单位时 | 自解释字段 |
| DTO/VO | L1 — 类级 | 类 Javadoc | 特殊字段 | 自解释字段 |
| Mapper | L0 — 最小 | 自定义 SQL 方法 | 参数说明 | 自动生成方法 |
| AOP | L2 — 方法级 | 类 Javadoc + Pointcut 说明 | 通知方法 | — |
| Exception | L0-L1 | 类 Javadoc（可选） | — | 自解释方法 |
| Vue 组件 script | L2 — 逻辑级 | 组件职责 + 复杂方法 | 状态分组 | 自解释模板 |
| Vue 组件 template | L1 — 区块级 | HTML 区块分隔 | 行内分组 | 单行模板 |
| API 模块 | L2 — 接口级 | 函数 Javadoc + @param | 接口类型 | 自解释参数 |
| Store | L2 — Action 级 | Action 用途 | 状态属性 | 计算属性 |
| Router | L1 — 关键点 | Guard 策略 | 路由分组 | — |
| Type/Interface | L1 — 类级 | Interface Javadoc | 字段注释 | 自解释字段 |
| ESLint 配置 | L0-L1 | 非标准规则说明 | — | 标准配置 |

---

### 层注释示例对照

**好的注释（保留或可采纳）：**

| 文件 | 示例位置 | 好的原因 |
|------|----------|----------|
| `SeckillProperties.java` | 类注释 + 每个字段 `/** ... */` | 完整的配置说明、默认值标记、功能列表 |
| `RedissonConfig.java` | `RedissonClient` Bean 方法注释 | 说明了适用场景和集群建议 |
| `AuthController.java` | `login()`, `refresh()`, `logout()` | 简短准确，说明了业务含义 |
| `application.yaml` | 虚拟线程配置注释 | 说明了技术背景和效果 |
| `datasource.yaml` | JDBC URL 连接参数 | 逐参数说明了意义 |
| `rocketmq.ts` | 所有 API 函数 | 一致的 JSDoc 格式，`@param` 齐全 |
| `user.ts` (store) | `login()`, `fetchCurrentUser()` | 方法用途 + 边际情况说明 |
| `request.ts` | `extractErrorMessage()` | 复杂逻辑的分情况说明 |

**差的注释（应避免）：**

| 示例 | 为什么差 | 改进 |
|------|----------|------|
| `// 获取用户列表` 在 `getUserList()` 上 | 方法名已说明 | 删除，或改成说明分页逻辑 |
| `// 用户 ID` 在 `private Long userId` 字段上 | 字段名自解释 | 删除 |
| `// 创建时间` 在 `createTime` 字段上 | 字段名自解释 | 删除，除非说明自动填充策略 |
| `@param username 用户名` | 参数名自解释 | 删除，或改成 `不允许为空` |
| `// 循环遍历列表` 在 for 循环上 | 循环代码自解释 | 删除 |

---

### 当前已有注释状态（用于 Roadmap Phase 编排）

**已有注释的文件（无需增加注释，只需检查一致性）：**

| 文件 | 当前状态 | 需要动作 |
|------|----------|----------|
| `AuthController.java` | 好 | 无 |
| `RocketMQController.java` | 好 | 无 |
| `SeckillProperties.java` | 好（标杆） | 无 |
| `RedissonConfig.java` | 好（标杆） | 无 |
| `RocketMQConfig.java` | 好 | 无 |
| `ApiResponse.java` | 好 | 无 |
| `application.yaml` | 好 | 无 |
| 所有 config YAML | 好 | 无 |
| `build.gradle.kts` | 好 | 无 |
| `auth.ts` (API) | 好 | 无 |
| `rocketmq.ts` (API) | 好（标杆） | 无 |
| `user.ts` (store) | 好（标杆） | 无 |
| `router/*.ts` | 好 | 无 |
| `guards.ts` | 好 | 无 |
| `request.ts` | 好 | 无 |
| `types.ts` | 好 | 无 |

**需要补全注释的文件（按 Phase 优先级）：**

| 优先级 | 文件 | 当前状态 | 需要补充的内容 |
|--------|------|----------|---------------|
| P0 | `BusinessException.java` | 无注释 | 类 Javadoc：说明异常体系和使用方式 |
| P0 | `SecurityConfig.java` | 无注释 | 类注释 + 过滤链策略说明 |
| P0 | `CorsConfig.java` | 无注释 | 类注释 + CORS 策略说明 |
| P0 | `WebConfig.java` | 无注释 | 类注释 + 配置内容说明 |
| P0 | `PasswordEncoder.java` | 无注释 | 类注释：说明加密策略和强度 |
| P1 | `JwtTokenProvider.java` | 无类注释 | 类 Javadoc：说明 Token 结构和验证流程 |
| P1 | `JwtAuthenticationFilter.java` | 无类注释 | 类 Javadoc：说明过滤逻辑和豁免路径 |
| P1 | `AuditAspect.java` | 无类注释 | 类 Javadoc：说明 AOP 审计策略和排除规则 |
| P2 | `AuthService.java` | 有零星注释 | 类注释 + 核心方法注释 |
| P2 | `RocketMQTopicServiceImpl.java` | 无注释 | 类注释 + 复杂逻辑方法注释 |
| P3 | `User.java` (Entity) | 无注释 | 仅 `deleted`/`status` 等特殊字段注释 |
| P3 | `UserMapper.java` | 无注释 | 自定义方法注释 |
| P3 | `RocketMQController.java` | 已有注释 | 检查一致性即可 |

**备注：** P0 为当前 Phase 必须完成，P1 为紧接 Phase，P2 为后续 Phase，P3 为低优先级。

---

### 注释写作规则总结

```
DO:
  1. 写"为什么"——为什么用这种算法？为什么返回 null 而不是空集合？
  2. 写"约束"——参数不能为 null，返回值可能为 null
  3. 写"对比"——为什么选 A 方案不选 B
  4. 写"注意事项"——复杂线程安全、性能影响、API 兼容性
  5. 用中文写注释（项目规范要求）
  6. Controller 方法注释包含 HTTP 方法和路径
  7. 配置属性注释包含默认值和单位

DON'T:
  1. 写重复代码的注释（"i++ 将 i 加 1"）
  2. 写过时的文件头（"作者：张三 最后修改：2024"）
  3. 写显而易见的注释（"// 获取用户信息" 在 getUserInfo() 上）
  4. 写 TODO 留到永远（有 TODO 必须在同 Phase 解决）
  5. 写归档性注释（"这段代码已废弃"——应该直接删代码）
  6. 在 Entity 字段上写和字段名完全一样的注释
  7. 在 Getter/Setter 上写任何注释
```

---

### 来源

- 项目实际代码审查（springboot/ 和 app-vue/ 目录全部文件的注释模式分析）
- Checkstyle 配置分析（`config/checkstyle/checkstyle.xml` —— 未启用注释规则）
- ESLint 配置分析（`eslint.config.js` —— 未启用注释规则）
- 行业最佳实践：Google Java Style Guide 注释部分, Vue Style Guide 文档建议
