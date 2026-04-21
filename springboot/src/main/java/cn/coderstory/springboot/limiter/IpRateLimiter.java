package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IpRateLimiter {
    private static final int IP_QPS = 10;

    private final RedissonClient redissonClient;

    public IpRateLimiter(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean isIpAllowed(String ip) {
        String blacklistKey = "seckill:blacklist:ip:" + ip;
        if (redissonClient.getBucket(blacklistKey).isExists()) {
            return false;
        }

        RRateLimiter rateLimiter = redissonClient.getRateLimiter("seckill:ip:rate:" + ip);
        rateLimiter.trySetRate(RateType.OVERALL, IP_QPS, 1, RateIntervalUnit.SECONDS);

        boolean allowed = rateLimiter.tryAcquire();
        if (!allowed) {
            log.warn("IP限流触发，IP: {}", ip);
        }
        return allowed;
    }
}
