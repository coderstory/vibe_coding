# Codebase Concerns

**Analysis Date:** 2026-05-10

## Tech Debt

### 1. JWT 硬编码密钥

- Issue: `security.yaml` 中硬编码了 JWT 签名密钥 `mySecretKeyForJwtTokenGeneration123456789`，`JwtTokenProvider.java` 中兜底相同密钥。虽然注释标明"生产环境通过环境变量注入"，但配置文件中仍存在可用的默认值，构成安全风险。
- Files: `springboot/src/main/resources/config/security.yaml:12`, `springboot/src/main/java/cn/coderstory/springboot/config/JwtTokenProvider.java:25`
- Impact: 任何获得配置访问权限的人可伪造任意用户 JWT Token，完全绕过认证系统。
- Fix approach: 删除默认值，强制通过环境变量注入。在 `SecurityConfig` 或 `ApplicationRunner` 中增加启动时校验，如果 `jwt.secret` 仍为默认值则拒绝启动。

### 2. 数据库密码硬编码默认值

- Issue: `datasource.yaml` 中数据库密码默认值为 `123456`，用户名默认 `root`。Flyway 迁移脚本中也使用了同样的默认值。
- Files: `springboot/src/main/resources/config/datasource.yaml:13-14`, `springboot/build.gradle.kts:105-106`
- Impact: 与 JWT 密钥相同级别的安全风险 — 内网攻击者可凭默认凭据直接访问数据库。
- Fix approach: 删除默认值，强制环境变量注入。生产环境通过独立的 secrets 管理机制（如 Docker Secrets/Vault）注入。

### 3. 缺少方法级权限控制（RBAC 未实现）

- Issue: 19 个 Controller 中没有任何 `@PreAuthorize` 或 `@Secured` 注解。`SecurityConfig.java` 中未启用 `@EnableMethodSecurity`，`JwtAuthenticationFilter` 对所有通过 token 验证的用户硬编码 `ROLE_USER`。实际角色（roleId）在认证过程中从未被设置到 SecurityContext。菜单权限（`RoleMenuPermission`）模型存在，但服务端未执行任何权限校验。
- Files: `springboot/src/main/java/cn/coderstory/springboot/config/SecurityConfig.java`, `springboot/src/main/java/cn/coderstory/springboot/config/JwtAuthenticationFilter.java:59-64`, 19 个 Controller 文件
- Impact: 任意已登录用户可操作全部管理功能（创建用户、删除角色、修改菜单、重置密码等）。权限系统形同虚设。
- Fix approach: 在 `SecurityConfig` 中添加 `@EnableMethodSecurity`，`JwtAuthenticationFilter` 中从数据库查询用户角色并设置真实权限到 SecurityContext，对所有写操作 Controller 端点添加 `@PreAuthorize` 注解。

### 4. CSRF 完全禁用

- Issue: `SecurityConfig` 中 `csrf(AbstractHttpConfigurer::disable)`。对于 JWT 无状态应用来说这是常见做法，但没有额外的反 CSRF 保护（如同源策略检查、SameSite Cookie 配置等）。
- Files: `springboot/src/main/java/cn/coderstory/springboot/config/SecurityConfig.java:46`
- Impact: 虽然 JWT 通常存储在 `Authorization` header 中（不受传统 CSRF 影响），但如果前端将 Token 存储在 Cookie 中，则存在 CSRF 攻击面。
- Fix approach: 确认 Token 仅通过 `Authorization` header 传递。如需要 Cookie 存储，应添加 CSRF 保护或使用 SameSite=Strict。

### 5. CORS 配置过于宽松

- Issue: `CorsConfig` 允许所有来源 (`*`)、所有方法、所有请求头，且允许携带凭据 (`allowCredentials=true`)。`addAllowedOriginPattern("*")` 与 `allowCredentials(true)` 的组合在部分浏览器中可能触发安全警告。
- Files: `springboot/src/main/java/cn/coderstory/springboot/config/CorsConfig.java:28-31`
- Impact: 任意第三方网站可发起跨域请求（前提是 Token 通过 Cookie 传递），增加 CSRF/XSS 攻击面。
- Fix approach: 生产环境限制到具体的域名白名单。注释已标记"生产环境应限制允许的来源域名"但未实现。

### 6. AuthService 中使用 RuntimeException 而非 BusinessException

- Issue: `AuthService.login()` 中密码错误/用户不存在/用户禁用均抛出 `RuntimeException`，这些异常被 `GlobalExceptionHandler` 的 `handleException(Exception.class)` 捕获，返回 500 错误。应使用 `BusinessException.badRequest()` 或 `BusinessException.unauthorized()` 返回正确的 HTTP 状态码。
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/auth/AuthService.java:49,53,57`
- Impact: 登录失败返回 500 而非 401/400，前端难以区分"系统错误"和"身份验证失败"，用户体验差，且日志中错误级别偏高。
- Fix approach: 改为 `throw BusinessException.unauthorized("用户名或密码错误")`。

### 7. 架构测试被禁用

- Issue: `ArchitectureTest` 中两个 ArchUnit 规则（Controller 不应直接依赖 Mapper、Service 不应依赖 Controller）被 `@Disabled` 标记，注释中称"Phase 18 修复后启用"。但 Phase 18 已标注为"代码重构"阶段，目前仍存在违规。
- Files: `springboot/src/test/java/cn/coderstory/springboot/ArchitectureTest.java:16,26`
- Impact: 分层依赖违规无法被自动化检测，架构退化风险持续存在。从 `UserController` 实际代码看，`RoleMapper` 仍被直接注入（`line 34: private final RoleMapper roleMapper`）。
- Fix approach: 修复 `UserController` 中的 `RoleMapper` 直接注入，改为通过 `RoleService` 访问。修复后启用禁用的测试。

### 8. Checkstyle 忽略失败

- Issue: `build.gradle.kts:94` 设置 `isIgnoreFailures = true`，代码风格检查不会导致构建失败，使其失去了作为质量门禁的意义。
- Files: `springboot/build.gradle.kts:94`
- Impact: Checkstyle 规则形同虚设，风格违规不会被拦截。
- Fix approach: 修复现有违规后设置为 `false`，或暂时保留但建立定期 Review 机制。

### 9. 请求参数缺少 Bean Validation

- Issue: 所有 Controller 中没有任何 `@Valid` 或 `@Validated` 注解，请求体验证完全依赖手动 `Map.get()` 和类型转换，缺少格式校验、必填校验和错误消息标准化。
- Files: 所有 19 个 Controller 文件
- Impact: 参数验证不统一，空值检查遗漏可导致 NPE（`UserController.createUser` 中 `request.get("password")` 未判空直接传参）。错误的参数类型在运行时才暴露。
- Fix approach: 引入 Jakarta Bean Validation，为重要 DTO 添加 `@NotBlank`、`@NotNull` 等注解，Controller 参数前加 `@Valid`。

### 10. 剩余的 TODO

- Issue: `ReservationNotifyServiceImpl.java:111` 包含一个未实现的 `TODO(#TODO-1)`，提醒通知逻辑仍是空壳。
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/seckill/impl/ReservationNotifyServiceImpl.java:111`
- Impact: 预约提醒功能不可用。
- Fix approach: 实现实际的提醒通知逻辑（如通过 SSE 或消息队列推送）。

---

## Known Bugs

### 1. 首次 SSE 连接建立时可能丢失消息

- Symptoms: 前端 SSE 连接建立与秒杀请求发送之间存在时序竞争。如果秒杀结果在 SSE 连接完全就绪前到达，消息会丢失。
- Files: `springboot/src/main/java/cn/coderstory/springboot/sse/seckill/SeckillSseService.java`, 前端 `SeckillSseController.subscribe()`
- Trigger: 高并发秒杀场景下，消息处理速度超过 SSE 连接建立速度。
- Workaround: CLAUDE.md 提到"前端先生成 queueId，再建立 SSE 连接"的修复，但需确认已实现。
- Status: CLAUDE.md 中已记录（Pitfall 8），但未验证修复完整性。

### 2. QpsChart 内存泄漏（ECharts dispose 缺失）

- Symptoms: `QpsChart.vue` 在 `onUnmounted` 中只清除了定时器，但未释放 ECharts 实例。vue-echarts 的 `v-chart` 组件虽然可能在内部处理 dispose，但 `chartOption` 作为响应式引用每次更新都会触发重新渲染，长时间运行后可能导致内存增长。
- Files: `app-vue/src/views/rocketmq/QpsChart.vue:102-104`
- Trigger: 用户在 RocketMQ 监控页面和子页面间反复切换，每个页面切换创建新的图表实例，旧实例未被释放。
- Workaround: 当前无。
- Fix approach: 在 `onUnmounted` 中通过 ref 获取 chart 实例并调用 `dispose()`。或者依赖 vue-echarts 的自动清理（需确认 v-chart 组件实现了 onUnmounted dispose）。

---

## Security Considerations

### 1. X-Forwarded-For IP 伪造

- Risk: 多个位置使用 `X-Forwarded-For` 头获取客户端 IP（`AuthController.java:134`, `AuditAspect.java:231`, `SeckillController.java:120`）。如果服务部署在反向代理之后且未正确配置可信代理列表，攻击者可伪造 IP 头绕过 IP 限流和黑名单。
- Files: `springboot/src/main/java/cn/coderstory/springboot/controller/auth/AuthController.java:132-141`, `springboot/src/main/java/cn/coderstory/springboot/aspect/AuditAspect.java:230-241`
- Current mitigation: 三阶回退策略（X-Forwarded-For -> X-Real-IP -> getRemoteAddr()），但缺少可信代理 IP 白名单校验。
- Recommendations: 在反向代理层配置 `proxy_set_header X-Real-IP $remote_addr;`，后端只信任 `X-Real-IP` 或使用 Spring 的 `ForwardedHeaderFilter`。

### 2. 认证服务不区分"用户不存在"和"密码错误"

- Risk: `AuthService.login()` 对"用户不存在"和"密码错误"返回相同的错误消息，这是良好的安全实践。但两者均抛出 `RuntimeException`（返回 500），与"用户被禁用"状态无法区分。
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/auth/AuthService.java:46-57`
- Current mitigation: 错误消息统一为"用户名或密码错误"。
- Recommendations: 保持统一错误消息，但返回 401 状态码而非 500。

### 3. 无登录失败次数限制

- Risk: `/api/auth/login` 端点无暴力破解防护（无 IP 级别的失败计数、无 CAPTCHA、无账号锁定机制）。
- Files: `springboot/src/main/java/cn/coderstory/springboot/controller/auth/AuthController.java:42-51`
- Current mitigation: 无。
- Recommendations: 添加 Redis 计数器，5 次失败后临时锁定 IP 或账号 15 分钟。或者集成验证码服务。

---

## Performance Bottlenecks

### 1. OSHI 磁盘/网络列表全量采集

- Problem: `sampleDisks()` 和 `sampleNetwork()` 每次采集都创建完整的 `HWDiskStore`/`NetworkIF` 列表，然后通过循环逐个进行差分计算。差分计算在 Java 堆上进行，涉及大量对象分配。
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/impl/HardwareMetricsServiceImpl.java:232-320`
- Cause: OSHI 返回的列表是每次 API 调用时从原生层重新构建的，不是缓存引用。
- Improvement path: 对磁盘/网络数量较多的服务器，考虑只采样有变化的设备。对于无变化的设备跳过对象创建。

### 2. Trend 查询全量扫描

- Problem: `HardwareMonitorController.extractTrend()` 每次查询都会遍历整个历史缓冲区的快照（最大 360 点），然后根据指标类型逐一提取。虽然 360 点不大，但每次请求都完整复制列表再做 `subList`。
- Files: `springboot/src/main/java/cn/coderstory/springboot/controller/monitor/hardware/HardwareMonitorController.java:98-115`
- Cause: `RingBuffer.snapshot()` 每次都创建新的 ArrayList 并复制所有元素。
- Improvement path: 对于频繁的短范围查询（如 range=10），直接从 RingBuffer 数组提取，避免全量复制。或者将 RingBuffer 改为支持直接按索引范围读取。

### 3. RingBuffer 读写锁竞争

- Problem: `RingBuffer` 使用 `ReentrantReadWriteLock` 保护。每 2s 采集线程获取写锁写入，而前端轮询（每 5s）和趋势查询（每请求）获取读锁。虽然读写锁允许多读，但写锁会阻塞所有读操作。
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/RingBuffer.java:48-81`
- Cause: 写锁获取期间（`add()` 方法），所有读操作（`snapshot()`, `size()`, `getCurrentMetrics()`）必须等待。
- Improvement path: 使用 `StampedLock` 的乐观读模式，或者采用无锁数据结构（如 `AtomicReferenceArray` + volatile head 索引）。

### 4. 前端监控轮询无退避

- Problem: `MonitorDashboard.vue` 每 5s 轮询，`QpsChart.vue` 每 10s 轮询。网络错误时不会降低轮询频率，持续发起请求加重服务端压力。
- Files: `app-vue/src/views/monitor/MonitorDashboard.vue:18`, `app-vue/src/views/rocketmq/QpsChart.vue:99`
- Cause: 定时器固定间隔，未检测上次请求是否成功。
- Improvement path: 在 `catch` 块中动态调整轮询间隔（如错误时翻倍，最大 60s），成功时重置。

---

## Fragile Areas

### 1. UserController 的 Map 参数解析

- Files: `springboot/src/main/java/cn/coderstory/springboot/controller/user/UserController.java:96-112`
- Why fragile: 手动从 `Map<String, Object>` 逐字段解析和类型转换，`roleIdObj` 需要三重判断（null、instanceof Number、longValue）。任何字段名拼写错误或类型不匹配都会在运行时静默失败（字段值为 null）。
- Safe modification: 引入 `CreateUserRequest` DTO，使用 `@Valid` 注解校验。
- Test coverage: 无针对 `createUser` 端点参数验证的测试。

### 2. SecurityConfig 白名单解析

- Files: `springboot/src/main/resources/config/security.yaml:6`
- Why fragile: 白名单路径通过逗号分隔的字符串注入，Spring Boot 自动拆分为数组。如果某路径包含逗号（极不可能但可能），会导致路径解析错误。更严重的是，白名单路径 `/api/knowledge/files/**` 放行了知识库文件下载，但未对文件访问权限做额外校验。
- Test coverage: 无安全相关的集成测试验证白名单是否生效。

### 3. HardwareMonitorController 与 RingBuffer 容量耦合

- Files: `springboot/src/main/java/cn/coderstory/springboot/controller/monitor/hardware/HardwareMonitorController.java:72`
- Why fragile: Controller 中硬编码 360 作为最大范围限制，与 `MonitorHardwareProperties` 的默认 `trendBufferSize = 360` 耦合。如果配置中修改了 `trend-buffer-size` 但 Controller 未同步更新，会出现请求范围大于实际缓冲区的情况。
- Safe modification: 将最大范围值抽取到配置或注入 `MonitorHardwareProperties`。

### 4. AuthService.logout 不使 Token 失效

- Files: `springboot/src/main/java/cn/coderstory/springboot/service/auth/AuthService.java:133-138`
- Why fragile: 登出操作仅记录审计日志，不使 Token 失效。Token 在被盗后可在剩余有效期内（最长 24 小时）继续使用。无 Token 黑名单机制。
- Test coverage: 无登出相关测试。

---

## Scaling Limits

### 1. RingBuffer 趋势数据容量

- Current capacity: 360 个数据点（默认配置），每 10s 写入一次（samplingInterval=2000ms x trendDecimation=5），覆盖约 1 小时。
- Limit: 单点趋势数据（`HardwareMetricsDTO`）包含 CPU、内存、磁盘列表和网络列表的完整快照，每个快照可达数 KB。360 个点全量存储在内存中。
- Scaling path: 趋势数据的存储不应与服务同生命周期。对于长时间运行的监控服务，需要将历史趋势持久化到时序数据库（如 InfluxDB）或至少写入数据库表。

### 2. SSE 连接数

- Current capacity: `ConcurrentHashMap` 存储，无连接数上限。
- Limit: 每个 SSE 连接占用一个 Tomcat 线程（即使启用虚拟线程，仍有内存开销）。默认 5 分钟超时，超时后自动清理。
- Scaling path: 添加最大连接数限制，在超出时拒绝新连接并返回 429。考虑使用 WebSocket 替代 SSE 以获得更好的资源利用率。

### 3. 单线程采集瓶颈

- Current capacity: 单线程 `ScheduledExecutorService` 每 2s 执行一次采集。
- Limit: 采集间隔受限于最慢的采样步骤（当前为磁盘和网络，约 100-500ms）。如果采集耗时超过 2s，会导致调度偏移和线程堆积。
- Scaling path: 将 CPU/内存和磁盘/网络拆分为独立的调度线程，磁盘/网络使用更长的间隔（如 10s）。

---

## Dependencies at Risk

### 1. Spring Boot AOP 版本不匹配

- Risk: `libs.versions.toml` 中 `spring-boot-starter-aop` 指定为 `4.0.0-M2`（里程碑版本），而 Spring Boot BOM 为 `4.1.0-RC1`（发布候选版本）。AOP starter 是 Spring Boot BOM 管理的依赖，不应单独指定版本。
- Files: `springboot/gradle/libs.versions.toml:6,35`
- Impact: 版本不一致可能导致 AOP 通知行为异常、编译警告或运行时错误。如果 `4.0.0-M2` 有未修复的 bug，可能影响审计切面的正常工作。
- Migration plan: 删除 `spring-boot-starter-aop` 的 `version.ref`，让它从 BOM 继承版本。

### 2. OSHI FFM API 兼容性

- Risk: `oshi-core-ffm 7.1.0` 使用 Java FFM（Foreign Function & Memory）API，需要 Java 22+ 且需要 `--enable-preview` 和 `--enable-native-access` JVM 参数。项目使用 Java 26，FFM API 在 Java 22 中经历了多次变更（从孵化到预览到最终），版本兼容性需验证。
- Files: `springboot/gradle/libs.versions.toml:66`, `springboot/build.gradle.kts:82-83`
- Impact: OSHI FFM 强烈绑定 JVM 版本。升级 Java 版本时必须同步升级 OSHI，否则可能因 FFM API 变更导致 UnsatisfiedLinkError 或 Crash。Java 22 中 FFM 最终化后的 API 与 Java 19-21 的孵化 API 不兼容。
- Migration plan: 锁定 OSHI 与 Java 版本的映射关系，在 CI 中验证兼容性。

### 3. MyBatis Plus 版本与 Spring Boot 4.x 兼容性

- Risk: `mybatis-plus = "3.5.16"` 使用 `mybatis-plus-spring-boot4-starter` 模块（针对 Spring Boot 4.x），但 MyBatis Plus 3.5.16 是否为 Spring Boot 4.1 RC1 做好了兼容测试，需验证。
- Files: `springboot/gradle/libs.versions.toml:42`
- Impact: 启动失败、SQL 执行异常或自动配置不生效。
- Migration plan: 关注 MyBatis Plus 发布说明，升级到与 Spring Boot 4.1 兼容的版本。

---

## Missing Critical Features

### 1. JWT Token 黑名单/吊销机制

- Problem: 登出后 Token 仍有效直到过期（最长 24 小时）。无服务端 Token 吊销能力。
- Blocks: 安全事件响应（账号被盗后无法强制下线）、密码修改后旧 Token 仍有效。

### 2. 密码强度策略

- Problem: 用户注册和密码重置时无任何密码强度要求（长度、复杂度、常见密码库检查）。
- Blocks: 弱密码攻击防护。

### 3. 审计日志查询接口缺少鉴权

- Problem: `/api/audit/logs` 接口（如果存在）可能被任意已登录用户查询，暴露所有用户的操作记录。
- Fix: 为审计日志相关端点添加 `@PreAuthorize`。

### 4. 硬件监控端点无鉴权

- Problem: `/api/monitor/hardware/current`、`/api/monitor/hardware/trend` 和 `/api/monitor/hardware/system` 三个端点虽然通过 `authorizeHttpRequests` 的 `.anyRequest().authenticated()` 保护（需要登录），但无 RBAC 粒度控制。任何登录用户都可查看服务器硬件指标，可能泄露基础设施信息。

---

## Test Coverage Gaps

### 1. 安全/认证模块无测试

- What's not tested: `JwtTokenProvider`、`JwtAuthenticationFilter`、`SecurityConfig`、`CorsConfig` 均无单元测试或集成测试。Token 刷新流程、白名单路径放行、过期 Token 拒绝等关键安全行为无验证。
- Files: `springboot/src/main/java/cn/coderstory/springboot/config/JwtTokenProvider.java`, `JwtAuthenticationFilter.java`, `SecurityConfig.java`
- Risk: 安全配置修改导致认证绕过漏洞，不会被自动化测试捕获。
- Priority: High

### 2. SSE 模块无测试

- What's not tested: `SeckillSseService` 和 `SeckillSseController` 均无测试。并发连接管理、连接超时清理、错误回调处理均未验证。
- Files: `springboot/src/main/java/cn/coderstory/springboot/sse/seckill/SeckillSseService.java`, `controller/seckill/SeckillSseController.java`
- Risk: SSE 连接泄漏、消息丢失信道、超时未清理导致内存泄漏。
- Priority: Medium

### 3. 前端只有 2 个测试文件

- What's not tested: 前端 41 个 Vue/TS 源文件中仅有 2 个测试文件（`api/__tests__/user.test.ts` 和 `rocketmq/__tests__/ConsumerGroupList.test.ts`）。监控面板、SSE 订阅、知识库等模块的前端逻辑完全无测试。
- Files: `app-vue/src/`（约 40 个无测试的源文件）
- Risk: 前端重构或组件升级时无法验证回归。
- Priority: High

### 4. 集成测试仅限 Windows

- What's not tested: `HardwareMetricsServiceIT` 标记为 `@EnabledOnOs(OS.WINDOWS)` 且被 `build.gradle.kts` 的 `exclude("**/*IT.class")` 排除。需要手动运行。这意味着后端在 CI/CD 流水线中无有效集成测试。
- Files: `springboot/src/test/java/cn/coderstory/springboot/service/monitor/hardware/HardwareMetricsServiceIT.java`
- Risk: OSHI 在不同操作系统上的行为差异不会被发现。CI 流水线无法运行集成测试。
- Priority: Medium

### 5. 部分 Service 实现类无测试

- What's not tested: 22 个 Service 实现类中，约一半没有对应的测试文件：
  - `MenuService` 实现
  - `KnowledgeService` 系列
  - `RocketMQ` 相关 Service
  - `MonitorService`
  - `AuditService`
  - `BlacklistService`
  - `IdempotentService`
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/` 下约 10 个无测试的实现
- Risk: 这些模块的修改缺少回归保护。
- Priority: Medium

---

*Concerns audit: 2026-05-10*
