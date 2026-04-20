package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.service.impl.SignServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SignService 单元测试
 *
 * 使用 Mockito 进行纯单元测试，不需要启动 Spring 上下文
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@DisplayName("SignService 单元测试")
class SignServiceTest {

    private SignServiceImpl signService;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        signService = new SignServiceImpl(redisTemplate);
    }

    /**
     * 测试用例：生成签名
     */
    @Test
    @DisplayName("应生成有效签名")
    void shouldGenerateValidSignature() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");

        assertNotNull(result);
        assertNotNull(result.getSign());
        assertFalse(result.getSign().isEmpty());
        assertTrue(result.getTimestamp() > 0);
    }

    /**
     * 测试用例：不同参数生成不同签名
     */
    @Test
    @DisplayName("不同用户应生成不同签名")
    void differentUsersShouldGenerateDifferentSignatures() {
        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        SignService.SignResult result2 = signService.generateSign(2L, 1L, "test-secret-key");

        assertNotEquals(result1.getSign(), result2.getSign());
    }

    /**
     * 测试用例：不同商品生成不同签名
     */
    @Test
    @DisplayName("不同商品应生成不同签名")
    void differentGoodsShouldGenerateDifferentSignatures() {
        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        SignService.SignResult result2 = signService.generateSign(1L, 2L, "test-secret-key");

        assertNotEquals(result1.getSign(), result2.getSign());
    }

    /**
     * 测试用例：验证有效签名
     */
    @Test
    @DisplayName("应验证正确签名")
    void shouldVerifyCorrectSignature() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");
        when(valueOperations.get(anyString())).thenReturn(result.getSign());

        boolean verifyResult = signService.verifySign(result.getSign(), result.getTimestamp());

        assertTrue(verifyResult);
    }

    /**
     * 测试用例：拒绝过期签名
     */
    @Test
    @DisplayName("应拒绝过期签名")
    void shouldRejectExpiredSignature() {
        long expiredTimestamp = System.currentTimeMillis() - 600000;

        boolean verifyResult = signService.verifySign("some-signature", expiredTimestamp);

        assertFalse(verifyResult);
    }

    /**
     * 测试用例：拒绝未存在的签名
     */
    @Test
    @DisplayName("应拒绝未存在的签名")
    void shouldRejectNonExistentSignature() {
        when(valueOperations.get(anyString())).thenReturn(null);

        boolean verifyResult = signService.verifySign("non-existent-signature", System.currentTimeMillis());

        assertFalse(verifyResult);
    }

    /**
     * 测试用例：签名只能使用一次
     */
    @Test
    @DisplayName("签名只能验证一次")
    void signatureShouldBeUsableOnlyOnce() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");
        when(valueOperations.get(anyString())).thenReturn(result.getSign());

        boolean firstVerify = signService.verifySign(result.getSign(), result.getTimestamp());
        boolean secondVerify = signService.verifySign(result.getSign(), result.getTimestamp());

        assertTrue(firstVerify);
        assertFalse(secondVerify);
    }
}
