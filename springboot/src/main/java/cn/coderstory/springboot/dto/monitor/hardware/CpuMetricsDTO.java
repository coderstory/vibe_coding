package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.Data;

/**
 * CPU 指标 DTO。
 * <p>
 * 包含总体 CPU 使用率、各核使用率数组、逻辑/物理核心数和处理器信息。
 */
@Data
public class CpuMetricsDTO {

    /** 总体 CPU 使用率（百分比，范围 0-100） */
    private double systemLoad;

    /** 各逻辑核使用率数组（百分比，范围 0-100） */
    private double[] perCoreLoad;

    /** 逻辑核心数 */
    private int logicalCores;

    /** 物理核心数 */
    private int physicalCores;

    /** 处理器名称 */
    private String processorName;

    /** 处理器频率（Hz） */
    private long processorFrequency;

    /** 是否为 64 位处理器 */
    private boolean cpu64bit;

    /**
     * 返回全零实例，用于降级场景。
     *
     * @return 空的 CPU 指标实例
     */
    public static CpuMetricsDTO empty() {
        return new CpuMetricsDTO();
    }
}
