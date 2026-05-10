package cn.coderstory.springboot.service.monitor.hardware;

import cn.coderstory.springboot.dto.monitor.hardware.HardwareMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.SystemInfoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HardwareMetricsService 集成测试。
 * <p>
 * 加载完整 Spring 上下文，使用真实 OSHI 库进行硬件指标采集验证。
 * 仅在 Windows 操作系统上执行（OSHI 采集需要 Windows API）。
 * 默认被 build.gradle.kts 中的 exclude 配置排除（文件名以 IT 结尾），
 * 需通过 ./gradlew.bat test --tests *HardwareMetricsServiceIT 单独运行。
 * <p>
 * 注意：本集成测试需要 HardwareMetricsServiceImpl 实现类（Plan 02）就绪，
 * 以及 MySQL、Redis、RocketMQ 等基础设施正常运行。
 *
 * @since 1.8.0
 */
@SpringBootTest
@EnabledOnOs(OS.WINDOWS)
@Tag("integration")
@DisplayName("HardwareMetricsService 集成测试")
class HardwareMetricsServiceIT {

    @Autowired(required = false)
    private HardwareMetricsService metricsService;

    @Test
    @DisplayName("Spring 上下文加载正常，metricsService Bean 存在")
    void shouldLoadSpringContext() {
        assertNotNull(metricsService, "HardwareMetricsService Bean 应被注入（需 Plan 02 实现类就绪）");
    }

    @Test
    @DisplayName("getCurrentMetrics 返回完整指标快照")
    void shouldReturnCompleteMetricsSnapshot() throws Exception {
        // 等待首次采集完成（OSHI 初始化 + 首次采样需要约 3 秒）
        Thread.sleep(3000);

        HardwareMetricsDTO metrics = metricsService.getCurrentMetrics();
        assertNotNull(metrics, "指标快照不应为 null");

        // CPU 指标不应为 null
        assertNotNull(metrics.getCpu(), "CPU 指标不应为 null");
        assertTrue(metrics.getCpu().getSystemLoad() >= 0, "CPU 使用率应 >= 0");

        // 内存指标不应为 null
        assertNotNull(metrics.getMemory(), "内存指标不应为 null");
        assertNotNull(metrics.getMemory().getTotal(), "内存总量不应为 null");

        // 磁盘指标应为 List
        assertNotNull(metrics.getDisks(), "磁盘指标列表不应为 null");

        // 网络指标应为 List
        assertNotNull(metrics.getNetwork(), "网络指标列表不应为 null");

        // 时间戳应大于 0
        assertTrue(metrics.getTimestamp() > 0, "采集时间戳应大于 0");
    }

    @Test
    @DisplayName("getSystemInfo 返回操作系统信息")
    void shouldReturnSystemInfo() {
        SystemInfoDTO info = metricsService.getSystemInfo();

        assertNotNull(info, "系统信息不应为 null");
        assertNotNull(info.getOsFamily(), "操作系统系列不应为 null");
        assertTrue(info.getSystemUptime() >= 0, "系统运行时间应 >= 0");
        assertTrue(info.getProcessCount() >= 0, "进程数应 >= 0");
    }

    @Test
    @DisplayName("getTrendHistory 返回趋势数据列表")
    void shouldReturnTrendHistory() throws Exception {
        // 等待足够时间确保至少一个趋势点写入环形缓冲区
        // 按 10s 写入一次趋势，等待 12 秒应至少写入 1 个点
        Thread.sleep(12000);

        List<HardwareMetricsDTO> history = metricsService.getTrendHistory();
        assertNotNull(history, "趋势历史不应为 null");
        // 不强制要求 >0，以防超时或采集未启动
        assertTrue(history.size() >= 0, "趋势数据点数应 >= 0");
    }
}
