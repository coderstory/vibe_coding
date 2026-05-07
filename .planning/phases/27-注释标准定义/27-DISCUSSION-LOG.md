# Phase 27: 注释标准定义 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-07
**Phase:** 27-注释标准定义
**Areas discussed:** L0-L3 层级映射, 语言策略细则, Javadoc/TSDoc 模板, TODO 管理与检查清单

---

## L0-L3 层级映射

### L0 层级（不强制）

| Option | Description | Selected |
|--------|-------------|----------|
| Entity/Model 类（全部） | 实体类字段命名自解释度高，注释价值低 | ✓ |
| Entity + Mapper 接口 | Mapper 方法名已包含 SQL 意图 | |
| Mapper + ServiceImpl | ServiceImpl 只是委托给 Mapper | |

**User's choice:** Entity/Model 类（全部）
**Notes:** 实体字段命名足够自解释，无需额外注释

### L1 层级（建议）

| Option | Description | Selected |
|--------|-------------|----------|
| Mapper 接口 | 仅在 SQL 逻辑非直观时加行内注释 | |
| Mapper 接口 + 工具类私有方法 | Mapper 建议 + 工具类中非公开方法建议注释 | ✓ |
| Mapper + ServiceImpl + 工具类全部 | 偏保守策略 | |
| 你决定 | Claude 自行判断 | |

**User's choice:** Mapper 接口 + 工具类私有方法
**Notes:** 适中策略，兼顾覆盖面和必要性

### L2 层级（鼓励）

| Option | Description | Selected |
|--------|-------------|----------|
| Service 接口 + Vue 组件 | 方法级 @param/@return + 组件职责和 Props/Emits | ✓ |
| Service 接口 + Vue 组件 + Pinia Store | 同上 + Store action 说明 | |
| Service + Vue + Store + Router | 覆盖面最广 | |

**User's choice:** Service 接口 + Vue 组件
**Notes:** 核心业务接口和视图层覆盖即可

### L3 层级（强制）

| Option | Description | Selected |
|--------|-------------|----------|
| Config + Controller + 异常 + JWT + AOP | 所有配置类、Controller 类级+方法级 | |
| 同上 + 工具类 | 增加 PasswordEncoder 等工具类 | |
| 全部 + YAML 段头 | 官方配置文件也需要段头注释 | ✓ |

**User's choice:** 全部 Config + Controller + 异常 + JWT + AOP + YAML 段头
**Notes:** 配置文件段头注释能让新人快速理解每个配置块的作用

---

## 语言策略细则

### Javadoc 描述语言

| Option | Description | Selected |
|--------|-------------|----------|
| 纯中文 + 英文术语保留 | 如"根据用户 ID 查询用户信息" — 术语保留英文 | ✓ |
| 中英双语 | 同一段注释同时包含中英文 | |
| 英文描述 + 中文补充 | 主描述英文，复杂处加中文 | |

**User's choice:** 纯中文 + 英文术语保留
**Notes:** 技术术语如 token/cache/JWT/RocketMQ 保留英文

### @param/@return 语言

| Option | Description | Selected |
|--------|-------------|----------|
| 中文描述 | 如 @param userId 用户ID | ✓ |
| 英文描述 | 如 @param userId user ID | |
| 不描述 | 仅在必要时加 | |

**User's choice:** 中文描述
**Notes:** 与主描述保持一致

### 行内注释规范

| Option | Description | Selected |
|--------|-------------|----------|
| 仅复杂逻辑加行内注释 | 算法/状态机/多线程/RocketMQ 事务处 | ✓ |
| 仅 L3 层类的方法内部 | Controller 方法关键步骤 | |
| 全部按需 | 开发者自行判断 | |

**User's choice:** 仅复杂逻辑加行内注释
**Notes:** 一般代码不写行内注释

### 注释书写风格

| Option | Description | Selected |
|--------|-------------|----------|
| 陈述句（描述做什么） | "根据用户 ID 查询用户信息" | ✓ |
| 祈使句（指示做什么） | "根据用户 ID 查询用户信息" | |

**User's choice:** 陈述句（描述做什么）
**Notes:** 更符合新人阅读习惯

---

## Javadoc/TSDoc 模板

### 类级 Javadoc

| Option | Description | Selected |
|--------|-------------|----------|
| 职责 + 示例 + 作者 | 保留 @author/@since | |
| 仅职责说明 | 清晰描述类的作用 | |
| 职责 + @since | 保留引入版本号 | ✓ |

**User's choice:** 职责 + @since
**Notes:** 版本号有助于追踪引入时机，禁止 @author

### 方法级 Javadoc

| Option | Description | Selected |
|--------|-------------|----------|
| 说明 + @param + @return | 逐个参数 + 有返回值时 | ✓ |
| 仅方法说明 | 按需加标签 | |
| 完整：说明 + 全部标签 | 含 @throws | |
| 你决定 | Claude 自行判断 | |

**User's choice:** 说明 + @param + @return
**Notes:** 标准和必要的覆盖

### Vue 组件注释格式

| Option | Description | Selected |
|--------|-------------|----------|
| // 行注释 + Props 行内 JSDoc | 简单组件用 //，Props 用行内 | ✓ |
| 完整 /** */ 块注释 | 组件顶部一个块说明 | |

**User's choice:** // 行注释 + Props 行内 JSDoc
**Notes:** 轻量且足够

### API/Store 注释格式

| Option | Description | Selected |
|--------|-------------|----------|
| API 函数 JSDoc + Store 行注释 | 函数 @param + Store 职责 | ✓ |
| 仅 API 函数 JSDoc | Store 内部不重复 | |
| 你决定 | Claude 自行判断 | |

**User's choice:** API 函数 JSDoc + Store 行注释
**Notes:** 覆盖 API 调用者和状态管理使用者

---

## TODO 管理与检查清单

### TODO 格式

| Option | Description | Selected |
|--------|-------------|----------|
| // TODO(#123): 描述 | Issue 号写在括号内 | ✓ |
| // TODO: 描述 (ref #123) | Issue 号写在末尾 | |
| // TODO: 描述（不强制关联） | 宽松模式 | |

**User's choice:** // TODO(#123): 描述
**Notes:** GitHub Issue 引用风格

### PR 检查清单

| Option | Description | Selected |
|--------|-------------|----------|
| L3 层 Javadoc 完整性 | 检查是否缺少类级职责 Javadoc | ✓ |
| TODO Issue 编号检查 | TODO 是否关联 Issue 编号 | |
| 无空骨架检查 | 只有标签没有内容的 Javadoc | ✓ |
| 注释代码块检查 | 被注释掉的代码块 | ✓ |

**User's choice:** L3 Javadoc 完整性 + 无空骨架 + 注释代码块检查
**Notes:** TODO 格式检查排除在 PR 检查之外（可通过自动化处理）

### 现有 TODO 处理

| Option | Description | Selected |
|--------|-------------|----------|
| 不回溯，仅约束新 TODO | 现有 TODO 保持不动 | |
| 回溯处理所有 TODO | 全局搜索并批量处理 | ✓ |

**User's choice:** 回溯处理所有 TODO
**Notes:** 需要全局搜索现有 TODO 并补充 Issue 编号或清理

---

## 额外决策：标准落地方式

**User said:** "将相关标准写入claude.md，约束后续的开发行为"

**Notes:** 注释规范不是独立的文档，而是写入 CLAUDE.md 作为项目级可执行约定。Phase 28-30 执行时以 CLAUDE.md 中的规范为校验依据。

---

## Claude's Discretion

- 注释示例模板的具体措辞
- 行内注释"复杂逻辑"的具体判断标准
- TODO 回溯的批量处理方式
- PR 检查清单在评审流程中的具体集成方式

## Deferred Ideas

None — 讨论严格保持在 Phase 27 范围内。
