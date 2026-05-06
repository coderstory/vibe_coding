# Requirements: Vue + Spring Boot 管理后台

**Defined:** 2026-05-07
**Core Value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## v1.6 Requirements

### 后端代码清理 (BAC)

- [ ] **BAC-01**: 删除后端 Java 代码中所有未使用的私有方法
- [ ] **BAC-02**: 删除后端 Java 代码中所有未使用的字段和局部变量
- [ ] **BAC-03**: 删除所有未使用的 import 语句
- [ ] **BAC-04**: 合并过于零散的辅助工具类（Util/Helper 类精简）
- [ ] **BAC-05**: 删除未使用的 DTO/VO 类及冗余的实体字段
- [ ] **BAC-06**: 删除注释掉的代码块
- [ ] **BAC-07**: 精简冗余的 Mapper XML 映射配置
- [ ] **BAC-08**: 检查并删除未被任何 Controller 引用的 Service 方法

### 前端代码清理 (FEC)

- [ ] **FEC-01**: 删除未使用的 .vue 组件（脚手架残留组件）
- [ ] **FEC-02**: 删除 Vue/TS 中未使用的变量、导入和类型定义
- [ ] **FEC-03**: 精简 API 层（删除未被任何页面引用的 API 函数）
- [ ] **FEC-04**: 删除未使用的 CSS 样式和重复的样式定义
- [ ] **FEC-05**: 删除注释掉的代码块
- [ ] **FEC-06**: 合并冗余的 TypeScript 类型定义

### 依赖清理 (DEC)

- [ ] **DEC-01**: 删除 build.gradle.kts 中未使用的依赖项
- [ ] **DEC-02**: 修正 Gradle 依赖作用域（implementation vs api vs compileOnly）
- [ ] **DEC-03**: 删除 package.json 中未使用的 npm 包
- [ ] **DEC-04**: 将 devDependencies 中纯类型包移至 @types 规范
- [ ] **DEC-05**: 执行 npm prune 清理 node_modules 中多余的包

### 配置清理 (COC)

- [ ] **COC-01**: 删除 application.yaml 中无 `@Value` 或 `@ConfigurationProperties` 绑定的冗余属性
- [ ] **COC-02**: 清理无引用的 profile 配置
- [ ] **COC-03**: 检查并清理 .env 文件中过期的环境变量

## v2 待定

暂未定义。

## Out of Scope

| 功能 | 原因 |
|------|------|
| 不新增任何业务功能 | 本里程碑仅做清理不做新功能 |
| 不修改数据库 schema | 仅清理代码，不涉及数据层 |
| 不进行框架升级或替换 | 当前框架版本已稳定 |
| 不重构核心架构 | 仅在现有结构内清理 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| BAC-01 | Phase 22 | Pending |
| BAC-02 | Phase 22 | Pending |
| BAC-03 | Phase 22 | Pending |
| BAC-04 | Phase 23 | Pending |
| BAC-05 | Phase 23 | Pending |
| BAC-06 | Phase 22 | Pending |
| BAC-07 | Phase 23 | Pending |
| BAC-08 | Phase 23 | Pending |
| FEC-01 | Phase 24 | Pending |
| FEC-02 | Phase 24 | Pending |
| FEC-03 | Phase 24 | Pending |
| FEC-04 | Phase 24 | Pending |
| FEC-05 | Phase 24 | Pending |
| FEC-06 | Phase 24 | Pending |
| DEC-01 | Phase 25 | Pending |
| DEC-02 | Phase 25 | Pending |
| DEC-03 | Phase 25 | Pending |
| DEC-04 | Phase 25 | Pending |
| DEC-05 | Phase 25 | Pending |
| COC-01 | Phase 26 | Pending |
| COC-02 | Phase 26 | Pending |
| COC-03 | Phase 26 | Pending |

**Coverage:**
- v1.6 requirements: 22 total
- Mapped to phases: 22
- Unmapped: 0

---

*Requirements defined: 2026-05-07*
*Last updated: 2026-05-07 — v1.6 requirements created*
