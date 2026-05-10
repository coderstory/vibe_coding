package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.Data;

/**
 * 硬件指标顶层 DTO。
 * <p>
 * 包含采集时间戳和所有子指标（CPU、内存、磁盘列表、网络列表、系统信息）。
 * 作为 REST API 返回的统一结构，一次请求返回全部硬件指标快照。
 */
@Data
public class HardwareMetricsDTO {

    /** 采集时间戳（毫秒） */
    private long timestamp;

    /** CPU 指标 */
    private CpuMetricsDTO cpu;

    /** 内存指标 */
    private MemoryMetricsDTO memory;

    /** 磁盘指标列表 */
    private java.util.List<DiskMetricsDTO> disks;

    /** 网络接口指标列表 */
    private java.util.List<NetworkMetricsDTO> network;

    /** 系统基本信息 */
    private SystemInfoDTO system;

    /**
     * 返回空实例，用于首次采集前或降级场景。
     *
     * @return 空的硬件指标实例
     */
    public static HardwareMetricsDTO empty() {
        return new HardwareMetricsDTO();
    }
}
