package cn.coderstory.springboot.limiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QpsLimiter 单元测试")
class QpsLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private QpsLimiter qpsLimiter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        qpsLimiter = new QpsLimiter(redisTemplate);
    }

    @Test
    @DisplayName("应允许有效请求通过")
    void shouldAllowValidRequest() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
            .thenReturn(1L);

        boolean result = qpsLimiter.tryAcquire(1L);

        assertTrue(result);
    }

    @Test
    @DisplayName("应拒绝QPS超限请求")
    void shouldRejectWhenQpsExceeded() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
            .thenReturn(0L);

        boolean result = qpsLimiter.tryAcquire(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("应处理Redis返回null的情况")
    void shouldHandleNullResponse() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any(), any()))
            .thenReturn(null);

        boolean result = qpsLimiter.tryAcquire(1L);

        assertFalse(result);
    }

    @Test
    @DisplayName("不同商品应有独立的限流key")
    void differentGoodsShouldHaveDifferentKeys() {
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList("seckill:qps:1")), any(), any(), any()))
            .thenReturn(1L);
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList("seckill:qps:2")), any(), any(), any()))
            .thenReturn(0L);

        boolean result1 = qpsLimiter.tryAcquire(1L);
        boolean result2 = qpsLimiter.tryAcquire(2L);

        assertTrue(result1);
        assertFalse(result2);
    }
}