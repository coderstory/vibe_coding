package cn.coderstory.springboot.service.monitor.hardware;

import cn.coderstory.springboot.dto.monitor.hardware.HardwareMetricsDTO;
import cn.coderstory.springboot.dto.monitor.hardware.SystemInfoDTO;

import java.util.List;

/**
 * 硬件指标采集服务接口。
 * <p>
 * 定义硬件指标采集的核心契约，支持获取当前快照、趋势历史数据和系统基本信息。
 * 采用接口模式设计（TDD-01），便于使用 Mockito 进行单元测试，
 * OSHI 依赖通过构造函数注入，可在测试中 mock。
 */
public interface HardwareMetricsService {

    /**
     * 获取当前硬件指标快照。
     * <p>
     * 从采集缓存中读取最新一次采样结果，而不是直接调用 OSHI。
     * 首次成功采集前返回 empty DTO。
     *
     * @return 当前硬件指标快照
     */
    HardwareMetricsDTO getCurrentMetrics();

    /**
     * 获取全部趋势历史数据。
     * <p>
     * 从环形缓冲区中读取所有已存储的采样点，按时间顺序返回。
     *
     * @return 趋势历史数据列表
     */
    List<HardwareMetricsDTO> getTrendHistory();

    /**
     * 获取系统基本信息。
     * <p>
     * 包含操作系统版本、系统运行时间和进程数等信息。
     *
     * @return 系统基本信息 DTO
     */
    SystemInfoDTO getSystemInfo();
}
