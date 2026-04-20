package cn.coderstory.springboot.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 单机模式配置类
 *
 * 功能说明：
 * - 基于 Spring Boot 的 Redisson 自动配置
 * - 提供 RedissonClient Bean 供全局使用
 * - 支持分布式锁、分布式缓存等场景
 *
 * 使用示例：
 * <pre>
 *     @Autowired
 *     private RedissonClient redissonClient;
 *
 *     // 获取分布式锁
 *     RLock lock = redissonClient.getLock("myLock");
 *     lock.lock();
 *     try {
 *         // 业务逻辑
 *     } finally {
 *         lock.unlock();
 *     }
 * </pre>
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "redisson")
public class RedissonConfig {

    /**
     * 单机模式配置
     */
    private SingleServerConfig singleServerConfig;

    @Data
    public static class SingleServerConfig {
        /**
         * Redis 连接地址
         * 格式: redis://host:port
         */
        private String address;

        /**
         * 最小空闲连接数
         */
        private int connectionMinimumIdleSize = 5;

        /**
         * 连接池大小
         */
        private int connectionPoolSize = 20;

        /**
         * 空闲连接超时时间(ms)
         */
        private int idleConnectionTimeout = 10000;

        /**
         * 连接超时时间(ms)
         */
        private int connectTimeout = 10000;

        /**
         * 命令执行超时时间(ms)
         */
        private int timeout = 3000;
    }

    /**
     * 创建 RedissonClient 单机模式客户端
     *
     * 配置说明：
     * - 使用单节点模式，适用于开发测试和单机部署场景
     * - 生产环境建议使用集群模式或哨兵模式
     *
     * @return RedissonClient 实例
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 设置单机模式连接
        String address = singleServerConfig != null && singleServerConfig.getAddress() != null
            ? singleServerConfig.getAddress()
            : "redis://localhost:6379";

        config.useSingleServer()
            .setAddress(address)
            .setConnectionMinimumIdleSize(
                singleServerConfig != null ? singleServerConfig.getConnectionMinimumIdleSize() : 5
            )
            .setConnectionPoolSize(
                singleServerConfig != null ? singleServerConfig.getConnectionPoolSize() : 20
            )
            .setIdleConnectionTimeout(
                singleServerConfig != null ? singleServerConfig.getIdleConnectionTimeout() : 10000
            )
            .setConnectTimeout(
                singleServerConfig != null ? singleServerConfig.getConnectTimeout() : 10000
            )
            .setTimeout(
                singleServerConfig != null ? singleServerConfig.getTimeout() : 3000
            )
            .setRetryAttempts(3)
            .setRetryInterval(1500);

        return Redisson.create(config);
    }
}
