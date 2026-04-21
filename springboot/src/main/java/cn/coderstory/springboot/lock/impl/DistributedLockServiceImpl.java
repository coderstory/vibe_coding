package cn.coderstory.springboot.lock.impl;

import cn.coderstory.springboot.config.SeckillProperties;
import cn.coderstory.springboot.lock.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务实现类
 *
 * 功能描述：
 * - 基于 Redisson 实现的分布式锁服务
 * - 提供多种获取和释放锁的方式
 * - 提供带锁执行的模板方法，简化业务代码
 *
 * 实现特点：
 * - 使用 Redisson 的可重入锁（RLock）
 * - 支持公平锁和非公平锁
 * - 自动处理锁的获取和释放
 *
 * 使用场景：
 * - 秒杀活动的并发控制
 * - 商品库存的原子操作
 * - 订单创建的幂等性保证
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockServiceImpl implements DistributedLockService {

    // ==================== 依赖注入 ====================

    /**
     * Redisson 客户端
     * - 用于获取各种分布式锁
     * - 由 RedissonConfig 配置类自动注入
     */
    private final RedissonClient redissonClient;

    /**
     * 秒杀业务配置
     * - 用于获取默认的锁等待时间和持有时间
     */
    private final SeckillProperties seckillProperties;

    // ==================== 锁键前缀常量 ====================

    /** 秒杀活动锁前缀（与活动数据key区分） */
    private static final String ACTIVITY_LOCK_PREFIX = "seckill:lock:activity:";

    /** 商品库存锁前缀 */
    private static final String STOCK_LOCK_PREFIX = "seckill:lock:stock:";

    /** 用户订单锁前缀 */
    private static final String ORDER_LOCK_PREFIX = "seckill:order:";

    // ==================== 公共方法 ====================

    /**
     * 获取分布式锁（不等待）
     *
     * @param lockKey 锁的键
     * @return 锁对象，获取失败返回 null
     */
    @Override
    public RLock getLock(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            log.warn("获取分布式锁失败：锁键为空");
            return null;
        }
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁（带等待时间）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    @Override
    public boolean tryLock(String lockKey, long waitTime, TimeUnit unit) {
        long leaseTime = seckillProperties.getLock().getLeaseTime();
        return tryLock(lockKey, waitTime, leaseTime, unit);
    }

    /**
     * 尝试获取锁（带等待时间和持有时间）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        if (lockKey == null || lockKey.isEmpty()) {
            log.warn("尝试获取分布式锁失败：锁键为空");
            return false;
        }

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (acquired) {
                log.debug("成功获取分布式锁: lockKey={}, waitTime={}, leaseTime={}", lockKey, waitTime, leaseTime);
            } else {
                log.debug("获取分布式锁超时: lockKey={}, waitTime={}", lockKey, waitTime);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: lockKey={}", lockKey, e);
            return false;
        } catch (Exception e) {
            log.error("获取分布式锁异常: lockKey={}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的键
     */
    @Override
    public void unlock(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            log.warn("释放分布式锁失败：锁键为空");
            return;
        }

        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("成功释放分布式锁: lockKey={}", lockKey);
            } else {
                log.warn("当前线程未持有锁，无法释放: lockKey={}", lockKey);
            }
        } catch (Exception e) {
            log.error("释放分布式锁异常: lockKey={}", lockKey, e);
        }
    }

    /**
     * 检查锁是否被当前线程持有
     *
     * @param lockKey 锁的键
     * @return 是否被当前线程持有
     */
    @Override
    public boolean isLocked(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            return false;
        }
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    /**
     * 执行带锁的业务逻辑（使用默认等待和持有时间）
     *
     * @param lockKey 锁的键
     * @param supplier 业务逻辑
     * @return 业务执行结果
     */
    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        long waitTime = seckillProperties.getLock().getWaitTime();
        TimeUnit unit = TimeUnit.SECONDS;
        return executeWithLock(lockKey, waitTime, unit, supplier);
    }

    /**
     * 执行带锁的业务逻辑（带等待时间）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待获取锁的时间
     * @param unit 时间单位
     * @param supplier 业务逻辑
     * @return 业务执行结果，获取锁失败返回 null
     */
    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, TimeUnit unit, Supplier<T> supplier) {
        if (lockKey == null || lockKey.isEmpty() || supplier == null) {
            log.warn("执行带锁业务逻辑失败：参数异常");
            return null;
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(waitTime, seckillProperties.getLock().getLeaseTime(), unit);
            if (!acquired) {
                log.warn("获取锁失败，执行被拒绝: lockKey={}", lockKey);
                return null;
            }

            log.debug("开始执行带锁业务: lockKey={}", lockKey);
            return supplier.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("带锁业务执行被中断: lockKey={}", lockKey, e);
            return null;
        } catch (Exception e) {
            log.error("带锁业务执行异常: lockKey={}", lockKey, e);
            throw e;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("带锁业务执行完成，释放锁: lockKey={}", lockKey);
            }
        }
    }

    /**
     * 执行带锁的业务逻辑（无返回值，使用默认时间）
     *
     * @param lockKey 锁的键
     * @param runnable 业务逻辑
     */
    @Override
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 执行带锁的业务逻辑（带等待时间，无返回值）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待获取锁的时间
     * @param unit 时间单位
     * @param runnable 业务逻辑
     */
    @Override
    public void executeWithLock(String lockKey, long waitTime, TimeUnit unit, Runnable runnable) {
        executeWithLock(lockKey, waitTime, unit, () -> {
            runnable.run();
            return null;
        });
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取秒杀活动锁
     *
     * @param activityId 活动ID
     * @return 活动锁键
     */
    public static String getActivityLockKey(Long activityId) {
        return ACTIVITY_LOCK_PREFIX + activityId;
    }

    /**
     * 获取商品库存锁
     *
     * @param goodsId 商品ID
     * @return 库存锁键
     */
    public static String getStockLockKey(Long goodsId) {
        return STOCK_LOCK_PREFIX + goodsId;
    }

    /**
     * 获取用户订单锁
     *
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @return 订单锁键
     */
    public static String getOrderLockKey(Long userId, Long goodsId) {
        return ORDER_LOCK_PREFIX + userId + ":" + goodsId;
    }
}
