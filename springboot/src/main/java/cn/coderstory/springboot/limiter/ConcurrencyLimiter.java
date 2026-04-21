package cn.coderstory.springboot.limiter;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConcurrencyLimiter {

    private static final int MAX_CONCURRENT = 2000;
    private static final String SEMAPHORE_KEY = "seckill:concurrency:semaphore";

    private final RSemaphore semaphore;

    public ConcurrencyLimiter(RedissonClient redissonClient) {
        this.semaphore = redissonClient.getSemaphore(SEMAPHORE_KEY);
        this.semaphore.trySetPermits(MAX_CONCURRENT);
    }

    public boolean tryAcquire() {
        boolean acquired = semaphore.tryAcquire();
        if (!acquired) {
            log.warn("并发限流触发，限制: {}", MAX_CONCURRENT);
        } else {
            log.debug("并发计数增加，限制: {}", MAX_CONCURRENT);
        }
        return acquired;
    }

    public void release() {
        semaphore.release();
        log.debug("并发计数释放");
    }
}
