# Roadmap: Vue + Spring Boot 管理后台

> **创建日期:** 2026-04-03
> **更新日期:** 2026-05-06
> **当前里程碑:** v1.5 前后端代码重构与目录整理
> **目标:** 重构前后端代码结构，整理代码和配置文件目录，消除技术债务

---

## Milestones

- ✅ **v1.0 基础框架搭建** — Phases 1-4 (shipped 2026-04-02)
- ✅ **v1.1 夏日海滩风主题修复** — Phases 5-6 (shipped 2026-04-03)
- ✅ **v1.2 用户管理模块** — Phases 7-8 (shipped 2026-04-18)
- ✅ **v1.3 RocketMQ 管理功能** — Phases 9-12 (shipped 2026-04-29)
- ✅ **v1.4 Maven→Gradle + Spring Boot 4.1 升级** — Phases 13-16 (shipped 2026-05-06)
- 🚧 **v1.5 前后端代码重构与目录整理** — Phases 17-21 (in progress)

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

---

## 🚧 v1.5 前后端代码重构与目录整理 (In Progress)

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
**Plans**: TBD

### Phase 18: 后端包结构重组
**Goal**: 按业务域垂直切分后端包结构，修复分层违规
**Depends on**: Phase 17（工具链就绪后方可迁移验证）
**Requirements**: BACK-01, BACK-02, BACK-03, BACK-04, BACK-05, BACK-06, BACK-07
**Success Criteria** (what must be TRUE):
  1. `shared/` 通用层包含 config/security/aspect/exception/util/limiter 子包，跨域组件全部迁移到位
  2. 所有业务域包（user/role/menu/auth/audit/knowledge/seckill/rocketmq/order/monitor）结构完整，每个域内 controller/service/impl/mapper/entity/dto 统一
  3. `./gradlew.bat test` 全部通过，无 Mapper XML namespace 或 @MapperScan 断裂
  4. ArchUnit 规则验证通过：Controller 未直接注入 Mapper，分层依赖方向正确
  5. `./gradlew.bat bootRun` 正常启动，运行时所有 API 端点功能无回归
**Plans**: TBD

### Phase 19: 配置文件整理
**Goal**: 拆分 application.yaml 为关注点文件，消除配置冗余
**Depends on**: Phase 18（包结构确定后配置归属才明确）
**Requirements**: CONF-01, CONF-02, CONF-03, CONF-04
**Success Criteria** (what must be TRUE):
  1. 154 行 application.yaml 按关注点拆分为 datasource/cache/mq/security/business 共 5 个独立配置文件
  2. spring.config.import 机制正确加载所有拆分配置文件，无遗漏或加载顺序错误
  3. `./gradlew.bat bootRun` 和 `./gradlew.bat bootRun --args='--spring.profiles.active=test'` 双 profile 启动后配置覆盖完整
  4. application-test.yaml 中与主配置文件重复的内容已消除，仅保留 test profile 特有配置
**Plans**: TBD

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
**Plans**: TBD
**UI hint**: yes

### Phase 21: 代码规范统一
**Goal**: 统一前后端命名规范，清理无用代码和导入
**Depends on**: Phase 18 + Phase 20（目录重组完成后才能全面收敛规范）
**Requirements**: QUAL-01, QUAL-02, QUAL-03
**Success Criteria** (what must be TRUE):
  1. 所有前端组件使用 PascalCase 命名，页面组件统一使用 Page 后缀
  2. TypeScript 类型定义中 interface 仅用于对象类型，type 仅用于联合类型和工具类型
  3. `npm run lint` 零 warning，项目中无不使用的导入、未引用的变量和废弃的脚手架组件
  4. `./gradlew.bat check` 全部通过，无 Checkstyle/PMD 规则违规
**Plans**: TBD
**UI hint**: yes

---

## Progress

**Execution Order:** Phases execute in numeric order: 17 → 18 → 19 → 20 → 21

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 17. 基础设施搭建 | v1.5 | 0/TBD | Not started | - |
| 18. 后端包结构重组 | v1.5 | 0/TBD | Not started | - |
| 19. 配置文件整理 | v1.5 | 0/TBD | Not started | - |
| 20. 前端目录重组 | v1.5 | 0/TBD | Not started | - |
| 21. 代码规范统一 | v1.5 | 0/TBD | Not started | - |

---

*路线图更新: 2026-05-06 — v1.5 milestone phases defined*
