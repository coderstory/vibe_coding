# Roadmap: Vue + Spring Boot 管理后台

> **创建日期:** 2026-04-03
> **更新日期:** 2026-05-07
> **当前里程碑:** v1.6 代码深度清理与优化
> **目标:** 全面清理前后端死代码、无用依赖和冗余配置，提升代码库整洁度

---

## Milestones

- ✅ **v1.0 基础框架搭建** — Phases 1-4 (shipped 2026-04-02)
- ✅ **v1.1 夏日海滩风主题修复** — Phases 5-6 (shipped 2026-04-03)
- ✅ **v1.2 用户管理模块** — Phases 7-8 (shipped 2026-04-18)
- ✅ **v1.3 RocketMQ 管理功能** — Phases 9-12 (shipped 2026-04-29)
- ✅ **v1.4 Maven→Gradle + Spring Boot 4.1 升级** — Phases 13-16 (shipped 2026-05-06)
- ✅ **v1.5 前后端代码重构与目录整理** — Phases 17-21 (shipped 2026-05-07)
- 🚧 **v1.6 代码深度清理与优化** — Phases 22-26 (planning)

---

## Phases

<details>
<summary>✅ v1.0 基础框架搭建 (Phases 1-4) — SHIPPED 2026-04-02</summary>

### Phase 1: 项目初始化与基础框架
**Goal**: Vue 3 + Vite 前端项目初始化，Element Plus 组件库集成
**Plans**: 3 plans

### Phase 2: 布局框架与主题
**Goal**: 管理后台基本布局框架（侧边栏、顶栏、标签页）和夏日海滩风主题
**Plans**: 3 plans

### Phase 3: 登录与认证
**Goal**: 登录页面和 JWT 认证流程
**Plans**: 2 plans

### Phase 4: 基础页面搭建
**Goal**: 用户管理、角色管理、审计日志页面骨架
**Plans**: 3 plans

</details>

<details>
<summary>✅ v1.1 夏日海滩风主题修复与完善 (Phases 5-6) — SHIPPED 2026-04-03</summary>

### Phase 5: 主题颜色与组件修复
**Goal**: 修复弹窗显示问题，调整配色方案，完善 Element Plus 组件样式
**Plans**: 3 plans

### Phase 6: 海浪动画与视觉统一
**Goal**: 添加动态海浪效果，确保视觉风格统一
**Plans**: 2 plans

</details>

<details>
<summary>✅ v1.2 用户管理模块 (Phases 7-8) — SHIPPED 2026-04-18</summary>

### Phase 7: 用户列表页
**Goal**: 用户列表页（筛选+分页+状态切换）及 CRUD API
**Plans**: 2 plans

### Phase 8: 用户详情页
**Goal**: 用户详情页和编辑功能
**Plans**: 2 plans

</details>

<details>
<summary>✅ v1.3 RocketMQ 管理功能 (Phases 9-12) — SHIPPED 2026-04-29</summary>

### Phase 9: Topic 管理
**Goal**: Topic 创建、删除、配置查看功能
**Plans**: 2 plans

### Phase 10: Consumer Group 管理
**Goal**: Consumer Group 消费进度和状态查看
**Plans**: 2 plans

### Phase 11: 消息管理
**Goal**: 消息查询、详情查看、轨迹追踪
**Plans**: 2 plans

### Phase 12: 监控面板
**Goal**: 集群概览、Broker 状态、消息堆积量监控
**Plans**: 2 plans

</details>

<details>
<summary>✅ v1.4 Maven→Gradle + Spring Boot 4.1 升级 (Phases 13-16) — SHIPPED 2026-05-06</summary>

### Phase 13: 迁移准备
**Goal**: 创建 Gradle 项目结构，配置 Wrapper 和基础构建文件
**Plans**: 5 plans

### Phase 14: 依赖升级
**Goal**: 升级 Spring Boot 到 4.1.0-RC1，所有依赖升级到最新兼容版本
**Plans**: 1 plan

### Phase 15: 配置迁移
**Goal**: 迁移 Maven 特殊配置到 Gradle，修复硬编码凭证
**Plans**: 6 plans

### Phase 16: 构建验证与回归测试
**Goal**: 验证构建成功，所有功能回归测试通过
**Plans**: 14 plans

</details>

<details>
<summary>✅ v1.5 前后端代码重构与目录整理 (Phases 17-21) — SHIPPED 2026-05-07</summary>

**Milestone Goal:** 重构前后端代码结构，整理代码和配置文件目录，消除技术债务。建立代码质量工具链作为重构安全网，按业务域垂直切分包结构，拆分配置文件实现关注点分离，统一前端目录组织，收敛命名与代码规范。

### Phase 17: 基础设施搭建
**Goal**: 集成前后端代码质量工具链，搭建重构安全网
**Depends on**: 无（已完成的 v1.4 工作产物）
**Requirements**: TOOL-01, TOOL-02, TOOL-03, TOOL-04, TOOL-05, TOOL-06, TOOL-07, TOOL-08, TOOL-09, TOOL-10
**Success Criteria** (what must be TRUE):
  1. EditorConfig 配置生效，IDE 自动统一缩进风格、换行符和文件编码
  2. `npm run lint` 通过，ESLint 10.x flat config + typescript-eslint + eslint-plugin-vue + @stylistic/eslint-plugin 规则全部生效
  3. `npx stylelint "**/*.css"` 通过，Stylelint 17.x 对 CSS 文件静态分析正常
  4. `./gradlew.bat check` 通过，ArchUnit 包依赖规则 + Checkstyle 代码风格 + PMD 源码异味 + SpotBugs 字节码检查 + Error Prone 编译检测均无报错
  5. `./gradlew.bat test jacocoTestReport` 成功生成 JaCoCo 测试覆盖率报告
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07

### Phase 18: 后端包结构重组
**Goal**: 按业务域垂直切分后端包结构，修复分层违规
**Depends on**: Phase 17
**Requirements**: BACK-01, BACK-02, BACK-03, BACK-04, BACK-05, BACK-06, BACK-07
**Success Criteria** (what must be TRUE):
  1. `shared/` 通用层包含 config/security/aspect/exception/util/limiter 子包，跨域组件全部迁移到位
  2. 所有业务域包（user/role/menu/auth/audit/knowledge/seckill/rocketmq/order/monitor）结构完整
  3. `./gradlew.bat test` 全部通过，无 Mapper XML namespace 或 @MapperScan 断裂
  4. ArchUnit 规则验证通过：Controller 未直接注入 Mapper，分层依赖方向正确
  5. `./gradlew.bat bootRun` 正常启动，运行时所有 API 端点功能无回归
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07

### Phase 19: 配置文件整理
**Goal**: 拆分 application.yaml 为关注点文件，消除配置冗余
**Depends on**: Phase 18
**Requirements**: CONF-01, CONF-02, CONF-03, CONF-04
**Success Criteria** (what must be TRUE):
  1. 154 行 application.yaml 按关注点拆分为 datasource/cache/mq/security/business 共 5 个独立配置文件
  2. spring.config.import 机制正确加载所有拆分配置文件，无遗漏或加载顺序错误
  3. `./gradlew.bat bootRun` 和 `./gradlew.bat bootRun --args='--spring.profiles.active=test'` 双 profile 启动后配置覆盖完整
  4. application-test.yaml 中与主配置文件重复的内容已消除，仅保留 test profile 特有配置
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07

### Phase 20: 前端目录重组
**Goal**: 按域组织 components/api/router/types 目录，清理无用代码
**Depends on**: 技术上独立，建议在 Phase 18-19 后执行以保持前后端命名一致
**Requirements**: FRNT-01, FRNT-02, FRNT-03, FRNT-04, FRNT-05, FRNT-06, FRNT-07
**Success Criteria** (what must be TRUE):
  1. components/ 拆分为 common/layout/business 三个子目录，所有组件归类正确
  2. api/modules/ 按业务域（auth/user/role/menu/seckill/rocketmq 等）组织，统一从 index.ts 导出
  3. router/ 拆分为 modules/*.ts + guards.ts，7KB 单一 index.ts 消除
  4. `npm run build` 成功通过，所有懒加载路径正确无 404 或白屏
  5. 完整登录流程正常，核心业务页面（用户管理、角色管理、RocketMQ 监控、秒杀）均可正常访问
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07
**UI hint**: yes

### Phase 21: 代码规范统一
**Goal**: 统一前后端命名规范，清理无用代码和导入
**Depends on**: Phase 18 + Phase 20
**Requirements**: QUAL-01, QUAL-02, QUAL-03
**Success Criteria** (what must be TRUE):
  1. 所有前端组件使用 PascalCase 命名，页面组件统一使用 Page 后缀
  2. TypeScript 类型定义中 interface 仅用于对象类型，type 仅用于联合类型和工具类型
  3. `npm run lint` 零 warning，项目中无不使用的导入、未引用的变量和废弃的脚手架组件
  4. `./gradlew.bat check` 全部通过，无 Checkstyle/PMD 规则违规
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07
**UI hint**: yes

</details>

---

## 🚧 v1.6 代码深度清理与优化 (Planning)

**Milestone Goal:** 全面清理前后端死代码（未使用方法/字段/导入/组件/API），清理无用依赖和冗余配置，提升代码库整洁度。不新增业务功能，不修改数据库 schema，不重构核心架构。

### Phase 22: 后端死代码清理
**Goal**: 后端 Java 代码中所有死代码（未使用方法/字段/导入/注释块）被安全移除，编译和测试零回归
**Depends on**: v1.5 产物（Phase 21 代码规范统一完成后代码基线稳定）
**Requirements**: BAC-01, BAC-02, BAC-03, BAC-06
**Success Criteria** (what must be TRUE):
  1. `./gradlew.bat build -x test` 编译通过，删除未使用方法后无编译错误
  2. `./gradlew.bat check` 全部通过，PMD/SpotBugs/Checkstyle 无新增告警
  3. 所有已删除的方法、字段、导入在项目中无残留引用（IDE 全局搜索确认）
  4. 项目中无注释掉的代码块残留（grep/IDE 检查确认）
  5. `./gradlew.bat bootRun` 启动正常，所有现有 API 端点功能无回归
**Plans**: TBD

### Phase 23: 后端结构体优化
**Goal**: 后端代码结构与目录重构——合并零散工具类、拆分臃肿大类、删除无用 DTO/VO/实体字段、精简 Mapper XML、移除未引用的 Service 方法；将目录从 domain-first 重构为 type-first（`controller/seckill/` `service/seckill/` 等）
**Depends on**: Phase 22（死代码清理后剩余代码引用关系清晰）
**Requirements**: BAC-04, BAC-05, BAC-07, BAC-08, BAC-09, BAC-10
**Success Criteria** (what must be TRUE):
  1. 合并后工具类（Util/Helper）的公共调用点功能无变化
  2. 拆分后的大类各子类职责单一清晰，`./gradlew.bat build` 编译通过
  3. 删除无用 DTO/VO 和冗余实体字段后编译通过
  4. Mapper XML 精简后所有现有数据库查询正常返回结果
  5. 删除未引用 Service 方法后所有现有 Controller 接口功能完整
  6. 目录已从 `{domain}/controller/` 转为 `controller/{domain}/`，所有 package 声明和 import 引用同步更新
  7. `git mv` 分步移动，每步 `./gradlew.bat build` 验证，无包断裂
  8. `./gradlew.bat test` 全部通过，测试覆盖率不低于清理前水平
**Plans**: TBD

### Phase 24: 前端代码清理
**Goal**: 前端代码精简——移除无用组件/API/CSS/注释，合并冗余类型定义，确保构建和 lint 零告警
**Depends on**: Phase 22（前后端清理可并行，但后端先开始以建立清理模式）；技术上独立于 Phase 23
**Requirements**: FEC-01, FEC-02, FEC-03, FEC-04, FEC-05, FEC-06
**Success Criteria** (what must be TRUE):
  1. `npm run build` 成功通过，删除无用组件后构建产物无 404 或白屏
  2. `npm run lint` 零 warning，无未使用变量、导入或类型定义告警
  3. 所有现有页面（用户管理、角色管理、RocketMQ 监控、秒杀等）可正常访问和交互
  4. 合并冗余类型定义后 `npx vue-tsc --noEmit` 零错误
  5. CSS 清理后页面展示效果与清理前视觉一致（截图对比验证）
**Plans**: TBD
**UI hint**: yes

### Phase 25: 依赖清理
**Goal**: Gradle 和 npm 依赖精简——移除未用依赖、修正作用域、规范化 @types 包、清理 node_modules
**Depends on**: Phase 23 + Phase 24（前后端代码清理完成后才能准确判定哪些依赖未使用）
**Requirements**: DEC-01, DEC-02, DEC-03, DEC-04, DEC-05
**Success Criteria** (what must be TRUE):
  1. `./gradlew.bat build` 编译通过，移除未用依赖后无编译或运行时错误
  2. Gradle 依赖作用域修正后（api→implementation、添加 compileOnly 等）编译通过且依赖传递正确
  3. `npm run build` 成功，移除未用 npm 包后功能无回归
  4. `@types/*` 包均位于 devDependencies，生产构建 `npm run build -- --mode production` 不包含类型包
  5. `npm prune` 执行后 node_modules 体积减小且 `npm run dev` 能正常启动
**Plans**: TBD

### Phase 26: 配置清理
**Goal**: 配置文件精简——移除 application.yaml 中无引用的属性、清理无引用 profile 和环境变量
**Depends on**: Phase 23（后端代码清理完成后能准确判定 @Value/@ConfigurationProperties 引用）
**Requirements**: COC-01, COC-02, COC-03
**Success Criteria** (what must be TRUE):
  1. 删除 application*.yaml 中无 Java 代码引用的属性后 `./gradlew.bat bootRun` 启动正常
  2. 清理无引用 profile 后 `./gradlew.bat bootRun --args='--spring.profiles.active=test'` 正确加载测试配置
  3. `.env` 清理后前后端环境变量加载完整，无缺失或冗余
  4. 配置变更后 dev 和 test 环境启动验证全部通过，核心业务流程无异常
**Plans**: TBD

---

## Progress

**Execution Order:** Phases execute in numeric order: 22 → 23 → 24 → 25 → 26

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 17. 基础设施搭建 | v1.5 | 1/1 | ✅ Completed | 2026-05-07 |
| 18. 后端包结构重组 | v1.5 | 1/1 | ✅ Completed | 2026-05-07 |
| 19. 配置文件整理 | v1.5 | 1/1 | ✅ Completed | 2026-05-07 |
| 20. 前端目录重组 | v1.5 | 1/1 | ✅ Completed | 2026-05-07 |
| 21. 代码规范统一 | v1.5 | 1/1 | ✅ Completed | 2026-05-07 |
| 22. 后端死代码清理 | v1.6 | 0/1 | Not started | - |
| 23. 后端结构体优化 | v1.6 | 0/1 | Not started | - |
| 24. 前端代码清理 | v1.6 | 0/1 | Not started | - |
| 25. 依赖清理 | v1.6 | 0/1 | Not started | - |
| 26. 配置清理 | v1.6 | 0/1 | Not started | - |

---

*路线图更新: 2026-05-07 — v1.6 roadmap created*
