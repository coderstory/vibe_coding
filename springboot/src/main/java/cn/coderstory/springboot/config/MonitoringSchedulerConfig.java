package cn.coderstory.springboot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oshi.ffm.SystemInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 硬件监控调度器配置类。
 * <p>
 * 创建独立于 {@code @Scheduled} 注解的调度线程池，用于 OSHI 定时采集硬件指标。
 * 使用 {@link ScheduledExecutorService} 替代 {@code @Scheduled} 以获得更好的线程隔离、
 * 自定义线程名称和异常处理能力。采集线程命名为 "monitor-hardware-collector"，
 * 设置为 daemon 线程以避免阻塞 JVM 关闭。
 * <p>
 * 同时创建 OSHI FFM 入口 {@link SystemInfo} 的单例 Bean，供采集服务通过依赖注入使用。
 *
 * @since 1.8.0
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class MonitoringSchedulerConfig {

    /**
     * 创建硬件采集的单线程调度器。
     * <p>
     * 线程名 "monitor-hardware-collector" 便于日志和监控识别。
     * destroyMethod = "shutdown" 确保应用关闭时优雅释放线程资源。
     *
     * @return 单线程调度执行器服务
     */
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService monitoringScheduler() {
        return Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "monitor-hardware-collector");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 创建 OSHI FFM 入口 SystemInfo 实例。
     * <p>
     * 该实例封装了 FFM（Foreign Function & Memory）API 调用，用于采集硬件指标。
     * oshi.ffm.SystemInfo 不实现 AutoCloseable，使用空 destroyMethod 避免
     * Spring Boot 7.x 验证异常。
     *
     * @return OSHI SystemInfo 实例
     */
    @Bean(destroyMethod = "")
    public SystemInfo systemInfo() {
        return new SystemInfo();
    }
}
