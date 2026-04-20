package cn.coderstory.springboot.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotentService 单元测试")
class IdempotentServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private IdempotentService idempotentService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        idempotentService = new IdempotentService(redisTemplate);
    }

    @Test
    @DisplayName("应允许首次请求通过")
    void shouldAllowFirstRequest() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(true);

        boolean result = idempotentService.tryAcquire("unique-key-123", Duration.ofSeconds(30));

        assertTrue(result);
    }

    @Test
    @DisplayName("应拒绝重复请求")
    void shouldRejectDuplicateRequest() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(false);

        boolean result = idempotentService.tryAcquire("unique-key-123", Duration.ofSeconds(30));

        assertFalse(result);
    }

    @Test
    @DisplayName("应使用指定的过期时间")
    void shouldUseSpecifiedExpireTime() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), eq(Duration.ofSeconds(60))))
            .thenReturn(true);

        idempotentService.tryAcquire("key", Duration.ofSeconds(60));

        verify(valueOperations).setIfAbsent(anyString(), eq("1"), eq(Duration.ofSeconds(60)));
    }

    @Test
    @DisplayName("应使用默认过期时间")
    void shouldUseDefaultExpireTime() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
            .thenReturn(true);

        idempotentService.tryAcquire("key");

        verify(valueOperations).setIfAbsent(anyString(), eq("1"), eq(Duration.ofSeconds(30)));
    }

    @Test
    @DisplayName("不同key应独立处理")
    void differentKeysShouldBeHandledIndependently() {
        when(valueOperations.setIfAbsent(eq("seckill:idempotent:key1"), anyString(), any(Duration.class)))
            .thenReturn(true);
        when(valueOperations.setIfAbsent(eq("seckill:idempotent:key2"), anyString(), any(Duration.class)))
            .thenReturn(true);

        boolean result1 = idempotentService.tryAcquire("key1");
        boolean result2 = idempotentService.tryAcquire("key2");

        assertTrue(result1);
        assertTrue(result2);
    }
}