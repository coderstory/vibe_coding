---
phase: 31
plan: 01
type: execute
subsystem: 后端采集服务
tags: [基础层, OSHI, 依赖管理, 配置类, DTO, RingBuffer, Service接口]
requires: []
provides: [OSHI依赖, 硬件监控配置, 调度器Bean, 硬件指标DTO, 环形缓冲区, 采集服务接口]
affects: [springboot/build, springboot/config, springboot/dto/monitor/hardware, springboot/service/monitor/hardware]
tech-stack:
  added:
    - oshi-core-ffm:7.1.0 — OSHI FFM 硬件指标库
  patterns:
    - @ConfigurationProperties 配置段映射
    - ScheduledExecutorService 独立调度线程池
    - ReentrantReadWriteLock 线程安全环形缓冲区
    - 接口模式 + 局部降级 DTO
key-files:
  created:
    - springboot/src/main/java/cn/coderstory/springboot/config/MonitorHardwareProperties.java
    - springboot/src/main/java/cn/coderstory/springboot/config/MonitoringSchedulerConfig.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/MetricValue.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/CpuMetricsDTO.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/MemoryMetricsDTO.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/DiskMetricsDTO.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/NetworkMetricsDTO.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/SystemInfoDTO.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/HardwareMetricsDTO.java
    - springboot/src/main/java/cn/coderstory/springboot/dto/monitor/hardware/TrendDataPoint.java
    - springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/RingBuffer.java
    - springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/HardwareMetricsService.java
  modified:
    - springboot/gradle/libs.versions.toml
    - springboot/build.gradle.kts
    - springboot/src/main/resources/config/business.yaml
decisions:
  - D-02 适配: Gradle 不支持 `~` 版本范围语法，改为固定版本 `7.1.0`
metrics:
  duration: ~15 分钟
  completed_date: 2026-05-10
  commits: 2
---

# Phase 31 Plan 01: 基础层 — Summary

**Executed:** 2026-05-10
**Status:**  Complete

## 目标

创建 Phase 31 基础层 -- OSHI 依赖集成、配置类、DTO 数据结构、RingBuffer 和 Service 接口。为后续 Plan 02（采集实现）和 Plan 03（REST API）提供全部前置依赖。

## 任务执行

| # | 任务 | 状态 | 提交 |
|---|------|------|------|
| 1 | OSHI 依赖 + 配置类 + business.yaml |  2e86f48 |
| 2 | DTO 定义 + RingBuffer + Service 接口 |  194cb80 |

## 关键决策实现

### D-01: oshi-core-ffm 依赖
- `oshi-core-ffm` 7.1.0 添加到 `libs.versions.toml`，使用精确版本（非 `~` 范围，因 Gradle 不支持 Maven 的 `~` 语法）
- `build.gradle.kts` dependencies 块添加 `implementation(libs.oshi.core.ffm)`

### D-02: 版本范围约束适配
- 原计划使用 Gradle `~7.1.0` 范围约束，但 Gradle 不支持 Maven 的 `~` 版本范围语法
- 改为固定版本 `7.1.0`（RESEARCH 确认的最新兼容版本）

### D-03: SystemInfo Bean 单例
- MonitoringSchedulerConfig 创建 `@Bean(destroyMethod = "close")` 的 SystemInfo 实例
- 下游 Service 通过 `@RequiredArgsConstructor` 直接注入

### D-04~D-07: DTO 数据结构
- `HardwareMetricsDTO`：顶层 DTO，含 timestamp + 所有子指标
- `MetricValue<T>`：泛型格式化值 `{value, format, unit}`，含 `of()` 静态工厂
- `CpuMetricsDTO`：`systemLoad` 总体 + `perCoreLoad[]` 各核数组
- `MemoryMetricsDTO`：total/available/used/usagePercent 均为 MetricValue
- `DiskMetricsDTO`：容量 + IOPS + 读写速度字段完整
- `NetworkMetricsDTO`：上下行速率 + 包速率
- `SystemInfoDTO`：OS 版本 + 运行时间 + 进程数
- `TrendDataPoint`：`timestamp, value, metric` 趋势点

### D-08~D-11: 调度和线程安全
- `MonitoringSchedulerConfig`：创建 `ScheduledExecutorService` 单线程 daemon 调度器
- 线程名 `monitor-hardware-collector`
- `SystemInfo` 独立 Bean，destroyMethod 保证资源释放
- `RingBuffer<T>`：内部 `Object[]` 数组 + `ReentrantReadWriteLock`

### D-13: 接口模式
- `HardwareMetricsService` 纯接口，定义 `getCurrentMetrics()`、`getTrendHistory()`、`getSystemInfo()` 三个方法

## 文件清单

### 修改的文件

| 文件 | 操作 |
|------|------|
| `springboot/gradle/libs.versions.toml` | 添加 OSHI 版本 `7.1.0` 和 `oshi-core-ffm` 库 |
| `springboot/build.gradle.kts` | 添加 `implementation(libs.oshi.core.ffm)` |
| `springboot/src/main/resources/config/business.yaml` | 添加 `monitor.hardware` 配置段（5 个参数） |

### 创建的文件

| 文件 | 说明 |
|------|------|
| `config/MonitorHardwareProperties.java` | `@ConfigurationProperties(prefix = "monitor.hardware")`，5 字段含默认值 |
| `config/MonitoringSchedulerConfig.java` | 2 个 Bean：ScheduledExecutorService + SystemInfo |
| `dto/monitor/hardware/MetricValue.java` | 泛型格式化值 `{value, format, unit}` |
| `dto/monitor/hardware/CpuMetricsDTO.java` | CPU 指标，含 `perCoreLoad[]` 数组 |
| `dto/monitor/hardware/MemoryMetricsDTO.java` | 内存指标，MetricValue 包装 |
| `dto/monitor/hardware/DiskMetricsDTO.java` | 磁盘容量 + IOPS + 读写速度 |
| `dto/monitor/hardware/NetworkMetricsDTO.java` | 网络吞吐量 + 包速率 |
| `dto/monitor/hardware/SystemInfoDTO.java` | OS 版本 + 运行时间 + 进程数 |
| `dto/monitor/hardware/HardwareMetricsDTO.java` | 顶层 DTO，嵌套所有子指标 |
| `dto/monitor/hardware/TrendDataPoint.java` | 趋势数据点 `{timestamp, value, metric}` |
| `service/monitor/hardware/RingBuffer.java` | 泛型环形缓冲区，360 容量，RWLock |
| `service/monitor/hardware/HardwareMetricsService.java` | 采集服务接口，3 方法契约 |

## 验证结果

- [x] `./gradlew.bat compileJava` 编译通过
- [x] `./gradlew.bat build -x test -x check` 编译打包通过
- [x] `libs.versions.toml` 包含 OSHI 版本和 oshi-core-ffm 库
- [x] `build.gradle.kts` 包含 OSHI 依赖
- [x] `business.yaml` 包含 monitor.hardware 配置段（5 个参数）
- [x] MonitorHardwareProperties 5 个字段均有默认值
- [x] MonitoringSchedulerConfig 创建单线程 daemon 调度器
- [x] RingBuffer 提供 add/snapshot/size/capacity 方法
- [x] HardwareMetricsService 定义 3 个方法

## Deviations from Plan

### 适配 D-02 版本语法
- **原因：** Gradle 不支持 Maven `~` 版本范围约束
- **调整：** `oshi = "7.1.0"` 固定版本（非 `~7.1.0`）
- **影响：** 无，7.1.0 已验证为最新兼容版本

## Threat Surface Scan

未发现计划外的新网络端点、认证路径或文件访问模式。所有新增代码为纯内存操作（DTO 数据结构 + 配置映射 + 采集契约接口）。

## Self-Check: PASSED

- 15 个文件全部确认存在
- 2 次提交记录确认存在（2e86f48, 194cb80）
- 最新编译验证通过
