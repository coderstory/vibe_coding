# Phase 28: 配置层 + Controller 层注释 — Discussion Log

**Date:** 2026-05-08

## Areas Discussed

### 1. 执行顺序

| Question | Options | Selection |
|----------|---------|-----------|
| Phase 28 执行顺序？ | 配置→代码 / 按模块分组 / 配置和代码并行 | **配置→代码** |
| 配置层拆几个 Plan？ | 一个 Plan / 两个 Plan | **一个 Plan** |
| Java L3 层拆几个 Plan？ | 一个 Plan / 两个 Plan / 多个小 Plan | **一个 Plan** |

**Decisions:**
- D-01: 先配置层（YAML + 构建 + Lint），后 Java 代码
- D-02: 配置层 1 个 Plan
- D-03: Java 层 1 个 Plan
- D-04: 共 2 个 Wave

### 2. 注释粒度

| Question | Options | Selection |
|----------|---------|-----------|
| CRUD @param/@return 粒度？ | 简洁风格 / 详细风格 | **简洁风格** |
| 复杂方法注释？ | 与 CRUD 一致 / 复杂方法可略详 / 通过方法说明区分 | **复杂方法可略详** |
| 私有方法注释？ | 行内注释 / 不注释 / 完整 Javadoc | **行内注释** |

**Decisions:**
- D-05: CRUD 使用简洁风格
- D-06: 复杂方法可略详细，不超过 2 句
- D-07: 私有方法行内注释
- D-08: 方法说明用陈述句

### 3. YAML 风格统一

| Question | Options | Selection |
|----------|---------|-----------|
| 现有注释处理？ | 保留+增量补充 / 统一重写 | **保留+增量补充** |
| YAML 注释深度？ | 统一标准 / 按重要性分级 | **统一标准** |

**Decisions:**
- D-09: 保留现有，增量补充
- D-10: 统一标准

### 4. @since 版本号

| Question | Options | Selection |
|----------|---------|-----------|
| @since 用什么版本？ | 统一 v1.7 / 按原始版本 / 你决定 | **统一 v1.7** |

**Decision:**
- D-11: 统一 `@since 1.7.0`

## Summary

11 条决策（D-01~D-11）已记录在 28-CONTEXT.md。

**Plan 结构:**
- Plan 01 (Wave 1): 配置层 — YAML 配置（6 个）+ 构建文件（2 个）+ Lint 配置（2 个）
- Plan 02 (Wave 2): Java L3 层 — Controller（18 个）+ Config（6 个）+ Security/JWT（3 个）+ Exception/Util/AOP（4 个）
