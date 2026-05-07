# Requirements: Vue + Spring Boot 管理后台

**Defined:** 2026-05-07
**Core Value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## v1.7 Requirements

### 注释标准定义

- [ ] **STD-01**: 确定注释语言策略（中文注释 + 英文技术术语保留）
- [ ] **STD-02**: 定义 L0-L3 注释层级规则（什么必须/禁止/可选注释）
- [ ] **STD-03**: 制定 TODO 管理规则（必须关联 Issue 编号）
- [ ] **STD-04**: 明确 Javadoc/TSDoc 模板规范（禁止空骨架、禁止`@author`/`@since`）
- [ ] **STD-05**: 制定 PR Review 注释检查清单

### 配置文件注释

- [ ] **CFG-01**: datasource.yaml 补充段头注释和关键属性行内说明
- [ ] **CFG-02**: cache.yaml 补充段头注释和关键属性行内说明
- [ ] **CFG-03**: mq.yaml 补充段头注释和关键属性行内说明
- [ ] **CFG-04**: security.yaml 补充段头注释和关键属性行内说明
- [ ] **CFG-05**: business.yaml 补充段头注释和关键属性行内说明
- [ ] **CFG-06**: application.yaml 补充整体描述和各 Profile 说明
- [ ] **CFG-07**: build.gradle.kts 和 libs.versions.toml 补充构建块注释
- [ ] **CFG-08**: ESLint flat config 和 Stylelint 配置补充注释

### 后端 Java 代码注释

- [ ] **BKND-01**: 所有 Controller 类补充类级 Javadoc（职责说明）和方法级 `@param`/`@return`
- [ ] **BKND-02**: 配置类（`@ConfigurationProperties`）补充类 Javadoc 和字段说明
- [ ] **BKND-03**: `SecurityConfig`、`CorsConfig`、`WebConfig` 等安全/Web 配置类补充注释
- [ ] **BKND-04**: `BusinessException` 异常体系补充类级和枚举值注释
- [ ] **BKND-05**: `JwtTokenProvider`、`JwtAuthenticationFilter` 补充类级 + 方法级 Javadoc
- [ ] **BKND-06**: `AuditAspect` AOP 切面补充注释
- [ ] **BKND-07**: `PasswordEncoder` 等工具类补充类级 Javadoc
- [ ] **BKND-08**: ~15 个 Service 接口补充完整 Javadoc（职责 + 方法说明）

### 前端 Vue/TS 代码注释

- [ ] **FRNT-01**: 所有 Vue 组件补充组件职责注释和 `defineProps`/`defineEmits` JSDoc
- [ ] **FRNT-02**: API 模块函数补充 `@param` 参数说明
- [ ] **FRNT-03**: Pinia Store 文件补充 Store 职责和 action 说明
- [ ] **FRNT-04**: Router 模块补充路由说明和 guard 策略注释

### 注释维护机制

- [ ] **MAINT-01**: 在 PR Review 流程中加入注释同步检查条目
- [ ] **MAINT-02**: 建立 TODO 注释定期清理机制
- [ ] **MAINT-03**: 季度注释漂移抽查

## v2 Requirements

### API 文档站点

- **DOC-01**: 评估 Smart-Doc 或 SpringDoc 集成
- **DOC-02**: 生成 API 文档站点

### 自动化校验

- **DOC-03**: 启用 Checkstyle Javadoc 格式校验规则
- **DOC-04**: 集成 eslint-plugin-tsdoc 格式校验

## Out of Scope

| 功能 | 原因 |
|------|------|
| Swagger/Knife4j 运行时注解 | v1.7 聚焦纯注释，不引入运行时依赖 |
| `@author`/`@since` 标签 | Git blame/log 是更准确的作者/日期信息来源 |
| 代码内 PlantUML/Mermaid 图表 | 图表放入 `docs/` 目录管理 |
| 自动生成文档站点 | 等注释完成后在 v2+ 中评估 |
| 注释覆盖率工具引入 | 超出 v1.7 范围，且现有工具链已足够 |
| 不新增任何业务功能 | 本里程碑仅做注释不做新功能 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| STD-01 ~ STD-05 | Phase 0 | Pending |
| CFG-01 ~ CFG-08 | Phase 1 | Pending |
| BKND-01 ~ BKND-07 | Phase 1 | Pending |
| BKND-08 | Phase 2 | Pending |
| FRNT-01 ~ FRNT-02 | Phase 2 | Pending |
| FRNT-03 ~ FRNT-04 | Phase 2 | Pending |
| MAINT-01 ~ MAINT-03 | Phase 4 | Pending |

**Coverage:**
- v1.7 requirements: 26 total
- Mapped to phases: 26
- Unmapped: 0 ✓

---
*Requirements defined: 2026-05-07*
*Last updated: 2026-05-07 after initial definition*
