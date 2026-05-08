package cn.coderstory.springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性校验服务。
 * <p>
 * 使用 Redisson 的 PermitExpirableSemaphore 实现基于 key 的互斥访问。
 * 相同的 idempotentKey 在同一时间段内只允许首次请求通过，后续请求被拦截，
 * 用于防止秒杀场景下的重复下单和重复支付。
 *
 * @since 1.7.0
 */
@Slf4j
@Service
public class IdempotentService {

    private static final String SEMAPHORE_KEY = "seckill:idempotent:semaphore";
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofSeconds(30);

    private final RedissonClient redissonClient;

    public IdempotentService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取幂等性许可（带自定义过期时间）。
     * <p>
     * 基于指定的 idempotentKey 创建可过期信号量，成功获取许可表示请求通过幂等校验。
     * 获取失败表示相同 key 的请求已在处理中，属于重复请求。
     *
     * @param idempotentKey 幂等性 key，标识唯一请求
     * @param expireTime    许可的过期时间，超过该时间后许可自动释放
     * @return 是否成功获取许可（true 表示通过幂等校验）
     */
    public boolean tryAcquire(String idempotentKey, Duration expireTime) {
        RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(SEMAPHORE_KEY + ":" + idempotentKey);
        semaphore.trySetPermits(1);
        try {
            String permitId = semaphore.tryAcquire(0, expireTime.toMillis(), TimeUnit.MILLISECONDS);
            boolean acquired = permitId != null;
            if (!acquired) {
                log.warn("幂等性拦截，key: {}", idempotentKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("幂等性检查被中断，key: {}", idempotentKey);
            return false;
        }
    }

    /**
     * 尝试获取幂等性许可（使用默认过期时间）。
     *
     * @param idempotentKey 幂等性 key，标识唯一请求
     * @return 是否成功获取许可（true 表示通过幂等校验）
     */
    public boolean tryAcquire(String idempotentKey) {
        return tryAcquire(idempotentKey, DEFAULT_EXPIRE_TIME);
    }
}
