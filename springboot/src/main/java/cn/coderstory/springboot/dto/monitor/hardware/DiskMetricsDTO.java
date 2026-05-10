package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.Data;

/**
 * 磁盘指标 DTO。
 * <p>
 * 包含磁盘名称、型号、类型、容量信息以及 IOPS 和读写速度指标。
 * 读写速度为差分计算值（字节/秒），IOPS 为每秒操作数。
 */
@Data
public class DiskMetricsDTO {

    /** 磁盘名称（如 /dev/sda） */
    private String name;

    /** 磁盘型号 */
    private String model;

    /** 磁盘总容量 */
    private MetricValue<Long> size;

    /** 磁盘类型（如 HDD、SSD） */
    private String diskType;

    /** 挂载点 */
    private String mount;

    /** 分区总空间 */
    private MetricValue<Long> totalSpace;

    /** 分区已用空间 */
    private MetricValue<Long> usedSpace;

    /** 分区可用空间 */
    private MetricValue<Long> usableSpace;

    /** 读取速率（字节/秒） */
    private MetricValue<Long> readBytesPerSec;

    /** 写入速率（字节/秒） */
    private MetricValue<Long> writeBytesPerSec;

    /** 读取 IOPS */
    private MetricValue<Long> readsPerSec;

    /** 写入 IOPS */
    private MetricValue<Long> writesPerSec;
}
