package cn.coderstory.springboot.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

/**
 * 黑名单服务 - 风控基础组件
 *
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                           黑名单工作原理                                     │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │                                                                             │
 * │   请求进入 ──► 检查IP是否在黑名单 ──► 是 ──► 拒绝访问                          │
 * │                    │                                                         │
 * │                    ▼                                                         │
 * │                    否                                                        │
 * │                    │                                                         │
 * │                    ▼                                                         │
 * │                继续处理                                                      │
 * │                                                                            │
 └────────────────────────────────────────────────────────────────────────────┘
 *
 * 功能说明：
 * - 黑名单是风控系统的基础组件，用于封禁恶意用户/IP
 * - 黑名单数据存储在Redis中，支持设置过期时间（临时封禁）
 * - 可用于：IP封禁、用户封禁、设备封禁等场景
 *
 * 使用场景：
 * 1. 用户多次违规操作后封禁
 * 2. IP被识别为攻击源后封禁
 * 3. 机器人行为被识别后封禁
 * 4. 秒杀作弊行为封禁
 *
 * 实现细节：
 * - 使用Redis String存储，key格式: seckill:blacklist:ip:{ip}
 * - value固定为"1"
 * - 通过TTL实现自动解封
 *
 * 使用示例：
 * ```java
 * // 检查是否在黑名单
 * if (blacklistService.isBlacklisted(ip)) {
 *     throw new BusinessException("访问受限");
 * }
 *
 * // 封禁IP 1小时
 * blacklistService.addToBlacklist(ip, Duration.ofHours(1));
 *
 * // 解封IP
 * blacklistService.removeFromBlacklist(ip);
 * ```
 *
 * @author system
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistService {

    /** Redis key前缀 */
    private static final String KEY_PREFIX = "seckill:blacklist:ip:";

    /** Redis模板 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 检查IP是否在黑名单中
     *
     * @param ip 待检查的IP地址
     * @return true - 在黑名单中，应拒绝访问; false - 正常访问
     *
     * 实现原理：
     * 使用Redis EXISTS命令检查key是否存在
     * 时间复杂度: O(1)
     */
    public boolean isBlacklisted(String ip) {
        String key = KEY_PREFIX + ip;
        boolean blacklisted = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        if (blacklisted) {
            log.warn("黑名单拦截，IP: {}", ip);
        }
        return blacklisted;
    }

    /**
     * 将IP加入黑名单
     *
     * @param ip 待封禁的IP地址
     * @param duration 封禁时长
     *
     * 实现原理：
     * 1. 构建Redis key: seckill:blacklist:ip:{ip}
     * 2. 使用SETEX设置值和过期时间
     * 3. 过期后自动从Redis删除，实现自动解封
     */
    public void addToBlacklist(String ip, Duration duration) {
        String key = KEY_PREFIX + ip;
        redisTemplate.opsForValue().set(key, "1", duration);
        log.info("IP加入黑名单，IP: {}, 时长: {}", ip, duration);
    }

    /**
     * 将IP从黑名单移除
     *
     * @param ip 待解封的IP地址
     *
     * 实现原理：
     * 使用Redis DEL命令删除key
     */
    public void removeFromBlacklist(String ip) {
        String key = KEY_PREFIX + ip;
        redisTemplate.delete(key);
        log.info("IP移出黑名单，IP: {}", ip);
    }
}