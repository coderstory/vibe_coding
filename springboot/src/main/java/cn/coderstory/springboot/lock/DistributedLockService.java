package cn.coderstory.springboot.lock;

import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务接口
 * <p>
 * 功能说明：
 * - 提供基于 Redisson 的分布式锁操作能力
 * - 支持公平锁、非公平锁等多种锁类型
 * - 提供便捷的带锁执行模板方法
 * <p>
 * 锁键命名规范：
 * - 秒杀活动锁: seckill:lock:activity:{activityId}
 * - 商品库存锁: seckill:lock:stock:{goodsId}
 * - 用户订单锁: seckill:order:{userId}:{goodsId}
 */
public interface DistributedLockService {

    /**
     * 获取分布式锁（不等待，获取不到立即返回）
     *
     * @param lockKey 锁的键
     * @return 锁对象，获取失败返回 null
     */
    RLock getLock(String lockKey);

    /**
     * 尝试获取锁（带等待时间）
     *
     * @param lockKey  锁的键
     * @param waitTime 等待时间
     * @param unit     时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String lockKey, long waitTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param lockKey 锁的键
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
     * @param lockKey  锁的键
     * @param supplier 业务逻辑
     * @return 业务执行结果
     */
    <T> T executeWithLock(String lockKey, Supplier<T> supplier);

    /**
     * 执行带锁的业务逻辑（带等待时间）
     *
     * @param lockKey  锁的键
     * @param waitTime 等待获取锁的时间
     * @param unit     时间单位
     * @param supplier 业务逻辑
     * @return 业务执行结果，获取锁失败返回 null
     */
    <T> T executeWithLock(String lockKey, long waitTime, TimeUnit unit, Supplier<T> supplier);
}
