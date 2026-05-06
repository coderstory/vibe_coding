# CLAUDE.md — AI 编程指南

本项目使用 Claude Code 进行 AI 辅助开发，本文件提供项目规范、沟通规则和常见陷阱指南。

## AI 语言配置

**默认语言：中文**
- 所有回复、注释、错误信息、文档、提交信息均使用中文
- 仅当用户明确要求、代码本身是英文、或技术术语无公认中文翻译时才使用英文
- 语言检测：发现即将输出英文时立即切换为中文（如 `"I'll..."` → `"我将..."`、`"Error:"` → `"错误："`）

## 交互要求

1. **全程中文，简洁直接** — 不总结已做的事，发现问题立即报告
2. **证据支撑** — 声称完成/修复前必须运行验证命令并提供输出
3. **先诊断再回滚** — build 失败先分析根因，不立即回滚
4. **直接执行不问意见** — 确认目标后直接执行，不询问"是否要执行"
5. **目录前置检查** — 执行命令和提交前确认当前目录正确
6. **禁止主动使用 emoji**
7. **禁止调用 gpt 系列模型**

## 工作规范

### Git
- commit 描述部分必须使用中文
- 提交前清理多余/缓存文件
- `.claude/projects/` 下的 memory 文件不提交

### 编辑
- 使用最小唯一字符串作为 `old_string`，避免跨段落匹配误删
- 编辑前先读文件确认精确定位，不确定时用 Write 重写整个文件

---

## 技术栈

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

## 项目架构

```
vibe_coding/
├── app-vue/                    # Vue 3 前端
│   └── src/
│       ├── api/modules/        # API 层按业务域拆分
│       ├── components/         # 组件（common/layout/business）
│       ├── router/modules/     # 路由按模块拆分 + guards.ts
│       ├── store/              # Pinia 状态管理
│       ├── views/              # 页面（auth/dashboard/system/seckill）
│       └── composables/        # 组合式函数
│
├── springboot/                 # Spring Boot 后端
│   └── src/main/java/cn/coderstory/springboot/
│       ├── shared/             # 通用层（config/security/aspect/exception/util/limiter）
│       └── {domain}/           # 业务域：user/role/menu/auth/audit/knowledge/seckill/rocketmq/order/monitor
│           ├── controller/
│           ├── service/impl/
│           ├── mapper/
│           ├── entity/
│           └── dto/
│
└── docs/
```

### 认证流程
1. 前端 `POST /api/auth/login` → 返回 `{token, refreshToken, user}`
2. Token 存 `localStorage`，请求携带 `Authorization: Bearer <token>`
3. 后端 `JwtAuthenticationFilter` 解析 token 设置 SecurityContext
4. Token 过期（401）→ 自动调用 `POST /api/auth/refresh` 刷新

### 秒杀系统架构
```
请求 → Redis原子预扣减 → RocketMQ事务消息 → 数据库乐观锁
         ↓                    ↓                    ↓
      第一层限流           削峰填谷              最终一致性
```

---

## 代码风格

### 前端

| 规范 | 规则 |
|------|------|
| 组件 | Composition API（`<script setup lang="ts">`），PascalCase 命名 |
| Props | camelCase，类型定义用 `defineProps<{...}>()` |
| CSS 类 | kebab-case，`<style scoped>` |
| Composables | camelCase + `use` 前缀 |
| 路径 | 优先使用 `@` 别名 |
| API 类型 | 集中在 `api/types.ts` |
| 弹窗 | 添加 `lock-scroll` 和 `append-to-body` |

### 后端

| 规范 | 规则 |
|------|------|
| Controller | `*Controller`，PascalCase 类名 |
| Service | `*Service` 接口 + `*ServiceImpl` 实现 |
| Mapper | `*Mapper`，复杂 SQL 写在 `mapper.xml` |
| 依赖注入 | `@RequiredArgsConstructor` + `private final`（不用 `@Autowired` 字段注入） |
| Lombok | `@Data`, `@Slf4j`, `@RequiredArgsConstructor` |
| 实体 | `@TableName` + `@TableId(type = IdType.AUTO)` + `@TableLogic` |
| 异常 | `BusinessException.notFound()/.badRequest()/.conflict()` |
| 响应 | `ApiResponse<T>` 封装 `{code, message, data}` |
| 审计日志 | AOP 切面 `cn.coderstory.springboot.service..*`，方法前缀推断 CRUD，`ThreadLocal` 防重复，`@Async` 异步写入 |

### 通用规则

- 新增表/字段必须创建 Flyway 迁移脚本
- 后端不用 `@SuppressWarnings`
- API 参数注意空值检查，避免 NPE
- 复用 MyBatis Plus 特性（`LambdaQueryWrapper` 等）

---

## 构建命令

### 前端
```powershell
cd app-vue
npm install
npm run dev     # 开发（端口 5173）
npm run build   # 生产构建
npm run test    # 单元测试
npm run lint    # ESLint + Stylelint
```

### 后端
```powershell
cd springboot
./gradlew.bat bootRun                   # 运行
./gradlew.bat build                     # 编译打包
./gradlew.bat test                      # 测试
./gradlew.bat test --tests "*ClassName" # 单个测试类
./gradlew.bat check                     # 全量代码质量检查
```

---

## 数据库迁移

Flyway 脚本位于 `springboot/src/main/resources/db/migration/`，命名 `V{版本号}__{描述}.sql`。

**规则**：每次变更创建新脚本，禁止修改已执行脚本；使用幂等语句。

配置拆分（`config/` 目录下）：
- `datasource.yaml` / `cache.yaml` / `mq.yaml` / `security.yaml` / `business.yaml`

---

## 常见陷阱

### 1. Windows 中文路径编码
**症状**：Gradle 测试报 `ClassNotFoundException`
**修复**：`build.gradle.kts` 添加 `-Dfile.encoding=GBK`

### 2. localhost DNS 解析失败
**症状**：`UnknownHostException: localhost`
**修复**：所有配置用 `127.0.0.1` 替代 `localhost`

### 3. RocketMQ 5.x API 包路径变更
**症状**：`ClassNotFoundException`，类路径变更（如 `TopicList` 移到 `remoting.protocol.body`）
**注意**：升级 RocketMQ 后检查 API 包路径

### 4. Edit 工具跨段落匹配误删
**规避**：用最小唯一字符串作为 `old_string`，不跨段落匹配

### 5. 依赖版本对齐
**常见**：Lombok/Spring AOP 版本需与 Spring Boot 对齐；`webmvc` vs `web` 一致性
**建议**：使用 `libs.versions.toml` 统一管理

### 6. GSD 命令语法
**注意**：使用冒号 `gsd:execute-phase`（不是 `gsd-execute-phase`）

### 7. Gradle 构建缓存
**修复**：`./gradlew.bat clean build` 或 `--no-build-cache`

### 8. SSE 连接时序（秒杀系统）
**问题**：SSE 连接与请求发送的时序竞争
**修复**：前端先生成 `queueId`，再建立 SSE 连接

---

## 演变历史

| 版本 | 内容 | 日期 |
|------|------|------|
| v1.0 | 基础框架（Vue 3 + Element Plus + Spring Boot） | 2026-04-02 |
| v1.1 | 主题修复（海浪动画、弹窗修复、配色调整） | 2026-04-03 |
| v1.2 | 用户管理模块 | 2026-04-18 |
| v1.3 | RocketMQ 管理（Topic/Consumer Group/消息/监控） | 2026-04-29 |
| v1.4 | Maven→Gradle + Spring Boot 4.1 + 依赖升级 | 2026-05-06 |
| v1.5 | 代码重构（质量工具链/包结构重组/配置拆分/规范统一） | 2026-05-07 |

**滞留项**：敏感信息环境变量加固（JWT secret、DB 密码）

---

## 参考

- `docs/seckill/user-guide.md` — 秒杀系统手册
- `docs/browser-automation-guide.md` — 浏览器自动化指南
- `.planning/` — GSD 规划文件
