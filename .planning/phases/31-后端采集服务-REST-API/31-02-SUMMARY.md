# 31-02: 采集实现 — Summary

**Executed:** 2026-05-10
**Status:** ✅ Complete

## Tasks

- T1: 单元测试（RingBufferTest + HardwareMetricsServiceImplTest） — ✅ (cb16274)
- T2: HardwareMetricsServiceImpl 实现 — ✅ (b084f15)

## Files Created

| File | Lines | Description |
|------|-------|-------------|
| `service/monitor/hardware/impl/HardwareMetricsServiceImpl.java` | 488 | 采集实现：CPU/内存/磁盘/网络/系统信息，RWLock 缓存，RingBuffer 趋势，局部降级 |
| `test/.../RingBufferTest.java` | 128 | 环形缓冲区单元测试 |
| `test/.../HardwareMetricsServiceImplTest.java` | 574 | Service 层 Mockito 测试，覆盖所有指标和降级路径 |

## Implementation Details

- CPU: `getSystemCpuLoadBetweenTicks()` + `getProcessorCpuLoadBetweenTicks()` → perCoreLoad[]
- Memory: `getTotal()` / `getAvailable()` → 计算使用率
- Disk: `HWDiskStore[]` — 保留前次快照做差分（IOPS + 读写速度），每 3 次采样
- Network: `NetworkIF[]` — 差分 `getBytesSent/Recv()`，每 3 次采样
- System: `OperatingSystem` — 版本/运行时间/进程数，每次采样更新
- Sync: `ReentrantReadWriteLock` — 采样写锁，API 读锁
- Trend: 每 5 次采样（10s）取快照写入 RingBuffer（360 容量）
- Degradation: 每个 sampler try-catch，失败返回 null 值
- Lifecycle: `@PostConstruct` 预热 ticks，`@PreDestroy` 关闭 scheduler

## Verification

- [x] `./gradlew.bat build -x test` 编译通过
- [x] `./gradlew.bat test --tests "*RingBufferTest"` 全部通过
- [x] `./gradlew.bat test --tests "*HardwareMetricsServiceImplTest"` 全部通过
