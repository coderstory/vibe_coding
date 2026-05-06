# Phase 13 Discussion Log

**Phase:** 13-migration-prep
**Date:** 2026-04-29
**Topic:** 迁移准备灰色地带讨论

## Discussion Summary

### Gray Area 1: Groovy DSL vs Kotlin DSL
- **Options presented:**
  1. Groovy DSL — 语法与 pom.xml 相似，迁移更直接
  2. Kotlin DSL — 类型安全，IDE 支持更好，需要额外学习
- **User selection:** Kotlin DSL
- **Rationale:** 类型安全，IDE 支持更好

### Gray Area 2: 版本管理策略
- **Options presented:**
  1. Version Catalog (Recommended) — Gradle 最佳实践，在 libs.versions.toml 中集中管理版本
  2. buildscript ext — 在 build.gradle 中用 ext 块管理，类似 Maven properties
- **User selection:** Version Catalog (Recommended)
- **Rationale:** Gradle 最佳实践，便于依赖升级

### Gray Area 3: Gradle Wrapper 生成方式
- **Options presented:**
  1. 使用 sdkman/gradle wrapper (Recommended) — 通过 Gradle 命令生成，先安装 Gradle 9.4
  2. 手动创建 — 手动下载 gradle-wrapper.jar 和 properties 文件
- **User selection:** 使用 sdkman/gradle wrapper (Recommended)
- **Rationale:** 推荐的标准方式

## Decisions Captured

| Decision | Value | Rationale |
|----------|-------|-----------|
| DSL 选择 | Kotlin DSL | 类型安全，IDE 支持更好 |
| 版本管理 | Version Catalog (libs.versions.toml) | Gradle 最佳实践 |
| Wrapper 生成 | gradle wrapper 命令 | 推荐的标准方式 |

## Deferred Ideas
- 删除 pom.xml 和 mvnw.cmd — 迁移完成后在 Phase 16 处理
- Gradle 缓存配置优化 — 可后续添加

---

*Discussion completed: 2026-04-29*
