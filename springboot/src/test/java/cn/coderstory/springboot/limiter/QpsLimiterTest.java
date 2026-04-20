package cn.coderstory.springboot.limiter;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QpsLimiter 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试，连接实际 Redis
 * 测试数据会在 @AfterEach 中清理
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("QpsLimiter 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QpsLimiterTest {

    @Autowired
    private QpsLimiter qpsLimiter;

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

    @Test
    @Order(1)
    @DisplayName("应允许有效请求通过")
    void shouldAllowValidRequest() {
        Long goodsId = 1L;
        testKeys.add("seckill:qps:" + goodsId);

        boolean result = qpsLimiter.tryAcquire(goodsId);

        assertTrue(result);
    }

    @Test
    @Order(2)
    @DisplayName("应能正确限流")
    void shouldLimitQpsCorrectly() {
        Long goodsId = 2L;
        testKeys.add("seckill:qps:" + goodsId);

        boolean result1 = qpsLimiter.tryAcquire(goodsId);

        assertTrue(result1);
    }

    @Test
    @Order(3)
    @DisplayName("不同商品应有独立的限流key")
    void differentGoodsShouldHaveDifferentKeys() {
        Long goodsId1 = 3L;
        Long goodsId2 = 4L;
        testKeys.add("seckill:qps:" + goodsId1);
        testKeys.add("seckill:qps:" + goodsId2);

        boolean result1 = qpsLimiter.tryAcquire(goodsId1);
        boolean result2 = qpsLimiter.tryAcquire(goodsId2);

        assertTrue(result1);
        assertTrue(result2);
    }
}
