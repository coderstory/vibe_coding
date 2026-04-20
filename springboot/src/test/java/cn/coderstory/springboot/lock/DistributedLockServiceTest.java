package cn.coderstory.springboot.lock;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.*;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DistributedLockService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试，连接实际 Redis
 * 测试数据会在 @AfterEach 中清理
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("DistributedLockService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DistributedLockServiceTest {

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Set<String> testLockKeys = new HashSet<>();

    @AfterEach
    void tearDown() {
        testLockKeys.forEach(key -> {
            try {
                RLock lock = distributedLockService.getLock(key);
                if (lock != null && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception ignored) {
            }
        });
        testLockKeys.clear();
    }

    private String addTestLockKey(String prefix) {
        String key = prefix + System.currentTimeMillis();
        testLockKeys.add(key);
        return key;
    }

    @Test
    @Order(1)
    @DisplayName("应能获取锁")
    void shouldAcquireLock() {
        String lockKey = addTestLockKey("test:lock:");

        RLock lock = distributedLockService.getLock(lockKey);

        assertNotNull(lock);
    }

    @Test
    @Order(2)
    @DisplayName("应能尝试获取锁")
    void shouldTryAcquireLock() throws InterruptedException {
        String lockKey = addTestLockKey("test:tryLock:");

        boolean acquired = distributedLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);

        assertTrue(acquired);

        distributedLockService.unlock(lockKey);
    }

    @Test
    @Order(3)
    @DisplayName("应能释放锁")
    void shouldUnlock() throws InterruptedException {
        String lockKey = addTestLockKey("test:unlock:");

        boolean acquired = distributedLockService.tryLock(lockKey, 5, TimeUnit.SECONDS);
        assertTrue(acquired);

        distributedLockService.unlock(lockKey);

        boolean isLocked = distributedLockService.isLocked(lockKey);
        assertFalse(isLocked);
    }

    @Test
    @Order(4)
    @DisplayName("应能执行带锁业务")
    void shouldExecuteWithLock() {
        String lockKey = addTestLockKey("test:execute:");
        AtomicInteger counter = new AtomicInteger(0);

        String result = distributedLockService.executeWithLock(lockKey, () -> {
            counter.incrementAndGet();
            return "success";
        });

        assertEquals("success", result);
        assertEquals(1, counter.get());
    }

    @Test
    @Order(5)
    @DisplayName("锁被占用时带锁执行应返回null")
    void shouldReturnNullWhenLockNotAvailable() throws InterruptedException {
        String lockKey = addTestLockKey("test:blocked:");

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

    @Test
    @Order(6)
    @DisplayName("锁键前缀应正确生成")
    void lockKeyPrefixShouldBeCorrect() {
        assertEquals("seckill:activity:123", "seckill:activity:" + 123L);
        assertEquals("seckill:stock:456", "seckill:stock:" + 456L);
        assertEquals("seckill:order:1:100", "seckill:order:" + 1L + ":" + 100L);
    }
}
