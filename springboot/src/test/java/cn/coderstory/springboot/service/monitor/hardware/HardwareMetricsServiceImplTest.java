package cn.coderstory.springboot.service.monitor.hardware;

import cn.coderstory.springboot.config.MonitorHardwareProperties;
import cn.coderstory.springboot.dto.monitor.hardware.DiskMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.HardwareMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.NetworkMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.SystemInfoDTO;
import cn.coderstory.springboot.service.monitor.hardware.impl.HardwareMetricsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import oshi.ffm.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HardwareMetricsService 单元测试。
 * <p>
 * 使用 Mockito 模拟 OSHI FFM 依赖，验证采集服务的 CPU/内存/磁盘/网络/系统信息的
 * 正常采集和局部降级逻辑，以及环形缓冲区趋势存储和缓存一致性。
 *
 * @since 1.8.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HardwareMetricsService 单元测试")
class HardwareMetricsServiceImplTest {

    @Mock
    private SystemInfo systemInfo;
    @Mock
    private HardwareAbstractionLayer hal;
    @Mock
    private CentralProcessor processor;
    @Mock
    private CentralProcessor.ProcessorIdentifier processorIdentifier;

    @Mock
    private GlobalMemory memory;
    @Mock
    private OperatingSystem os;
    @Mock
    private OperatingSystem.OSVersionInfo versionInfo;
    @Mock
    private ScheduledExecutorService scheduler;
    @Mock
    private MonitorHardwareProperties properties;

    @InjectMocks
    private HardwareMetricsServiceImpl service;

    @BeforeEach
    void setUp() {
        when(systemInfo.getHardware()).thenReturn(hal);
        when(systemInfo.getOperatingSystem()).thenReturn(os);
        when(properties.getSamplingInterval()).thenReturn(2000);
        when(properties.getDiskNetInterval()).thenReturn(3);
        when(properties.getTrendBufferSize()).thenReturn(360);
        when(properties.getTrendDecimation()).thenReturn(5);

        // 默认 CPU mock（将根据测试需要覆盖）
        when(hal.getProcessor()).thenReturn(processor);
        when(processor.getLogicalProcessorCount()).thenReturn(4);
        when(processor.getPhysicalProcessorCount()).thenReturn(2);
        when(processor.getProcessorIdentifier()).thenReturn(processorIdentifier);
        when(processorIdentifier.getName()).thenReturn("Test CPU");
        when(processorIdentifier.getVendorFreq()).thenReturn(3000000000L);
        when(processorIdentifier.isCpu64bit()).thenReturn(true);

        // 默认内存 mock
        when(hal.getMemory()).thenReturn(memory);

        // 默认 OS mock
        when(os.getVersionInfo()).thenReturn(versionInfo);

        // 默认磁盘/网络 mock（init 时需要）
        when(hal.getDiskStores()).thenReturn(List.of());
        when(hal.getNetworkIFs()).thenReturn(List.of());
    }

    // ==================== CPU 采集测试 ====================

    @Nested
    @DisplayName("CPU 采集")
    class CpuCollectionTests {

        @BeforeEach
        void cpuSetUp() {
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L})
                    .thenReturn(new long[]{200L, 300L, 400L, 500L, 600L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}})
                    .thenReturn(new long[][]{{200L, 300L, 400L, 500L, 600L}});

            service.init();
        }

        @Test
        @DisplayName("CPU 正常采集返回系统使用率和各核使用率")
        void collectCpuNormal() {
            when(processor.getSystemCpuLoadBetweenTicks(any(), any())).thenReturn(0.5);
            when(processor.getProcessorCpuLoadBetweenTicks(any(), any())).thenReturn(new double[]{0.4, 0.6});

            service.collect();
            HardwareMetricsDTO result = service.getCurrentMetrics();

            assertNotNull(result);
            assertNotNull(result.getCpu());
            // 0.5 / 4 * 100 = 12.5
            assertEquals(12.5, result.getCpu().getSystemLoad(), 0.01);
            assertNotNull(result.getCpu().getPerCoreLoad());
            assertEquals(2, result.getCpu().getPerCoreLoad().length);
            assertEquals(4, result.getCpu().getLogicalCores());
            assertEquals(2, result.getCpu().getPhysicalCores());
            assertNotNull(result.getCpu().getProcessorName());
        }

        @Test
        @DisplayName("CPU 采集异常时局部降级，其他指标不受影响")
        void whenCpuFails_otherMetricsUnaffected() {
            // init 成功，但 collect 时 CPU 抛异常
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L})   // init
                    .thenThrow(new RuntimeException("OSHI CPU 错误"));

            // 给 sampleMemory 和 sampleSystem 配置 mock
            when(memory.getTotal()).thenReturn(16L * 1024 * 1024 * 1024);
            when(memory.getAvailable()).thenReturn(8L * 1024 * 1024 * 1024);
            when(os.getFamily()).thenReturn("Windows");

            service.collect();
            HardwareMetricsDTO result = service.getCurrentMetrics();

            // CPU 降级返回空 DTO（非 null，但无有效数据）
            assertNotNull(result.getCpu());
            assertEquals(0.0, result.getCpu().getSystemLoad(), 0.01);
            assertNull(result.getCpu().getPerCoreLoad());

            // 其他指标不受影响
            assertNotNull(result.getMemory());
            assertNotNull(result.getSystem());
        }
    }

    // ==================== 内存采集测试 ====================

    @Nested
    @DisplayName("内存采集")
    class MemoryCollectionTests {

        @BeforeEach
        void memSetUp() {
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});

            service.init();
        }

        @Test
        @DisplayName("内存正常采集返回总量、可用量、已用量和使用率")
        void collectMemoryNormal() {
            when(memory.getTotal()).thenReturn(16L * 1024 * 1024 * 1024);
            when(memory.getAvailable()).thenReturn(8L * 1024 * 1024 * 1024);

            service.collect();
            HardwareMetricsDTO result = service.getCurrentMetrics();

            assertNotNull(result);
            assertNotNull(result.getMemory());
            assertEquals(16L * 1024 * 1024 * 1024,
                    result.getMemory().getTotal().getValue().longValue());
            assertEquals(8L * 1024 * 1024 * 1024,
                    result.getMemory().getAvailable().getValue().longValue());
            assertEquals(8L * 1024 * 1024 * 1024,
                    result.getMemory().getUsed().getValue().longValue());
            assertEquals(50.0, result.getMemory().getUsagePercent().getValue(), 0.1);
        }
    }

    // ==================== 磁盘采集测试 ====================

    @Nested
    @DisplayName("磁盘采集")
    class DiskCollectionTests {

        private HWDiskStore diskPrev;
        private HWDiskStore diskCurr;

        @BeforeEach
        void diskSetUp() {
            diskPrev = mock(HWDiskStore.class);
            diskCurr = mock(HWDiskStore.class);

            when(diskPrev.getName()).thenReturn("/dev/sda");
            when(diskPrev.getModel()).thenReturn("Test Disk");
            when(diskPrev.getSize()).thenReturn(100L * 1024 * 1024 * 1024);
            when(diskPrev.getReadBytes()).thenReturn(10000L);
            when(diskPrev.getWriteBytes()).thenReturn(5000L);
            when(diskPrev.getReads()).thenReturn(100L);
            when(diskPrev.getWrites()).thenReturn(50L);
            when(diskPrev.getDiskType()).thenReturn("SSD");

            when(diskCurr.getName()).thenReturn("/dev/sda");
            when(diskCurr.getModel()).thenReturn("Test Disk");
            when(diskCurr.getSize()).thenReturn(100L * 1024 * 1024 * 1024);
            when(diskCurr.getReadBytes()).thenReturn(16000L);
            when(diskCurr.getWriteBytes()).thenReturn(8000L);
            when(diskCurr.getReads()).thenReturn(160L);
            when(diskCurr.getWrites()).thenReturn(80L);
            when(diskCurr.getDiskType()).thenReturn("SSD");

            when(hal.getDiskStores())
                    .thenReturn(List.of(diskPrev))   // init()
                    .thenReturn(List.of(diskCurr));  // sampleDisks()

            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});

            service.init();
        }

        @Test
        @DisplayName("磁盘每 3 次采样更新一次（前两次为 null，第三次有数据）")
        void diskUpdatesEveryThirdSample() {
            // 第 1 次采样：diskNetCounter=1，不更新磁盘
            service.collect();
            assertNull(service.getCurrentMetrics().getDisks());

            // 第 2 次采样：diskNetCounter=2，不更新磁盘
            service.collect();
            assertNull(service.getCurrentMetrics().getDisks());

            // 第 3 次采样：diskNetCounter=3，更新磁盘
            service.collect();
            assertNotNull(service.getCurrentMetrics().getDisks());
            assertEquals(1, service.getCurrentMetrics().getDisks().size());
        }

        @Test
        @DisplayName("磁盘差分计算返回正确的 IOPS 和读写速度")
        void diskDifferentialCalculation() {
            // 前两次不涉及磁盘，第三次才采样
            service.collect();
            service.collect();
            service.collect();

            List<DiskMetricsDTO> disks = service.getCurrentMetrics().getDisks();
            assertNotNull(disks);
            assertEquals(1, disks.size());

            DiskMetricsDTO disk = disks.get(0);
            assertEquals("/dev/sda", disk.getName());

            // 差分验证: (16000-10000)*1000/6000 = 1000
            assertNotNull(disk.getReadBytesPerSec());
            assertEquals(1000L, disk.getReadBytesPerSec().getValue().longValue());

            // (8000-5000)*1000/6000 = 500
            assertNotNull(disk.getWriteBytesPerSec());
            assertEquals(500L, disk.getWriteBytesPerSec().getValue().longValue());

            // (160-100)*1000/6000 = 10
            assertNotNull(disk.getReadsPerSec());
            assertEquals(10L, disk.getReadsPerSec().getValue().longValue());

            // (80-50)*1000/6000 = 5
            assertNotNull(disk.getWritesPerSec());
            assertEquals(5L, disk.getWritesPerSec().getValue().longValue());
        }
    }

    // ==================== 网络采集测试 ====================

    @Nested
    @DisplayName("网络采集")
    class NetworkCollectionTests {

        private NetworkIF netPrev;
        private NetworkIF netCurr;

        @BeforeEach
        void netSetUp() {
            netPrev = mock(NetworkIF.class);
            netCurr = mock(NetworkIF.class);

            when(netPrev.getDisplayName()).thenReturn("eth0");
            when(netPrev.getMacaddr()).thenReturn("00:11:22:33:44:55");
            when(netPrev.getSpeed()).thenReturn(1000000000L);
            when(netPrev.getBytesSent()).thenReturn(1000L);
            when(netPrev.getBytesRecv()).thenReturn(500L);
            when(netPrev.getPacketsSent()).thenReturn(100L);
            when(netPrev.getPacketsRecv()).thenReturn(50L);

            when(netCurr.getDisplayName()).thenReturn("eth0");
            when(netCurr.getMacaddr()).thenReturn("00:11:22:33:44:55");
            when(netCurr.getSpeed()).thenReturn(1000000000L);
            when(netCurr.getBytesSent()).thenReturn(7000L);
            when(netCurr.getBytesRecv()).thenReturn(3500L);
            when(netCurr.getPacketsSent()).thenReturn(300L);
            when(netCurr.getPacketsRecv()).thenReturn(150L);

            when(hal.getNetworkIFs())
                    .thenReturn(List.of(netPrev))   // init()
                    .thenReturn(List.of(netCurr));  // sampleNetwork()

            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});
            when(hal.getDiskStores()).thenReturn(List.of());

            service.init();
        }

        @Test
        @DisplayName("网络差分计算返回正确的上下行速率和包速率")
        void networkDifferentialCalculation() {
            // 前两次不涉及网络，第三次才采样
            service.collect();
            service.collect();
            service.collect();

            List<NetworkMetricsDTO> nets = service.getCurrentMetrics().getNetwork();
            assertNotNull(nets);
            assertEquals(1, nets.size());

            NetworkMetricsDTO net = nets.get(0);
            assertEquals("eth0", net.getDisplayName());

            // 差分验证: (7000-1000)*1000/6000 = 1000
            assertNotNull(net.getBytesSentPerSec());
            assertEquals(1000L, net.getBytesSentPerSec().getValue().longValue());

            // (3500-500)*1000/6000 = 500
            assertNotNull(net.getBytesRecvPerSec());
            assertEquals(500L, net.getBytesRecvPerSec().getValue().longValue());

            // (300-100)*1000/6000 ≈ 33
            assertNotNull(net.getPacketsSentPerSec());
            assertEquals(33L, net.getPacketsSentPerSec().getValue().longValue());

            // (150-50)*1000/6000 ≈ 16
            assertNotNull(net.getPacketsRecvPerSec());
            assertEquals(16L, net.getPacketsRecvPerSec().getValue().longValue());
        }
    }

    // ==================== 系统信息采集测试 ====================

    @Nested
    @DisplayName("系统信息采集")
    class SystemInfoCollectionTests {

        @BeforeEach
        void sysSetUp() {
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});

            when(os.getFamily()).thenReturn("Windows");
            when(os.getVersionInfo()).thenReturn(versionInfo);
            when(versionInfo.toString()).thenReturn("10.0.22621");
            when(versionInfo.getCodeName()).thenReturn("Windows 11 Pro");
            when(os.getSystemUptime()).thenReturn(123456L);
            when(os.getProcessCount()).thenReturn(200);

            service.init();
        }

        @Test
        @DisplayName("系统信息正常采集返回操作系统版本、运行时间和进程数")
        void collectSystemInfo() {
            service.collect();

            SystemInfoDTO systemInfoDto = service.getSystemInfo();
            assertNotNull(systemInfoDto);
            assertEquals("Windows", systemInfoDto.getOsFamily());
            assertEquals("10.0.22621", systemInfoDto.getOsVersion());
            assertEquals("Windows 11 Pro", systemInfoDto.getOsVersionInfo());
            assertEquals(123456L, systemInfoDto.getSystemUptime());
            assertEquals(200, systemInfoDto.getProcessCount());
            // 123456 秒 = 1d 10h 17m
            assertEquals("1d 10h 17m", systemInfoDto.getSystemUptimeFormatted());
        }
    }

    // ==================== 趋势存储测试 ====================

    @Nested
    @DisplayName("趋势存储")
    class TrendStorageTests {

        @BeforeEach
        void trendSetUp() {
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});

            service.init();
        }

        @Test
        @DisplayName("每 5 次采样向 RingBuffer 写入一个趋势点")
        void trendStoredEveryFifthSample() {
            // trendDecimation=5，所以前 4 次采集不存储趋势
            for (int i = 0; i < 4; i++) {
                service.collect();
            }
            assertEquals(0, service.getTrendHistory().size(),
                    "前 4 次采样不应写入趋势");

            // 第 5 次采集后 trendCounter >= 5，写入趋势
            service.collect();
            assertEquals(1, service.getTrendHistory().size(),
                    "第 5 次采样后应在 trendBuffer 中有一个点");

            // 再采 5 次，应有 2 个点
            for (int i = 0; i < 5; i++) {
                service.collect();
            }
            assertEquals(2, service.getTrendHistory().size(),
                    "第 10 次采样后应在 trendBuffer 中有 2 个点");
        }
    }

    // ==================== 初始状态测试 ====================

    @Nested
    @DisplayName("初始状态")
    class InitialStateTests {

        @Test
        @DisplayName("首次采集前 getCurrentMetrics 返回空实例")
        void whenNoCollection_returnsEmpty() {
            HardwareMetricsDTO result = service.getCurrentMetrics();
            assertNotNull(result);
            assertEquals(0L, result.getTimestamp());
            assertNull(result.getCpu());
            assertNull(result.getMemory());
            assertNull(result.getDisks());
            assertNull(result.getNetwork());
            assertNull(result.getSystem());
        }

        @Test
        @DisplayName("首次采集前 getSystemInfo 返回空实例")
        void whenNoCollection_systemInfoReturnsEmpty() {
            SystemInfoDTO sysInfo = service.getSystemInfo();
            assertNotNull(sysInfo);
            assertNull(sysInfo.getOsFamily());
        }

        @Test
        @DisplayName("首次采集前 getTrendHistory 返回空列表")
        void whenNoCollection_trendHistoryReturnsEmpty() {
            assertTrue(service.getTrendHistory().isEmpty());
        }
    }

    // ==================== PostConstruct 初始化测试 ====================

    @Nested
    @DisplayName("初始化")
    class InitTests {

        @Test
        @DisplayName("init 预热 CPU ticks 和磁盘网络状态")
        void initWarmsUpCpuTicks() {
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});

            service.init();

            // 验证 scheduler 被调用启动定时采集
            verify(scheduler).scheduleAtFixedRate(
                    any(Runnable.class), eq(0L), eq(2000L), eq(java.util.concurrent.TimeUnit.MILLISECONDS));
        }
    }

    // ==================== 局部降级综合测试 ====================

    @Nested
    @DisplayName("局部降级")
    class DegradationTests {

        @BeforeEach
        void degSetUp() {
            when(processor.getSystemCpuLoadTicks())
                    .thenReturn(new long[]{100L, 200L, 300L, 400L, 500L});
            when(processor.getProcessorCpuLoadTicks())
                    .thenReturn(new long[][]{{100L, 200L, 300L, 400L, 500L}});
            when(memory.getTotal()).thenReturn(16L * 1024 * 1024 * 1024);
            when(memory.getAvailable()).thenReturn(8L * 1024 * 1024 * 1024);
            when(os.getFamily()).thenReturn("Windows");

            service.init();
        }

        @Test
        @DisplayName("内存采集异常时不影响 CPU 和系统信息")
        void memoryFailureDoesNotAffectOthers() {
            // getTotal 在 sampleMemory 中被调用时直接抛出异常
            when(memory.getTotal()).thenThrow(new RuntimeException("OSHI 内存错误"));

            service.collect();
            HardwareMetricsDTO result = service.getCurrentMetrics();

            // 内存降级返回空 DTO（非 null，但无有效数据）
            assertNotNull(result.getMemory());
            assertNull(result.getMemory().getTotal());
            // CPU 和系统不受影响
            assertNotNull(result.getCpu());
            assertNotNull(result.getSystem());
        }
    }
}
