package cn.coderstory.springboot.security;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BlacklistService {

    private static final String BLOOM_FILTER_KEY = "seckill:blacklist:bloom";
    private static final String SET_KEY = "seckill:blacklist:set";

    private final RBloomFilter<String> bloomFilter;
    private final RSet<String> blacklistSet;

    public BlacklistService(RedissonClient redissonClient) {
        this.bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY);
        this.bloomFilter.tryInit(1000000, 0.01);
        this.blacklistSet = redissonClient.getSet(SET_KEY);
    }

    public boolean isBlacklisted(String ip) {
        if (!bloomFilter.contains(ip)) {
            return false;
        }
        return blacklistSet.contains(ip);
    }

    public void addToBlacklist(String ip) {
        bloomFilter.add(ip);
        blacklistSet.add(ip);
        log.info("IP加入黑名单，IP: {}", ip);
    }

    public void removeFromBlacklist(String ip) {
        blacklistSet.remove(ip);
        log.info("IP移出黑名单，IP: {}", ip);
    }
}
