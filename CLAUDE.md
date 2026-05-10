# CLAUDE.md — AI 编程指南

Claude Code 辅助开发。项目规范、沟通规则、常见陷阱。

## AI 语言配置

**默认语言：中文**
- 回复、注释、错误信息、文档、提交均中文
- 仅用户要求、代码英文、技术术语无公认翻译时用英文
- 检测英文即时切中文（如 `"I'll..."` → `"我将..."`、`"Error:"` → `"错误："`）

## 交互要求

1. **全程中文，简洁直接** — 不总结已做事，发现问题立即报告
2. **证据支撑** — 声称完成/修复前必须运行验证命令并提供输出
3. **先诊断再回滚** — build 失败先分析根因，不立即回滚
4. **直接执行不问意见** — 确认目标后直接执行，不询问"是否要执行"。但 TDD 铁律（第 37-45 行）优先级高于本条，任何场景不得以"直接执行"为由跳过 RED 阶段
5. **目录前置检查** — 执行命令和提交前确认当前目录正确
6. **禁止主动使用 emoji**

## 工作规范

### Git
- commit 描述中文 — 标题和正文均中文，仅技术专有名词可保留英文
- 提交前清理多余/缓存文件

### 提交前自检（每批次提交前执行）
1. **TDD 合规** — 本次提交包含新功能/修复？测试是否先于代码提交？
2. **语言检查** — commit message 是否中文？混入英文则修正
3. **范围检查** — `git status` 确认无多余/无关文件混入
4. **证据检查** — 声称"通过/成功"的结论，命令执行输出在哪？

### 编辑
- 最小唯一字符串作 `old_string`，避免跨段落匹配误删
- 编辑前读文件确认定位，不确定时用 Write 重写整个文件

### 范围
- 不执行未请求的操作。即使文件看似"多余"，只要不在请求范围内就不动
- 发现可疑文件应提问，不自认"清理"属于分内事

### TDD 铁律（强制）
所有新功能、Bug 修复、行为变更必须严格遵循红-绿-重构：

1. **先写测试，看到失败（RED）** — 没有失败的测试就不写生产代码。测试因功能缺失而失败（非拼写错误）
2. **写最少代码让测试通过（GREEN）** — 刚好够通过测试，不做 YAGNI 设计
3. **验证通过（GREEN）** — 必须运行命令确认全部测试 PASS
4. **后写代码/无测试前置 = 违规** — 已写代码未见 RED 则删掉重来。禁止"后补测试"或"改编实现为测试"
5. **每个单元一个行为** — 测试名中出现"和/与"表示应拆分
6. **异常也必须先写测试** — 发现 Bug 先写重现测试（RED），再修复（GREEN）

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
| 路径 | 优先 `@` 别名 |
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
### 1. Edit 工具跨段落匹配误删
**规避**：最小唯一字符串作 `old_string`，不跨段落匹配

## 注释规范

详细注释规范（语言策略、Javadoc/TSDoc 模板、YAML 注释、TODO 管理、PR Review 清单、维护机制）见 `docs/comment-standards.md`。

L0-L3 注释层级速览：

| 层级 | 约束 | 适用范围 |
|------|------|----------|
| L0 | 不强制 | Entity/Model 类全部 |
| L1 | 建议 | Mapper 接口 + 工具类私有方法 |
| L2 | 鼓励 | Service 接口 + Vue 组件 |
| L3 | 强制 | Config + Controller + 异常体系 + JWT + AOP + YAML 段头 |

L3 层文件缺少 Javadoc 视为 PR 阻塞项。


---

## 参考

- `docs/seckill/user-guide.md` — 秒杀系统手册
- `docs/browser-automation-guide.md` — 浏览器自动化指南
- `docs/comment-standards.md` — 注释规范详细说明
- `docs/superpowers-zh.md` — Superpowers-ZH 技能列表
- `.planning/` — GSD 规划文件
