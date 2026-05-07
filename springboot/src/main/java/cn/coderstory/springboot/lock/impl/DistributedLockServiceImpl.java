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

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockServiceImpl implements DistributedLockService {

    private static final String ACTIVITY_LOCK_PREFIX = "seckill:lock:activity:";
    private static final String STOCK_LOCK_PREFIX = "seckill:lock:stock:";

    private final RedissonClient redissonClient;
    private final SeckillProperties seckillProperties;

    public static String getActivityLockKey(Long activityId) {
        return ACTIVITY_LOCK_PREFIX + activityId;
    }

    public static String getStockLockKey(Long goodsId) {
        return STOCK_LOCK_PREFIX + goodsId;
    }

    @Override
    public RLock getLock(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            log.warn("获取分布式锁失败：锁键为空");
            return null;
        }
        return redissonClient.getLock(lockKey);
    }

    @Override
    public boolean tryLock(String lockKey, long waitTime, TimeUnit unit) {
        if (lockKey == null || lockKey.isEmpty()) {
            log.warn("尝试获取分布式锁失败：锁键为空");
            return false;
        }

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, seckillProperties.getLock().getLeaseTime(), unit);
            if (acquired) {
                log.debug("成功获取分布式锁: lockKey={}, waitTime={}, leaseTime={}", lockKey, waitTime, seckillProperties.getLock().getLeaseTime());
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

    @Override
    public boolean isLocked(String lockKey) {
        if (lockKey == null || lockKey.isEmpty()) {
            return false;
        }
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }

    @Override
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        long waitTime = seckillProperties.getLock().getWaitTime();
        TimeUnit unit = TimeUnit.SECONDS;
        return executeWithLock(lockKey, waitTime, unit, supplier);
    }

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
}
