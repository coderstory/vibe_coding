# Phase 28: 配置层 + Controller 层注释 - Context

**Gathered:** 2026-05-08
**Status:** Ready for planning

<domain>
## Phase Boundary

为配置文件和后端 L3 层 Java 代码添加符合 Phase 27 规范的注释。不引入新工具，不新增业务功能。

**要求（来自 REQUIREMENTS.md）：**
- CFG-01~CFG-08: 配置文件注释（datasource.yaml, cache.yaml, mq.yaml, security.yaml, business.yaml, application.yaml, build.gradle.kts, libs.versions.toml, eslint.config.js, stylelint.config.js）
- BKND-01~BKND-07: 后端 Java L3 层注释（Controller ×18, Config ×6, JWT/Security ×3, Exception/Util/AOP ×4）

</domain>

<decisions>
## Implementation Decisions

### 执行顺序
- **D-01:** 先配置层（YAML + 构建文件 + Lint 配置），后 Java 代码层。配置层简单直接，先快速完成
- **D-02:** 配置层所有文件合并在一个 Plan（Plan 01），不拆分子 Plan
- **D-03:** Java L3 层全部合并在一个 Plan（Plan 02），不拆分子 Plan
- **D-04:** 共 2 个 Wave：Wave 1 = Plan 01（配置层），Wave 2 = Plan 02（Java L3 层）

### 注释粒度
- **D-05:** CRUD Controller 方法的 @param/@return 使用简洁风格，如 `@param userId 用户 ID`、`@return 用户详情 DTO`
- **D-06:** 复杂方法（秒杀下单、Token 刷新、RocketMQ 处理等非 CRUD 逻辑）可略详细，但不能超过 2 句话
- **D-07:** 私有辅助方法使用行内注释即可，不需要完整 Javadoc 模板
- **D-08:** 所有方法级 Javadoc 包含方法说明（陈述句），说明"做什么"而非"如何做"

### YAML 注释风格
- **D-09:** 保留现有注释，增量补充缺失的段头注释和关键属性行内说明
- **D-10:** 所有 YAML 文件按统一标准处理：段头注释 + 关键属性行内说明，不按重要性分级

### @since 版本号
- **D-11:** 所有新加的类级 Javadoc 统一使用 `@since 1.7.0`（当前里程碑版本）

### Claude's Discretion
- Controller 批量 vs 逐一手写：标准 CRUD Controller 可使用批量模式化处理，减少重复工作
- Config 类字段的行内注释深度：字段名自解释度高的可省略注释
- `application.yaml` 的多 Profile 说明结构
- ESLint/Stylelint 配置注释的具体措辞

</decisions>

<specifics>
## Specific Ideas

- 配置文件注释优先关注"为什么配置这个"而非"配置了什么"
- Controller 注释关注 API 的业务语义，而非 HTTP 方法本身
- 密码、密钥等敏感配置所在行添加注释提醒"通过环境变量注入"
- @since 版本统一 v1.7 而非追溯原始创建版本，因为注释是 v1.7 才添加的

</specifics>

<canonical_refs>
## Canonical References

### 要求定义
- `.planning/REQUIREMENTS.md` §CFG-01~CFG-08, BKND-01~BKND-07 — Phase 28 的具体要求
- `.planning/ROADMAP.md` §Phase 28 — 阶段目标和成功标准
- `CLAUDE.md` §注释规范 — Phase 27 定义的注释标准（语言策略、L0-L3 层级、Javadoc 模板、YAML 规范）

### 受影响的文件

**配置层（Plan 01）：**
- `springboot/src/main/resources/config/datasource.yaml`
- `springboot/src/main/resources/config/cache.yaml`
- `springboot/src/main/resources/config/mq.yaml`
- `springboot/src/main/resources/config/security.yaml`
- `springboot/src/main/resources/config/business.yaml`
- `springboot/src/main/resources/application.yaml`
- `springboot/build.gradle.kts`
- `springboot/gradle/libs.versions.toml`
- `app-vue/eslint.config.js`
- `app-vue/stylelint.config.js`

**Java L3 层（Plan 02）：**
- Controller（18 个）：
  - `controller/audit/AuditLogController.java`
  - `controller/auth/AuthController.java`
  - `controller/knowledge/KnowledgeController.java`
  - `controller/menu/MenuController.java`
  - `controller/monitor/MonitorController.java`
  - `controller/order/CartController.java`
  - `controller/order/OrderController.java`
  - `controller/rocketmq/RocketMQController.java`
  - `controller/rocketmq/RocketMQDashboardController.java`
  - `controller/role/RoleController.java`
  - `controller/seckill/ActivityController.java`
  - `controller/seckill/GoodsController.java`
  - `controller/seckill/PreheatController.java`
  - `controller/seckill/ReservationController.java`
  - `controller/seckill/SeckillActivityController.java`
  - `controller/seckill/SeckillController.java`
  - `controller/seckill/SeckillSseController.java`
  - `controller/user/UserController.java`
- Config 类（6 个）：
  - `config/CorsConfig.java`
  - `config/RedissonConfig.java`
  - `config/RocketMQConfig.java`
  - `config/SeckillProperties.java`
  - `config/SecurityConfig.java`
  - `config/WebConfig.java`
- JWT/Security（3 个）：
  - `config/JwtAuthenticationFilter.java`
  - `config/JwtTokenProvider.java`
  - `config/SecurityConfig.java`（同上）
- Exception/Util/AOP（4 个）：
  - `aspect/AuditAspect.java`
  - `config/PasswordEncoder.java`
  - `exception/BusinessException.java`
  - `util/ZstdUtil.java`

### 先前阶段参考
- `.planning/phases/27-注释标准定义/27-CONTEXT.md` — Phase 27 决策，所有 D-01~D-20 直接适用

</canonical_refs>

<code_context>
## Existing Code Insights

### 配置文件状态
- 5 个 config YAML 文件存在零散段头注释，但风格不一致
- application.yaml 已有部分 Profile 说明，但缺少整体描述
- build.gradle.kts 和 libs.versions.toml 注释较少
- ESLint/Stylelint 配置基本无注释

### Java 代码注释现状
- AuthController 已有类级 Javadoc，但多数其他 Controller 和 Config 类缺少类级 Javadoc
- Phase 27 抽样验证发现 CorsConfig 和 JwtTokenProvider 缺少类级 Javadoc
- BusinessException、AuditAspect、JwtAuthenticationFilter 等 L3 层类均缺少完整 Javadoc

### 项目约束
- 不引入新工具（仅使用 Javadoc/`#` 注释）
- 不引入 Swagger/Knife4j 运行时注解
- @author 标签禁止使用
- 注释风格遵循 Phase 27 定义的 CLAUDE.md §注释规范

</code_context>

<deferred>
## Deferred Ideas

None — 讨论严格保持在 Phase 28 范围内。

</deferred>

---

*Phase: 28-配置层-Controller层注释*
*Context gathered: 2026-05-08*
