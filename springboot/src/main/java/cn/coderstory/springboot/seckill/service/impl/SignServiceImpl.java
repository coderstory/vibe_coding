package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {
    private final StringRedisTemplate redisTemplate;
    private static final long SIGN_EXPIRE_MS = 5 * 60 * 1000;
    private static final String BITMAP_KEY_PREFIX = "seckill:sign:bitmap:";

    @Override
    public SignResult generateSign(Long userId, Long goodsId, String activitySignKey) {
        long timestamp = System.currentTimeMillis();
        String content = userId + ":" + goodsId + ":" + timestamp;
        String sign = hmacSHA256(content, activitySignKey);
        return new SignResult(sign, timestamp);
    }

    @Override
    public boolean verifySign(String sign, long timestamp, Long activityId, Duration duration) {
        if (System.currentTimeMillis() - timestamp > SIGN_EXPIRE_MS) {
            return false;
        }

        String bitmapKey = BITMAP_KEY_PREFIX + activityId;
        long bitOffset = hashToBitOffset(sign);

        Boolean existed = redisTemplate.opsForValue().getBit(bitmapKey, bitOffset);
        if (Boolean.TRUE.equals(existed)) {
            return false;
        }

        redisTemplate.opsForValue().setBit(bitmapKey, bitOffset, true);
        redisTemplate.expire(bitmapKey, duration);
        return true;
    }

    private long hashToBitOffset(String sign) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sign.getBytes(StandardCharsets.UTF_8));
            long hash = 0;
            for (int i = 0; i < 4; i++) {
                hash = (hash << 8) | (digest[i] & 0xFF);
            }
            return Math.abs(hash);
        } catch (Exception e) {
            throw new RuntimeException("MD5计算失败", e);
        }
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC计算失败", e);
        }
    }
}
