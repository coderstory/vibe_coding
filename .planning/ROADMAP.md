# Roadmap: Vue + Spring Boot 管理后台

> **创建日期:** 2026-04-03
> **更新日期:** 2026-05-10
> **当前里程碑:** v1.8 Windows 硬件负载监控 ✅ Completed
> **目标:** Windows 系统硬件负载监控页面，实时展示 CPU/内存/磁盘/网络指标

---

## Milestones

- ✅ **v1.0 基础框架搭建** — Phases 1-4 (shipped 2026-04-02)
- ✅ **v1.1 夏日海滩风主题修复** — Phases 5-6 (shipped 2026-04-03)
- ✅ **v1.2 用户管理模块** — Phases 7-8 (shipped 2026-04-18)
- ✅ **v1.3 RocketMQ 管理功能** — Phases 9-12 (shipped 2026-04-29)
- ✅ **v1.4 Maven→Gradle + Spring Boot 4.1 升级** — Phases 13-16 (shipped 2026-05-06)
- ✅ **v1.5 前后端代码重构与目录整理** — Phases 17-21 (shipped 2026-05-07)
- ✅ **v1.6 代码深度清理与优化** — Phases 22-26 (shipped 2026-05-07)
- ✅ **v1.7 注释与文档工程** — Phases 27-30 (shipped 2026-05-09)
- 🚧 **v1.8 Windows 硬件负载监控** — Phases 31-34 (planning)

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

## ✅ v1.6 代码深度清理与优化 (Completed)

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
**Goal**: Gradle 和 npm 依赖精简——移除未用依赖、修正作用域、移除冗余包
**Depends on**: Phase 23 + Phase 24
**Requirements**: DEC-01, DEC-02, DEC-03, DEC-04, DEC-05
**Success Criteria** (what must be TRUE):
  1. `./gradlew.bat build` 编译通过
  2. Gradle 依赖作用域修正后编译通过（spring-boot-starter-aop 保留显式版本 4.0.0-M2）
  3. `npm run build` 成功
  4. `@types/*` 包均位于 devDependencies（无 @types 包待处理）
  5. `npm prune` 后 node_modules 体积减小（移除 vue-test-utils）
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07

### Phase 26: 配置清理
**Goal**: 配置文件精简——移除 application-test.yaml 中与主配置重复的属性
**Depends on**: Phase 23
**Requirements**: COC-01, COC-02, COC-03
**Success Criteria** (what must be TRUE):
  1. 删除 application*.yaml 中无引用的属性后 bootRun 启动正常
  2. 无引用 profile 清理后 bootRun 多 profile 正常启动
  3. `.env` 清理后环境变量加载完整（项目无 .env 文件，跳过）
  4. 配置变更后 dev 和 test 环境启动验证全部通过
**Plans**: 1 plan
**Status**: ✅ Completed 2026-05-07

---

## ✅ v1.7 注释与文档工程 (Completed)

**Milestone Goal:** 全项目配置文件和前后端代码添加必要注释，降低新人上手门槛。不引入新工具，不新增业务功能。

### Phase 27: 注释标准定义
**Goal**: 全项目注释规范和标准确立，所有注释编写有据可依
**Depends on**: v1.6 产物（Phase 26 配置清理完成后代码基线稳定）
**Requirements**: STD-01, STD-02, STD-03, STD-04, STD-05
**Success Criteria** (what must be TRUE):
  1. 注释语言策略确定：中文注释 + 英文技术术语保留，无中英混用
  2. L0-L3 注释层级规则建立：L0 按需/L1 鼓励/L2 鼓励/L3 强制，明确禁止注释清单和必须注释清单
  3. TODO 必须关联 Issue 编号，禁止裸写 TODO
  4. Javadoc/TSDoc 模板规范确定：禁止空骨架、禁止 `@author`/`@since`
  5. PR Review 注释检查清单完成，在后续 Phase 28-30 评审中实际执行
**Plans**: 3 plans
Plans:
- [x] 27-01-PLAN.md — 注释规范写入 CLAUDE.md（语言策略、L0-L3 层级、Javadoc/TSDoc 模板、YAML 注释、TODO 管理、PR 检查清单）
- [x] 27-02-PLAN.md — TODO 回溯处理（全局搜索并分类、更新格式或清理）
- [x] 27-03-PLAN.md — PR 检查清单细化（判定标准、抽样验证）
**Status**: ✅ Completed 2026-05-08

### Phase 28: 配置层 + Controller 层注释
**Goal**: 所有配置文件和后端 Controller/配置类/工具类方法级注释补全完成
**Depends on**: Phase 27（注释标准定义完成后编写风格有据可依）
**Requirements**: CFG-01, CFG-02, CFG-03, CFG-04, CFG-05, CFG-06, CFG-07, CFG-08, BKND-01, BKND-02, BKND-03, BKND-04, BKND-05, BKND-06, BKND-07
**Success Criteria** (what must be TRUE):
  1. 5 个 YAML 配置文件（datasource/cache/mq/security/business）每份包含段头注释和关键属性行内说明
  2. application.yaml 包含整体描述和各 Profile 说明
  3. build.gradle.kts 和 libs.versions.toml 有构建块注释
  4. ESLint flat config 和 Stylelint 配置有必要的配置项注释
  5. 所有 18+ 个 Controller 包含类级 Javadoc（职责说明）和方法级 `@param`/`@return`
  6. 所有配置类（@ConfigurationProperties）、安全/Web 配置类、JWT 相关类、AOP 切面、异常体系类包含类级 Javadoc 和关键字段/方法说明
**Plans**: 2 plans
Plans:
- [x] 28-01-PLAN.md — 配置文件注释（YAML/Gradle/Lint 配置，Wave 1）
- [x] 28-02-PLAN.md — 后端 Java L3 层注释（Controller/Config/Security/Exception/AOP，Wave 2）
**Status**: ✅ Completed 2026-05-09

### Phase 29: Service 接口 + Vue/TS 注释
**Goal**: 后端 Service 接口完整 Javadoc、前端 Vue/TS 组件级和 API 级注释全部补全
**Depends on**: Phase 28（可并行开始，但工具类注释完成后再梳理 Service 接口签名更清晰）
**Requirements**: BKND-08, FRNT-01, FRNT-02, FRNT-03, FRNT-04
**Success Criteria** (what must be TRUE):
  1. ~15 个 Service 接口包含完整 Javadoc（接口职责 + 每个方法参数/返回值/异常说明）
  2. 所有 Vue 组件包含组件职责注释，`defineProps`/`defineEmits` 包含 JSDoc 类型说明
  3. API 模块函数包含 `@param` 参数说明
  4. Pinia Store 文件包含 Store 职责和 action 说明
  5. Router 模块包含路由说明和 guard 策略注释
**Plans**: 2 plans
Plans:
- [x] 29-01-PLAN.md — 后端 Service 接口 Javadoc 补全
- [x] 29-02-PLAN.md — Vue 组件 + TS/API/Store/Router 注释补全
**Status**: ✅ Completed 2026-05-09
**UI hint**: yes

### Phase 30: 注释维护机制建立
**Goal**: 注释长期维护机制建立，确保注释与代码同步
**Depends on**: Phase 27（标准是维护机制的基础）
**Requirements**: MAINT-01, MAINT-02, MAINT-03
**Success Criteria** (what must be TRUE):
  1. PR Review 模板中包含注释同步检查条目，每个 PR 评审时自动检查
  2. TODO 注释定期清理流程建立，每轮清理后无过期 TODO 残留
  3. 季度注释漂移抽查机制建立，首次抽查范围和时间已确定
**Plans**: 2 plans
Plans:
- [x] 30-01-PLAN.md — PR 检查清单 + TODO 管理流程文档
- [x] 30-02-PLAN.md — CLAUDE.md 注释维护章节更新
**Status**: ✅ Completed 2026-05-09

---

## 🚧 v1.8 Windows 硬件负载监控 (Planning)

**Milestone Goal:** Windows 系统硬件负载监控页面，实时展示 CPU/内存/磁盘/网络指标。通过 OSHI 7.x FFM 库采集硬件数据，REST API + SSE 推送至前端，ECharts 实时可视化。

### Phase 31: 后端采集服务 + REST API
**Goal**: 后端集成 OSHI 7.x FFM 库，实现 CPU/内存/磁盘/网络指标的定时采集，提供 REST API 和环形缓冲区趋势查询
**Depends on**: v1.7 产物（Phase 30 注释维护机制建立完成后代码基线稳定）
**Requirements**: HWM-01, HWM-02, HWM-03, HWM-04, HWM-05, HWM-06, HWM-07, HWM-08, HWM-09, TDD-01, TDD-03
**Success Criteria** (what must be TRUE):
  1. 后端启动后自动以 2s 间隔采集 CPU 使用率（总体和各核）、内存指标（总量/已用/可用）、磁盘容量和分区信息、磁盘 IOPS/读写速度、网络接口吞吐量
  2. `GET /api/monitor/hardware/current` 返回当前所有硬件指标的快照 JSON
  3. `GET /api/monitor/hardware/trend?metric=cpu&range=360` 返回近 1h 环形缓冲区趋势数据
  4. `GET /api/monitor/hardware/system` 返回系统基本信息（OS 版本、运行时间、进程数）
  5. 采集服务通过接口抽象，OSHI 依赖可 mock，核心采集/计算/缓存逻辑有单元测试覆盖
**Plans**: 3 plans
Plans:
- [x] 31-01-PLAN.md — 基础层: OSHI 依赖、DTO 定义、RingBuffer、Service 接口、配置类 (Wave 1)
- [ ] 31-02-PLAN.md — 采集实现: HardwareMetricsServiceImpl + 单元测试 (Wave 2)
- [x] 31-03-PLAN.md — REST API: 3 个端点 + Controller 测试 + 集成测试 (Wave 2)

### Phase 32: SSE 实时推送 + 前端基础页面
**Goal**: 后端通过 SSE 实时推送硬件指标，前端展示 CPU/内存仪表盘、磁盘容量和系统信息
**Depends on**: Phase 31（SSE 推送需要采集服务提供数据）
**Requirements**: HWM-10, HWM-11, HWM-12, HWM-13, HWM-14, HWM-15, HWM-17, HWM-19, TDD-02, TDD-05
**Success Criteria** (what must be TRUE):
  1. 用户打开硬件监控页面后，页面通过 SSE 实时接收硬件指标（每 2s 推送一次）
  2. SSE 连接包含心跳机制，过期连接自动清理，断线自动重连
  3. CPU 使用率以环形图展示，显示核数/型号信息卡片
  4. 内存使用率以环形图展示，显示总量/已用/可用信息
  5. 磁盘分区以进度条列表展示容量使用情况
  6. 系统基本信息卡片展示 OS 版本、运行时间、进程数
  7. SSE 推送服务和前端 SSE composable 设计为可测试的接口抽象
**Plans**: 3 plans
Plans:
- [ ] 32-01-PLAN.md — 后端 SSE 广播服务 + Controller 端点 + 测试 (Wave 1)
- [ ] 32-02-PLAN.md — 前端类型定义 + SSE composable + 单元测试 (Wave 1)
- [ ] 32-03-PLAN.md — 前端页面 (左导航/ECharts 环形图/磁盘进度条/系统信息) (Wave 2)
**UI hint**: yes

### Phase 33: 完整图表 + 趋势
**Goal**: 磁盘 IO/网络吞吐量实时折线图、近 1 小时趋势图，图表组件适配海滩风主题
**Depends on**: Phase 32（图表数据来自 SSE 推送）
**Requirements**: HWM-16, HWM-18, HWM-20, HWM-22, TDD-04
**Success Criteria** (what must be TRUE):
  1. 用户可以看到磁盘 IOPS 和读写速度实时折线图
  2. 用户可以看到网络接口上下行速率实时折线图
  3. 用户可以看到 CPU/内存的近 1 小时趋势折线图（ECharts 滑动窗口，300 点上限）
  4. 所有图表组件适配夏日海滩风主题配色（海洋蓝主调 + 琥珀色强调）
  5. ECharts 实例在组件卸载时正确 dispose，通过 markRaw 避免响应式代理，无内存泄漏
  6. Vue 组件逻辑（composable、图表数据处理）有单元测试覆盖
**Plans**: 1 plan
Plans:
- [x] 33-01-PLAN.md — 完整图表组件（磁盘 IO/网络实时折线图 + CPU/内存趋势图）+ TDD + 页面集成
**Status**: ✅ Completed 2026-05-10
**UI hint**: yes

### Phase 34: 安全加固 + 打磨
**Goal**: 硬件监控 API 权限控制、页面加载/异常/断连状态处理
**Depends on**: Phase 32（状态处理在页面基础上打磨）
**Requirements**: HWM-21, HWM-23, HWM-24
**Success Criteria** (what must be TRUE):
  1. 非 ADMIN 角色用户无法访问硬件监控 API 端点，返回 403 错误
  2. 页面加载时显示加载骨架态，数据为空时显示空状态提示，请求/连接失败时显示错误信息
  3. SSE 断线重连时页面顶部显示连接状态提示（"正在重连..."），恢复连接后自动消失
  4. 用户离开监控页面时 SSE 连接正确关闭，无资源泄漏
**Plans**: 1 plan
Plans:
- [x] 34-01-PLAN.md — JWT 角色声明 + SecurityConfig 权限 + 前端骨架/错误/断连状态
**Status**: ✅ Completed 2026-05-10
**UI hint**: yes

---

## Progress

**Execution Order:** Phases execute in numeric order: 31 -> 32 -> 33 -> 34

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 17. 基础设施搭建 | v1.5 | 1/1 | Completed | 2026-05-07 |
| 18. 后端包结构重组 | v1.5 | 1/1 | Completed | 2026-05-07 |
| 19. 配置文件整理 | v1.5 | 1/1 | Completed | 2026-05-07 |
| 20. 前端目录重组 | v1.5 | 1/1 | Completed | 2026-05-07 |
| 21. 代码规范统一 | v1.5 | 1/1 | Completed | 2026-05-07 |
| 22. 后端死代码清理 | v1.6 | 1/1 | Completed | 2026-05-07 |
| 23. 后端结构体优化 | v1.6 | 1/1 | Completed | 2026-05-07 |
| 24. 前端代码清理 | v1.6 | 1/1 | Completed | 2026-05-07 |
| 25. 依赖清理 | v1.6 | 1/1 | Completed | 2026-05-07 |
| 26. 配置清理 | v1.6 | 1/1 | Completed | 2026-05-07 |
| 27. 注释标准定义 | v1.7 | 3/3 | Completed | 2026-05-08 |
| 28. 配置层+Controller层注释 | v1.7 | 2/2 | Completed | 2026-05-09 |
| 29. Service接口+Vue/TS注释 | v1.7 | 2/2 | Completed | 2026-05-09 |
| 30. 注释维护机制建立 | v1.7 | 2/2 | Completed | 2026-05-09 |
| 31. 后端采集服务+REST API | v1.8 | 3/3 | Completed | 2026-05-10 |
| 32. SSE实时推送+前端基础页面 | v1.8 | 3/3 | Completed | 2026-05-10 |
| 33. 完整图表+趋势 | v1.8 | 1/1 | Completed | 2026-05-10 |
| 34. 安全加固+打磨 | v1.8 | 1/1 | Completed | 2026-05-10 |

---

*路线图更新: 2026-05-10 — Phase 32 规划完成 (3 plans)*
