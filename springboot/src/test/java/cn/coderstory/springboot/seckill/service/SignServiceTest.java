package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.SpringbootApplication;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("SignService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SignServiceTest {

    private final Set<String> testKeys = new HashSet<>();
    @Autowired
    private SignService signService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        if (!testKeys.isEmpty()) {
            redisTemplate.delete(testKeys);
            testKeys.clear();
        }
    }

    private void addBitmapKey(Long activityId) {
        String key = "seckill:sign:bitmap:" + activityId;
        testKeys.add(key);
    }

    @Test
    @Order(1)
    @DisplayName("应生成有效签名")
    void shouldGenerateValidSignature() {
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");

        assertNotNull(result);
        assertNotNull(result.sign());
        assertFalse(result.sign().isEmpty());
        assertTrue(result.timestamp() > 0);
    }

    @Test
    @Order(2)
    @DisplayName("不同用户应生成不同签名")
    void differentUsersShouldGenerateDifferentSignatures() {
        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        SignService.SignResult result2 = signService.generateSign(2L, 1L, "test-secret-key");

        assertNotEquals(result1.sign(), result2.sign());
    }

    @Test
    @Order(3)
    @DisplayName("不同商品应生成不同签名")
    void differentGoodsShouldGenerateDifferentSignatures() {
        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        SignService.SignResult result2 = signService.generateSign(1L, 2L, "test-secret-key");

        assertNotEquals(result1.sign(), result2.sign());
    }

    @Test
    @Order(4)
    @DisplayName("应验证正确签名")
    void shouldVerifyCorrectSignature() {
        Long activityId = 1L;
        addBitmapKey(activityId);
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");

        boolean verifyResult = signService.verifySign(result.sign(), result.timestamp(), activityId, Duration.ofHours(2));

        assertTrue(verifyResult);
    }

    @Test
    @Order(5)
    @DisplayName("应拒绝过期签名")
    void shouldRejectExpiredSignature() {
        Long activityId = 1L;
        addBitmapKey(activityId);
        long expiredTimestamp = System.currentTimeMillis() - 600000;

        boolean verifyResult = signService.verifySign("some-signature", expiredTimestamp, activityId, Duration.ofHours(2));

        assertFalse(verifyResult);
    }

    @Test
    @Order(6)
    @DisplayName("应拒绝重复验证同一签名")
    void shouldRejectDuplicateVerifyOfSameSignature() {
        Long activityId = 1L;
        addBitmapKey(activityId);
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");

        boolean firstVerify = signService.verifySign(result.sign(), result.timestamp(), activityId, Duration.ofHours(2));
        assertTrue(firstVerify);

        boolean secondVerify = signService.verifySign(result.sign(), result.timestamp(), activityId, Duration.ofHours(2));
        assertFalse(secondVerify);
    }

    @Test
    @Order(7)
    @DisplayName("签名只能验证一次")
    void signatureShouldBeUsableOnlyOnce() {
        Long activityId = 1L;
        addBitmapKey(activityId);
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");

        boolean firstVerify = signService.verifySign(result.sign(), result.timestamp(), activityId, Duration.ofHours(2));
        boolean secondVerify = signService.verifySign(result.sign(), result.timestamp(), activityId, Duration.ofHours(2));

        assertTrue(firstVerify);
        assertFalse(secondVerify);
    }

    @Test
    @Order(8)
    @DisplayName("不同活动签名应相互隔离")
    void differentActivitiesShouldBeIsolated() {
        Long activityId1 = 1L;
        Long activityId2 = 2L;
        addBitmapKey(activityId1);
        addBitmapKey(activityId2);

        SignService.SignResult result1 = signService.generateSign(1L, 1L, "test-secret-key");
        boolean verifyInActivity1 = signService.verifySign(result1.sign(), result1.timestamp(), activityId1, Duration.ofHours(2));
        assertTrue(verifyInActivity1);

        boolean verifySameInActivity2 = signService.verifySign(result1.sign(), result1.timestamp(), activityId2, Duration.ofHours(2));
        assertTrue(verifySameInActivity2);
    }

    @Test
    @Order(9)
    @DisplayName("Bitmap 过期时间应正确设置")
    void bitmapExpireTimeShouldBeSet() {
        Long activityId = 999L;
        addBitmapKey(activityId);
        SignService.SignResult result = signService.generateSign(1L, 1L, "test-secret-key");

        signService.verifySign(result.sign(), result.timestamp(), activityId, Duration.ofHours(3));

        Long ttl = redisTemplate.getExpire("seckill:sign:bitmap:" + activityId);
        assertNotNull(ttl);
        assertTrue(ttl > 0);
        assertTrue(ttl <= 3 * 3600);
    }
}
