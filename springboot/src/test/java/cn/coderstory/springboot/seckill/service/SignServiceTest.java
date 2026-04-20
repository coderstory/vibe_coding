package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SignService 集成测试
 *
 * 使用 @SpringBootTest 进行集成测试，连接实际 Redis
 * 测试数据会在 @AfterEach 中清理
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("SignService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignServiceTest {

    @Autowired
    private SignService signService;

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

    private String generateTestKey(String prefix) {
        String key = prefix + System.currentTimeMillis();
        testKeys.add(key);
        return key;
    }

    @Test
    @Order(1)
    @DisplayName("应生成有效签名")
    void shouldGenerateValidSignature() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");
        testKeys.add(result.getSign());

        assertNotNull(result);
        assertNotNull(result.getSign());
        assertFalse(result.getSign().isEmpty());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    @Order(2)
    @DisplayName("不同用户应生成不同签名")
    void differentUsersShouldGenerateDifferentSignatures() {
        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        SignService.SignResult result2 = signService.generateSign(2L, 1L, "test-secret-key");
        testKeys.add(result1.getSign());
        testKeys.add(result2.getSign());

        assertNotEquals(result1.getSign(), result2.getSign());
    }

    @Test
    @Order(3)
    @DisplayName("不同商品应生成不同签名")
    void differentGoodsShouldGenerateDifferentSignatures() {
        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        SignService.SignResult result2 = signService.generateSign(1L, 2L, "test-secret-key");
        testKeys.add(result1.getSign());
        testKeys.add(result2.getSign());

        assertNotEquals(result1.getSign(), result2.getSign());
    }

    @Test
    @Order(4)
    @DisplayName("应验证正确签名")
    void shouldVerifyCorrectSignature() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");
        testKeys.add(result.getSign());

        boolean verifyResult = signService.verifySign(result.getSign(), result.getTimestamp());

        assertTrue(verifyResult);
    }

    @Test
    @Order(5)
    @DisplayName("应拒绝过期签名")
    void shouldRejectExpiredSignature() {
        long expiredTimestamp = System.currentTimeMillis() - 600000;

        boolean verifyResult = signService.verifySign("some-signature", expiredTimestamp);

        assertFalse(verifyResult);
    }

    @Test
    @Order(6)
    @DisplayName("应拒绝未存在的签名")
    void shouldRejectNonExistentSignature() {
        boolean verifyResult = signService.verifySign("non-existent-signature", System.currentTimeMillis());

        assertFalse(verifyResult);
    }

    @Test
    @Order(7)
    @DisplayName("签名只能验证一次")
    void signatureShouldBeUsableOnlyOnce() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");
        testKeys.add(result.getSign());

        boolean firstVerify = signService.verifySign(result.getSign(), result.getTimestamp());
        boolean secondVerify = signService.verifySign(result.getSign(), result.getTimestamp());

        assertTrue(firstVerify);
        assertFalse(secondVerify);
    }
}
