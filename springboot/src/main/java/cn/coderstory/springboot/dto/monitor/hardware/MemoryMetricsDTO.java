package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.Data;

/**
 * 内存指标 DTO。
 * <p>
 * 包含物理内存的总量、可用量、已用量和使用率，所有值通过 MetricValue 格式化。
 */
@Data
public class MemoryMetricsDTO {

    /** 物理内存总量 */
    private MetricValue<Long> total;

    /** 可用物理内存 */
    private MetricValue<Long> available;

    /** 已用物理内存 */
    private MetricValue<Long> used;

    /** 内存使用率（百分比，范围 0-100） */
    private MetricValue<Double> usagePercent;

    /**
     * 返回空实例，用于降级场景。
     *
     * @return 空的内存指标实例
     */
    public static MemoryMetricsDTO empty() {
        return new MemoryMetricsDTO();
    }
}
