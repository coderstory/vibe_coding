
# Phase 31: 后端采集服务 + REST API — Context

**Gathered:** 2026-05-09
**Status:** Ready for planning

<domain>
## Phase Boundary

后端集成 OSHI 7.x FFM 库，实现 CPU/内存/磁盘/网络指标定时采集，提供 REST API 和环形缓冲区趋势查询。
不涉及 SSE 推送、不涉及前端页面。

**成功标准（来自 ROADMAP.md）：**
1. 后端启动后自动以 2s 间隔采集 CPU 使用率（总体和各核）、内存指标、磁盘容量和分区信息、磁盘 IOPS/读写速度、网络接口吞吐量
2. `GET /api/monitor/hardware/current` 返回当前所有硬件指标快照 JSON
3. `GET /api/monitor/hardware/trend?metric=cpu&range=360` 返回近 1h 环形缓冲区趋势数据
4. `GET /api/monitor/hardware/system` 返回系统基本信息（OS 版本、运行时间、进程数）
5. 采集服务通过接口抽象，OSHI 依赖可 mock，核心采集/计算/缓存逻辑有单元测试覆盖

**需求（来自 REQUIREMENTS.md）：**
- HWM-01: 系统集成 OSHI 7.x FFM 库，在 JDK 26 上正常采集硬件指标
- HWM-02: 后端定时采集 CPU 使用率（2s 间隔），支持总体和各核使用率
- HWM-03: 后端定时采集内存指标（总量、已用、可用、使用率）
- HWM-04: 后端定时采集磁盘容量和分区信息
- HWM-05: 后端定时采集磁盘 IOPS 和读写速度
- HWM-06: 后端定时采集网络接口吞吐量（上下行速率）
- HWM-07: 后端提供 REST API 返回当前硬件指标快照
- HWM-08: 后端维护内存环形缓冲区（360 点 = 1h），支持趋势数据查询
- HWM-09: 后端采集系统基本信息（OS 版本、运行时间、进程数）
- TDD-01: 后端采集服务设计为接口模式，OSHI 依赖可 mock，便于单元测试

</domain>

<decisions>
## Implementation Decisions

### OSHI 库选择
- **D-01:** 使用 `oshi-core-ffm`（FFM API），JDK 26 禁用 JNA
- **D-02:** 版本使用 Gradle `~` 范围（latest compatible），构建验证失败再锁定具体版本
- **D-03:** SystemInfo 在 `@PostConstruct` 时初始化，启动时 fail-fast

### DTO 设计
- **D-04:** 单一大 DTO `HardwareMetricsDTO`，含嵌套分类 DTO（CpuMetricsDTO/MemoryMetricsDTO/DiskMetricsDTO/NetworkMetricsDTO），一次返回全部
- **D-05:** 后端格式化数值（返回格式化字符串 + 原始值，如 `{value: 8.5, format: "8.5 GB", unit: "GB"}`）
- **D-06:** 趋势 DTO 复用同一类型，附加 `timestamp` 字段
- **D-07:** CPU 返回总体使用率 + 各核数组（`perCoreLoad: number[]`）

### 采样策略
- **D-08:** 统一 2s 主循环，CPU/内存每次更新，磁盘/网络每 3 次采样更新一次（内部计数器）
- **D-09:** 使用 `ScheduledExecutorService` 手动管理调度线程池
- **D-10:** 采样与 API 之间使用 `ReentrantReadWriteLock` 保证强一致性
- **D-11:** 调度器放在独立 `@Configuration` 类 `MonitoringSchedulerConfig`

### 错误处理
- **D-12:** OSHI 采集采用局部降级策略 —— 单个指标采集失败不影响其他指标，失败指标返回 null/默认值

### TDD
- **D-13:** 单个 `HardwareMetricsService` 接口 + 实现类，OSHI 调用通过接口抽象
- **D-14:** 全链路测试包括集成测试（真实 OSHI 加载，需 Windows CI 环境）
- **D-15:** 使用 Mockito 进行单元测试

### 现有代码关系
- **D-16:** 新建 `service/monitor/hardware/` 包，不修改现有 `MonitorService`（Redis 秒杀指标）和 `MonitorController`
- **D-17:** 现有 `monitor-service-wiring.md` TODO 通过 Phase 31 不处理，独立于硬件监控范围

### Claude's Discretion
- 环形缓冲区具体实现（数组 vs `CircularFifoQueue`）
- 系统信息刷新频率（OS 版本不变，运行时间/进程数可在每次采样时更新）
- REST API 参数校验细节
- 配置项命名和默认值
- DTO 中具体字段命名

</decisions>

<canonical_refs>
## Canonical References

### 阶段定义
- `.planning/ROADMAP.md` §Phase 31 — 阶段目标和成功标准
- `.planning/REQUIREMENTS.md` §v1.8 — HWM-01~09, TDD-01 需求详细说明

### 现有架构
- `.planning/research/ARCHITECTURE.md` — 硬件监控整体架构设计、SSE 模式、反模式
- `.planning/research/SUMMARY.md` — 调研摘要、特征集、风险、构建顺序
- `.planning/research/PITFALLS.md` — 已知陷阱和风险缓解
- `.planning/research/FEATURES.md` — 特征全景、采集频率标准

### 现有代码模式
- `springboot/.../service/monitor/MonitorService.java` — 现有 Redis 监控服务（不修改）
- `springboot/.../controller/monitor/MonitorController.java` — 现有监控控制器（不修改）
- `springboot/.../sse/seckill/SeckillSseService.java` — SSE 模式参考（Phase 32 用）
- `springboot/gradle/libs.versions.toml` — 依赖版本管理（需添加 OSHI）
- `springboot/build.gradle.kts` — 构建文件（需添加 OSHI 依赖）

### 技术标准
- `CLAUDE.md` §项目架构、§代码风格（后端规范）、§构建命令
- `springboot/src/main/resources/config/business.yaml` — 业务配置（monitor.hardware 放这里）

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- 暂无直接可复用的硬件监控代码（全新功能模块）
- `SeckillSseService` 的 SSE 模式将在 Phase 32 复用

### Established Patterns
- 按业务域垂直分包（`{domain}/controller/`, `{domain}/service/`）
- `@RequiredArgsConstructor` + `private final` 依赖注入
- `ApiResponse<T>` 统一响应封装
- `@Slf4j` 日志注解
- MyBatis Plus `LambdaQueryWrapper`（本阶段暂不需要数据库操作）

### Integration Points
- `libs.versions.toml` + `build.gradle.kts` — 新增 OSHI 依赖
- `config/business.yaml` — 新增 `monitor.hardware` 配置段
- `controller/monitor/hardware/` 包 — 新建包，独立于现有 MonitorController
- 无需数据库迁移（Flyway 脚本不变）

</code_context>

<specifics>
## Specific Ideas

- OSHI 7.x FFM 使用 `oshi.ffm.SystemInfo`（非 `oshi.SystemInfo`）作为入口点，API 导入路径不变（`oshi.hardware.*`, `oshi.software.os.*`）
- 磁盘/网络每 3 次采样 = 6s 更新一次，内部计数器实现
- 后端格式化使用统一工具方法，返回 `{value, format, unit}` 结构

</specifics>

<deferred>
## Deferred Ideas

- SSE 实时推送 → Phase 32
- 前端监控页面 → Phase 32-33
- 环形缓冲区 360 点支持 1h 趋势 → 在本阶段实现（HWM-08），但前端图表展示在 Phase 33
- 安全加固（ADMIN 角色校验） → Phase 34
- 现有 `monitor-service-wiring.md` TODO 修复 → 独立于硬件监控，由原有计划处理

</deferred>

---

*Phase: 31-后端采集服务-REST-API*
*Context gathered: 2026-05-09*
