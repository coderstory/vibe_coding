package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 格式化指标值结构。
 * <p>
 * 包含原始数值、格式化字符串和单位，便于前端直接展示。
 * 统一数据格式化格式，避免前端重复处理单位转换。
 *
 * @param <T> 原始数值类型（通常为 Long、Double 或 Integer）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricValue<T> {

    /** 原始数值（如 8.5） */
    private T value;

    /** 格式化字符串（如 "8.5 GB"） */
    private String format;

    /** 单位（如 "GB"） */
    private String unit;

    /**
     * 静态工厂方法，创建 MetricValue 实例。
     *
     * @param value  原始数值
     * @param format 格式化字符串
     * @param unit   单位
     * @param <T>    数值类型
     * @return MetricValue 实例
     */
    public static <T> MetricValue<T> of(T value, String format, String unit) {
        return new MetricValue<>(value, format, unit);
    }
}
