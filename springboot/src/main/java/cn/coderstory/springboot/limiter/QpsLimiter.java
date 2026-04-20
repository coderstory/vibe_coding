package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;

/**
 * QPS限流器 - 滑动窗口算法实现
 *
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                           滑动窗口限流原理                                  │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │                                                                             │
 * │  时间轴 ───────────────────────────────────────────────────────────────►   │
 * │  │          │          │          │          │          │          │        │
 * │  └────────────────────────────────────────────────────────────────────►   │
 * │       T-3s      T-2s      T-1s       T        T+1s      T+2s      T+3s   │
 * │                                                                            │
 * │  算法原理：                                                                 │
 * │  1. 使用Redis Sorted Set存储时间戳                                         │
 * │  2. 每个时间窗口内只允许固定数量的请求通过                                    │
 * │  3. 自动清理过期的时间戳，避免内存膨胀                                       │
 * │  4. 原子操作保证多线程安全                                                  │
 * │                                                                            │
 * │  优势：                                                                     │
 * │  - 比固定窗口算法更平滑，避免临界时刻突发流量                                 │
 * │  - 内存占用可控，自动过期清理                                                │
 * │  - 纯Redis实现，性能高                                                     │
 * │                                                                            │
 └────────────────────────────────────────────────────────────────────────────┘
 *
 * Lua脚本逻辑：
 * 1. ZREMRANGEBYSCORE - 移除窗口外的旧记录
 * 2. ZCARD - 统计当前窗口内的请求数
 * 3. 判断是否超过限制
 * 4. ZADD - 添加新请求的时间戳
 * 5. PEXPIRE - 设置key过期时间
 *
 * 使用示例：
 * ```java
 * if (qpsLimiter.tryAcquire(goodsId)) {
 *     // 允许通过，执行秒杀逻辑
 * } else {
 *     // 限流，拒绝请求
 * }
 * ```
 *
 * @author system
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QpsLimiter {

    /** 每秒最大QPS限制 */
    private static final int QPS_LIMIT = 1000;

    /** Redis模板，用于执行Redis操作 */
    private final StringRedisTemplate redisTemplate;

    /**
     * Lua脚本 - 滑动窗口限流
     *
     * KEYS[1]: 限流key (如 seckill:qps:1)
     * ARGV[1]: 当前时间戳(毫秒)
     * ARGV[2]: 窗口大小(毫秒)，固定1000
     * ARGV[3]: QPS限制数量
     */
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        -- 移除窗口外的时间戳
        local old = now - window
        redis.call('ZREMRANGEBYSCORE', key, '-inf', old)
        -- 统计当前窗口内的请求数
        local count = redis.call('ZCARD', key)
        -- 超过限制，拒绝请求
        if count >= limit then
            return 0
        end
        -- 添加新请求，使用时间戳+随机数作为member确保唯一性
        redis.call('ZADD', key, now, now .. ':' .. math.random())
        -- 设置过期时间，防止key无限增长
        redis.call('PEXPIRE', key, window)
        return 1
        """;

    /**
     * 尝试获取限流许可
     *
     * @param goodsId 商品ID，用于生成唯一的限流key
     * @return true - 允许通过; false - 限流拒绝
     *
     * 实现细节：
     * 1. 构建限流key: "seckill:qps:{goodsId}"
     * 2. 执行Lua脚本，保证操作的原子性
     * 3. 返回1表示允许，返回0表示拒绝
     */
    public boolean tryAcquire(Long goodsId) {
        String key = "seckill:qps:" + goodsId;
        long now = System.currentTimeMillis();

        Long result = redisTemplate.execute(
            RedisScript.of(LUA_SCRIPT, Long.class),
            Collections.singletonList(key),
            String.valueOf(now), "1000", String.valueOf(QPS_LIMIT)
        );

        boolean allowed = result != null && result == 1;
        if (!allowed) {
            log.warn("QPS限流触发，goodsId={}", goodsId);
        }
        return allowed;
    }
}