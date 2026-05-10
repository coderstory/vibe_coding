package cn.coderstory.springboot.controller.monitor.hardware;

import cn.coderstory.springboot.dto.monitor.hardware.*;
import cn.coderstory.springboot.service.monitor.hardware.HardwareMetricsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HardwareMonitorController 单元测试。
 * <p>
 * 使用 MockMvcBuilders.standaloneSetup() 构建测试环境，通过 @Mock 模拟 HardwareMetricsService。
 * 覆盖三个端点的正常路径和异常路径，验证 JSON 响应结构和业务码。
 *
 * @since 1.8.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HardwareMonitorController 单元测试")
class HardwareMonitorControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HardwareMetricsService metricsService;

    @InjectMocks
    private HardwareMonitorController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ==================== 测试辅助方法 ====================

    private HardwareMetricsDTO mockHardwareMetricsDTO() {
        HardwareMetricsDTO dto = new HardwareMetricsDTO();
        dto.setTimestamp(System.currentTimeMillis());

        CpuMetricsDTO cpu = new CpuMetricsDTO();
        cpu.setSystemLoad(45.2);
        cpu.setPerCoreLoad(new double[]{40.0, 50.0, 45.0, 55.0});
        cpu.setLogicalCores(4);
        cpu.setPhysicalCores(4);
        cpu.setProcessorName("Test CPU");
        cpu.setProcessorFrequency(3000000000L);
        cpu.setCpu64bit(true);
        dto.setCpu(cpu);

        MemoryMetricsDTO memory = new MemoryMetricsDTO();
        memory.setTotal(MetricValue.of(17179869184L, "16.0 GB", "GB"));
        memory.setAvailable(MetricValue.of(8589934592L, "8.0 GB", "GB"));
        memory.setUsed(MetricValue.of(8589934592L, "8.0 GB", "GB"));
        memory.setUsagePercent(MetricValue.of(50.0, "50.0%", "%"));
        dto.setMemory(memory);

        List<DiskMetricsDTO> disks = new ArrayList<>();
        DiskMetricsDTO disk = new DiskMetricsDTO();
        disk.setName("C:");
        disk.setModel("Test SSD");
        disk.setSize(MetricValue.of(512110190592L, "476.9 GB", "GB"));
        disk.setDiskType("SSD");
        disk.setReadBytesPerSec(MetricValue.of(1048576L, "1.0 MB/s", "B/s"));
        disk.setWriteBytesPerSec(MetricValue.of(524288L, "512.0 KB/s", "B/s"));
        disks.add(disk);
        dto.setDisks(disks);

        List<NetworkMetricsDTO> networks = new ArrayList<>();
        NetworkMetricsDTO net = new NetworkMetricsDTO();
        net.setDisplayName("eth0");
        net.setMacAddress("00:11:22:33:44:55");
        net.setSpeed(1000000000L);
        net.setBytesSentPerSec(MetricValue.of(102400L, "100.0 KB/s", "B/s"));
        net.setBytesRecvPerSec(MetricValue.of(204800L, "200.0 KB/s", "B/s"));
        networks.add(net);
        dto.setNetwork(networks);

        return dto;
    }

    private SystemInfoDTO mockSystemInfo() {
        SystemInfoDTO info = new SystemInfoDTO();
        info.setOsFamily("Windows");
        info.setOsVersion("11");
        info.setOsVersionInfo("23H2");
        info.setSystemUptime(3600);
        info.setSystemUptimeFormatted("1h 0m 0s");
        info.setProcessCount(200);
        return info;
    }

    private List<HardwareMetricsDTO> mockHistory() {
        List<HardwareMetricsDTO> history = new ArrayList<>();
        long baseTime = System.currentTimeMillis() - 360_000L;
        for (int i = 0; i < 360; i++) {
            HardwareMetricsDTO dto = new HardwareMetricsDTO();
            dto.setTimestamp(baseTime + i * 1000L);

            CpuMetricsDTO cpu = new CpuMetricsDTO();
            cpu.setSystemLoad(30.0 + i * 0.1);
            dto.setCpu(cpu);

            MemoryMetricsDTO memory = new MemoryMetricsDTO();
            memory.setUsagePercent(MetricValue.of(40.0 + i * 0.05, String.format("%.1f%%", 40.0 + i * 0.05), "%"));
            dto.setMemory(memory);

            List<DiskMetricsDTO> disks = new ArrayList<>();
            DiskMetricsDTO disk = new DiskMetricsDTO();
            disk.setReadBytesPerSec(MetricValue.of(1000L + i, String.format("%d B/s", 1000L + i), "B/s"));
            disk.setWriteBytesPerSec(MetricValue.of(500L + i, String.format("%d B/s", 500L + i), "B/s"));
            disks.add(disk);
            dto.setDisks(disks);

            List<NetworkMetricsDTO> networks = new ArrayList<>();
            NetworkMetricsDTO net = new NetworkMetricsDTO();
            net.setBytesSentPerSec(MetricValue.of(200L + i, String.format("%d B/s", 200L + i), "B/s"));
            net.setBytesRecvPerSec(MetricValue.of(300L + i, String.format("%d B/s", 300L + i), "B/s"));
            networks.add(net);
            dto.setNetwork(networks);

            history.add(dto);
        }
        return history;
    }

    // ==================== GET /current 端点测试 ====================

    @Nested
    @DisplayName("GET /api/monitor/hardware/current")
    class GetCurrentEndpoint {

        @Test
        @DisplayName("正常返回 HardwareMetricsDTO 完整 JSON")
        void shouldReturnFullMetricsDto() throws Exception {
            when(metricsService.getCurrentMetrics()).thenReturn(mockHardwareMetricsDTO());

            mockMvc.perform(get("/api/monitor/hardware/current"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.data.cpu.systemLoad").value(45.2))
                    .andExpect(jsonPath("$.data.cpu.logicalCores").value(4))
                    .andExpect(jsonPath("$.data.memory.usagePercent.value").value(50.0))
                    .andExpect(jsonPath("$.data.disks").isArray())
                    .andExpect(jsonPath("$.data.disks[0].name").value("C:"))
                    .andExpect(jsonPath("$.data.network").isArray())
                    .andExpect(jsonPath("$.data.network[0].displayName").value("eth0"));
        }

        @Test
        @DisplayName("首次采集前返回空 DTO，无嵌套字段")
        void shouldReturnEmptyDtoBeforeFirstCollection() throws Exception {
            when(metricsService.getCurrentMetrics()).thenReturn(HardwareMetricsDTO.empty());

            mockMvc.perform(get("/api/monitor/hardware/current"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.cpu").doesNotExist());
        }
    }

    // ==================== GET /trend 端点测试 ====================

    @Nested
    @DisplayName("GET /api/monitor/hardware/trend")
    class GetTrendEndpoint {

        @Test
        @DisplayName("cpu 指标趋势返回趋势点列表")
        void shouldReturnCpuTrendPoints() throws Exception {
            when(metricsService.getTrendHistory()).thenReturn(mockHistory());

            mockMvc.perform(get("/api/monitor/hardware/trend")
                            .param("metric", "cpu")
                            .param("range", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(10))
                    .andExpect(jsonPath("$.data[0].metric").value("cpu"))
                    .andExpect(jsonPath("$.data[0].value").isNumber())
                    .andExpect(jsonPath("$.data[0].timestamp").isNumber());
        }

        @Test
        @DisplayName("非法 metric 参数返回 400 业务码")
        void shouldReturn400ForInvalidMetric() throws Exception {
            mockMvc.perform(get("/api/monitor/hardware/trend")
                            .param("metric", "invalid"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400))
                    .andExpect(jsonPath("$.message").value(containsString("不支持的指标")));
        }

        @Test
        @DisplayName("range 超过 360 时截断到 360")
        void shouldCapRangeTo360() throws Exception {
            when(metricsService.getTrendHistory()).thenReturn(mockHistory());

            mockMvc.perform(get("/api/monitor/hardware/trend")
                            .param("metric", "cpu")
                            .param("range", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(360));
        }

        @Test
        @DisplayName("memory 指标提取 usagePercent 值")
        void shouldExtractMemoryUsagePercent() throws Exception {
            when(metricsService.getTrendHistory()).thenReturn(mockHistory());

            mockMvc.perform(get("/api/monitor/hardware/trend")
                            .param("metric", "memory")
                            .param("range", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].metric").value("memory"))
                    .andExpect(jsonPath("$.data[0].value").isNumber());
        }
    }

    // ==================== GET /system 端点测试 ====================

    @Nested
    @DisplayName("GET /api/monitor/hardware/system")
    class GetSystemEndpoint {

        @Test
        @DisplayName("正常返回系统信息")
        void shouldReturnSystemInfo() throws Exception {
            when(metricsService.getSystemInfo()).thenReturn(mockSystemInfo());

            mockMvc.perform(get("/api/monitor/hardware/system"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.osFamily").value("Windows"))
                    .andExpect(jsonPath("$.data.osVersion").value("11"))
                    .andExpect(jsonPath("$.data.systemUptime").value(3600))
                    .andExpect(jsonPath("$.data.processCount").value(200));
        }

        @Test
        @DisplayName("无数据时返回空字符串或零值字段")
        void shouldReturnDefaultValuesWhenNoData() throws Exception {
            SystemInfoDTO emptyInfo = new SystemInfoDTO();
            when(metricsService.getSystemInfo()).thenReturn(emptyInfo);

            mockMvc.perform(get("/api/monitor/hardware/system"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.osFamily").doesNotExist());
        }
    }
}
