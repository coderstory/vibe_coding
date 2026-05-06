# 项目调研摘要

**项目:** Vue + Spring Boot 管理后台
**域:** 代码重构与目录整理
**调研日期:** 2026-05-06
**置信度:** HIGH

---

## 执行摘要

本次 v1.5 里程碑是一次**代码架构卫生工程**。核心问题是后端包结构混用按层/按域两种风格、前端目录扁平化严重、配置文件缺少关注点分离且敏感信息存在硬编码。推荐策略是**按业务领域统一垂直切分 + 增量迁移**。

后端建立 `shared/` 通用层和 `user/`、`auth/`、`menu/`、`role/`、`knowledge/`、`audit/`、`rocketmq/`、`seckill/`、`order/`、`monitor/` 等业务域包，合并零散 `mq/`/`stock/`/`sse/` 到 `seckill/` 域。前端同步按域拆分 `api/modules/`、`router/modules/`、`types/`。

关键风险是 Vue Router 懒加载路径断裂（27 条动态 import）、MyBatis Mapper XML namespace 断裂、以及 Spring Component Scan 失效。所有风险可通过**先移动后修改的两步 commit 策略**和**每次移动后立即构建验证**来规避。

## 关键发现

### 1. 推荐工具链

**前端工具（需新增/升级）：**
- ESLint 10.x flat config 升级（当前 9.x）
- `@stylistic/eslint-plugin` 替代已废弃的 ESLint 核心风格规则
- Stylelint 17.x + stylelint-config-standard（CSS 质量检查）
- `vue-tsc` CI 集成（Vue SFC 类型检查）

**后端工具（需新增）：**
- ArchUnit 1.4.0 — 重构核心工具，定义"controller 不能直接调用 mapper"等架构规则
- Checkstyle (Gradle 内置) — 代码风格检查
- PMD (Gradle 内置) — 源码异味检测
- SpotBugs 4.9.3 — 字节码 bug 检测
- JaCoCo — 测试覆盖率
- Error Prone 2.37.0 — 编译时错误检测

### 2. 代码组织规范

**后端问题（已确认）：**
- Controller 直接注入 Mapper（UserController、SeckillController 等）
- `MenuServiceImpl.java` 和 `RoleServiceImpl.java` 在 `service/` 根目录而非 `service/impl/`
- DTO 层仅在 seckill 域存在，其他域使用 `Map<String, Object>` 作为请求体
- 秒杀相关 `mq/`、`stock/`、`sse/` 模块松散分散

**前端问题（已确认）：**
- `components/` 扁平化含 10 个 `.vue` 文件
- `api/` 扁平化含 15 个文件
- `router/index.ts` 为 7KB 单一文件
- 存在无用脚手架模板组件（`HelloWorld.vue`、`AboutView.vue` 等）
- `vite.config.js` 应为 `vite.config.ts`

### 3. 架构建议

**后端架构** — 按业务域垂直切分：
```
cn.coderstory.springboot/
├── shared/              # 跨业务通用组件
│   ├── config/          # Security/Web/Cors/Redis 等配置
│   ├── security/        # JWT 认证/授权
│   ├── aspect/          # AOP 切面
│   ├── exception/       # 全局异常处理
│   ├── util/            # 工具类
│   └── limiter/         # 限流组件
├── user/                # 用户管理域
│   ├── controller/      # UserController
│   ├── service/         # UserService 接口
│   ├── service/impl/    # UserServiceImpl
│   ├── mapper/          # UserMapper
│   ├── entity/          # User
│   └── dto/             # UserRequest/UserResponse
├── role/                # 角色管理域
├── menu/                # 菜单管理域
├── auth/                # 认证域
├── audit/               # 审计日志域
├── knowledge/           # 知识库域
├── seckill/             # 秒杀域（含 mq/stock/sse）
├── rocketmq/            # RocketMQ 监控域
├── order/               # 订单域
└── monitor/             # 系统监控域
```

**前端架构** — 按域组织：
```
src/
├── api/
│   ├── modules/         # 按域: auth/user/role/menu/seckill/rocketmq...
│   └── index.ts         # 统一导出
├── components/
│   ├── common/          # 通用组件 (BaseTable/BaseForm)
│   ├── layout/          # 布局组件
│   └── business/        # 业务组件
├── composables/         # 按域拆分或单文件
├── router/
│   ├── modules/         # 按域拆分
│   └── guards.ts        # 路由守卫
├── store/               # 按域拆分
├── types/               # 按域拆分
└── views/               # 按域（已基本完成）
```

### 4. 关键重构陷阱

| 陷阱 | 风险 | 预防策略 |
|------|------|---------|
| Vue Router 懒加载路径断裂 | 移动 .vue 文件后运行时白屏 | 每次移动后 `npm run build`（非 `npm run dev`）|
| MyBatis XML 三重绑定断裂 | XML 路径/namespace/@MapperScan 三处需同步 | 每次移动后 `./gradlew.bat test` |
| 秒杀 Redis Key 不可修改 | 运行时 Redis 数据和 MQ 消息丢失 | 只提取常量引用，不改变 `seckill:stock:` 等值 |
| JWT secret 硬编码 | 安全风险 | 重构时迁移到环境变量 |
| 配置文件拆分级联失效 | spring.config.import 加载顺序导致配置缺失 | 双 profile 启动验证 |

## 路线图建议

基于调研，推荐 **5 个阶段**：

### Phase 1: 基础设施搭建
**先决条件：** 无（零依赖）
**内容：** EditorConfig、ESLint flat config 升级、Prettier/Stylelint 配置、ArchUnit/Checkstyle/PMD/SpotBugs/JaCoCo 集成
**验证：** `./gradlew.bat check` + `npm run lint` + `npx vue-tsc --noEmit`

### Phase 2: 后端包结构重组
**先决条件：** Phase 1（工具链就绪）
**内容：** 建 shared/ 通用层 → 逐个业务域迁移 → 合并零散模块 → 修复分层违规 → 统一 Service 接口+impl
**验证：** `./gradlew.bat test`（每个域迁移后）
**关键风险：** `@MapperScan` 通配符覆盖、Component Scan 路径

### Phase 3: 配置文件拆分与安全加固
**先决条件：** Phase 2（包结构确定后配置归属才明确）
**内容：** 拆分 application.yaml → 消除 test.yaml 冗余 → JWT secret 强制环境变量 → 双 profile 验证
**验证：** `./gradlew.bat bootRun` + `--spring.profiles.active=test`

### Phase 4: 前端目录重组
**先决条件：** 技术上独立，建议在 Phase 2-3 后（保持命名一致）
**内容：** components/ 分区 → api/modules/ → router/modules/ → types/ 拆分 → 清理脚手架残留
**验证：** `npm run build` + 手动登录全流程

### Phase 5: 命名规范与质量收敛
**先决条件：** Phase 2 + Phase 4（目录重组完成）
**内容：** Page 后缀统一、DTO/VO 规范、TypeScript interface 规范、`@ConfigurationProperties` 类型安全配置、工具规则收紧
**验证：** `./gradlew.bat check`（maxWarnings=0） + `npm run lint` + JaCoCo 覆盖率门槛

## 置信度评估

| 领域 | 置信度 | 原因 |
|------|--------|------|
| 工具链 | HIGH | npm registry 实时版本 + Gradle 生态确认 |
| 目录规范 | HIGH | Vue 官方风格指南 + 阿里巴巴 P3C + 逐文件审查 |
| 架构 | HIGH | 82+ Java 文件 + 50+ Vue/TS 文件逐行分析 |
| 陷阱 | HIGH | 基于实际代码路径检查（具体文件+行号） |
| **总体** | **HIGH** | 所有维度基于实际代码库分析 |

### 待解决

- `@MapperScan` 通配符 `**` 在包重组后的覆盖验证（Phase 2 首个域迁移后立即验证）
- RocketMQ Consumer 组件扫描路径验证（StockConsumer 当前已注释）
- Flyway 迁移文件 checksum 保护（6 个已执行脚本不可修改）

---
*调研完成: 2026-05-06*
*可用于路线图: 是*
