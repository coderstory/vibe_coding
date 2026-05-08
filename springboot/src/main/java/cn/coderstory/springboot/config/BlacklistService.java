package cn.coderstory.springboot.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

/**
 * IP 黑名单服务。
 * <p>
 * 使用 Redis BloomFilter 进行高效的存在性预检，配合 RSet 存储精确的黑名单 IP 列表。
 * BloomFilter 提供 O(1) 的误判率可控的预检能力，避免大量无效 IP 直接查询 Set 带来的性能开销。
 *
 * @since 1.7.0
 */
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

    /**
     * 检查指定 IP 是否在黑名单中。
     * <p>
     * 先通过 BloomFilter 快速预检，不存在则直接返回；存在时再查询精确的 Set 集合确认。
     *
     * @param ip 待检查的 IP 地址
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String ip) {
        if (!bloomFilter.contains(ip)) {
            return false;
        }
        return blacklistSet.contains(ip);
    }

    /**
     * 将指定 IP 加入黑名单。
     * <p>
     * 同时写入 BloomFilter 和 Set，确保预检和精确查询的一致性。
     *
     * @param ip 待加入黑名单的 IP 地址
     */
    public void addToBlacklist(String ip) {
        bloomFilter.add(ip);
        blacklistSet.add(ip);
        log.info("IP加入黑名单，IP: {}", ip);
    }

    /**
     * 将指定 IP 移出黑名单。
     * <p>
     * 仅从 Set 中移除（BloomFilter 不支持删除），允许少量误判残留。
     *
     * @param ip 待移出黑名单的 IP 地址
     */
    public void removeFromBlacklist(String ip) {
        blacklistSet.remove(ip);
        log.info("IP移出黑名单，IP: {}", ip);
    }
}
