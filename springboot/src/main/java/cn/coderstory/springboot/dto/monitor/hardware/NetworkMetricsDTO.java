package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.Data;

/**
 * 网络接口指标 DTO。
 * <p>
 * 包含网络接口名称、MAC 地址、连接速率和上下行吞吐量指标。
 * 吞吐量为差分计算值（字节/秒），包速率为每秒包数。
 */
@Data
public class NetworkMetricsDTO {

    /** 网络接口显示名称 */
    private String displayName;

    /** MAC 地址 */
    private String macAddress;

    /** 连接速率（bps） */
    private long speed;

    /** 是否为已知虚拟机和容器环境 */
    private boolean isKnownVm;

    /** 发送速率（字节/秒） */
    private MetricValue<Long> bytesSentPerSec;

    /** 接收速率（字节/秒） */
    private MetricValue<Long> bytesRecvPerSec;

    /** 发送包速率（包/秒） */
    private MetricValue<Long> packetsSentPerSec;

    /** 接收包速率（包/秒） */
    private MetricValue<Long> packetsRecvPerSec;
}
