package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 并发限流器 - 信号量模式实现
 *
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                           并发控制原理                                       │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │                                                                             │
 * │   请求入口                                                                   │
 * │       │                                                                     │
 * │       ▼                                                                     │
 * │   ┌────────────────┐                                                        │
 * │   │  INCR counter  │ ← 原子递增，统计当前正在处理的请求数                        │
 * │   └────────┬───────┘                                                        │
 * │            │                                                                │
 * │            ▼                                                                │
 * │   ┌────────────────┐                                                        │
 * │   │ count <= limit?│ ← 判断是否超过最大并发数                                  │
 * │   └────────┬───────┘                                                        │
 * │            │                                                                │
 * │      ┌─────┴─────┐                                                          │
 * │      │           │                                                          │
 * │      ▼           ▼                                                          │
 * │    YES          NO                                                          │
 * │      │           │                                                          │
 * │      ▼           ▼                                                          │
 * │   放行         DECR + 拒绝                                                   │
 * │      │                                                                     │
 * │      ▼                                                                     │
 * │   处理请求                                                                   │
 * │      │                                                                     │
 * │      ▼                                                                     │
 * │   DECR counter ← 请求完成后递减计数器                                        │
 * │                                                                            │
 └────────────────────────────────────────────────────────────────────────────┘
 *
 * 核心特点：
 * - 使用Redis INCR/DECR原子操作保证计数准确性
 * - 简单高效，比令牌桶算法更轻量
 * - 适合控制系统的最大并发处理能力
 *
 * 与QpsLimiter的区别：
 * - QpsLimiter：控制单位时间内的请求数量（流量整形）
 * - ConcurrencyLimiter：控制同时处理的请求数量（并发控制）
 * 两者配合使用，既控制速率又控制并发
 *
 * 使用示例：
 * ```java
 * if (concurrencyLimiter.tryAcquire()) {
 *     try {
 *         // 执行秒杀逻辑
 *     } finally {
 *         concurrencyLimiter.release(); // 无论成功失败都要释放
 *     }
 * } else {
 *     // 并发超限，拒绝请求
 * }
 * ```
 *
 * @author system
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConcurrencyLimiter {

    /** 最大并发数 */
    private static final int MAX_CONCURRENT = 2000;

    /** Redis计数器key */
    private static final String COUNTER_KEY = "seckill:processing:count";

    /** Redis模板 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 尝试获取并发许可
     *
     * @return true - 允许执行; false - 并发超限拒绝
     *
     * 原理：
     * 1. 原子递增计数器
     * 2. 判断递增后的值是否超过限制
     * 3. 如果超过限制，则递减并拒绝
     * 4. 如果未超过限制，则允许执行（计数保持递增状态）
     */
    public boolean tryAcquire() {
        Long count = redisTemplate.opsForValue().increment(COUNTER_KEY);
        if (count != null && count <= MAX_CONCURRENT) {
            log.debug("并发计数增加，当前: {}, 限制: {}", count, MAX_CONCURRENT);
            return true;
        }
        // 超过限制，回滚计数
        if (count != null) {
            redisTemplate.opsForValue().decrement(COUNTER_KEY);
            log.warn("并发限流触发，当前: {}, 限制: {}", count, MAX_CONCURRENT);
        }
        return false;
    }

    /**
     * 释放并发许可
     *
     * 重要：无论业务处理成功还是失败，都必须调用此方法释放计数！
     * 否则会导致计数器永久增长，后续请求永远被拒绝
     *
     * 调用时机：
     * - try-finally块中finally
     * - 无论正常返回还是异常抛出都要执行
     */
    public void release() {
        Long count = redisTemplate.opsForValue().decrement(COUNTER_KEY);
        log.debug("并发计数释放，当前: {}", count);
    }
}