# Requirements: Vue + Spring Boot 管理后台

**Defined:** 2026-05-06
**Core Value:** 提供清晰、高效的企业级管理后台界面，通过夏日海滩风主题营造清爽专业的视觉体验。

## v1.5 Requirements

### 代码质量工具链 (TOOL)

- [ ] **TOOL-01**: 配置 EditorConfig 统一编辑器设置
- [ ] **TOOL-02**: 升级 ESLint 到 10.x flat config 并集成 typescript-eslint + eslint-plugin-vue
- [ ] **TOOL-03**: 集成 @stylistic/eslint-plugin 替代已废弃的 ESLint 核心风格规则
- [ ] **TOOL-04**: 配置 Stylelint 17.x 对 CSS 文件进行静态分析
- [ ] **TOOL-05**: 集成 ArchUnit 1.4.0 定义包结构和依赖规则（如"Controller 不能直接调用 Mapper"）
- [ ] **TOOL-06**: 集成 Checkstyle (Gradle 内置) 统一 Java 代码风格
- [ ] **TOOL-07**: 集成 PMD (Gradle 内置) 检测源码异味
- [ ] **TOOL-08**: 集成 SpotBugs 4.9.3 检测字节码级 bug
- [ ] **TOOL-09**: 集成 JaCoCo 测试覆盖率统计
- [ ] **TOOL-10**: 集成 Error Prone 2.37.0 编译时错误检测

### 后端包结构重组 (BACKEND)

- [ ] **BACK-01**: 创建 shared/ 通用层，迁移 config/security/aspect/exception/util 等跨域组件
- [ ] **BACK-02**: 按业务域垂直切分后端包结构（shared + user/role/menu/auth/audit/knowledge/seckill/rocketmq/order/monitor）
- [ ] **BACK-03**: 每个业务域内部统一 controller/service/impl/mapper/entity/dto 结构
- [ ] **BACK-04**: 修复 Controller 直接注入 Mapper 的分层违规（UserController、SeckillController 等）
- [ ] **BACK-05**: 合并零散模块（mq/stock/sse）到 seckill/ 域
- [ ] **BACK-06**: 统一 Service 接口+impl 分离（修复 MenuServiceImpl 等放在 service/ 根目录问题）
- [ ] **BACK-07**: 使用 `git mv` 分步移动，每个域迁移后运行 `./gradlew.bat test` 验证

### 配置文件整理 (CONFIG)

- [ ] **CONF-01**: 将 154 行 application.yaml 按关注点拆分为 5 个配置文件（datasource/cache/mq/security/business）
- [ ] **CONF-02**: 使用 spring.config.import 实现配置文件的关注点分离加载
- [ ] **CONF-03**: 消除 application-test.yaml 与主文件的重复内容
- [ ] **CONF-04**: 每次拆分后用 `./gradlew.bat bootRun` 双 profile 启动验证

### 前端目录重组 (FRONTEND)

- [ ] **FRNT-01**: components/ 拆分为 common/layout/business 子目录
- [ ] **FRNT-02**: api/ 创建 modules/ 按业务域组织（auth/user/role/menu/seckill/rocketmq 等）
- [ ] **FRNT-03**: router/ 拆分为 modules/*.ts + guards.ts（按域分模块）
- [ ] **FRNT-04**: 创建 types/ 目录按域组织独立类型定义文件
- [ ] **FRNT-05**: 清理无用脚手架模板组件（HelloWorld.vue、AboutView.vue 等）
- [ ] **FRNT-06**: vite.config.js 迁移为 vite.config.ts
- [ ] **FRNT-07**: 每次移动文件后执行 `npm run build` 验证懒加载路径

### 代码规范统一 (QUALITY)

- [ ] **QUAL-01**: 前端组件/页面命名规范统一（PascalCase 组件、Page 后缀页面）
- [ ] **QUAL-02**: TypeScript 统一使用 interface 定义对象类型，type 仅用于联合类型/工具类型
- [ ] **QUAL-03**: 清理未使用的导入、变量和组件

## v2 待定

暂未定义。

## Out of Scope

| 功能 | 原因 |
|------|------|
| 敏感信息环境变量加固（JWT secret、DB 密码） | 用户要求暂不处理 |
| DTO/VO 分离规范 | 当前规模下手动 toVO() 足够 |
| Spring Boot 多模块拆分 | ~100 Java 文件单模块即可，200+ 后再考虑 |
| 新增业务功能 | 本里程碑仅重构不新增功能 |
| 前端技术栈变更 | 保持 Vue 3 + Vite + Element Plus 不变 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| TOOL-01 | Phase 17 基础设施搭建 | Pending |
| TOOL-02 | Phase 17 基础设施搭建 | Pending |
| TOOL-03 | Phase 17 基础设施搭建 | Pending |
| TOOL-04 | Phase 17 基础设施搭建 | Pending |
| TOOL-05 | Phase 17 基础设施搭建 | Pending |
| TOOL-06 | Phase 17 基础设施搭建 | Pending |
| TOOL-07 | Phase 17 基础设施搭建 | Pending |
| TOOL-08 | Phase 17 基础设施搭建 | Pending |
| TOOL-09 | Phase 17 基础设施搭建 | Pending |
| TOOL-10 | Phase 17 基础设施搭建 | Pending |
| BACK-01 | Phase 18 后端包结构重组 | Pending |
| BACK-02 | Phase 18 后端包结构重组 | Pending |
| BACK-03 | Phase 18 后端包结构重组 | Pending |
| BACK-04 | Phase 18 后端包结构重组 | Pending |
| BACK-05 | Phase 18 后端包结构重组 | Pending |
| BACK-06 | Phase 18 后端包结构重组 | Pending |
| BACK-07 | Phase 18 后端包结构重组 | Pending |
| CONF-01 | Phase 19 配置文件整理 | Pending |
| CONF-02 | Phase 19 配置文件整理 | Pending |
| CONF-03 | Phase 19 配置文件整理 | Pending |
| CONF-04 | Phase 19 配置文件整理 | Pending |
| FRNT-01 | Phase 20 前端目录重组 | Pending |
| FRNT-02 | Phase 20 前端目录重组 | Pending |
| FRNT-03 | Phase 20 前端目录重组 | Pending |
| FRNT-04 | Phase 20 前端目录重组 | Pending |
| FRNT-05 | Phase 20 前端目录重组 | Pending |
| FRNT-06 | Phase 20 前端目录重组 | Pending |
| FRNT-07 | Phase 20 前端目录重组 | Pending |
| QUAL-01 | Phase 21 代码规范统一 | Pending |
| QUAL-02 | Phase 21 代码规范统一 | Pending |
| QUAL-03 | Phase 21 代码规范统一 | Pending |

**Coverage:**
- v1.5 requirements: 31 total
- Mapped to phases: 31
- Unmapped: 0

---

*Requirements defined: 2026-05-06*
*Last updated: 2026-05-06 — Traceability updated with Phase 17-21 mappings*
