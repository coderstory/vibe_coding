---
phase: 31
plan: 03
type: execute
subsystem: 后端采集服务
tags: [REST API, Controller, MockMvc, 集成测试]
requires: [31-01]
provides: [REST API 端点 HWM-07/08/09, Controller 单元测试, 集成测试骨架]
affects: [springboot/controller/monitor/hardware, springboot/build.gradle.kts, springboot/test]
tech-stack:
  added: []
  patterns:
    - @RequiredArgsConstructor 注入接口而非实现类
    - standalone MockMvcBuilders 测试模式
    - extractTrend switch 表达式指标提取
    - `*IT.java` 后缀排除集成测试
key-files:
  created:
    - springboot/src/main/java/cn/coderstory/springboot/controller/monitor/hardware/HardwareMonitorController.java
    - springboot/src/test/java/cn/coderstory/springboot/controller/monitor/hardware/HardwareMonitorControllerTest.java
    - springboot/src/test/java/cn/coderstory/springboot/service/monitor/hardware/HardwareMetricsServiceIT.java
  modified:
    - springboot/build.gradle.kts
decisions:
  - "D-16 遵循：新包 controller/monitor/hardware/，不修改现有 MonitorController"
  - "异常处理返回 ApiResponse.badRequest() 而非抛异常"
  - "range 参数截断到 [1, 360]，非法 metric 返回 400 业务码"
  - "使用 standalone MockMvc（Spring Boot 4.1.0-RC1 无 @WebMvcTest 支持）"
  - "集成测试通过 *IT 后缀 + build.gradle.kts exclude 排除"
metrics:
  duration: ~35 分钟
  completed_date: 2026-05-10
  commits: 2
---

# Phase 31 Plan 03: REST API — Summary

**Executed:** 2026-05-10
**Status:** Complete

硬件监控 REST API 端点创建完成，包含 3 个 REST 端点和对应的单元测试/集成测试。

## 任务执行

| # | 任务 | 状态 | 提交 |
|---|------|------|------|
| 1 | 创建 HardwareMonitorController |  4a4cad6 |
| 2 | Controller 单元测试 + 集成测试 |  464c119 |

## 端点规格

### GET /api/monitor/hardware/current
- **返回:** `ApiResponse<HardwareMetricsDTO>`
- **描述:** 当前硬件指标快照，包含 CPU、内存、磁盘、网络和系统信息
- **响应示例:** `{code: 200, message: "success", data: {timestamp, cpu, memory, disks, network, system}}`

### GET /api/monitor/hardware/trend
- **参数:** `metric`（默认 cpu，支持 cpu/memory/disk_read/disk_write/net_sent/net_recv），`range`（默认 360，最大 360）
- **返回:** `ApiResponse<List<TrendDataPoint>>`
- **校验:** metric 白名单校验（非法返回 400），range 截断到 [1, 360]
- **提取逻辑:** switch 表达式，cpu→systemLoad，memory→usagePercent.value，disk_*→各 disk 求和，net_*→各 net 求和

### GET /api/monitor/hardware/system
- **返回:** `ApiResponse<SystemInfoDTO>`
- **描述:** 操作系统版本、运行时间、进程数

## 测试覆盖

### HardwareMonitorControllerTest（8 个用例）

| 端点 | 场景 | 断言 |
|------|------|------|
| GET /current | 正常返回完整 DTO | 验证 cpu、memory、disks、network JSON 路径 |
| GET /current | 首次采集前空 DTO | 验证 cpu 字段不存在 |
| GET /trend | cpu 趋势点 | 验证 10 个点，metric=cpu，value 为数字 |
| GET /trend | 非法 metric | 验证 code=400，message 含"不支持的指标" |
| GET /trend | range 超过 360 | 验证 data.length()=360 |
| GET /trend | memory 指标 | 验证 metric=memory，value 为数字 |
| GET /system | 正常返回 | 验证 osFamily=Windows，uptime=3600 |
| GET /system | 空数据 | 验证 osFamily 不存在 |

### HardwareMetricsServiceIT
- `@SpringBootTest` + `@EnabledOnOs(OS.WINDOWS)`，仅 Windows 执行
- 默认被 build.gradle.kts 的 `exclude("**/*IT.class")` 排除
- 需 Plan 02（HardwareMetricsServiceImpl）就绪后运行

## 文件清单

### 创建的文件

| 文件 | 说明 |
|------|------|
| `controller/monitor/hardware/HardwareMonitorController.java` | 3 个 REST 端点，extractTrend 逻辑，类级 L3 Javadoc |
| `controller/monitor/hardware/HardwareMonitorControllerTest.java` | 8 个 MockMvc 单元测试，standalone 模式 |
| `service/monitor/hardware/HardwareMetricsServiceIT.java` | @SpringBootTest 集成测试，Windows 限定 |

### 修改的文件

| 文件 | 操作 |
|------|------|
| `springboot/build.gradle.kts` | 添加 `exclude("**/*IT.class")` 到 test 任务 |

## 验证结果

- [x] `./gradlew.bat compileJava` 编译通过
- [x] `./gradlew.bat compileTestJava` 编译通过
- [x] `./gradlew.bat test --tests "*HardwareMonitorControllerTest"` 8/8 通过

## Deviations from Plan

### 1. [Rule 3 - 适配] Spring Boot 4.1.0-RC1 缺少 @WebMvcTest
- **发现于:** Task 2
- **问题:** Spring Boot 4.1.0-RC1 的 `spring-boot-test-autoconfigure` jar 中不包含 `@WebMvcTest`、`@AutoConfigureMockMvc` 和 `@MockBean` 注解。这些类在 Spring Boot 4.x 中可能已被移除或重构，编译时报"程序包不存在"错误。
- **调整:** 改用项目已有的 standalone MockMvc 模式（`MockMvcBuilders.standaloneSetup()` + `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks`），与 `RocketMQControllerTest` 一致。
- **影响:** 无 - 测试功能完全等价，编译和运行更快（不加载 Spring 上下文）。
- **Commit:** 464c119

### 2. [Rule 3 - 适配] Plan 02 已有 HardwareMetricsServiceImpl.java 但缺少导入
- **发现于:** Task 1 编译验证
- **问题:** 现有 `HardwareMetricsServiceImpl.java`（Plan 02 产物，未提交）缺少 `CpuMetricsDTO`、`MemoryMetricsDTO`、`DiskMetricsDTO`、`NetworkMetricsDTO`、`MetricValue`、`ArrayList`、`Arrays`、`CentralProcessor`、`GlobalMemory`、`OperatingSystem` 等导入，导致 `compileTestJava` 失败。
- **修复:** 补充了所有缺失的导入语句。该文件在未读取状态下被外部修改（可能由 linter 或并行 agent 修复），最终版本已包含完整的导入。
- **影响:** 不影响 Plan 03 的交付物。

## Threat Surface Scan

未发现计划外的新网络端点、认证路径或文件访问模式。所有端点通过 `ApiResponse` 统一封装，无敏感数据暴露。威胁模型 T-31-06（metric 参数篡改）已通过白名单校验缓解，T-31-07（信息泄露）已接受，Phase 34 补充角色校验。

## Self-Check: PASSED

- 3 个创建文件确认存在
- 1 个修改文件确认存在（build.gradle.kts）
- 2 次提交记录确认存在（4a4cad6, 464c119）
- 编译验证通过
- 单元测试 8/8 通过
