# Redis 代码 Redisson 优化规范

## Why
当前项目中存在多个使用 `StringRedisTemplate` 实现的功能，这些功能可以用 Redisson 内置的高级数据结构替代，带来以下好处：

- **更简洁的代码**：无需手动实现原子操作和并发控制
- **更好的性能**：Redisson 底层使用 Netty 异步 I/O
- **更可靠的实现**：经过大量生产验证的分布式数据结构
- **更丰富的功能**：内置分布式信号量、限流器、布隆过滤器等

## What Changes

### 需要替换的组件

| 当前实现 | Redisson 替代方案 | 优势 |
|----------|-------------------|------|
| `ConcurrencyLimiter` | `RSemaphore` | 内置信号量，无需手动 increment/decrement |
| `IdempotentService` | `RPermitExpirableSemaphore` | 内置幂等 + 过期，无需手动 setIfAbsent + expire |
| `IpRateLimiter` | `RRateLimiter` | 内置速率限制，无需手动 increment + expire |
| `BlacklistService` | `RBloomFilter` + `RSet` | 布隆过滤器快速判断，RSet 存储完整黑名单 |

### 不需要替换的组件

| 组件 | 原因 |
|------|------|
| `DistributedLockService` | 已经使用 `RedissonClient.getLock()` |
| `QpsLimiter` | 使用 Lua 脚本实现滑动窗口算法，定制化程度高 |
| `SignServiceImpl` | 刚优化为使用 Redis Bitmap，已有 Bitmap 需求 |

## Impact

- Affected specs: 多个服务需要重构
- Affected code:
  - `ConcurrencyLimiter.java` → 使用 `RSemaphore` 重构
  - `IdempotentService.java` → 使用 `RPermitExpirableSemaphore` 重构
  - `IpRateLimiter.java` → 使用 `RRateLimiter` 重构
  - `BlacklistService.java` → 使用 `RBloomFilter` + `RSet` 重构
  - 相关测试文件

## 技术方案

### 1. ConcurrencyLimiter → RSemaphore

**Before:**
```java
public boolean tryAcquire() {
    Long count = redisTemplate.opsForValue().increment(COUNTER_KEY);
    if (count != null && count <= MAX_CONCURRENT) {
        return true;
    }
    redisTemplate.opsForValue().decrement(COUNTER_KEY);
    return false;
}
```

**After:**
```java
// 初始化时设置许可数
RSemaphore semaphore = redissonClient.getSemaphore("seckill:concurrency:semaphore");
semaphore.trySetPermits(MAX_CONCURRENT);

public boolean tryAcquire() {
    return semaphore.tryAcquire();
}

public void release() {
    semaphore.release();
}
```

### 2. IdempotentService → RPermitExpirableSemaphore

**Before:**
```java
public boolean tryAcquire(String idempotentKey, Duration expireTime) {
    String key = KEY_PREFIX + idempotentKey;
    Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", expireTime);
    return Boolean.TRUE.equals(success);
}
```

**After:**
```java
// 初始化时创建
RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore("seckill:idempotent:semaphore");

public boolean tryAcquire(String idempotentKey, Duration expireTime) {
    try {
        String permitId = semaphore.tryAcquire(0, expireTime.toMillis(), TimeUnit.MILLISECONDS);
        return permitId != null;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
    }
}
```

### 3. IpRateLimiter → RRateLimiter

**Before:**
```java
public boolean isIpAllowed(String ip) {
    String qpsKey = "seckill:ip:qps:" + ip;
    Long count = redisTemplate.opsForValue().increment(qpsKey);
    if (count == 1) {
        redisTemplate.expire(qpsKey, 1, TimeUnit.SECONDS);
    }
    return count == null || count <= IP_QPS;
}
```

**After:**
```java
// 每个 IP 一个 RateLimiter
RRateLimiter rateLimiter = redissonClient.getRateLimiter("seckill:ip:rate:" + ip);
rateLimiter.trySetRate(RateType.OVERALL, IP_QPS, 1, RateIntervalUnit.SECONDS);

public boolean isIpAllowed(String ip) {
    return rateLimiter.tryAcquire();
}
```

### 4. BlacklistService → RBloomFilter + RSet

**Before:**
```java
public boolean isBlacklisted(String ip) {
    String key = KEY_PREFIX + ip;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
}

public void addToBlacklist(String ip, Duration duration) {
    String key = KEY_PREFIX + ip;
    redisTemplate.opsForValue().set(key, "1", duration);
}
```

**After:**
```java
// 布隆过滤器用于快速判断（可能误判），RSet 用于精确判断
RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("seckill:blacklist:bloom");
bloomFilter.tryInit(1000000, 0.01); // 100万容量，1%误判率

RSet<String> blacklistSet = redissonClient.getSet("seckill:blacklist:set");

public boolean isBlacklisted(String ip) {
    // 先用布隆过滤器快速判断
    if (!bloomFilter.contains(ip)) {
        return false; // 一定不在黑名单
    }
    // 布隆过滤器可能误判，再用 RSet 精确判断
    return blacklistSet.contains(ip);
}

public void addToBlacklist(String ip, Duration duration) {
    bloomFilter.add(ip);
    blacklistSet.add(ip);
    // RSet 可以设置过期时间
}

public void removeFromBlacklist(String ip) {
    bloomFilter.delete(ip);
    blacklistSet.remove(ip);
}
```

## ADDED Requirements

### Requirement: Redisson 信号量替代并发限流
系统 SHALL 使用 `RSemaphore` 实现并发控制。

#### Scenario: 并发控制
- **Given** 当前并发数未达上限
- **When** 调用 tryAcquire
- **Then** 返回 true 并占用一个许可

### Requirement: Redisson 可过期许可信号量替代幂等性
系统 SHALL 使用 `RPermitExpirableSemaphore` 实现幂等性保证。

#### Scenario: 幂等性保证
- **Given** 首次请求
- **When** 调用 tryAcquire
- **Then** 获取许可成功，自动过期

### Requirement: Redisson 速率限制器替代 IP 限流
系统 SHALL 使用 `RRateLimiter` 实现 IP 级别限流。

#### Scenario: IP 限流
- **Given** IP 请求频率未超限
- **When** 调用 isIpAllowed
- **Then** 返回 true

### Requirement: Redisson 布隆过滤器 + Set 替代黑名单
系统 SHALL 使用 `RBloomFilter` + `RSet` 实现黑名单服务。

#### Scenario: 黑名单检查
- **Given** 请求进入
- **When** 调用 isBlacklisted
- **Then** 使用布隆过滤器快速判断，再用 RSet 精确确认

## 测试变更说明

### 需要修改的测试
1. **ConcurrencyLimiterTest** - 需要初始化 RSemaphore 许可数
2. **IdempotentServiceTest** - 需要使用 RPermitExpirableSemaphore
3. **IpRateLimiterTest** - 需要配置 RRateLimiter
4. **BlacklistServiceTest** - 需要配置 RBloomFilter 和 RSet

### 新增测试用例
1. 测试 RSemaphore 并发控制正确性
2. 测试 RPermitExpirableSemaphore 过期自动释放
3. 测试 RRateLimiter 速率限制
4. 测试 RBloomFilter 误判率在可接受范围
