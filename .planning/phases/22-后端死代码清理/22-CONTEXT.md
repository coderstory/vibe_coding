# Phase 22: 后端死代码清理 - Context

**Gathered:** 2026-05-07
**Status:** Ready for planning

<domain>
## Phase Boundary

安全移除后端 Java 代码中所有死代码 — 未使用的私有方法、未使用的字段和局部变量、未使用的 import 语句、注释掉的代码块。编译和测试零回归。

**要求（来自 REQUIREMENTS.md）：**
- BAC-01: 删除未使用的私有方法
- BAC-02: 删除未使用的字段和局部变量
- BAC-03: 删除未使用的 import 语句
- BAC-06: 删除注释掉的代码块

</domain>

<decisions>
## Implementation Decisions

### 检测与删除策略
- **D-01:** 采用混合模式 — IntelliJ 全局扫描定位 + 按模块分批删除
- **D-02:** 执行顺序：先易后难 — import 优化 → 注释块清理 → 未用字段 → 未用私有方法
- **D-03:** 每完成一个模块/包的清理后立即提交，不跨模块聚合

### 安全与回退机制
- **D-04:** 按模块分批 git 提交，每个 commit 仅包含单个模块/包的清理
- **D-05:** 每批清理后执行 `./gradlew.bat build -x test` 确保编译通过
- **D-06:** 所有清理完成后执行 `./gradlew.bat check` 确保零新增告警 + `./gradlew.bat bootRun` 启动验证

### 注释代码处理规则
- **D-07:** 区分处理 — 明显废弃的注释代码直接删除；含 TODO/FIXME/业务逻辑说明的注释保留；有疑问的先标记再审查
- **D-08:** 纯调试用注释（System.out、日志等被注释的行）一律删除

### Claude's Discretion
- 具体的模块分批顺序
- 哪些 import 属于"明显未使用"的判断标准
- 注释代码的具体分类标准

</decisions>

<specifics>
## Specific Ideas

- "先易后难"原则：import → 注释块 → 字段 → 方法，逐步降低风险
- 秒杀模块（seckill）的清理需额外谨慎，删除前后对比验证

</specifics>

<canonical_refs>
## Canonical References

### 要求定义
- `.planning/REQUIREMENTS.md` §BAC-01~BAC-06 — Phase 22 的具体清理要求
- `.planning/ROADMAP.md` §Phase 22 — 阶段目标和成功标准
- `.planning/PROJECT.md` — 项目约束和关键决策

### 代码质量配置
- `springboot/config/checkstyle/checkstyle.xml` — Checkstyle 规则（import 顺序、未使用检查）
- `springboot/build.gradle.kts` — PMD/SpotBugs 配置

### 代码基线
- `springboot/src/main/java/cn/coderstory/springboot/` — 后端全部 121 个 Java 源文件

</canonical_refs>

<code_context>
## Existing Code Insights

### 代码结构
- 后端共 121 个 Java 源文件，按 10 个业务域 + shared 通用层组织
- 涉及域：audit, auth, knowledge, menu, monitor, order, rocketmq, role, seckill, user, shared

### 清理工具
- IntelliJ IDEA 的 Code → Inspect Code 可全局检测未用声明
- PMD (`./gradlew.bat check`) 的 `UnusedPrivateMethod`/`UnusedLocalVariable` 规则可用
- SpotBugs 的 `DLS_DEAD_LOCAL_STORE` 可检测死变量赋值

### 注意事项
- 部分 import 可能被 Lombok `@Data`/`@Builder` 等注解隐式使用（如 `AccessLevel`）
- 私有方法可能通过反射调用（需全局搜索确认）
- 秒杀模块有 RocketMQ 事务消息 + Redis Lua 复杂流程，需谨慎

</code_context>

<deferred>
## Deferred Ideas

None — 讨论严格保持在 Phase 22 范围内。

</deferred>

---

*Phase: 22-后端死代码清理*
*Context gathered: 2026-05-07*
