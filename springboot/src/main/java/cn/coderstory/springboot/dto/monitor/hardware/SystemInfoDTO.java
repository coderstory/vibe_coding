package cn.coderstory.springboot.dto.monitor.hardware;

import lombok.Data;

/**
 * 系统基本信息 DTO。
 * <p>
 * 包含操作系统名称、版本、系统运行时间和进程数等信息。
 * 运行时间同时提供秒数和格式化字符串两种形式。
 */
@Data
public class SystemInfoDTO {

    /** 操作系统系列（如 Windows、Linux） */
    private String osFamily;

    /** 操作系统版本号 */
    private String osVersion;

    /** 操作系统版本详细信息 */
    private String osVersionInfo;

    /** 系统运行时间（秒） */
    private long systemUptime;

    /** 系统运行时间格式化字符串 */
    private String systemUptimeFormatted;

    /** 当前进程数 */
    private int processCount;
}
