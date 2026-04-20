package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IpRateLimiter {
    private final StringRedisTemplate redisTemplate;
    private static final int IP_QPS = 10;

    public boolean isIpAllowed(String ip) {
        String blacklistKey = "seckill:blacklist:ip:" + ip;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            return false;
        }
        String qpsKey = "seckill:ip:qps:" + ip;
        Long count = redisTemplate.opsForValue().increment(qpsKey);
        if (count == 1) {
            redisTemplate.expire(qpsKey, 1, TimeUnit.SECONDS);
        }
        return count == null || count <= IP_QPS;
    }
}