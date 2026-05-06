# CLAUDE.md — AI 编程指南

本项目使用 Claude Code 进行 AI 辅助开发，本文件为 AI 代理提供项目规范、沟通配置和常见陷阱指南。

## AI 语言配置

**默认语言：中文**
- 所有回复、注释、错误信息、文档均使用中文
- 仅当用户明确要求、代码本身是英文、或技术术语无公认中文翻译时才使用英文

### Superpowers-ZH 配置
- **技能框架**：已安装 20 个中文增强技能
- **自动激活**：收到任务时自动检查匹配的技能
- **强制中文**：所有交互优先使用中文沟通

## 交互要求

1. **全程使用中文沟通** — 包括回复、代码注释、错误信息、文档
2. **禁止输出英文** — 除非用户明确要求、代码本身是英文、或无公认真译的技术术语
3. **回复简洁直接** — 不要总结已做的事，发现问题立即报告
4. **证据支撑断言** — 声称完成/修复前必须运行验证命令并提供输出
5. **不确定时主动询问** — 不要自行假设

---

## 技术栈（当前状态）

| 模块 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + TypeScript | 3.5+ / 8+ |
| 后端 | Spring Boot + Java | 4.1.0-RC1 / 26 |
| 构建工具 | Gradle (Kotlin DSL) | 9.5 |
| ORM | MyBatis Plus | 3.5.x |
| 数据库 | MySQL + Flyway | admin_system |
| 缓存 | Redis + Redisson | 8.0+ |
| 消息队列 | RocketMQ | 5.3.2 |
| 认证 | JWT (jjwt) | 0.13.0 |
| 代码质量 | ArchUnit + Checkstyle + PMD + SpotBugs + Error Prone + JaCoCo | — |
| 前端 lint | ESLint 10.x flat config + Stylelint 17.x | — |

**端口**：前端 5173，后端 8080

---

## 项目架构（v1.5 重构后）

### 模块结构

```
vibe_coding/
├── app-vue/                    # Vue 3 前端（Vite + TypeScript）
│   └── src/
│       ├── api/modules/        # API 层，按业务域拆分（auth/user/role/menu/seckill/rocketmq）
│       ├── components/         # 组件（common/ + layout/ + business/）
│       ├── router/modules/     # 路由按模块拆分 + guards.ts 导航守卫
│       ├── store/              # Pinia 状态管理
│       ├── views/              # 页面（auth/ dashboard/ system/ seckill/）
│       └── composables/        # 组合式函数
│
├── springboot/                 # Spring Boot 后端（Gradle + Kotlin DSL）
│   └── src/main/java/cn/coderstory/springboot/
│       ├── shared/             # 通用层：config/security/aspect/exception/util/limiter
│       ├── user/               # 用户管理域
│       ├── role/               # 角色管理域
│       ├── menu/               # 菜单管理域
│       ├── auth/               # 认证域
│       ├── audit/              # 审计日志域
│       ├── knowledge/          # 知识库域
│       ├── seckill/            # 秒杀系统域
│       ├── rocketmq/           # RocketMQ 管理域
│       ├── order/              # 订单域
│       └── monitor/            # 监控域
│
└── docs/                       # 项目文档
    ├── seckill/                # 秒杀系统文档
    └── superpowers/specs/      # 设计规格文档
```

### 后端包规范（每个业务域内）

```
{domain}/
├── controller/     # REST 控制器
├── service/impl/   # 业务接口与实现
├── mapper/         # MyBatis Plus Mapper
├── entity/         # 数据实体
└── dto/            # 数据传输对象
```

### 认证流程

1. 前端 POST `/api/auth/login` → 返回 `{token, refreshToken, user}`
2. Token 存 `localStorage`，请求携带 `Authorization: Bearer <token>`
3. 后端 `JwtAuthenticationFilter` 解析 token 设置 SecurityContext
4. Token 过期（401）→ 前端自动调用 `POST /api/auth/refresh` 刷新
5. 白名单配置在 `application.yaml` 的 `security.whitelist`

### 秒杀系统架构（高并发核心）

```
请求 → Redis原子预扣减 → RocketMQ事务消息 → 数据库乐观锁
         ↓                    ↓                    ↓
      第一层限流           削峰填谷              最终一致性
```

- **第一层**：Redis Lua 脚本保证库存原子操作
- **第二层**：RocketMQ 事务消息异步下单
- **第三层**：数据库乐观锁（version）防超卖

---

## 构建与开发命令

### 前端 (app-vue)

```powershell
cd app-vue
npm install
npm run dev        # 开发模式（端口 5173）
npm run build      # 生产构建
npm run test       # 单元测试（vitest）
npm run lint       # ESLint + Stylelint 检查
```

### 后端 (springboot)

```powershell
cd springboot
./gradlew.bat bootRun               # 运行应用
./gradlew.bat build                 # 编译打包
./gradlew.bat test                  # 运行测试
./gradlew.bat test --tests "*ClassName"  # 单个测试类
./gradlew.bat build -x test         # 跳过测试打包
./gradlew.bat check                 # 全量代码质量检查（Checkstyle/PMD/SpotBugs/ArchUnit）
./gradlew.bat test jacocoTestReport # 测试 + 覆盖率报告
```

---

## 数据库迁移

Flyway 脚本位于 `springboot/src/main/resources/db/migration/`

命名规范：`V{版本号}__{描述}.sql`，如 `V1__init.sql`

**规则**：
- 每次变更创建新脚本，禁止修改已执行脚本
- 使用幂等语句（`CREATE TABLE IF NOT EXISTS`）

配置文件已拆分为 5 个关注点：
- `config/datasource.yaml` — 数据源
- `config/cache.yaml` — 缓存
- `config/mq.yaml` — 消息队列
- `config/security.yaml` — 安全
- `config/business.yaml` — 业务配置

---

## 常见陷阱与规避（从历史修复中总结）

### 1. Windows 中文路径编码问题
**症状**：Gradle 执行测试时报 `ClassNotFoundException`
**原因**：Windows 中文用户名/路径导致编码问题
**修复**：在 `build.gradle.kts` 中添加 `-Dfile.encoding=GBK`

### 2. localhost DNS 解析失败
**症状**：`UnknownHostException: localhost`
**原因**：Windows 下 DNS 解析 intermittent 失败
**修复**：所有配置和代码中使用 `127.0.0.1` 替代 `localhost`

### 3. RocketMQ 5.x API 兼容性
**症状**：`ClassNotFoundException`，包路径变更
**原因**：RocketMQ 5.x 将部分类从 `remoting` 移到 `remoting.protocol.body/route`
**注意**：引入新版本 RocketMQ 依赖时需检查 API 包路径

### 4. Edit 工具误删
**症状**：跨段落匹配导致意外删除文件内容
**规避**：
- 编辑前先读完整文件
- 使用**最小唯一字符串**作为 `old_string`
- 避免跨段落匹配

### 5. 依赖版本对齐
**症状**：启动失败、Bean 注入失败
**常见**：
- Lombok + Spring AOP 版本需与 Spring Boot 对齐
- `spring-boot-starter-webmvc` vs `spring-boot-starter-web` 一致性

### 6. GSD 命令语法
**注意**：GSD 命令使用冒号 `gsd:execute-phase`（不是 `gsd-execute-phase`）

---

## 里程碑完成记录

| 版本 | 内容 | 完成日期 |
|------|------|---------|
| v1.0 | 基础框架（Vue 3 + Element Plus + Spring Boot） | 2026-04-02 |
| v1.1 | 夏日海滩风主题修复与完善 | 2026-04-03 |
| v1.2 | 用户管理模块 | 2026-04-18 |
| v1.3 | RocketMQ 管理功能（Topic/Consumer Group/消息/监控） | 2026-04-29 |
| v1.4 | Maven→Gradle 迁移 + Spring Boot 4.1 + 依赖升级 | 2026-05-06 |
| v1.5 | 前后端代码重构与目录整理 | 2026-05-07 |

---

## 参考文档

- `AGENTS.md` — AI 编程规范和代码风格指南
- `docs/seckill/user-guide.md` — 秒杀系统用户手册
- `docs/browser-automation-guide.md` — 浏览器自动化指南
- `.planning/` — GSD 工作流规划文件
