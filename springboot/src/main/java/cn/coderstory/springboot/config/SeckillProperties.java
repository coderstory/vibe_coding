package cn.coderstory.springboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 秒杀业务配置属性类
 *
 * 功能说明：
 * - 集中管理秒杀系统的业务配置参数
 * - 从 application.yaml 中读取配置
 * - 提供类型安全的配置访问
 *
 * 配置项说明：
 * - limiter: 限流器相关配置（QPS、并发数、IP限流）
 * - lock: 分布式锁相关配置（等待时间、持有时间）
 * - order: 订单相关配置（超时时间）
 * - sse: SSE实时通知相关配置（超时时间）
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@Component
@ConfigurationProperties(prefix = "seckill")
public class SeckillProperties {

    /**
     * 限流器配置
     */
    private LimiterConfig limiter = new LimiterConfig();

    /**
     * 分布式锁配置
     */
    private LockConfig lock = new LockConfig();

    /**
     * 订单配置
     */
    private OrderConfig order = new OrderConfig();

    /**
     * SSE配置
     */
    private SseConfig sse = new SseConfig();

    /**
     * 限流器配置类
     *
     * 用于控制秒杀请求的流量，防止系统过载
     */
    @Data
    public static class LimiterConfig {
        /**
         * 每秒允许请求数（QPS限制）
         * 默认值: 1000
         * 说明: 全局商品维度的QPS上限
         */
        private int qps = 1000;

        /**
         * 最大并发处理数
         * 默认值: 500
         * 说明: 同时处理的最大请求数
         */
        private int concurrency = 500;

        /**
         * 每IP每秒最大请求数
         * 默认值: 100
         * 说明: 单个IP地址的请求频率限制
         */
        private int ipRate = 100;
    }

    /**
     * 分布式锁配置类
     *
     * 用于控制分布式环境下的并发访问
     */
    @Data
    public static class LockConfig {
        /**
         * 等待获取锁时间（秒）
         * 默认值: 5
         * 说明: 尝试获取锁的最大等待时间
         */
        private long waitTime = 5;

        /**
         * 锁持有时间（秒）
         * 默认值: 10
         * 说明: 锁自动释放时间，防止死锁
         */
        private long leaseTime = 10;
    }

    /**
     * 订单配置类
     *
     * 用于控制订单的生命周期
     */
    @Data
    public static class OrderConfig {
        /**
         * 订单超时时间（秒）
         * 默认值: 900 (15分钟)
         * 说明: 订单创建后未支付的超时时间
         */
        private int timeout = 900;
    }

    /**
     * SSE实时通知配置类
     *
     * 用于控制服务器推送事件的生命周期
     */
    @Data
    public static class SseConfig {
        /**
         * SSE连接超时时间（毫秒）
         * 默认值: 300000 (5分钟)
         * 说明: SSE连接的最大保持时间
         */
        private long timeout = 300000;
    }
}
