package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {
    private final StringRedisTemplate redisTemplate;
    private static final long SIGN_EXPIRE_MS = 5 * 60 * 1000;

    @Override
    public SignResult generateSign(Long userId, Long goodsId, String activitySignKey) {
        long timestamp = System.currentTimeMillis();
        String content = userId + ":" + goodsId + ":" + timestamp;
        String sign = hmacSHA256(content, activitySignKey);
        String signKey = "seckill:sign:" + sign;
        redisTemplate.opsForValue().set(signKey, "1", 5, TimeUnit.MINUTES);
        return new SignResult(sign, timestamp);
    }

    @Override
    public boolean verifySign(String sign, long timestamp) {
        if (System.currentTimeMillis() - timestamp > SIGN_EXPIRE_MS) {
            return false;
        }
        String signKey = "seckill:sign:" + sign;
        Boolean used = redisTemplate.hasKey(signKey);
        if (Boolean.TRUE.equals(used)) {
            return false;
        }
        redisTemplate.opsForValue().set(signKey, "used", 1, TimeUnit.MINUTES);
        return true;
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