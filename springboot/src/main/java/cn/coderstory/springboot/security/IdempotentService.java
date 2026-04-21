package cn.coderstory.springboot.security;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class IdempotentService {

    private static final String SEMAPHORE_KEY = "seckill:idempotent:semaphore";
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofSeconds(30);

    private final RedissonClient redissonClient;

    public IdempotentService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

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

    public boolean tryAcquire(String idempotentKey) {
        return tryAcquire(idempotentKey, DEFAULT_EXPIRE_TIME);
    }
}
