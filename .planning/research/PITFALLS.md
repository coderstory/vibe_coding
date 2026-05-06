# Pitfalls Research: 代码重构与目录整理

**Domain:** Vue 3 + Spring Boot 管理后台代码重构
**Researched:** 2026-05-06
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: Vue Router 懒加载路径断裂

**What goes wrong:**
路由配置中 27 条路由全部使用 `() => import('@/views/xxx/Xxx.vue')` 动态导入。移动任何 `.vue` 文件后，构建不会报错（语法正确），但运行时用户访问该路由时 Vite 会抛出 `Failed to fetch dynamically imported module`，页面白屏。

当前涉及路径（部分）：
- `@/views/auth/Login.vue`
- `@/views/system/UserManagement.vue`
- `@/views/seckill/activity/ActivityList.vue`
- `@/views/rocketmq/TopicList.vue`
- 等 27 条

**Why it happens:**
Vue Router 的 `() => import(...)` 是运行时动态导入，路径是字符串字面量。TypeScript 编译器不会检查字符串是否正确，Vite 在构建时才解析这些路径。如果 `vite.config.js` 的 `resolve.alias`（`@` -> `./src`）和 `tsconfig.json` 的 `paths`（`@/*` -> `./src/*`）不一致，IDE 不会报错但构建会失败。

**How to avoid:**
1. 移动文件前，先 `grep '@/views/'` 找到所有引用该路径的路由条目
2. 移动后立即同步更新 `router/index.ts` 中的 import 路径
3. 移动后立即执行 `npm run build`（不是 `npm run dev`），构建会全面解析懒加载路径
4. 考虑将路由的 component 路径提取为常量映射表，减少重复字符串
5. 保持 `vite.config.js` 和 `tsconfig.json` 的路径别名同步

**Warning signs:**
- `npm run dev` 正常运行，但 `npm run build` 报 `Could not resolve` 错误
- 浏览器控制台出现 `TypeError: Failed to fetch dynamically imported module`
- 切换路由时页面白屏

**Phase to address:**
前端目录整理阶段（Phase 17-01），每次移动文件后立即构建验证。

---

### Pitfall 2: MyBatis Plus Mapper XML 路径断裂

**What goes wrong:**
`application.yaml` 中配置 `mybatis-plus.mapper-locations: classpath:mapper/*.xml`。当前 XML 文件位于 `src/main/resources/mapper/` 下共 5 个：
- `UserMapper.xml`（namespace: `cn.coderstory.springboot.mapper.UserMapper`）
- `MenuMapper.xml`（namespace: `cn.coderstory.springboot.mapper.MenuMapper`）
- `KnowledgeArticleMapper.xml`
- `KnowledgeArticleTagMapper.xml`
- `KnowledgeCategoryMapper.xml`

如果移动 XML 文件目录、重命名文件、或移动对应的 Mapper 接口，会出现三种断裂：
1. **路径断裂**：XML 不在 `classpath:mapper/` 下，MyBatis 找不到 XML
2. **命名空间断裂**：XML 的 `namespace` 与 Mapper 接口的 FQCN 不匹配，启动时报 `org.apache.ibatis.binding.BindingException`
3. **@MapperScan 断裂**：`@MapperScan("cn.coderstory.springboot.**.mapper")` 使用通配符，但若重命名 mapper 包，通配符会失效

**Why it happens:**
MyBatis 的 XML 绑定依赖三个独立配置项的对齐：YAML 的 `mapper-locations`、XML 的 `namespace`、注解的 `@MapperScan`。修改任何一项都需要其他两项保持同步，但没有编译期检查——所有断裂都在启动时或首次查询时才暴露。

**How to avoid:**
1. XML 文件应保持与 Mapper 接口在同一逻辑模块下，不要单独建目录
2. 若移动 Mapper 接口，同步更新对应 XML 的 `namespace`
3. 若移动 XML 目录，同步更新 `application.yaml` 和 `application-test.yaml` 的 `mapper-locations`
4. 重构后运行 `./gradlew.bat test` 确保所有 Mapper 测试通过
5. 考虑在 `@Mapper` 注解的接口上保持一致，不依赖 `@MapperScan` 通配符（更安全但更繁琐）

**Warning signs:**
- 启动日志出现 `BindingException: Invalid bound statement (not found)`
- `MapperRegistry` 未找到 XML 文件
- 首次查询时报错，CRUD 全部失败

**Phase to address:**
后端包结构整理阶段（Phase 17-02），每次移动后运行完整测试套件。

---

### Pitfall 3: Pinia Store 引用路径断裂

**What goes wrong:**
`router/index.ts` 第 6 行直接导入 `import { useUserStore } from '@/store/user'`。此外，所有使用用户状态的组件都可能通过 `@/store/user` 引入。如果 `store/user.ts` 被移动或重命名，路由守卫（`router.beforeEach`）会因无法实例化 Pinia store 而导致整个路由系统崩溃——用户无法登录，所有需要认证的页面都无法访问。

更严重的是，路由守卫在 `router.beforeEach` 中调用 `useUserStore()`，这是应用启动后第一个执行的逻辑，失败意味着整个 SPA 白屏。

**Why it happens:**
Pinia store 的导入是标准的 ES module 导入，TypeScript 会在编译时检查路径。但如果只改了文件名没有更新所有引用（或 IDE 重构遗漏），会出现编译错误。关键风险在于 `router/index.ts`——它不仅导入 store，还在导航守卫中调用 store 方法，断裂影响面覆盖整个应用。

**How to avoid:**
1. 使用 IDE 的"重命名"功能（F2 或 Rename Symbol）而非手动改文件名
2. 移动 store 文件后，全局搜索 `from '@/store/` 找到所有引用
3. 移动后立即验证：启动 `npm run dev`，打开浏览器访问 `/login` 和 `/index`，确认路由守卫正常
4. 考虑为 store 创建 `index.ts` 统一导出（barrel export），减少外部直接引用单个 store 文件

**Warning signs:**
- TypeScript 编译报 `Cannot find module '@/store/user'`
- 浏览器白屏，console 显示模块加载失败
- 路由守卫报错 `Cannot read properties of undefined`

**Phase to address:**
前端目录整理阶段（Phase 17-01），store 文件改动后在路由守卫、登录流程、JWT 刷新流程中做端到端验证。

---

### Pitfall 4: 配置文件拆分导致运行时配置丢失

**What goes wrong:**
当前 `application.yaml` 是单一文件 154 行，覆盖 10 个配置域（RocketMQ、数据源、Redis、Redisson、Flyway、MyBatis、JWT、安全、秒杀参数、日志）。`application-test.yaml` 只有部分配置（99 行），缺失 `server.port`、`jwt`、`security.whitelist`、`logging` 等关键配置。

如果重构时拆分配置文件（例如拆成 `application-db.yaml`、`application-mq.yaml`），风险包括：
1. **profile 级联丢失**：`application-test.yaml` 作为 profile 文件不再能覆盖主文件中的值
2. **spring.config.import 顺序错误**：Spring Boot 按 `import` 顺序加载，后面的覆盖前面的，顺序错误导致预期配置不生效
3. **环境变量引用断裂**：`application.yaml` 大量使用 `${DB_HOST:127.0.0.1}` 占位符，拆分后环境变量可能无法正确传播到子文件
4. **secret 密钥泄露风险**：JWT `secret` 当前硬编码在 `application.yaml` 第 143 行，拆分时若不小心提交到公开 repo，安全风险极大

**Why it happens:**
Spring Boot 的配置优先级机制复杂（命令行 > 环境变量 > application-{profile}.yaml > application.yaml）。开发者通常只在开发环境测试默认 profile，不测试 test profile，导致 test profile 的配置缺失在生产环境才暴露。

**How to avoid:**
1. 拆分前先完整列出所有配置项（当前约 60+ 个 key），确认每个 key 的归属
2. 拆分后用 `--spring.profiles.active=test` 启动，验证 test profile 覆盖正确
3. JWT `secret` 在拆分时立即迁移到环境变量或 external config，绝不留存在 YAML 中
4. 保留一个完整的 `application.yaml` 作为 fallback，避免 profile 缺失时服务无法启动
5. 每次配置拆分后运行 `./gradlew.bat bootRun` 并用 curl 测试所有关键接口（登录、秒杀、CRUD）

**Warning signs:**
- 启动日志中 `@Value` 注入为 null
- 某个 profile 下服务启动失败（配置缺失）
- test profile 中数据库连接到生产库（test 缺少 datasource 配置，fallback 到主文件）
- 秒杀接口限流器未生效（seckill 配置块缺失）

**Phase to address:**
配置文件整理阶段（Phase 17-03），每拆分一个配置块都在两个 profile 下验证。

---

### Pitfall 5: 移动后端代码导致 Spring Component Scan 失效

**What goes wrong:**
`SpringbootApplication` 位于 `cn.coderstory.springboot`，`@SpringBootApplication` 默认扫描该包及其所有子包。当前所有业务代码都在 `cn.coderstory.springboot.*` 下（如 `seckill`、`order`、`monitor`、`stock`、`mq` 子包）。如果重构时将某些代码移出 `cn.coderstory.springboot` 包树（例如新建 `cn.coderstory.common` 顶层包），Spring 将无法扫描到这些组件。

具体影响：
- `@Service`、`@Component`、`@Repository` 标注的类不再被 Spring 管理
- `@Autowired` / `@RequiredArgsConstructor` 注入失败报 `NoSuchBeanDefinitionException`
- `@Controller` 标注的 REST 接口 404
- `@RocketMQMessageListener`（StockConsumer 第 17 行，已注释）若启用了需额外确认 RocketMQ 扫描路径

**Why it happens:**
`@SpringBootApplication` 的组件扫描范围由主类所在包决定，这是隐式约定。开发者在 IDE 中移动文件时，IDE 不感知 Spring 的扫描规则。代码移动到新包后，语法和导入都正确（IDE 不报错），但 Spring 容器无法找到这些组件。

**How to avoid:**
1. 移动代码时始终保持在 `cn.coderstory.springboot` 包树下
2. 若必须创建新的顶层包，在主类上添加 `@ComponentScan(basePackages = {"cn.coderstory.springboot", "cn.coderstory.xxx"})`
3. `@MapperScan("cn.coderstory.springboot.**.mapper")` 的通配符 `**` 能覆盖深层子包，但若 Mapper 移到新顶层包，也需更新
4. 每次移动后运行 `./gradlew.bat bootRun`，确认应用启动日志中 Spring 容器正常初始化
5. 运行 `./gradlew.bat test` 验证所有 Spring 上下文测试通过

**Warning signs:**
- 启动日志出现 `NoSuchBeanDefinitionException`
- `@Autowired` 字段的创建 Bean 报错
- 某个接口返回 404（Controller 未被扫描）
- Gradle 测试中 Spring 上下文加载失败

**Phase to address:**
后端包结构整理阶段（Phase 17-02），每次移动后运行完整测试套件和启动验证。

---

### Pitfall 6: 秒杀系统 Redis Key 和 RocketMQ Topic 硬编码断裂

**What goes wrong:**
秒杀系统的核心数据结构依赖硬编码的字符串常量：

Redis Key 前缀（分布在多个服务实现类中）：
- `"seckill:stock:"` — SeckillServiceImpl.java 第 80 行，PreheatServiceImpl.java 第 54 行，StockConsumer.java 第 49 行
- `"seckill:activity:"` — PreheatServiceImpl.java 第 57 行
- `"seckill:reservation:"` — PreheatServiceImpl.java 第 61 行

RocketMQ Topic（OrderTransactionProducer.java 第 104-105 行）：
- `"seckill_order_create"` — 事务消息主题
- `"seckill_stock_deduct"` — 库存扣减主题（当前未启用）

Lua 脚本（嵌入在 SeckillServiceImpl.java 和 StockConsumer.java 中）引用同样的 Redis key 格式。

如果重构时为了"统一常量管理"将这些字符串改为新的命名规范，会导致：
1. **Redis 数据不可读**：旧的预热数据（活动、库存、预约）都在旧 key 下，新 key 找不到，秒杀功能完全瘫痪
2. **RocketMQ 消息丢失**：Producer 发送到旧 topic，Consumer 监听新 topic，消息丢失
3. **Lua 脚本逻辑断裂**：脚本中使用字符串拼接构造 key，与新的 key 格式不匹配

**Why it happens:**
重构时的"代码整理"冲动会驱使开发者将散落在各处的硬编码字符串提取到常量类。但如果改动了字符串值而不同时迁移 Redis 数据或协调 RocketMQ topic 变更，就会产生断裂。更隐蔽的是，测试环境可能刚好清空了 Redis，测试通过但生产环境数据无法读取。

**How to avoid:**
1. **绝对不修改** Redis key 前缀和 RocketMQ topic 名称——将它们标记为"基础设施常量，不可变更"
2. 如果必须提取为常量类，保持字符串值不变，仅改变引用方式
3. 重构前使用 `redis-cli KEYS "seckill:*"` 导出所有现有 key 格式，作为回归清单
4. 重构后用 Redis 预热接口 `/api/seckill/preheat/{activityId}` 验证数据能正确读写
5. 将 key 前缀集中到单一常量类时，同时在类上添加 `// DO NOT MODIFY - 基础设施常量` 注释警告

**Warning signs:**
- 秒杀详情页显示库存为 0（预热数据丢失）
- 预约列表为空
- SSE 推送失败（queueId 关联不到库存扣减）
- RocketMQ console 显示消息堆积（消费者找不到消息）

**Phase to address:**
后端包结构整理阶段（Phase 17-02），秒杀模块重构后必须执行预热-秒杀-订单全链路端到端回归测试。

---

### Pitfall 7: 重构期间阻断正常开发

**What goes wrong:**
大规模重构如果采用"分支隔离+全量改动+合并"的策略，会导致：
1. 重构分支存活数周，期间主分支继续有功能开发和 bug 修复
2. 合并时冲突爆炸（文件移动 + 内容修改的冲突特别难解决）
3. 其他人无法在重构后的目录结构上开发，形成瓶颈
4. 重构完成后需要全团队重新学习新目录结构，生产效率骤降

**Why it happens:**
"先全部重构再合并"的心理模型最直观，但忽略了并行开发冲突。Vue 组件和 Java 类同时被移动和修改时，Git 将移动视为删除+新增，与内容修改产生冲突。

**How to avoid:**
1. **按模块增量重构**：先重构一个独立模块（如 RocketMQ 监控模块），完成后立即合并，团队适应新结构后再重构下一个
2. **重构顺序按依赖关系**：先重构底层（API 封装层、工具类），再重构上层（视图组件、Controller）
3. **每次重构保持可运行状态**：绝不提交不能编译/构建的代码，每个 commit 都应该是可部署的
4. **使用 IDE 的 Refactor > Move 功能**：IDE 的 Move 重构会自动更新所有引用，减少遗漏
5. **前端和后端分开重构**：不同时修改前后端，避免双重调试
6. **建议重构顺序**：
   - 第 1 步：清理未使用的代码和依赖（风险最低）
   - 第 2 步：后端目录整理（按模块重构，每个模块独立完成）
   - 第 3 步：配置文件拆分（每次一个配置块，验证后继续）
   - 第 4 步：前端目录整理（组件/视图/API 顺序重构）
   - 第 5 步：命名规范和风格统一（最后做，影响面小但改动量大）

**Warning signs:**
- 重构分支超过 3 天未合并
- 主分支和重构分支有冲突文件超过 5 个
- 团队成员不知道该在哪个分支上开发
- 重构范围不断扩大（scope creep）

**Phase to address:**
所有重构阶段（Phase 17-01 到 17-05），在规划阶段就确定增量策略。

---

### Pitfall 8: 重构过程中缺少可执行的验证策略

**What goes wrong:**
重构完成后"看起来没问题"就合并，但实际上某些功能已损坏。常见场景：
- 移动了 Service 类，编译通过但 `@Transactional` 注解失效（AOP 代理问题）
- 移动了 Vue 组件，`npm run dev` 正常但 `npm run build` 失败（生产构建有更严格的 tree-shaking）
- 运行时才发现 Mapper XML 找不到（编译期不检查 XML 路径）

当前项目的测试覆盖情况（共 12 个测试文件）：
- 后端：11 个测试类（包含 2 个秒杀相关、1 个分布式锁、1 个限流器）
- 前端：1 个测试文件（`ConsumerGroupList.test.ts`）

**Why it happens:**
开发者的验证习惯是"启动项目，点几个页面看看"，而不是执行完整的自动化验证。特别是：
- `npm run dev`（开发模式，HMR 即时更新）和 `npm run build`（生产构建，全面 tree-shaking 和路径解析）的行为不同
- 后端 `./gradlew.bat bootRun` 只启动默认 profile，不验证 test profile
- 测试套件如果没有覆盖到被移动的模块，就无法发现回归

**How to avoid:**
1. **每次移动文件后必须执行的最小验证**：
   - 前端：`npm run build`（比 `npm run dev` 更严格）
   - 后端：`./gradlew.bat build -x test`（确保编译和资源复制正确）
2. **每次重构提交后运行完整测试**：`./gradlew.bat test && npm run test`
3. **建立重构检查清单**（按模块）：

| 重构动作 | 验证命令 | 通过标准 |
|---------|---------|---------|
| 移动 Vue 组件 | `npm run build` | 无错误输出 |
| 移动 API 文件 | `npm run build` | 无错误输出 |
| 移动 Java 类 | `./gradlew.bat compileJava` | BUILD SUCCESSFUL |
| 移动 Mapper XML | `./gradlew.bat test --tests "*Mapper*"` | 所有 Mapper 测试通过 |
| 移动配置文件 | `./gradlew.bat bootRun`（两个 profile） | 启动无异常 |
| 移动 Store 文件 | `npm run build` + 浏览器验证登录 | 登录流程正常 |
| 修改路由配置 | `npm run build` + curl 所有路由 | 每个路由返回 200 或正确重定向 |

4. **端到端关键路径回归测试**（每次合并前）：
   - 登录 -> 获取 token -> 访问需认证页面 -> 刷新 token -> 登出
   - 秒杀预热 -> 查看库存 -> 执行秒杀 -> 查看订单
   - RocketMQ Topic 列表 -> Consumer Group 列表 -> 消息查询

**Warning signs:**
- "我就改了一个文件，不用测试"的想法出现
- 连续多个 commit 没有运行测试套件
- 只在开发环境验证，未在 test profile 下验证

**Phase to address:**
所有重构阶段。在每个阶段的 PLAN.md 中明确列出验证命令和通过标准。

---

### Pitfall 9: Git 历史丢失与目录重组的冲突

**What goes wrong:**
当使用 `git mv` 或 IDE 重构移动文件时，Git 能正确追踪重命名（`git log --follow` 可追溯历史）。但如果同时修改文件内容（如改 import 路径），Git 会将其视为"删除旧文件 + 创建新文件"，`git blame` 和 `git log --follow` 将丢失历史。

对于 Java 类，移动文件时 `package` 声明必须改动，这使得纯重命名几乎不可能——即使文件内容只改了 package 一行，Git 的相似度检测也可能不识别为重命名。

**Why it happens:**
Git 不存储"文件被重命名"的元数据，而是通过内容相似度（默认 50% 阈值）推断重命名。当文件移动（改变 package）且同时有内容修改时，相似度可能低于阈值。

**How to avoid:**
1. **分离移动和修改**：将重构拆成两步 commit
   - Commit 1：只移动文件（只改 package 声明，不改任何业务逻辑），用 `git mv`
   - Commit 2：修改内容和 import
2. **提高 Git 相似度检测阈值**：在 merge 或 log 时使用 `-M50%`（默认）或 `-M20%`（更宽松）
3. **在 commit message 中记录重命名关系**：格式如 `refactor: 将 XxxService 从 order.service 移到 order.service.impl（原路径: order.service.XxxService）`
4. **接受部分历史丢失**：对于改动超过 70% 的文件，保持可追踪的成本高于收益。确保重命名映射记录在 commit message 中即可

**What to accept:**
- 大型重构中，`git log --follow` 追溯部分文件历史会中断。这是可接受的代价
- 更重要的不是追溯每一个文件的历史，而是通过 `git bisect` 能定位到"哪个 commit 引入了 bug"——这只需要逻辑变更的 commit 粒度和清晰的 commit message
- 如果某个文件的历史特别重要（如核心业务逻辑），考虑不移动它，或单独处理

**Warning signs:**
- 一个 commit 同时移动和重写了大段代码
- `git log --follow <file>` 找不到旧历史
- commit message 只说"重构"但不说清楚移动了什么

**Phase to address:**
所有重构阶段的 commit 规范。在 `AGENTS.md` 或阶段 PLAN.md 中明确要求"先移动后修改"的 commit 策略。

---

### Pitfall 10: 前端 API 封装层重构导致请求全部失败

**What goes wrong:**
当前 API 层结构：
- `src/api/request.ts` — Axios 实例（baseURL, 拦截器, token 刷新逻辑）
- `src/api/auth.ts` — 认证接口
- `src/api/user.ts`、`src/api/role.ts`、`src/api/seckill.ts` 等 — 业务接口
- `src/api/types.ts` — 类型定义

所有视图组件都通过 `import { xxxApi } from '@/api/xxx'` 引用。如果重构 API 目录结构（例如按模块分文件夹 `src/api/seckill/index.ts`），需要更新所有视图组件的 import 路径。

特别地，`request.ts` 的响应拦截器（第 162-189 行）依赖 `ApiResponse` 类型（从 `./types` 导入），并且硬编码了对 401 的处理逻辑和 token 刷新流程。这个文件的重构风险最高——如果移动了 `request.ts` 或其依赖的 `types.ts`，整个应用的 HTTP 通信都会中断。

**Why it happens:**
前端没有编译期检查所有 import 的工具——TypeScript 只检查已打开文件的类型。`vite build` 会检查所有静态 import，但不会报告哪些文件 import 了已移动的模块（而是直接报错）。开发者可能只更新了部分引用，遗漏了其他文件。

**How to avoid:**
1. 移动 API 文件前，用 `grep "from '@/api/" src/` 列出所有引用该 API 文件的视图组件
2. 优先保持 API 目录扁平化（当前结构合理），除非 API 文件增加到 30+ 个才需要分子目录
3. 如果必须重构 API 层，在每个子目录中创建 `index.ts` barrel export，让外部引用路径不变
4. 绝不移动 `request.ts` 的路径——它是整个前端 HTTP 层的基石
5. 重构 API 层后运行 `npm run build`，确认没有 import 错误

**Warning signs:**
- `npm run build` 报 `Could not resolve '@/api/xxx'`
- 页面加载后所有 API 请求都 pending
- Axios 拦截器报 `Cannot read properties of undefined`

**Phase to address:**
前端目录整理阶段（Phase 17-01），API 层重构后必须构建验证 + 手动测试登录和数据加载。

---

## Technical Debt Patterns

Shortcuts that seem reasonable during refactoring but create long-term problems.

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| 用 `// TODO: 待重构` 注释代替实际移动文件 | 快速完成当前任务 | 注释堆积，新成员不知从何下手，代码在两个位置存在副本 | 仅在重构受阻于外部依赖时（如等待其他团队合并），最多保留 1 周 |
| 批量改 import 但不更新测试文件 | 节省测试维护时间 | 测试变成僵尸代码，CI 失败被忽略 | 绝不接受——测试必须与源码同步更新 |
| 删除"看起来没用"的配置项 | 减少文件行数 | 生产环境配置缺失导致运行时故障 | 先 grep 确认无引用，再通过 CI 验证，最后删除 |
| 复制旧目录结构到新位置，保留两份 | 零风险迁移 | 代码重复维护，新功能不知道该加在哪份 | 仅在验证期（最多 24 小时）保留，验证通过立即删除旧文件 |
| 把 application.yaml 拆得过细（10+ 个文件） | 关注点分离清晰 | 新人难以理解配置加载顺序，debug 困难 | 最多拆成 4-5 个文件（db、mq、security、business），保留主文件做 fallback |

## Integration Gotchas

Common mistakes when refactoring code that connects to external systems.

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| MyBatis + Mapper XML | 移动 Mapper 接口但不更新 XML namespace | 移动后立即检查对应 XML 的 `<mapper namespace="...">` 是否匹配 |
| RocketMQ Producer/Consumer | 重命名 topic 或 group 字符串 | Topic 和 Group 是基础设施常量，重构时只改变量名不改字符串值 |
| Redis 缓存 Key | 修改 key 前缀但不迁移或清空旧数据 | Key 前缀锁死不变；如果确实要改，需编写数据迁移脚本并灰度切换 |
| Flyway 迁移脚本 | 修改已执行的迁移文件 | Flyway 使用 checksum 校验，已执行的脚本一个字都不能改。新增需求写新的 V 版本文件 |
| Vue Router + Nginx | 路由路径变更后忘记更新 Nginx rewrite 规则 | 前端路由路径变更需同步检查 Nginx 配置的 try_files 和 proxy_pass 规则 |
| JWT Token 认证 | 移动 User 实体导致 JWT 载荷序列化失败 | 移动实体类后验证登录流程：登录 -> 获取 token -> 解析 token -> 访问受保护接口 |
| Element Plus 组件 | 重命名组件目录导致动态组件解析失败 | 项目中全局注册了所有 Element Plus Icons（main.ts 第 17-19 行），移动不会影响；但自定义封装的组件需验证 |

## Performance Traps

Patterns that cause performance issues after directory reorganization.

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Vite 预构建缓存失效 | `npm run dev` 首次启动变慢（2-3 分钟） | 移动 `node_modules` 依赖不变不会触发；删除 `node_modules/.vite` 缓存后可强制重建 | 大量移动文件后 |
| Gradle 增量编译失效 | `./gradlew.bat build` 全量重编译 | 移动文件后 Gradle 的增量编译检测会认为所有依赖该文件的模块都需重编译；这是正常的一次性成本 | 每次移动超过 10 个 Java 文件 |
| 路由懒加载 chunk 碎片化 | 每个页面独立 chunk，但命名随机，浏览器缓存失效 | 在路由的 `import()` 中使用 webpackChunkName 注释（Vite 也支持）：`() => import(/* webpackChunkName: "seckill" */ '@/views/seckill/SeckillIndex.vue')` | 页面超过 30 个时 |
| MyBatis XML 重解析 | 应用启动变慢 | MyBatis 在启动时解析所有 mapper XML；如果拆分了 mapper 目录，确保 `mapper-locations` 配置准确，避免扫描不必要目录 | Boot 启动时间增加超过 2 秒 |

## "Looks Done But Isn't" Checklist

Things that appear complete after refactoring but are missing critical validation.

- [ ] **Vue 组件移动**：检查 `npm run build` 是否成功（不仅仅是 `npm run dev`）
- [ ] **后端类移动**：检查是否有 `NoSuchBeanDefinitionException`（而不仅仅是编译通过）
- [ ] **Mapper XML 移动**：检查是否所有 CRUD 操作正常（而不仅仅是启动无报错）
- [ ] **配置文件拆分**：检查 test profile 是否也能启动（而不仅仅是 default profile）
- [ ] **Store 文件移动**：检查登录 -> 刷新 token -> 登出完整流程（而不仅仅是 store 能 import）
- [ ] **秒杀模块重构**：检查预热 -> 抢购 -> 订单创建 -> SSE 通知全链路（而不仅仅是秒杀页面能打开）
- [ ] **API 文件移动**：检查所有页面的数据加载（而不仅仅是首页能打开）
- [ ] **依赖清理**：检查删除的依赖是否被其他模块传递依赖（而不仅仅是当前模块不再 import）

## Pitfall-to-Phase Mapping

How the v1.5 milestone phases should address these pitfalls.

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| P1: Router 懒加载断裂 | Phase 17-01（前端目录整理） | `npm run build` + curl 每个路由 |
| P2: Mapper XML 断裂 | Phase 17-02（后端包整理） | `./gradlew.bat test --tests "*Mapper*"` |
| P3: Store 引用断裂 | Phase 17-01（前端目录整理） | 浏览器端到端：登录 -> 鉴权 -> 登出 |
| P4: 配置文件丢失 | Phase 17-03（配置整理） | 双 profile 启动验证 |
| P5: Component Scan 失效 | Phase 17-02（后端包整理） | `./gradlew.bat bootRun` + 启动日志检查 |
| P6: Redis Key 断裂 | Phase 17-02（后端包整理） | 秒杀全链路回归测试 |
| P7: 阻断正常开发 | 所有阶段 | 增量策略，每个模块单独合并 |
| P8: 缺少验证策略 | 所有阶段 | 每个阶段 PLAN.md 列出验证命令 |
| P9: Git 历史丢失 | 所有阶段 | Commit 策略：先移动后修改 |
| P10: API 层断裂 | Phase 17-01（前端目录整理） | `npm run build` + API 请求测试 |

## Recovery Strategies

When these pitfalls occur despite prevention, how to recover.

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Router 懒加载断裂 | LOW | 修复 import 路径 -> `npm run build` 验证。通常只需修改 1-2 个文件 |
| Mapper XML 断裂 | MEDIUM | 修复 namespace 或 mapper-locations -> 重启 -> 运行 Mapper 测试。可能需要修改多个 XML 文件 |
| Store 引用断裂 | LOW | 修复 import 路径 -> `npm run build`。影响文件通常少于 5 个 |
| 配置文件丢失 | HIGH | 从 git 历史恢复原配置 -> 对比差异 -> 重新拆分。可能导致短暂服务不可用 |
| Component Scan 失效 | LOW | 添加 `@ComponentScan` 或移回原包 -> 重启验证。修复通常是单行改动 |
| Redis Key 断裂 | HIGH | 需要数据迁移脚本或清空 Redis 重新预热。如果是生产数据丢失则不可逆 |
| 阻断正常开发 | MEDIUM | 合并重构分支到主分支（即使未完成），让团队在新结构上继续工作 |
| Git 历史丢失 | LOW | 不可恢复，但可在 commit message 中补充重命名映射说明 |

## Sources

- 项目源码分析：`springboot/src/main/java/cn/coderstory/springboot/` 下的包结构、`@MapperScan` 配置、Redis key 常量、RocketMQ topic 定义
- 项目源码分析：`app-vue/src/router/index.ts` 中的 27 条懒加载路由、`@/store/user` 的 Pinia store 引用
- 项目配置分析：`application.yaml`（154 行单文件）、`application-test.yaml`（99 行，部分配置缺失）
- MyBatis Plus 官方文档：`mapper-locations` 配置和 `namespace` 绑定机制
- Spring Boot 官方文档：`@SpringBootApplication` 组件扫描机制和 `spring.config.import` 配置导入
- Vite 官方文档：动态 import 和 `resolve.alias` 路径解析行为
- Git 官方文档：`git log --follow` 和重命名检测机制（`-M` 相似度阈值）

---
*Pitfalls research for: Vue 3 + Spring Boot 代码重构与目录整理*
*Researched: 2026-05-06*
