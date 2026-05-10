package cn.coderstory.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 硬件监控配置属性类。
 * <p>
 * 映射 business.yaml 中 monitor.hardware 配置段，包含 OSHI 采集服务的采样间隔、
 * 环形缓冲区大小和降级策略等参数。通过 {@link @ConfigurationProperties} 绑定，
 * 在应用启动时自动加载。
 *
 * @since 1.8.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "monitor.hardware")
public class MonitorHardwareProperties {

    /**
     * 主循环采样间隔（毫秒）。
     * 默认值: 2000（2 秒）。每次采集更新 CPU 和内存指标。
     */
    private int samplingInterval = 2000;

    /**
     * 磁盘和网络采样频率。
     * 默认值: 3（每 3 次主循环采样更新一次磁盘和网络指标，约 6 秒）。
     */
    private int diskNetInterval = 3;

    /**
     * 趋势数据环形缓冲区容量。
     * 默认值: 360（360 个数据点，配合趋势降采样周期可覆盖约 1 小时）。
     */
    private int trendBufferSize = 360;

    /**
     * 趋势降采样率。
     * 默认值: 5（每 N 次主循环采样写入一次趋势缓冲区）。
     */
    private int trendDecimation = 5;

    /**
     * 是否启用硬件采集。
     * 默认值: true。设置为 false 可完全禁用 OSHI 采集线程。
     */
    private boolean enabled = true;
}
