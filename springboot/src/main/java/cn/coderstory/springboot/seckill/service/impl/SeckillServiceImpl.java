package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.limiter.ConcurrencyLimiter;
import cn.coderstory.springboot.limiter.QpsLimiter;
import cn.coderstory.springboot.lock.DistributedLockService;
import cn.coderstory.springboot.lock.impl.DistributedLockServiceImpl;
import cn.coderstory.springboot.mq.producer.OrderTransactionProducer;
import cn.coderstory.springboot.security.IdempotentService;
import cn.coderstory.springboot.seckill.dto.SeckillRequest;
import cn.coderstory.springboot.seckill.dto.SeckillResponse;
import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.service.SignService;
import cn.coderstory.springboot.seckill.service.SeckillService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 秒杀服务实现类
 *
 * 功能描述：
 * - 实现秒杀抢购的核心业务逻辑
 * - 提供高并发场景下的商品限时抢购能力
 *
 * 核心流程：
 * 1. 幂等性检查 - 防止重复请求
 * 2. QPS 限流 - 控制每秒请求数
 * 3. 并发限流 - 控制最大并发数
 * 4. 活动状态校验 - 验证活动是否进行中
 * 5. 签名验证 - 验证请求合法性
 * 6. Redis 原子扣库存 - 利用 Redis 单线程保证原子性
 * 7. 发送事务消息 - 创建订单
 *
 * 技术要点：
 * - 使用 Redis Lua 脚本保证库存扣减原子性
 * - 使用分布式锁保证活动级别的并发安全
 * - 使用 RocketMQ 事务消息保证订单创建一致性
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    // ==================== 静态常量 ====================

    /**
     * Redis 原子扣库存 Lua 脚本
     *
     * 脚本逻辑：
     * 1. 检查库存 key 是否存在
     * 2. 检查库存是否充足
     * 3. 执行库存扣减
     *
     * 返回值：
     * - -1: key 不存在
     * - 0: 库存不足
     * - >0: 扣减后的剩余库存
     */
    private static final String DEDUCT_STOCK_LUA = """
        local stock = redis.call('GET', KEYS[1])
        if not stock then return -1 end
        if tonumber(stock) < tonumber(ARGV[1]) then return 0 end
        return redis.call('DECRBY', KEYS[1], ARGV[1])
        """;

    /** 库存 Key 前缀 */
    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    // ==================== 依赖注入 ====================

    /** 秒杀活动 Mapper */
    private final SeckillActivityMapper activityMapper;

    /** Redis 模板，用于执行 Lua 脚本和操作 String 类型数据 */
    private final StringRedisTemplate redisTemplate;

    /** 签名服务，用于生成和验证请求签名 */
    private final SignService signService;

    /** QPS 限流器，控制每秒请求数 */
    private final QpsLimiter qpsLimiter;

    /** 并发限流器，控制最大并发处理数 */
    private final ConcurrencyLimiter concurrencyLimiter;

    /** 幂等性服务，防止重复请求 */
    private final IdempotentService idempotentService;

    /** 订单事务消息生产者 */
    private final OrderTransactionProducer orderTransactionProducer;

    /** 分布式锁服务 */
    private final DistributedLockService distributedLockService;

    // ==================== 业务方法 ====================

    /**
     * 执行秒杀抢购
     *
     * 功能描述：
     * - 处理用户的秒杀抢购请求
     * - 采用多层防护策略保证系统稳定性
     * - 使用 Redis 原子扣减保证不超卖
     *
     * 处理流程：
     * 1. 生成队列ID，用于追踪请求状态
     * 2. 幂等性检查，同一请求短时间内只能处理一次
     * 3. QPS 限流，单商品每秒请求数限制
     * 4. 并发限流，最大同时处理请求数限制
     * 5. 活动状态校验，确保活动进行中
     * 6. 签名验证，确保请求来源合法
     * 7. Redis 原子扣库存，Lua 脚本保证原子性
     * 8. 发送事务消息，异步创建订单
     *
     * @param request 秒杀请求参数（包含商品ID、活动ID、签名等）
     * @param userId 用户ID
     * @return 秒杀响应（包含队列ID、状态、消息）
     *
     * @throws Exception 系统内部错误
     */
    @Override
    public SeckillResponse seckill(SeckillRequest request, Long userId) {
        // 生成唯一队列ID，用于追踪请求状态
        String queueId = UUID.randomUUID().toString();
        log.info("开始处理秒杀请求: userId={}, goodsId={}, activityId={}, queueId={}",
                userId, request.getGoodsId(), request.getActivityId(), queueId);

        // ========== 第一层防护：幂等性检查 ==========
        // 使用 Redis SETNX 保证同一请求短时间内只能处理一次
        if (!idempotentService.tryAcquire(request.getIdempotentKey(), Duration.ofMinutes(10))) {
            log.warn("重复请求被拒绝: userId={}, goodsId={}, idempotentKey={}",
                    userId, request.getGoodsId(), request.getIdempotentKey());
            return SeckillResponse.failed("重复请求");
        }

        // ========== 第二层防护：QPS 限流 ==========
        // 使用滑动窗口算法控制每秒请求数
        if (!qpsLimiter.tryAcquire(request.getGoodsId())) {
            log.warn("QPS 限流触发: userId={}, goodsId={}", userId, request.getGoodsId());
            idempotentService.tryAcquire(request.getIdempotentKey(), Duration.ofMinutes(10));
            return SeckillResponse.failed("请求过于频繁");
        }

        // ========== 第三层防护：并发限流 ==========
        // 使用信号量控制最大并发处理数
        if (!concurrencyLimiter.tryAcquire()) {
            log.warn("并发限流触发: userId={}, goodsId={}", userId, request.getGoodsId());
            return SeckillResponse.failed("系统繁忙，请稍后再试");
        }

        try {
            // ========== 第四层：活动状态校验 ==========
            // 获取活动信息并校验
            SeckillActivity activity = activityMapper.selectById(request.getActivityId());
            if (activity == null) {
                log.warn("活动不存在: activityId={}", request.getActivityId());
                return SeckillResponse.failed("活动不存在");
            }
            if (activity.getStatus() != 1) {
                log.warn("活动未开始或已结束: activityId={}, status={}",
                        request.getActivityId(), activity.getStatus());
                return SeckillResponse.failed("活动未开始或已结束");
            }

            // ========== 第五层：签名验证 ==========
            // 验证请求签名，防止非法请求
            if (request.getSign() != null && !request.getSign().isEmpty()) {
                if (!signService.verifySign(request.getSign(), request.getTimestamp())) {
                    log.warn("签名验证失败: userId={}, goodsId={}", userId, request.getGoodsId());
                    return SeckillResponse.failed("签名无效");
                }
            }

            // ========== 第六层：分布式锁（活动级别） ==========
            // 使用分布式锁保证活动维度的并发安全
            String activityLockKey = DistributedLockServiceImpl.getActivityLockKey(request.getActivityId());
            Boolean lockAcquired = distributedLockService.executeWithLock(
                    activityLockKey,
                    () -> {
                        // 执行库存扣减
                        return deductStockInRedis(request.getGoodsId());
                    }
            );

            if (lockAcquired == null || !lockAcquired) {
                log.warn("库存扣减失败或未获取到锁: goodsId={}", request.getGoodsId());
                return SeckillResponse.failed("库存不足");
            }

            // ========== 第七层：发送事务消息 ==========
            // 发送订单创建消息，MQ 会保证消息和本地事务的一致性
            try {
                orderTransactionProducer.sendOrderCreateMsg(
                        userId,
                        request.getGoodsId(),
                        request.getActivityId(),
                        queueId
                );
                log.info("订单创建消息发送成功: queueId={}", queueId);
            } catch (Exception e) {
                log.error("订单创建消息发送失败: queueId={}", queueId, e);
                // 注意：这里不直接返回失败，因为 Redis 库存已经扣减
                // 需要依赖 MQ 的回查机制或者定时对账来保证一致性
            }

            // 返回排队中状态，前端可以通过 queueId 查询结果
            return SeckillResponse.queued(queueId);

        } catch (Exception e) {
            log.error("秒杀处理异常: userId={}, goodsId={}, error={}",
                    userId, request.getGoodsId(), e.getMessage(), e);
            return SeckillResponse.failed("系统错误，请稍后重试");
        } finally {
            // 释放并发限流器
            concurrencyLimiter.release();
        }
    }

    /**
     * 在 Redis 中原子扣减库存
     *
     * 功能描述：
     * - 使用 Lua 脚本保证扣减操作的原子性
     * - Redis 单线程执行特性保证并发安全
     *
     * @param goodsId 商品ID
     * @return 是否扣减成功
     */
    private boolean deductStockInRedis(Long goodsId) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;

        // 执行 Lua 脚本，保证原子性
        Long remaining = redisTemplate.execute(
                RedisScript.of(DEDUCT_STOCK_LUA, Long.class),
                Collections.singletonList(stockKey),
                "1"  // 每次扣减 1 件
        );

        if (remaining == null) {
            log.warn("库存检查失败，Redis key 不存在: goodsId={}", goodsId);
            return false;
        }

        if (remaining < 0) {
            log.warn("库存不足: goodsId={}, remaining={}", goodsId, remaining);
            return false;
        }

        log.info("Redis 库存扣减成功: goodsId={}, remaining={}", goodsId, remaining);
        return true;
    }

    /**
     * 获取商品库存（从 Redis）
     *
     * @param goodsId 商品ID
     * @return 当前库存数量
     */
    @Override
    public int getStock(Long goodsId) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        String stock = redisTemplate.opsForValue().get(stockKey);
        if (stock != null) {
            return Integer.parseInt(stock);
        }
        return 0;
    }

    /**
     * 预热商品库存到 Redis
     *
     * 功能描述：
     * - 在秒杀开始前，将库存数据预加载到 Redis
     * - 避免秒杀开始时大量请求直接打到数据库
     *
     * @param goodsId 商品ID
     * @param stock 库存数量
     */
    @Override
    public void preloadStock(Long goodsId, int stock) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
        log.info("库存预热完成: goodsId={}, stock={}", goodsId, stock);
    }

    /**
     * 回滚 Redis 中的库存
     *
     * 功能描述：
     * - 当订单创建失败或超时取消时，回滚 Redis 库存
     * - 保证库存数据的最终一致性
     *
     * @param goodsId 商品ID
     * @param quantity 回滚数量
     */
    @Override
    public void rollbackStock(Long goodsId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + goodsId;
        redisTemplate.opsForValue().increment(stockKey, quantity);
        log.info("Redis 库存回滚成功: goodsId={}, quantity={}", goodsId, quantity);
    }
}
