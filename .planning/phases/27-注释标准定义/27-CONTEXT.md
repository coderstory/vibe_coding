# Phase 27: 注释标准定义 - Context

**Gathered:** 2026-05-07
**Status:** Ready for planning

<domain>
## Phase Boundary

制定全项目注释规范与标准，使 Phase 28-30 的注释编写有据可依。不引入新工具，不新增业务功能。

**要求（来自 REQUIREMENTS.md）：**
- STD-01: 确定注释语言策略（中文注释 + 英文技术术语保留）
- STD-02: 定义 L0-L3 注释层级规则（什么必须/禁止/可选注释）
- STD-03: 制定 TODO 管理规则（必须关联 Issue 编号）
- STD-04: 明确 Javadoc/TSDoc 模板规范（禁止空骨架、禁止 `@author`/`@since`）
- STD-05: 制定 PR Review 注释检查清单

</domain>

<decisions>
## Implementation Decisions

### L0-L3 层级映射
- **D-01:** L0（不强制）— Entity/Model 类全部。字段命名自解释度高，getter/setter 无业务逻辑
- **D-02:** L1（建议）— Mapper 接口 + 工具类私有方法
- **D-03:** L2（鼓励）— Service 接口 + Vue 组件（含组件职责和 Props/Emits 说明）
- **D-04:** L3（强制）— Config 全部 + Controller 全部（类级+方法级）+ 异常体系（BusinessException 类）+ JWT 相关类（JwtTokenProvider、JwtAuthenticationFilter）+ AOP 切面（AuditAspect）+ YAML 配置文件段头注释

### 语言策略细则
- **D-05:** Javadoc 描述使用纯中文，技术术语（如 token/cache/JWT/RocketMQ）保留英文
- **D-06:** `@param`/`@return` 标签值使用中文描述
- **D-07:** 行内注释仅在复杂逻辑处添加（算法/状态机/多线程/RocketMQ 事务等），一般代码不写
- **D-08:** 注释书写风格使用陈述句（"根据用户 ID 查询用户信息"，而非"查询"）

### Javadoc/TSDoc 模板
- **D-09:** 类级 Javadoc 包含：职责说明 + `@since` 版本号。禁止 `@author`
- **D-10:** 方法级 Javadoc 包含：方法说明 + `@param`（逐个参数）+ `@return`（有返回值时）
- **D-11:** Vue 组件使用 `//` 行注释说明组件职责，`defineProps`/`defineEmits` 使用行内 `/** */` JSDoc
- **D-12:** API 模块函数使用 `/** */` JSDoc 含 `@param`，Pinia Store 使用行注释说明职责
- **D-13:** 禁止空骨架注释（只有标签没有内容的 Javadoc）
- **D-14:** 禁止被注释掉的代码块残留

### TODO 管理
- **D-15:** TODO 格式：`// TODO(#123): 描述` — Issue 号写在括号内
- **D-16:** 现有代码中的 TODO 需回溯处理，全部补充 Issue 编号或清理

### PR Review 检查清单
- **D-17:** PR 评审时检查 L3 层文件是否缺少类级职责 Javadoc
- **D-18:** PR 评审时检查是否存在空骨架注释
- **D-19:** PR 评审时检查是否存在被注释掉的代码块

### 标准落地方式
- **D-20:** 注释规范内容写入 `CLAUDE.md`，作为项目级约束，确保后续所有开发行为遵循该标准。Phase 28-30 执行时 CLAUDE.md 中的规范即为校验依据

### Claude's Discretion
- 注释示例模板的具体措辞
- 行内注释"复杂逻辑"的具体判断标准
- TODO 回溯的批量处理方式
- PR 检查清单在评审流程中的具体集成方式

</decisions>

<specifics>
## Specific Ideas

- "将注释规范写入 CLAUDE.md 约束后续开发行为" — 标准不只是一个文档，而是项目的可执行约定
- TODO 格式借鉴 GitHub Issue 引用风格：`// TODO(#123): 描述`
- 注释风格参考行业实践：好的注释说"为什么"而非"是什么"

</specifics>

<canonical_refs>
## Canonical References

### 要求定义
- `.planning/REQUIREMENTS.md` §STD-01~STD-05 — Phase 27 的具体标准定义要求
- `.planning/ROADMAP.md` §Phase 27 — 阶段目标和成功标准
- `.planning/PROJECT.md` — 项目约束和关键决策

### 受影响的代码/配置基线
- `CLAUDE.md` — 注释规范将写入此文件（根目录）
- `springboot/src/main/java/cn/coderstory/springboot/` — 后端 Java 源文件
- `app-vue/src/` — 前端 Vue/TS 源文件
- `springboot/src/main/resources/config/*.yaml` — 后端配置文件
- `springboot/src/main/resources/application.yaml` — 主配置文件
- `springboot/build.gradle.kts` — Gradle 构建文件
- `springboot/gradle/libs.versions.toml` — 版本目录
- `app-vue/eslint.config.js` — ESLint 配置
- `app-vue/stylelint.config.js` — Stylelint 配置

### 先前阶段参考
- `.planning/phases/22-后端死代码清理/22-CONTEXT.md` — 代码清理决策，确保注释基线干净
- `.planning/phases/23-后端结构体优化/23-CONTEXT.md` — 结构优化决策，确保代码结构在注释前稳定

</canonical_refs>

<code_context>
## Existing Code Insights

### 代码库规模
- 后端：约 121 个 Java 源文件，按 10 个业务域 + shared 通用层组织
- 前端：Vue 3 + TypeScript，components/api/router/store 按域组织
- 配置：5 个 YAML 配置文件（共 124 行）+ Gradle 构建文件 + ESLint/Stylelint 配置

### 现有注释状态
- Config YAML 已有段头注释（如 `# ============ 数据源配置 ============`）但分布不一致
- build.gradle.kts 已有少量注释（如 flyway 版本说明）
- 大多数 Controller/Service 缺少 Javadoc
- Vue 组件缺少组件职责说明

### 项目约束
- Javadoc `@author`/`@since` 标签禁止使用（git blame/log 是更准确的来源）
- 不引入新工具（仅使用现有 Javadoc/TSDoc/YAML `#`）
- 不引入 Swagger/Knife4j 运行时注解

</code_context>

<deferred>
## Deferred Ideas

None — 讨论严格保持在 Phase 27 范围内。

</deferred>

---

*Phase: 27-注释标准定义*
*Context gathered: 2026-05-07*
