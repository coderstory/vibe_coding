package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 趋势数据点。
 * <p>
 * 用于趋势查询 API 返回值，包含时间戳、指标值和指标名称。
 * 一个趋势数据点代表某一时刻下的单个指标值（如 CPU 使用率）。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendDataPoint {

    /** 采样时间戳（毫秒） */
    private long timestamp;

    /** 指标值 */
    private double value;

    /** 指标名称（如 "cpu"、"memory"、"disk_read"、"net_sent"） */
    private String metric;
}
