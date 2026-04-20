package cn.coderstory.springboot.lock;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DistributedLockService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("DistributedLockService 集成测试")
class DistributedLockServiceTest {

    @Autowired
    private DistributedLockService distributedLockService;

    /**
     * 测试用例：获取锁
     */
    @Test
    @DisplayName("应能获取锁")
    void shouldAcquireLock() {
        String lockKey = "test:lock:" + System.currentTimeMillis();

        RLock lock = distributedLockService.getLock(lockKey);

        assertNotNull(lock);
    }

    /**
     * 测试用例：尝试获取锁
     */
    @Test
    @DisplayName("应能尝试获取锁")
    void shouldTryAcquireLock() throws InterruptedException {
        String lockKey = "test:tryLock:" + System.currentTimeMillis();

        boolean acquired = distributedLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);

        assertTrue(acquired);

        distributedLockService.unlock(lockKey);
    }

    /**
     * 测试用例：释放锁
     */
    @Test
    @DisplayName("应能释放锁")
    void shouldUnlock() throws InterruptedException {
        String lockKey = "test:unlock:" + System.currentTimeMillis();

        boolean acquired = distributedLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(acquired);

        distributedLockService.unlock(lockKey);

        boolean isLocked = distributedLockService.isLocked(lockKey);
        assertFalse(isLocked);
    }

    /**
     * 测试用例：执行带锁业务
     */
    @Test
    @DisplayName("应能执行带锁业务")
    void shouldExecuteWithLock() {
        String lockKey = "test:execute:" + System.currentTimeMillis();
        AtomicInteger counter = new AtomicInteger(0);

        String result = distributedLockService.executeWithLock(lockKey, () -> {
            counter.incrementAndGet();
            return "success";
        });

        assertEquals("success", result);
        assertEquals(1, counter.get());
    }

    /**
     * 测试用例：带锁执行时锁被占用
     */
    @Test
    @DisplayName("锁被占用时带锁执行应返回null")
    void shouldReturnNullWhenLockNotAvailable() throws InterruptedException {
        String lockKey = "test:blocked:" + System.currentTimeMillis();

        boolean firstLock = distributedLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(firstLock);

        CountDownLatch latch = new CountDownLatch(1);
        String[] result = new String[1];

        Thread thread = new Thread(() -> {
            result[0] = distributedLockService.executeWithLock(lockKey, 1, TimeUnit.SECONDS, () -> "should not execute");
            latch.countDown();
        });
        thread.start();

        boolean waited = latch.await(3, TimeUnit.SECONDS);
        assertTrue(waited);
        assertNull(result[0]);

        distributedLockService.unlock(lockKey);
    }

    /**
     * 测试用例：锁键前缀生成
     */
    @Test
    @DisplayName("锁键前缀应正确生成")
    void lockKeyPrefixShouldBeCorrect() {
        assertEquals("seckill:activity:123",
                getActivityLockKey(123L));
        assertEquals("seckill:stock:456",
                getStockLockKey(456L));
        assertEquals("seckill:order:1:100",
                getOrderLockKey(1L, 100L));
    }

    private String getActivityLockKey(Long activityId) {
        return "seckill:activity:" + activityId;
    }

    private String getStockLockKey(Long goodsId) {
        return "seckill:stock:" + goodsId;
    }

    private String getOrderLockKey(Long userId, Long goodsId) {
        return "seckill:order:" + userId + ":" + goodsId;
    }
}
