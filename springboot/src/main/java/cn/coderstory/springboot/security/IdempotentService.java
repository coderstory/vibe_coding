package cn.coderstory.springboot.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

/**
 * 幂等性服务 - 防重复提交组件
 *
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │                           幂等性保证原理                                     │
 * ├────────────────────────────────────────────────────────────────────────────┤
 * │                                                                             │
 * │   第一次请求                                                                 │
 * │       │                                                                     │
 * │       ▼                                                                     │
 * │   SETNX key = "1" (原子操作，返回true)                                       │
 * │       │                                                                     │
 * │       ▼                                                                     │
 * │   设置过期时间 ──► 处理业务                                                  │
 * │                                                                            │
 │   第二次请求(同一幂等键)                                                       │
 │       │                                                                     │
 *       ▼                                                                     │
 │   SETNX key (返回false，因为key已存在)                                        │
 *       │                                                                     │
 *       ▼                                                                     │
 │   直接返回失败，拒绝重复请求                                                   │
 * │                                                                            │
 └────────────────────────────────────────────────────────────────────────────┘
 *
 * 功能说明：
 * - 幂等性是分布式系统中保证请求只被处理一次的重要机制
 * - 在秒杀场景中，防止用户重复点击导致的重复购买
 * - 使用Redis的SETNX命令实现原子性保证
 *
 * 使用场景：
 * 1. 秒杀抢购 - 防止用户快速点击导致多次下单
 * 2. 支付回调 - 防止重复处理同一笔支付
 * 3. 表单提交 - 防止刷新页面导致重复提交
 * 4. 消息消费 - 防止消息重复投递导致重复处理
 *
 * 实现原理：
 * - 使用Redis SETNX (SET if Not eXists) 原子命令
 * - 只有key不存在时才设置成功，返回true
 * - key已存在时设置失败，返回false
 * - 配合过期时间，自动清理防止永久占用
 *
 * 注意事项：
 * - 过期时间需要根据业务场景合理设置
 * - 太短可能导致有效请求被拒绝
 * - 太长可能导致内存浪费
 *
 * 使用示例：
 * ```java
 * String idempotentKey = request.getIdempotentKey();
 * if (!idempotentService.tryAcquire(idempotentKey, Duration.ofSeconds(30))) {
 *     throw new BusinessException("请求已提交，请勿重复操作");
 * }
 * // 继续处理业务逻辑
 * ```
 *
 * @author system
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotentService {

    /** Redis key前缀 */
    private static final String KEY_PREFIX = "seckill:idempotent:";

    /** 默认过期时间：30秒 */
    private static final Duration DEFAULT_EXPIRE_TIME = Duration.ofSeconds(30);

    /** Redis模板 */
    private final StringRedisTemplate redisTemplate;

    /**
     * 尝试获取幂等性许可
     *
     * @param idempotentKey 幂等键，用于唯一标识一次请求
     * @param expireTime 过期时间，过期后相同的key可以再次请求
     * @return true - 首次请求，允许处理; false - 重复请求，拒绝处理
     *
     * 实现原理：
     * 1. 构建key: "seckill:idempotent:{idempotentKey}"
     * 2. 执行Redis SETNX命令（setIfAbsent）
     * 3. 如果成功设置，说明是首次请求，返回true
     * 4. 如果设置失败，说明已存在，返回false
     * 5. 同时设置过期时间，防止key永久存在
     *
     * 原子性保证：
     * - Redis SETNX是原子操作，无需考虑并发问题
     * - 即使多个线程同时请求同一幂等键，也只有一个能成功
     */
    public boolean tryAcquire(String idempotentKey, Duration expireTime) {
        String key = KEY_PREFIX + idempotentKey;
        // setIfAbsent 等同于 SETNX，原子操作
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", expireTime);
        boolean acquired = Boolean.TRUE.equals(success);
        if (!acquired) {
            log.warn("幂等性拦截，key: {}", idempotentKey);
        }
        return acquired;
    }

    /**
     * 尝试获取幂等性许可（使用默认过期时间）
     *
     * @param idempotentKey 幂等键
     * @return true - 首次请求; false - 重复请求
     */
    public boolean tryAcquire(String idempotentKey) {
        return tryAcquire(idempotentKey, DEFAULT_EXPIRE_TIME);
    }
}