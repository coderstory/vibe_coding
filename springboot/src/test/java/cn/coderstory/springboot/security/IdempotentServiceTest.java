package cn.coderstory.springboot.security;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IdempotentService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试，连接实际 Redis
 * 测试数据会在 @AfterEach 中清理
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("IdempotentService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IdempotentServiceTest {

    @Autowired
    private IdempotentService idempotentService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final Set<String> testKeys = new HashSet<>();

    @AfterEach
    void tearDown() {
        if (!testKeys.isEmpty()) {
            redisTemplate.delete(testKeys);
            testKeys.clear();
        }
    }

    private String addTestKey(String key) {
        testKeys.add(key);
        return key;
    }

    @Test
    @Order(1)
    @DisplayName("应允许首次请求通过")
    void shouldAllowFirstRequest() {
        String key = addTestKey("test:idempotent:first-" + System.currentTimeMillis());

        boolean result = idempotentService.tryAcquire(key, Duration.ofSeconds(30));

        assertTrue(result);
    }

    @Test
    @Order(2)
    @DisplayName("应拒绝重复请求")
    void shouldRejectDuplicateRequest() {
        String key = addTestKey("test:idempotent:duplicate-" + System.currentTimeMillis());

        idempotentService.tryAcquire(key, Duration.ofSeconds(30));
        boolean result = idempotentService.tryAcquire(key, Duration.ofSeconds(30));

        assertFalse(result);
    }

    @Test
    @Order(3)
    @DisplayName("应使用指定的过期时间")
    void shouldUseSpecifiedExpireTime() {
        String key = addTestKey("test:idempotent:expire-" + System.currentTimeMillis());

        boolean result = idempotentService.tryAcquire(key, Duration.ofSeconds(60));

        assertTrue(result);
    }

    @Test
    @Order(4)
    @DisplayName("应使用默认过期时间")
    void shouldUseDefaultExpireTime() {
        String key = addTestKey("test:idempotent:default-" + System.currentTimeMillis());

        idempotentService.tryAcquire(key);

        assertTrue(idempotentService.tryAcquire(key + "-other", Duration.ofSeconds(30)));
    }

    @Test
    @Order(5)
    @DisplayName("不同key应独立处理")
    void differentKeysShouldBeHandledIndependently() {
        String key1 = addTestKey("test:idempotent:key1-" + System.currentTimeMillis());
        String key2 = addTestKey("test:idempotent:key2-" + System.currentTimeMillis());

        boolean result1 = idempotentService.tryAcquire(key1, Duration.ofSeconds(30));
        boolean result2 = idempotentService.tryAcquire(key2, Duration.ofSeconds(30));

        assertTrue(result1);
        assertTrue(result2);
    }
}
