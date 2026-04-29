# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建与开发命令

### 前端 (app-vue)

```powershell
cd app-vue
npm install
npm run dev        # 开发模式（端口 5173）
npm run build      # 生产构建
npm run test       # 单元测试（vitest）
npm run lint       # ESLint 检查
```

### 后端 (springboot)

**注意：当前项目正从 Maven 迁移到 Gradle，build 文件以 pom.xml 为准**

```powershell
cd springboot
./mvnw.cmd spring-boot:run   # 运行应用
./mvnw.cmd package          # 编译打包
./mvnw.cmd test             # 运行测试
./mvnw.cmd test -Dtest=ClassName  # 运行单个测试类
./mvnw.cmd package -DskipTests   # 跳过测试打包
```

---

## 项目架构

### 模块结构

```
vibe_coding/
├── app-vue/           # Vue 3 前端（Vite + TypeScript）
│   └── src/
│       ├── api/       # API 层（Axios 封装）
│       ├── views/     # 页面（auth/, dashboard/, system/, seckill/）
│       ├── router/    # 路由 + 导航守卫
│       └── store/     # Pinia 状态管理
│
├── springboot/        # Spring Boot 后端
│   └── src/main/java/cn/coderstory/springboot/
│       ├── controller/   # REST 控制器
│       ├── service/impl/ # 业务逻辑
│       ├── mapper/       # MyBatis Plus Mapper
│       ├── entity/       # 数据实体
│       ├── config/       # 配置类（Cors、Security、Web）
│       ├── security/     # JWT 认证过滤器
│       ├── aspect/       # AOP 切面（审计日志）
│       └── exception/    # 异常处理
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

## 技术栈

| 模块 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + TypeScript | 3.5+ / 8+ |
| 后端 | Spring Boot + Java | 4.0.5 / 26 |
| ORM | MyBatis Plus | 3.5.x |
| 数据库 | MySQL + Flyway | admin_system |
| 缓存 | Redis | 8.0+ |
| 消息队列 | RocketMQ | 5.3.2 |
| 认证 | JWT | 0.13.0 |

**端口**：前端 5173，后端 8080

---

## 数据库迁移

Flyway 脚本位于 `springboot/src/main/resources/db/migration/`

命名规范：`V{版本号}__{描述}.sql`，如 `V1__init.sql`

**规则**：
- 每次变更创建新脚本，禁止修改已执行脚本
- 使用幂等语句（`CREATE TABLE IF NOT EXISTS`）

---

## 参考文档

- `AGENTS.md` - AI 编程规范和代码风格指南
- `docs/seckill/user-guide.md` - 秒杀系统用户手册
- `docs/browser-automation-guide.md` - 浏览器自动化指南


# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文代码审查规范——在保持专业严谨的同时，用符合国内团队文化的方式给出有效反馈
- **chinese-commit-conventions**: 中文 Git 提交规范 — 适配国内团队的 commit message 规范和 changelog 自动化
- **chinese-documentation**: 中文技术文档写作规范——排版、术语、结构一步到位，告别机翻味
- **chinese-git-workflow**: 适配国内 Git 平台和团队习惯的工作流规范——Gitee、Coding、极狐 GitLab、CNB 全覆盖
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发或执行实现计划之前使用——创建具有智能目录选择和安全验证的隔离 git 工作树
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
