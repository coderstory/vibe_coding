package cn.coderstory.springboot.controller.monitor.hardware;

import cn.coderstory.springboot.dto.ApiResponse;
import cn.coderstory.springboot.dto.monitor.hardware.DiskMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.HardwareMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.NetworkMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.SystemInfoDTO;
import cn.coderstory.springboot.dto.monitor.hardware.TrendDataPoint;
import cn.coderstory.springboot.service.monitor.hardware.HardwareMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 硬件监控 REST 控制器。
 * <p>
 * 提供硬件指标的实时快照、趋势查询和系统基本信息三个端点。
 * 所有端点通过 HardwareMetricsService 接口读取缓存数据，不直接调用 OSHI。
 *
 * @since 1.8.0
 */
@RestController
@RequestMapping("/api/monitor/hardware")
@RequiredArgsConstructor
@Slf4j
public class HardwareMonitorController {

    private final HardwareMetricsService metricsService;

    /** 支持的指标名称列表，用于趋势查询参数校验 */
    private static final Set<String> SUPPORTED_METRICS = Set.of(
            "cpu", "memory", "disk_read", "disk_write", "net_sent", "net_recv"
    );

    /**
     * 获取当前硬件指标快照。
     * <p>
     * 返回包含 CPU、内存、磁盘列表、网络接口列表和系统信息在内的完整指标快照。
     * 首次成功采集前返回空 DTO。
     *
     * @return 当前硬件指标快照
     */
    @GetMapping("/current")
    public ApiResponse<HardwareMetricsDTO> getCurrent() {
        return ApiResponse.success(metricsService.getCurrentMetrics());
    }

    /**
     * 按指标名称和时间范围查询趋势数据。
     * <p>
     * 从环形缓冲区中提取指定指标的时间序列数据，返回按时间顺序排列的趋势点列表。
     * range 参数超过 360 时自动截断到 360（环形缓冲区最大容量）。
     *
     * @param metric 指标名称（支持：cpu、memory、disk_read、disk_write、net_sent、net_recv）
     * @param range  趋势点数（范围 1-360，默认 360）
     * @return 趋势数据点列表
     */
    @GetMapping("/trend")
    public ApiResponse<List<TrendDataPoint>> getTrend(
            @RequestParam(defaultValue = "cpu") String metric,
            @RequestParam(defaultValue = "360") int range) {
        if (!SUPPORTED_METRICS.contains(metric)) {
            return ApiResponse.badRequest("不支持的指标: " + metric + "，支持: " + SUPPORTED_METRICS);
        }
        int actualRange = Math.min(Math.max(range, 1), 360);
        List<HardwareMetricsDTO> history = metricsService.getTrendHistory();
        List<TrendDataPoint> trend = extractTrend(history, metric, actualRange);
        return ApiResponse.success(trend);
    }

    /**
     * 获取系统基本信息。
     * <p>
     * 返回操作系统系列、版本号、系统运行时间和当前进程数等信息。
     *
     * @return 系统基本信息 DTO
     */
    @GetMapping("/system")
    public ApiResponse<SystemInfoDTO> getSystem() {
        return ApiResponse.success(metricsService.getSystemInfo());
    }

    /**
     * 从历史数据中提取指定指标的趋势点列表。
     *
     * @param history   历史采样点列表
     * @param metric    指标名称
     * @param maxPoints 最大趋势点数
     * @return 趋势数据点列表
     */
    private List<TrendDataPoint> extractTrend(List<HardwareMetricsDTO> history, String metric, int maxPoints) {
        List<TrendDataPoint> result = new ArrayList<>();
        int start = Math.max(0, history.size() - maxPoints);
        List<HardwareMetricsDTO> slice = history.subList(start, history.size());

        for (HardwareMetricsDTO dto : slice) {
            double value = switch (metric) {
                case "cpu" -> dto.getCpu() != null ? dto.getCpu().getSystemLoad() : 0.0;
                case "memory" -> dto.getMemory() != null ? dto.getMemory().getUsagePercent().getValue() : 0.0;
                case "disk_read" -> sumDiskReadBytes(dto.getDisks());
                case "disk_write" -> sumDiskWriteBytes(dto.getDisks());
                case "net_sent" -> sumNetSentBytes(dto.getNetwork());
                case "net_recv" -> sumNetRecvBytes(dto.getNetwork());
                default -> 0.0;
            };
            result.add(new TrendDataPoint(dto.getTimestamp(), value, metric));
        }
        return result;
    }

    /**
     * 对所有磁盘的读取速率求和。
     *
     * @param disks 磁盘指标列表
     * @return 总读取速率（字节/秒）
     */
    private static double sumDiskReadBytes(List<DiskMetricsDTO> disks) {
        if (disks == null || disks.isEmpty()) {
            return 0.0;
        }
        return disks.stream()
                .filter(d -> d.getReadBytesPerSec() != null && d.getReadBytesPerSec().getValue() != null)
                .mapToLong(d -> d.getReadBytesPerSec().getValue())
                .sum();
    }

    /**
     * 对所有磁盘的写入速率求和。
     *
     * @param disks 磁盘指标列表
     * @return 总写入速率（字节/秒）
     */
    private static double sumDiskWriteBytes(List<DiskMetricsDTO> disks) {
        if (disks == null || disks.isEmpty()) {
            return 0.0;
        }
        return disks.stream()
                .filter(d -> d.getWriteBytesPerSec() != null && d.getWriteBytesPerSec().getValue() != null)
                .mapToLong(d -> d.getWriteBytesPerSec().getValue())
                .sum();
    }

    /**
     * 对所有网络接口的发送速率求和。
     *
     * @param nets 网络接口指标列表
     * @return 总发送速率（字节/秒）
     */
    private static double sumNetSentBytes(List<NetworkMetricsDTO> nets) {
        if (nets == null || nets.isEmpty()) {
            return 0.0;
        }
        return nets.stream()
                .filter(n -> n.getBytesSentPerSec() != null && n.getBytesSentPerSec().getValue() != null)
                .mapToLong(n -> n.getBytesSentPerSec().getValue())
                .sum();
    }

    /**
     * 对所有网络接口的接收速率求和。
     *
     * @param nets 网络接口指标列表
     * @return 总接收速率（字节/秒）
     */
    private static double sumNetRecvBytes(List<NetworkMetricsDTO> nets) {
        if (nets == null || nets.isEmpty()) {
            return 0.0;
        }
        return nets.stream()
                .filter(n -> n.getBytesRecvPerSec() != null && n.getBytesRecvPerSec().getValue() != null)
                .mapToLong(n -> n.getBytesRecvPerSec().getValue())
                .sum();
    }
}
