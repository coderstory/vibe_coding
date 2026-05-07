# Project Research Summary

**Project:** Vue 3 + Spring Boot 管理后台 — v1.7 注释与文档工程
**Domain:** 代码注释与文档规范工程（对已有项目追加注释）
**Researched:** 2026-05-07
**Confidence:** HIGH

## Executive Summary

v1.7 注释工程的目标是降低新人上手门槛，核心手段是对全项目（后端 Java、前端 TypeScript/Vue、YAML 配置、Gradle 构建脚本）补充结构化注释。四项研究报告一致确认：**本项目无需引入新工具**，现有技术栈（Javadoc/TSDoc/KDoc/YAML 注释）已完全覆盖需求。关键的工作量估算约为 **15 人时（P0 范围）**，覆盖 90+ 个文件。

**研究得出的核心建议是：必须先做 Phase 0（注释规范定义），再进入 Phase 1（批量注释编写）。** 四项研究独立得出了同一结论——跳过标准定义直接写注释，将导致风格混乱（中英混用、过度注释/空骨架、团队摩擦）和长期维护成本失控。

**最大风险是"注释漂移"——注释和代码逐渐不一致。** 预防方案是 PR Review Checklist 中增加"注释同步检查"条目，以及合理控制注释量。

## Key Findings

### Recommended Stack

v1.7 不引入新的运行时工具，所有注释格式由现有技术栈原生确定。

**Core technologies (all already in the project):**
- **Javadoc** (`/** */`): Java 后端标准 — JDK 原生，Checkstyle/IDE 均支持
- **TSDoc** (`/** */` + TSDoc tags): TypeScript 前端标准 — Vue 3 + TS 项目首选
- **KDoc** (`/** */` + Markdown): Kotlin DSL 标准 — `build.gradle.kts` 中原生使用
- **YAML `#` comments**: 配置文件 — 需保持段头 + 行内说明的格式
- **Checkstyle Javadoc module** (10.21.4, already configured): 可选开启格式校验规则

**Optional add-ons (not mandatory for v1.7):**
- **eslint-plugin-tsdoc** (0.5.2): 校验 TSDoc 格式，仅校验不强制
- **markdownlint-cli2**: Markdown 文档格式检查
- **Smart-Doc** (3.1.2, downstream): 从 Javadoc 零注解生成 API 文档

**Confirmed excluded:**
- Springfox/Swagger 注解 — 不引入运行时注解
- Knife4j — 对 Spring Boot 4.x 支持不明确
- `@author`/`@since` 标签 — Git blame 提供更准确信息
- PMD CommentRequired — Checkstyle 已有此功能
- 代码内 PlantUML/Mermaid — 放入 `docs/` 目录管理

### Expected Features

**Must have (P0 — v1.7 必须完成，预估 ~15 人时):**

| 任务 | 文件数 | 预估时间 | 复杂度 |
|------|--------|---------|--------|
| YAML 配置注释补全（5 个文件） | 5 | 75min | 低 |
| Controller 补充 `@param`/`@return`（18 个） | 18 | 180min | 低 |
| Service 接口完整 Javadoc（~15 个） | 15 | 225min | 中 |
| Vue 组件 `defineProps`/`defineEmits` JSDoc（40+） | 40 | 320min | 低 |
| API 模块函数补充 `@param`（12 个） | 12 | 60min | 低 |
| Gradle 构建脚本注释 | 2 | 15min | 低 |
| ESLint 配置注释 | 1 | 10min | 低 |

**Should have (P1 — 可选):**
- 测试类方法注释
- 枚举/常量类 `@since` 标签
- `eslint-disable` 理由注释

**Defer to v2+ (P2):**
- 复杂业务逻辑行内注释
- Util 类方法注释
- SpringDoc OpenAPI Swagger 注解整合
- 文档站点生成（Dokka/Typedoc）

### Architecture Approach

注释体系采用 **L0-L3 深度分级 + 四层实施策略**：

**注释价值金字塔（自上而下优先级递减）：**
- **L3 详细文档**: 配置属性类、YAML 配置、公共 API（强制）
- **L2 方法级文档**: Controller 方法、Service 接口、AOP 切面、Vue 组件逻辑（强制/鼓励）
- **L1 关键点文档**: Service 实现复杂逻辑、Gradle 构建、DTO/VO 类级（鼓励）
- **L0 最小文档**: Entity 特殊字段、Mapper 自定义 SQL（按需）

**实施优先级：** Config 层 > Controller 层 > Service 接口层 > Vue 组件层 > Service 实现层 > Entity/DTO/VO 层 > Mapper 层

**核心规则：**
- Service 接口注释优先于实现类注释（调用方只看接口）
- 配置注释独立于代码注释，可最先完成
- 不在注释中写文件名/行号/Git 信息
- 合理复用现有标杆文件模式（`SeckillProperties.java`、`AuthController.java`、`datasource.yaml`、`rocketmq.ts`）

### Critical Pitfalls

1. **注释漂移（Comment Drift）** — 注释与代码不一致。预防: 同 PR 原则 + PR Review 注释同步检查。
2. **过度注释与空 Javadoc 骨架** — IDE 自动生成空 `@param`/`@return` 后不填充。预防: Phase 0 明确"不注释清单"。
3. **注释掩盖烂代码（Deodorant Comments）** — 用长篇注释解释本应重构的代码。预防: 先尝试重构，再考虑加注释。
4. **团队摩擦（Bike-shedding）** — 对注释风格无休止争论。预防: Phase 0 定标准，Review 只 check 合规不讨论标准。
5. **TODO 注释泛滥** — 无 Issue 跟踪的 TODO 成为永久噪声。预防: TODO 必须关联 Issue 编号，禁止裸写。

## Implications for Roadmap

### Phase 0: 注释标准定义（必须，预计 30 分钟会议）
**Rationale:** 四项研究独立指出——跳过标准定义会导致风格混乱和团队摩擦。
**Delivers:** 注释语言策略、层级决策、TODO 规则、空骨架禁令、PR Review 检查清单
**Avoids:** Pitfall 4 (团队摩擦), Pitfall 7 (中英混用)

### Phase 1: 配置 + Controller 层注释（预估 4.5 人时）
**Rationale:** 配置注释独立于业务理解，Controller 已有 Javadoc 只需补充标签。
**Delivers:** 5 个 YAML 配置补全、18 个 Controller `@param`/`@return`、Gradle 脚本注释、ESLint 配置注释

### Phase 2: Service 接口 + Vue 组件层注释（预估 9 人时）
**Rationale:** 需要中度业务理解，投入产出比最高。
**Delivers:** ~15 个 Service 接口完整 Javadoc、40+ Vue 组件 `defineProps`/`defineEmits` JSDoc、12 个 API 模块 `@param` 补充

### Phase 3: Service 实现 + Entity/DTO/VO 层注释（预估 3-5 人时，P1 可选）
**Rationale:** 需要深入实现逻辑理解，建议结合日常开发逐步完成。
**Delivers:** 实现类复杂逻辑行内注释、Entity/DTO/VO 非自解释字段注释、枚举 `@since`

### Phase 4: 维护机制建立（持续，非一次性）
**Rationale:** 注释的长期价值取决于维护，而非初次覆盖率。
**Delivers:** PR Review 注释检查清单、TODO 扫描清理、季度注释漂移抽查

### Phase Ordering Rationale
- Phase 0 先于一切 — 标准定义是执行的前提
- 配置 + Controller 先于 Service — 前者无需业务理解，快速建立节奏
- Service 接口优先于实现 — 调用方依赖接口
- Vue 组件与 API 模块并行 — 无数据依赖
- Entity/Mapper 最后 — 自解释度高，注释价值有限
- 维护机制持续 — 嵌入开发流程，非一次性

### Research Flags
**Needs deeper research:** 无。
**Standard patterns (skip research):** Phase 0-4 全部使用成熟标准。

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Javadoc/TSDoc/KDoc 均为原生标准 |
| Features | HIGH | P0/P1/P2 优先级基于 200+ 文件代码审查 |
| Architecture | HIGH | L0-L3 层级策略基于项目代码审查 + 行业最佳实践 |
| Pitfalls | HIGH | 10 个陷阱均有学术论文或经典著作支撑 |

**Overall confidence:** HIGH

### Gaps to Address
1. **注释实际工时 vs 估算偏差** — Phase 1 完成后复盘实际耗时
2. **Checkstyle Javadoc 强制规则启用决策** — 建议 Phase 0 讨论
3. **Smart-Doc 格式兼容性** — Controller Javadoc 编写时注意格式
4. **注释维护成本具体预算** — Phase 4 建立季度统计

## Sources

### Primary (HIGH confidence)
- Oracle Javadoc Specification
- TSDoc Official Documentation
- Checkstyle 10.x Javadoc Module Documentation
- ESLint flat config documentation
- Spring Boot 4.1 Configuration Documentation

### Secondary (MEDIUM confidence)
- Wang et al., "Characterizing and Detecting Comment-Update Inconsistencies", ACM TOSEM 2023
- Fowler, "Refactoring", Chapter on Comments
- Conventional Comments specification

### Tertiary (LOW confidence)
- eslint-plugin-tsdoc compatibility with Vue SFC

---
*Research completed: 2026-05-07*
*Ready for roadmap: yes*
