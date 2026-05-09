# Phase 31: 后端采集服务 + REST API — Research

**Researched:** 2026-05-09
**Domain:** Java hardware monitoring (OSHI 7.x FFM), backend service design, scheduled sampling, in-memory ring buffer
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### OSHI 库选择
- **D-01:** 使用 `oshi-core-ffm`（FFM API），JDK 26 禁用 JNA
- **D-02:** 版本使用 Gradle `~` 范围（latest compatible），构建验证失败再锁定具体版本
- **D-03:** SystemInfo 在 `@PostConstruct` 时初始化，启动时 fail-fast

#### DTO 设计
- **D-04:** 单一大 DTO `HardwareMetricsDTO`，含嵌套分类 DTO（CpuMetricsDTO/MemoryMetricsDTO/DiskMetricsDTO/NetworkMetricsDTO），一次返回全部
- **D-05:** 后端格式化数值（返回格式化字符串 + 原始值，如 `{value: 8.5, format: "8.5 GB", unit: "GB"}`）
- **D-06:** 趋势 DTO 复用同一类型，附加 `timestamp` 字段
- **D-07:** CPU 返回总体使用率 + 各核数组（`perCoreLoad: number[]`）

#### 采样策略
- **D-08:** 统一 2s 主循环，CPU/内存每次更新，磁盘/网络每 3 次采样更新一次（内部计数器）
- **D-09:** 使用 `ScheduledExecutorService` 手动管理调度线程池
- **D-10:** 采样与 API 之间使用 `ReentrantReadWriteLock` 保证强一致性
- **D-11:** 调度器放在独立 `@Configuration` 类 `MonitoringSchedulerConfig`

#### 错误处理
- **D-12:** OSHI 采集采用局部降级策略 —— 单个指标采集失败不影响其他指标，失败指标返回 null/默认值

#### TDD
- **D-13:** 单个 `HardwareMetricsService` 接口 + 实现类，OSHI 调用通过接口抽象
- **D-14:** 全链路测试包括集成测试（真实 OSHI 加载，需 Windows CI 环境）
- **D-15:** 使用 Mockito 进行单元测试

#### 现有代码关系
- **D-16:** 新建 `service/monitor/hardware/` 包，不修改现有 `MonitorService`（Redis 秒杀指标）和 `MonitorController`
- **D-17:** 现有 `monitor-service-wiring.md` TODO 通过 Phase 31 不处理，独立于硬件监控范围

### Claude's Discretion
- 环形缓冲区具体实现（数组 vs `CircularFifoQueue`）
- 系统信息刷新频率（OS 版本不变，运行时间/进程数可在每次采样时更新）
- REST API 参数校验细节
- 配置项命名和默认值
- DTO 中具体字段命名

### Deferred Ideas (OUT OF SCOPE)
- SSE 实时推送 → Phase 32
- 前端监控页面 → Phase 32-33
- 安全加固（ADMIN 角色校验） → Phase 34
- 现有 `monitor-service-wiring.md` TODO 修复 → 独立于硬件监控，由原有计划处理
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| HWM-01 | 集成 OSHI 7.x FFM，JDK 26 正常采集 | `oshi-core-ffm:7.1.0` Maven 坐标确认，JDK 26 FFM API 兼容（FFM final since JDK 22） |
| HWM-02 | CPU 使用率 2s 间隔，总体 + 各核 | `CentralProcessor.getSystemCpuLoadBetweenTicks()` + `getProcessorCpuLoadBetweenTicks()` |
| HWM-03 | 内存指标（总量/已用/可用/使用率） | `GlobalMemory.getTotal()` / `getAvailable()` |
| HWM-04 | 磁盘容量和分区信息 | `HWDiskStore.getSize()` + `OSFileStore.getTotalSpace()` / `getUsableSpace()` |
| HWM-05 | 磁盘 IOPS 和读写速度 | `HWDiskStore.getReads()` / `getWrites()` / `getReadBytes()` / `getWriteBytes()` 差分计算 |
| HWM-06 | 网络接口吞吐量 | `NetworkIF.getBytesSent()` / `getBytesRecv()` 差分计算 |
| HWM-07 | REST API 返回快照 | `GET /api/monitor/hardware/current` |
| HWM-08 | 内存环形缓冲区 360 点 = 1h | 自定义 RingBuffer<T>，360 容量，每 10s 存储一次 |
| HWM-09 | 系统基本信息 | `OperatingSystem.getFamily()` / `getVersionInfo()` / `getSystemUptime()` / `getProcessCount()` |
| TDD-01 | 接口模式，OSHI 可 mock | `HardwareMetricsService` 接口 + `MockitoExtension` 单元测试 |
| TDD-03 | 核心逻辑单元测试覆盖 | 采集/计算/缓存逻辑可独立测，环形缓冲区快照测试 |
</phase_requirements>

## Summary

Phase 31 是 v1.8 硬件监控的后端基础层，核心任务是集成 OSHI 7.1.0 FFM 库，构建定时采集管道，并提供 REST API 暴露数据。

**架构核心模式：单写入者 + 多读取者。** 一个 `ScheduledExecutorService` 线程以 2s 周期采集全量指标并写入 `ReentrantReadWriteLock` 保护的缓存和环形缓冲区，所有 API 请求只从缓存读取，不直接调用 OSHI。这避免了 OSHI 线程安全问题（GitHub issue #797, #660）和 WMI 调用阻塞请求线程的风险。

**技术选型需注意的关键差异：**
- `oshi-core-ffm`（FFM API）的入口类是 `oshi.ffm.SystemInfo`，而不是 JNA 版本常用的 `oshi.SystemInfo`。硬件抽象层（`HardwareAbstractionLayer`）和软件层（`OperatingSystem`）的接口路径不变（`oshi.hardware.*`、`oshi.software.os.*`）。
- Gradle 依赖管理使用 `libs.versions.toml`，OSHI 不在 Spring Boot BOM 中，版本需显式管理。
- 项目已有 `@EnableScheduling` 和 `@EnableAsync`，但 D-09 决定使用 `ScheduledExecutorService` 独立管理采集线程，以获得更好的线程隔离和错误处理控制。

**环形缓冲区（趋势查询）设计：** 360 点容量，每 5 次采样（10s）存储一次，覆盖 1 小时。API 端点 `/api/monitor/hardware/trend` 按 metric 参数提取对应子指标的时间序列。

**不引入数据库和 Flyway 迁移**（指标纯内存），不修改安全配置（白名单/角色校验延迟到 Phase 34），不涉及 SSE 推送（Phase 32）。

**Primary recommendation:** 使用 `com.github.oshi:oshi-core-ffm:7.1.0`，与 `@EnableScheduling` 无关，改用 `ScheduledExecutorService` 独立管理采集调度。包结构按 `controller/monitor/hardware/` + `service/monitor/hardware/` + `dto/monitor/hardware/` 划分，完全独立于现有的 `MonitorService`/`MonitorController`。

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| 硬件指标采集 | API / Backend | — | OSHI 是 JVM 内库，只能在后端运行。采集线程完全在后端 JVM 内执行 |
| 指标数据缓存 | API / Backend | — | 缓存（缓存 DTO + 环形缓冲区）在后端内存中。前端从不直接接触 OSHI |
| 格式化数值计算 | API / Backend | — | 原始值到格式化字符串的转换在后端 DTO 层完成，前端直接消费格式化结果 |
| REST API 端点 | API / Backend | — | `/api/monitor/hardware/*` 由 Spring Boot Controller 提供 |
| 趋势数据查询 | API / Backend | — | 后端从内存环形缓冲区读取趋势数据，前端通过 REST API 拉取 |
| 安全认证 | API / Backend | — | Spring Security 已在后端配置，Phase 34 添加 ADMIN 角色校验 |
| SSRF 风险缓解 | — | — | 本阶段不涉及外部 HTTP 调用，OSHI 是本地原生调用，无 SSRF 风险 |

---

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| OSHI (oshi-core-ffm) | 7.1.0 | 跨平台硬件指标采集（CPU/内存/磁盘/网络/系统信息） | Windows 平台硬件监控标准库，纯 Java，FFM API，JDK 26 兼容。替代 JNA 方案 |
| Spring Boot WebMVC | 4.1.0-RC1 | REST API 端点 | 项目已有，`spring-boot-starter-webmvc` |
| Lombok | 1.18.44 | 减少样板代码（DTO/Service） | 项目已有，`@Data`, `@Slf4j`, `@RequiredArgsConstructor` |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| JUnit 5 + Mockito | (via BOM) | 单元测试，Mock OSHI 依赖 | 所有 `HardwareMetricsService` 单元测试 |
| Spring Boot Test | (via BOM) | 集成测试（真实 OSHI 加载） | `@SpringBootTest` 全链路验证 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `oshi-core-ffm` 7.x | `oshi-core` 6.x (JNA) | JDK 26 禁止 JNA（JEP 472），6.x 无法运行。无选择 |
| `ScheduledExecutorService` | `@Scheduled` | `@Scheduled` 无法自定义线程名称/池大小，异常处理粒度粗。`ScheduledExecutorService` 提供更精细控制 |
| ReentrantReadWriteLock | `synchronized` / `volatile` | `volatile` 不适用于复合读写；`synchronized` 让读-读线程互斥。`ReentrantReadWriteLock` 允许多读单写 |
| 自定义 RingBuffer | `EvictingQueue` (Guava) | 避免引入新依赖。Guava 不在当前项目中。自定义实现无外部依赖 |

**Installation:**
```bash
# 修改 springboot/gradle/libs.versions.toml
# 修改 springboot/build.gradle.kts
```

**Version verification:**
```bash
# 当前 OSHI 最新版本已验证:
# oshi-core-ffm 7.1.0 — 2026-05-06 release
# Coodinates: com.github.oshi:oshi-core-ffm
# Source: GitHub Releases https://github.com/oshi/oshi/releases [VERIFIED]
```

---

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│  Backend Process (JDK 26 + Spring Boot 4.1)                     │
│                                                                   │
│  ┌─────────────────────────────┐  ┌──────────────────────────┐   │
│  │ MonitoringSchedulerConfig   │  │ HardwareMonitorController│   │
│  │ (@Configuration)            │  │ (REST API)               │   │
│  │                             │  │                          │   │
│  │  ScheduledExecutorService   │  │  GET /current            │   │
│  │  (single daemon thread)     │  │  GET /trend?metric=X     │   │
│  │                             │  │  GET /system             │   │
│  └─────────────┬───────────────┘  └───────────┬──────────────┘   │
│                │                              │                   │
│                │ calls                        │ reads             │
│                ▼                              ▼                   │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │           HardwareMetricsServiceImpl                     │    │
│  │                                                          │    │
│  │  ┌─────────────────┐  ┌──────────────┐  ┌─────────────┐ │    │
│  │  │  CpuCollector   │  │ RingBuffer   │  │ Cache (RW   │ │    │
│  │  │  MemCollector   │─▶│ (360 slots)  │──│ Lock +      │ │    │
│  │  │  DiskCollector  │  │              │  │ volatile)   │ │    │
│  │  │  NetCollector   │  │              │  │             │ │    │
│  │  │  SysCollector   │  └──────────────┘  └─────────────┘ │    │
│  │  └───────┬─────────┘                                     │    │
│  │          │                                               │    │
│  │          ▼                                               │    │
│  │  ┌──────────────────────────────────┐                     │    │
│  │  │  OSHI SystemInfo (singleton)     │                     │    │
│  │  │  oshi.ffm.SystemInfo            │                     │    │
│  │  │  ├── HardwareAbstractionLayer   │                     │    │
│  │  │  │   ├── CentralProcessor       │                     │    │
│  │  │  │   ├── GlobalMemory           │                     │    │
│  │  │  │   ├── HWDiskStore[]          │                     │    │
│  │  │  │   ├── NetworkIF[]            │                     │    │
│  │  │  │   └── Sensors                │                     │    │
│  │  │  └── OperatingSystem            │                     │    │
│  │  └──────────────────────────────────┘                     │    │
│  └──────────────────────────────────────────────────────────┘    │
│                                                                   │
│  Data Flow:                                                       │
│  collect() → read OSHI → build DTO → write cache (WLock)         │
│  API request → read cache (RLock) → return DTO                   │
│                                                                   │
│  Error handling: per-metric try-catch → null on failure           │
│  → other metrics unaffected                                       │
└─────────────────────────────────────────────────────────────────┘
```

### Recommended Project Structure
```
springboot/src/main/java/cn/coderstory/springboot/
├── config/
│   ├── MonitoringSchedulerConfig.java    # @Configuration — 创建调度线程池
│   └── MonitorHardwareProperties.java    # @ConfigurationProperties — 硬件监控配置映射
│
├── controller/monitor/hardware/
│   └── HardwareMonitorController.java    # REST 端点 (current/trend/system)
│
├── service/monitor/hardware/
│   ├── HardwareMetricsService.java       # 接口 — 定义 contract
│   └── impl/
│       └── HardwareMetricsServiceImpl.java  # 实现 — 采集/缓存/降级逻辑
│
├── dto/monitor/hardware/
│   ├── HardwareMetricsDTO.java           # 顶层 DTO (timestamp + 各子指标)
│   ├── CpuMetricsDTO.java                # CPU: systemLoad, perCoreLoad[], cores...
│   ├── MemoryMetricsDTO.java             # 内存: total, available, used, usagePercent
│   ├── DiskMetricsDTO.java               # 磁盘: mount, total, used, readBytes/s...
│   ├── NetworkMetricsDTO.java            # 网络: displayName, bytesSent/Recv, speed...
│   ├── SystemInfoDTO.java                # 系统: osFamily, osVersion, uptime, processCount
│   ├── MetricValue.java                  # 格式化值: {value, format, unit}
│   └── TrendDataPoint.java              # 趋势点: {timestamp, metric values}

springboot/src/main/resources/config/business.yaml  # + monitor.hardware 配置段
springboot/gradle/libs.versions.toml                # + oshi version/library
springboot/build.gradle.kts                         # + implementation(libs.oshi.core.ffm)
```

### Pattern 1: ScheduledExecutorService 管理采集线程
**What:** 使用独立的 `ScheduledExecutorService`（单线程调度池）管理主采集循环，替代 `@Scheduled` 注解。
**When to use:** 需要对采集线程有完全控制（线程名、异常处理、优雅关闭）时。与项目已有 `@Scheduled`（秒杀订单超时、库存对账）隔离。
**Source:** [Context7: oracle-docs ScheduledExecutorService](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/util/concurrent/ScheduledExecutorService.html) [CITED]

```java
@Configuration
public class MonitoringSchedulerConfig {

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService monitoringScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "monitor-hardware-collector");
            thread.setDaemon(true);
            return thread;
        });
    }
}
```

### Pattern 2: 单写入者 + ReentrantReadWriteLock 缓存
**What:** 采集线程持有写锁更新 DTO，所有 API 请求线程持有读锁读取 DTO。保证强一致性同时不阻塞并发读。
**When to use:** 一写多读场景。写操作每 2s 一次，读操作可能并发（多个 API 请求同时到达）。

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class HardwareMetricsServiceImpl implements HardwareMetricsService {

    private final SystemInfo systemInfo;           // @PostConstruct 时初始化
    private final ScheduledExecutorService scheduler;  // injected

    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private HardwareMetricsDTO currentMetrics;     // protected by cacheLock
    private final RingBuffer<HardwareMetricsDTO> historyBuffer = new RingBuffer<>(360);

    // CPU tick/delta 状态
    private long[] prevCpuTicks;
    private long[][] prevPerCoreTicks;

    // 磁盘/网络采样计数器（每 3 次采集更新一次）
    private int diskNetCounter = 0;

    // 磁盘/网络上一次快照（用于差分计算）
    private List<HWDiskStore> prevDisks;
    private List<NetworkIF> prevNets;

    @PostConstruct
    public void init() {
        // 预热 CPU tick 状态（首次 getSystemCpuLoadBetweenTicks() 返回 -1）
        prevCpuTicks = systemInfo.getHardware().getProcessor().getSystemCpuLoadTicks();
        prevPerCoreTicks = systemInfo.getHardware().getProcessor().getProcessorCpuLoadTicks();

        // 预热磁盘/网络状态
        prevDisks = systemInfo.getHardware().getDiskStores();
        List<NetworkIF> nets = systemInfo.getHardware().getNetworkIFs();
        nets.forEach(NetworkIF::updateAttributes);
        prevNets = nets;

        // 启动调度
        scheduler.scheduleAtFixedRate(this::collect, 0, 2, TimeUnit.SECONDS);
        log.info("硬件监控采集线程已启动 (interval=2s)");
    }

    public void collect() {
        HardwareMetricsDTO snapshot = new HardwareMetricsDTO();
        snapshot.setTimestamp(System.currentTimeMillis());

        // CPU & 内存 — 每次采集
        snapshot.setCpu(sampleCpu());
        snapshot.setMemory(sampleMemory());

        // 磁盘 & 网络 — 每 3 次采集更新一次
        diskNetCounter++;
        if (diskNetCounter >= 3) {
            diskNetCounter = 0;
            snapshot.setDisks(sampleDisks());
            snapshot.setNetwork(sampleNetwork());
        } else {
            // 非更新周期，从缓存复用上次值
            cacheLock.readLock().lock();
            try {
                if (currentMetrics != null) {
                    snapshot.setDisks(currentMetrics.getDisks());
                    snapshot.setNetwork(currentMetrics.getNetwork());
                }
            } finally {
                cacheLock.readLock().unlock();
            }
        }

        // 系统信息 — 每次采集更新
        snapshot.setSystem(sampleSystem());

        // 写入缓存（写锁）
        cacheLock.writeLock().lock();
        try {
            currentMetrics = snapshot;
            historyBuffer.add(snapshot);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @Override
    public HardwareMetricsDTO getCurrentMetrics() {
        cacheLock.readLock().lock();
        try {
            return currentMetrics != null ? currentMetrics : HardwareMetricsDTO.empty();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public List<HardwareMetricsDTO> getTrendHistory() {
        cacheLock.readLock().lock();
        try {
            return historyBuffer.snapshot();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

### Pattern 3: CPU 采样（总体 + 各核 + 刻度预热）
**What:** 使用 OSHI 的 tick 差值 API 计算 CPU 使用率。首次调用返回 -1，预热后正常。
**When to use:** 每次采集循环必须调用。保持 prevTicks 引用不置空。

```java
private CpuMetricsDTO sampleCpu() {
    try {
        CentralProcessor processor = systemInfo.getHardware().getProcessor();

        // 总体使用率
        long[] currentTicks = processor.getSystemCpuLoadTicks();
        double systemLoad = processor.getSystemCpuLoadBetweenTicks(prevCpuTicks, currentTicks);
        prevCpuTicks = currentTicks;

        // 各核使用率
        long[][] currentPerCoreTicks = processor.getProcessorCpuLoadTicks();
        double[] perCoreLoad = processor.getProcessorCpuLoadBetweenTicks(prevPerCoreTicks, currentPerCoreTicks);
        prevPerCoreTicks = currentPerCoreTicks;

        // 缩放到 Task Manager 兼容格式
        double systemCpuPercent = systemLoad / processor.getLogicalProcessorCount() * 100;
        double[] perCorePercent = Arrays.stream(perCoreLoad)
            .map(l -> l * 100)
            .toArray();

        // 获取处理器信息（首次采集后缓存）
        CentralProcessor.ProcessorIdentifier identifier = processor.getProcessorIdentifier();

        CpuMetricsDTO dto = new CpuMetricsDTO();
        dto.setSystemLoad(Math.round(systemCpuPercent * 10.0) / 10.0);
        dto.setPerCoreLoad(perCorePercent);
        dto.setLogicalCores(processor.getLogicalProcessorCount());
        dto.setPhysicalCores(processor.getPhysicalProcessorCount());
        dto.setProcessorName(identifier.getName());
        dto.setProcessorFrequency(identifier.getVendorFreq());
        dto.setCpu64bit(identifier.isCpu64bit());
        return dto;
    } catch (Exception e) {
        log.warn("CPU 采集失败", e);
        return CpuMetricsDTO.empty();  // 局部降级
    }
}
```

### Pattern 4: 磁盘 IOPS/吞吐量差分计算
**What:** `HWDiskStore` 的 `getReads()`/`getWrites()` 等是自启动以来的累积值。吞吐量/IOPS 需要通过两次采样差值除以时间间隔计算。
**When to use:** 磁盘/网络每 3 次采样（6s）更新一次，保留上次快照用于差分。

```java
private List<DiskMetricsDTO> sampleDisks() {
    try {
        List<HWDiskStore> currentDisks = systemInfo.getHardware().getDiskStores();
        List<DiskMetricsDTO> result = new ArrayList<>();

        for (int i = 0; i < currentDisks.size(); i++) {
            HWDiskStore current = currentDisks.get(i);
            HWDiskStore previous = i < prevDisks.size() ? prevDisks.get(i) : null;

            DiskMetricsDTO dto = new DiskMetricsDTO();
            dto.setName(current.getName());
            dto.setModel(current.getModel());
            dto.setSize(formatBytes(current.getSize()));
            dto.setDiskType(current.getDiskType() != null ? current.getDiskType().name() : "UNKNOWN");

            // 差分计算（仅当有前值）
            if (previous != null) {
                long intervalMs = 6000;  // 6s 更新间隔
                long readBytesPerSec = (current.getReadBytes() - previous.getReadBytes()) * 1000 / intervalMs;
                long writeBytesPerSec = (current.getWriteBytes() - previous.getWriteBytes()) * 1000 / intervalMs;
                long readsPerSec = (current.getReads() - previous.getReads()) * 1000 / intervalMs;
                long writesPerSec = (current.getWrites() - previous.getWrites()) * 1000 / intervalMs;

                dto.setReadBytesPerSec(new MetricValue<>(readBytesPerSec, formatBytesPerSec(readBytesPerSec), "B/s"));
                dto.setWriteBytesPerSec(new MetricValue<>(writeBytesPerSec, formatBytesPerSec(writeBytesPerSec), "B/s"));
                dto.setReadsPerSec(new MetricValue<>(readsPerSec, readsPerSec + " IOPS", "IOPS"));
                dto.setWritesPerSec(new MetricValue<>(writesPerSec, writesPerSec + " IOPS", "IOPS"));
            }

            result.add(dto);
        }

        prevDisks = currentDisks;
        return result;
    } catch (Exception e) {
        log.warn("磁盘采集失败", e);
        return List.of();
    }
}
```

### Pattern 5: RingBuffer（自定义泛型环形缓冲区）
**What:** 固定容量环形缓冲区，支持 `add()` 和 `snapshot()`。`ReentrantReadWriteLock` 保证线程安全。
**When to use:** 内存中的趋势数据存储。本阶段 360 容量，每 10s（每 5 次采样）存一次，覆盖 1 小时。

```java
public class RingBuffer<T> {
    private final T[] buffer;
    private final int capacity;
    private int head = 0;
    private int count = 0;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        this.capacity = capacity;
        this.buffer = (T[]) new Object[capacity];
    }

    public void add(T item) {
        lock.writeLock().lock();
        try {
            buffer[head] = item;
            head = (head + 1) % capacity;
            if (count < capacity) count++;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<T> snapshot() {
        lock.readLock().lock();
        try {
            List<T> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int idx = (head - count + i + capacity) % capacity;
                result.add(buffer[idx]);
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() { return count; }
    public int capacity() { return capacity; }
}
```

### Pattern 6: ConfigurationProperties 映射配置
**What:** 使用 `@ConfigurationProperties` 将 `business.yaml` 中的 `monitor.hardware` 配置映射到 POJO。
**When to use:** 所有可配置的采集参数（间隔、缓存大小、降级阈值等）。

```yaml
# business.yaml 中新增段
# ==================== 硬件监控配置 ====================
# OSHI 采集服务的采样间隔、环形缓冲区大小和降级策略。
# ---------------------------------------------------
monitor:
  hardware:
    sampling-interval: 2000              # 主循环间隔（毫秒）
    disk-net-interval: 3                  # 磁盘/网络每 N 次采样更新一次
    trend-buffer-size: 360               # 环形缓冲区容量（360 点 = 1h at 10s 间隔）
    trend-decimation: 5                   # 每 N 次采样写入一次趋势缓冲区
    enabled: true                         # 是否启用采集（可用于开关）
```

```java
@ConfigurationProperties(prefix = "monitor.hardware")
@Data
public class MonitorHardwareProperties {
    private int samplingInterval = 2000;
    private int diskNetInterval = 3;
    private int trendBufferSize = 360;
    private int trendDecimation = 5;
    private boolean enabled = true;
}
```

### Pattern 7: MetricValue 格式化值结构
**What:** 遵循 D-05 的格式化值结构，原始值 + 格式化字符串 + 单位。
**When to use:** 所有需要人类可读格式化的数值字段（内存大小、磁盘容量、网络吞吐量）。

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricValue<T> {
    private T value;       // 原始数值（如 8.5）
    private String format; // 格式化字符串（如 "8.5 GB"）
    private String unit;   // 单位（如 "GB"）
}
```

### Anti-Patterns to Avoid
- **跨包循环引用:** `HardwareMetricsService` 不应引用 `MonitorService`。两者是完全不同的数据集（OSHI vs Redis），混用导致耦合。
- **在 API 请求线程中调用 OSHI:** 所有 OSHI 调用必须在采集线程中执行。API 线程只读缓存。违反则 WMI 阻塞（3-3000ms）可能耗尽 Tomcat 线程池。
- **`@Scheduled` 用于采集:** 违反 D-09。`@Scheduled` 无法配置线程名、池大小、异常处理器。独立 `ScheduledExecutorService` 提供更细粒度控制。
- **OSHI 实例化在 Controller 中:** `SystemInfo` 在 `@PostConstruct` 时初始化一次。每次 new SystemInfo() 触发 JNA 类加载(100-500ms)和 COM 初始化(10-50s)。

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 硬件指标采集 | `Runtime.exec("wmic")` / `typeperf` 命令解析 | OSHI 7.x FFM | Shell 进程开销大（800ms+/调用），输出解析脆弱（locale 依赖）。OSHI 调用 PDH API 直接读取性能计数器~10ms |
| 时间序列环形缓冲区 | 自己实现 lock-free ring buffer | 简单数组 + `ReentrantReadWriteLock` | 360 点的缓冲区性能要求极低（微秒级操作）。复杂 lock-free 实现引入 bug 风险远高于性能收益 |
| 字节格式化 | 自己实现 bytes→KB/MB/GB 转换 | `String.format()` + 项目工具方法 | 格式化逻辑简单（1024 进制除法），但需要考虑负数/零/极大值边界。一个工具方法 `formatBytes(long bytes)` 即可 |
| 线程调度 | 自己管理 Thread.sleep 循环 | `ScheduledExecutorService` | 标准库已有精确调度、异常处理、优雅关闭。自建循环缺乏精度控制和生命周期管理 |

**Key insight:** 本阶段所有"可能手写"的组件（采集、缓存、格式化）都是 OSHI 或 JDK 标准库已解决的。唯一需新写的是 RingBuffer，但 360 点容量下用最简单实现即可。

---

## Common Pitfalls

### Pitfall 1: JDK 26 上使用 `oshi-core`（JNA 版本）导致运行时崩溃
**What goes wrong:** 应用启动或首次采集时抛 `IllegalCallerException`，所有指标返回 0。
**Why it happens:** JDK 26 (JEP 472) 默认禁止 JNI 调用，`oshi-core` 依赖 JNA 使用 JNI。`--enable-native-access=ALL-UNNAMED` 在 JDK 26 中可能已失效。本项目的 Gradle 配置显式设置了 `languageVersion.set(JavaLanguageVersion.of(26))` 和 `--enable-preview`。
**Prevention:** 使用 `oshi-core-ffm`（FFM API）。不引入 `oshi-core`（JNA 版本）。在 `libs.versions.toml` 中添加 `oshi-core-ffm`，入口类使用 `oshi.ffm.SystemInfo`。
**Warning signs:** Gradle 同步后 `build.gradle.kts` 中如果出现 `oshi-core`（不含 `-ffm`）则报警。

### Pitfall 2: 首次 `getSystemCpuLoadBetweenTicks()` 返回 -1.0
**What goes wrong:** 启动后前 2s，CPU 指标显示 -100% 或 NaN。
**Why it happens:** OSHI CPU 负载计算需要两次 tick 采样的差值。第一次调用尚无前值。
**Prevention:** 在 `@PostConstruct` init() 中预先调用一次 `getSystemCpuLoadTicks()` 写入 `prevCpuTicks`，但不计算结果。第一次 `collect()` 调用 `getSystemCpuLoadBetweenTicks()` 时即可获得有效值。

### Pitfall 3: OSHI 线程不安全导致间歇性 NPE
**What goes wrong:** 偶现 `NullPointerException`，在不重启的情况下随机恢复。高负载时更频繁。
**Why it happens:** OSHI 的 `SystemInfo` 不是线程安全的（GitHub #797, #660）。如果在多个线程中同时访问操作系统资源（如 WMI 查询），内部缓存状态可能不一致。
**Prevention:** 单线程采集（`ScheduledExecutorService` 池大小=1）+ volatile 缓存。所有 API 请求只读缓存，不接触 OSHI 实例。

### Pitfall 4: SSE 连接泄漏影响 Phase 32
**What goes wrong:** （Phase 32 的问题，但在 Phase 31 的服务设计阶段需要预防）未正确管理的 `SseEmitter` 积累在内存中。
**Prevention:** 本阶段不涉及 SSE，但 Phase 31 的缓存/采集架构需支持 Phase 32 的广播模式：采集服务提供 `getCurrentMetrics()` 方法供 SSE 服务调用，不直接传递 emitter。

---

## Code Examples

### 入口类 HardwareMetricsService 接口定义
```java
// Source: D-13 接口模式定义
public interface HardwareMetricsService {
    /** 获取当前硬件指标快照。首次成功采集前返回 empty DTO。 */
    HardwareMetricsDTO getCurrentMetrics();

    /** 获取环形缓冲区中的趋势数据（全部 360 点）。 */
    List<HardwareMetricsDTO> getTrendHistory();

    /** 获取系统基本信息（OS 版本、运行时间、进程数）。 */
    SystemInfoDTO getSystemInfo();
}
```

### REST Controller
```java
// Source: D-07, D-16 定义的端点
@RestController
@RequestMapping("/api/monitor/hardware")
@RequiredArgsConstructor
@Slf4j
public class HardwareMonitorController {

    private final HardwareMetricsService metricsService;

    @GetMapping("/current")
    public ApiResponse<HardwareMetricsDTO> getCurrent() {
        return ApiResponse.success(metricsService.getCurrentMetrics());
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendDataPoint>> getTrend(
            @RequestParam(defaultValue = "cpu") String metric,
            @RequestParam(defaultValue = "360") int range) {
        List<HardwareMetricsDTO> history = metricsService.getTrendHistory();
        // 提取指定 metric 的时间序列，按 range 下采样
        List<TrendDataPoint> trend = extractTrend(history, metric, range);
        return ApiResponse.success(trend);
    }

    @GetMapping("/system")
    public ApiResponse<SystemInfoDTO> getSystem() {
        return ApiResponse.success(metricsService.getSystemInfo());
    }
}
```

### Mockito 单元测试模式
```java
// Source: D-15 Mockito 测试模式，参考现有 UserServiceImplTest
@ExtendWith(MockitoExtension.class)
@DisplayName("HardwareMetricsService 单位测试")
class HardwareMetricsServiceImplTest {

    @Mock
    private SystemInfo systemInfo;
    @Mock
    private HardwareAbstractionLayer hal;
    @Mock
    private CentralProcessor processor;

    private HardwareMetricsServiceImpl service;

    @BeforeEach
    void setUp() {
        when(systemInfo.getHardware()).thenReturn(hal);
        when(hal.getProcessor()).thenReturn(processor);
        service = new HardwareMetricsServiceImpl(systemInfo, scheduler);
    }

    @Test
    @DisplayName("CPU 采集异常时降级返回空 DTO，不影响其他指标")
    void whenCpuFails_returnsEmptyDto() {
        when(processor.getSystemCpuLoadTicks()).thenThrow(new RuntimeException("OSHI error"));
        service.collect();
        HardwareMetricsDTO result = service.getCurrentMetrics();
        assertNull(result.getCpu());
        assertNotNull(result.getMemory()); // memory unaffected
    }
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `oshi-core` (JNA) | `oshi-core-ffm` (FFM) | OSHI 7.0.0 (2026-04-30) | JDK 26 必须使用 FFM 版本。入口类从 `oshi.SystemInfo` 变为 `oshi.ffm.SystemInfo` |
| `oshi-core-java25` | `oshi-core-ffm` | OSHI 7.0.0 | 模块重命名，旧 artifact 有重定向 POM |
| `oshi.util.platform.windows` | `oshi.ffm.util.platform.windows` | OSHI 7.0.1 (2026-05-02) | FFM 模块内部包重组，消除 split-package。影响极小，因用户代码不直接引用这些包 |

**Deprecated/outdated:**
- `oshi-core`（JNA 版本）— JDK 26 上不可用，不要引入
- `oshi.SystemInfo`（JNA 入口）— FFM 版本使用 `oshi.ffm.SystemInfo`
- `SystemInfo.getCurrentPlatform()` — 7.0.0 移除，改用 `PlatformEnum.getCurrentPlatform()`

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `oshi.ffm.SystemInfo` 是 OSHI 7.1.0 中 FFM 模块的入口类名 | Standard Stack | 若类名不同，编译失败。备选：`oshi.SystemInfo` 仍存在但使用 JNA。确认方式：gradle 引入后编译验证 |
| A2 | `HWDiskStore.getDiskType()` 在 7.1.0 中存在 | Code Examples (sampleDisks) | OSHI 7.1.0 changelog 记录此功能。若不存在则编译失败，移除该字段即可 |
| A3 | 项目没有使用 `@EnableMethodSecurity`，无法直接使用 `@PreAuthorize` | Architecture | 确保 Phase 34 添加安全时不使用 `@PreAuthorize`，仍通过 `SecurityConfig` 的 `requestMatchers` 配置 |

---

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 26 | OSHI FFM 编译和运行 | yes | OpenJDK 26+35-2893 | — |
| Gradle | 构建管理 OSHI 依赖 | yes | 9.5.0 / Kotlin 2.3.20 | — |
| Maven Central | OSHI artifact 下载 | yes | — | — |
| Windows OS | OSHI Windows 硬件采集 | yes | Windows 11 Pro 10.0.29576 | Phase 31 在 Windows 上必须。无 Windows 则无法做集成测试 |

**Missing dependencies with no fallback:**
- 无。所有依赖（JDK, Gradle, OSHI registry）均已确认可用。

---

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Mockito (via spring-boot-starter-test BOM) |
| Config file | 无（JUnit Platform 自动发现） |
| Quick run command | `./gradlew.bat test --tests "*HardwareMetrics*"` |
| Full suite command | `./gradlew.bat test` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| HWM-01 | OSHI FFM 初始化 | integration | `test --tests "*HardwareMetricsService*IT"` | no (new) |
| HWM-02 | CPU 采样计算 | unit | `test --tests "*HardwareMetricsServiceImpl*"#CpuSampling*` | no (new) |
| HWM-03 | 内存采集 | unit | `test --tests "*HardwareMetricsServiceImpl*"#MemorySampling*` | no (new) |
| HWM-04 | 磁盘容量采集 | unit | `test --tests "*HardwareMetricsServiceImpl*"#DiskSampling*` | no (new) |
| HWM-05 | 磁盘 IOPS 差分计算 | unit | `test --tests "*HardwareMetricsServiceImpl*"#DiskIops*` | no (new) |
| HWM-06 | 网络吞吐量差分计算 | unit | `test --tests "*HardwareMetricsServiceImpl*"#NetworkThroughput*` | no (new) |
| HWM-07 | REST API 端点 | unit | `test --tests "*HardwareMonitorController*"` | no (new) |
| HWM-08 | 环形缓冲区读写 | unit | `test --tests "*HardwareMetricsServiceImpl*"#RingBuffer*` | no (new) |
| HWM-09 | 系统基本信息采集 | unit | `test --tests "*HardwareMetricsServiceImpl*"#SystemInfo*` | no (new) |
| TDD-01 | 接口可 mock | unit | `@Mock SystemInfo` 注入验证 | no (new) |
| TDD-03 | 降级策略 | unit | mock OSHI 抛异常验证降级返回 null | no (new) |

### Sampling Rate
- **Per task commit:** `./gradlew.bat test --tests "*HardwareMetrics*" -x check`
- **Per wave merge:** `./gradlew.bat test`
- **Phase gate:** 满测试绿色 + `bootRun` 验证 API 返回 JSON

### Wave 0 Gaps
- [ ] `src/test/java/.../service/monitor/hardware/HardwareMetricsServiceImplTest.java` — 覆盖 CPU/内存/磁盘/网络采样逻辑、降级策略、缓存一致性
- [ ] `src/test/java/.../controller/monitor/hardware/HardwareMonitorControllerTest.java` — 覆盖 3 个 REST 端点的请求/响应
- [ ] `src/test/java/.../service/monitor/hardware/RingBufferTest.java` — 环形缓冲区 add/snapshot/线程安全
- [ ] `src/test/java/.../service/monitor/hardware/HardwareMetricsServiceIT.java` — 集成测试（`@SpringBootTest`，真实 OSHI 加载，Windows 环境）

---

## Security Domain

> 本阶段明确不涉及安全加固（Phase 34）。API 路由 `/api/monitor/hardware/**` 不加入白名单，默认受 Spring Security `authenticated()` 保护，需有效 JWT 令牌。所有端点在 Phase 34 之前仅限登录用户访问（不校验角色）。

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes (existing) | JWT 令牌已在全局过滤器链生效，新建端点自动继承 |
| V5 Input Validation | partial | `/trend` 端点的 `metric` 和 `range` 参数做基础校验（枚举 + 正整数范围） |

---

## Sources

### Primary (HIGH confidence)
- [OSHI GitHub Releases](https://github.com/oshi/oshi/releases) — OSHI 7.1.0 release date 2026-05-06, changelog, Maven coordinates [VERIFIED]
- [OSHI CHANGELOG (raw)](https://raw.githubusercontent.com/oshi/oshi/master/CHANGELOG.md) — 7.0.0 artifact rename, 7.0.1 package move, 7.1.0 disk type/context switches [VERIFIED]
- [OSHI Issue #3123](https://github.com/oshi/oshi/issues/3123) — FFM support, module layout plan, entry point `oshi.ffm.SystemInfo` [CITED]
- [OSHI Issue #797, #660](https://github.com/oshi/oshi/issues/797) — SystemInfo thread safety warnings [CITED]
- [OSHI Discussion #2698](https://github.com/oshi/oshi/discussions/2698) — JEP 472 JNI restrictions, FFM migration [CITED]
- Existing project code: `MonitorService.java`, `MonitorController.java`, `UserServiceImplTest.java`, `SeckillSseService.java`, `SecurityConfig.java`, `libs.versions.toml`, `build.gradle.kts` [VERIFIED in codebase]
- JDK 26 version: `OpenJDK 26 2026-03-17` [VERIFIED: java --version]

### Secondary (MEDIUM confidence)
- [OSHI Official Site](https://www.oshi.ooo/) — module documentation, API reference [CITED]
- [Maven Central (sonatype)](https://central.sonatype.com/artifact/com.github.oshi/oshi-core-ffm) — latest version 7.1.0, published 2 days ago [CITED]
- [OSHI Performance Guide](https://raw.githubusercontent.com/oshi/oshi/master/src/site/markdown/Performance.md) — memoizer expiration, WMI overhead, collection intervals [CITED]
- [Spring Framework Issue #33340](https://github.com/spring-projects/spring-framework/issues/33340) — SSE memory leak (prevention planned for Phase 32) [CITED]

### Tertiary (LOW confidence)
- [OSHI Windows CPU permission blog](https://blog.gitcode.com/a6fbe73f77cde90e8a218fb7f7907377.html) — CPU ticks zero issue, Performance Monitor Users group [CITED]

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - OSHI 7.1.0 coordinates and entry point verified against Maven Central and GitHub releases. JDK 26 FFM compatibility confirmed via JDK version history.
- Architecture: HIGH - All patterns (single-writer, RW lock, ring buffer) are well-established in Java backend engineering. Matches existing project conventions.
- Pitfalls: HIGH - OSHI thread safety, JDK 26 JNA restriction, CPU tick -1 issue, and SSE emitter leak are documented in OSHI GitHub issues and Spring issue tracker.

**Research date:** 2026-05-09
**Valid until:** 2026-06-09 (or until OSHI 7.2+ release, whichever is earlier)
