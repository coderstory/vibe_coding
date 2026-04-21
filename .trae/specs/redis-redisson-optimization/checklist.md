# Checklist

## ConcurrencyLimiter 重构
- [x] ConcurrencyLimiter 注入 RedissonClient
- [x] 使用 RSemaphore 替代 increment/decrement
- [x] 添加 trySetPermits 初始化
- [x] tryAcquire 调用 semaphore.tryAcquire()
- [x] release 调用 semaphore.release()
- [x] ConcurrencyLimiterTest 测试通过

## IdempotentService 重构
- [x] IdempotentService 注入 RedissonClient
- [x] 使用 RPermitExpirableSemaphore 替代 setIfAbsent
- [x] tryAcquire 调用 semaphore.tryAcquire(0, expireTime, TimeUnit)
- [x] IdempotentServiceTest 测试通过

## IpRateLimiter 重构
- [x] IpRateLimiter 注入 RedissonClient
- [x] 为每个 IP 创建独立的 RRateLimiter
- [x] 使用 trySetRate 配置速率限制
- [x] isIpAllowed 调用 rateLimiter.tryAcquire()
- [x] IpRateLimiterTest 测试通过

## BlacklistService 重构
- [x] BlacklistService 注入 RedissonClient
- [x] 使用 RBloomFilter 快速判断
- [x] 使用 RSet 精确存储黑名单
- [x] isBlacklisted 先检查布隆过滤器，再检查 RSet
- [x] BlacklistServiceTest 测试通过

## 验证检查
- [x] 所有测试通过
- [x] 代码不再使用 StringRedisTemplate 操作 Redis 基础类型
- [x] 所有 Redis 功能使用 Redisson 内置数据结构
