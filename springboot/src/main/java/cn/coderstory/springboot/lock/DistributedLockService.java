package cn.coderstory.springboot.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务接口
 *
 * 功能说明：
 * - 提供基于 Redisson 的分布式锁操作能力
 * - 支持公平锁、非公平锁等多种锁类型
 * - 提供便捷的带锁执行模板方法
 *
 * 锁键命名规范：
 * - 秒杀活动锁: seckill:activity:{activityId}
 * - 商品库存锁: seckill:stock:{goodsId}
 * - 用户订单锁: seckill:order:{userId}:{goodsId}
 *
 * @author system
 * @version 1.0
 * @since 2026-04-20
 */
public interface DistributedLockService {

    /**
     * 获取分布式锁（不等待，获取不到立即返回）
     *
     * @param lockKey 锁的键
     * @return 锁对象，获取失败返回 null
     *
     * @example
     * <pre>
     *     RLock lock = lockService.getLock("seckill:stock:1");
     *     if (lock != null && lock.tryLock()) {
     *         try {
     *             // 业务逻辑
     *         } finally {
     *             lock.unlock();
     *         }
     *     }
     * </pre>
     */
    RLock getLock(String lockKey);

    /**
     * 尝试获取锁（带等待时间）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param unit 时间单位
     * @return 是否获取成功
     *
     * @example
     * <pre>
     *     boolean acquired = lockService.tryLock("seckill:activity:1", 5, TimeUnit.SECONDS);
     *     if (acquired) {
     *         // 获取锁成功
     *     }
     * </pre>
     */
    boolean tryLock(String lockKey, long waitTime, TimeUnit unit);

    /**
     * 尝试获取锁（带等待时间和持有时间）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间
     * @param leaseTime 持有时间（自动释放时间）
     * @param unit 时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param lockKey 锁的键
     *
     * @example
     * <pre>
     *     lockService.unlock("seckill:stock:1");
     * </pre>
     */
    void unlock(String lockKey);

    /**
     * 检查锁是否被当前线程持有
     *
     * @param lockKey 锁的键
     * @return 是否被当前线程持有
     */
    boolean isLocked(String lockKey);

    /**
     * 执行带锁的业务逻辑（自动获取和释放锁）
     *
     * @param lockKey 锁的键
     * @param supplier 业务逻辑
     * @return 业务执行结果
     *
     * @example
     * <pre>
     *     String result = lockService.executeWithLock("seckill:stock:1", () -> {
     *         // 库存扣减业务逻辑
     *         return stockService.deductStock(goodsId, quantity);
     *     });
     * </pre>
     */
    <T> T executeWithLock(String lockKey, Supplier<T> supplier);

    /**
     * 执行带锁的业务逻辑（带等待时间）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待获取锁的时间
     * @param unit 时间单位
     * @param supplier 业务逻辑
     * @return 业务执行结果，获取锁失败返回 null
     */
    <T> T executeWithLock(String lockKey, long waitTime, TimeUnit unit, Supplier<T> supplier);

    /**
     * 执行带锁的业务逻辑（无返回值）
     *
     * @param lockKey 锁的键
     * @param runnable 业务逻辑
     */
    void executeWithLock(String lockKey, Runnable runnable);

    /**
     * 执行带锁的业务逻辑（带等待时间，无返回值）
     *
     * @param lockKey 锁的键
     * @param waitTime 等待获取锁的时间
     * @param unit 时间单位
     * @param runnable 业务逻辑
     */
    void executeWithLock(String lockKey, long waitTime, TimeUnit unit, Runnable runnable);
}
