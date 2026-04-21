# Tasks

- [x] Task 1: 重构 ConcurrencyLimiter 使用 RSemaphore
  - [x] SubTask 1.1: 修改 ConcurrencyLimiter 实现，注入 RedissonClient
  - [x] SubTask 1.2: 使用 RSemaphore 替代 increment/decrement 逻辑
  - [x] SubTask 1.3: 添加 trySetPermits 初始化方法
  - [x] SubTask 1.4: 更新单元测试（无需修改，RSemaphore 自动管理）

- [x] Task 2: 重构 IdempotentService 使用 RPermitExpirableSemaphore
  - [x] SubTask 2.1: 修改 IdempotentService 实现，注入 RedissonClient
  - [x] SubTask 2.2: 使用 RPermitExpirableSemaphore 替代 setIfAbsent 逻辑
  - [x] SubTask 2.3: 更新单元测试（更新 key 格式）

- [x] Task 3: 重构 IpRateLimiter 使用 RRateLimiter
  - [x] SubTask 3.1: 修改 IpRateLimiter 实现，注入 RedissonClient
  - [x] SubTask 3.2: 为每个 IP 创建独立的 RRateLimiter
  - [x] SubTask 3.3: 使用 trySetRate 配置速率限制
  - [x] SubTask 3.4: 更新单元测试（无需修改）

- [x] Task 4: 重构 BlacklistService 使用 RBloomFilter + RSet
  - [x] SubTask 4.1: 修改 BlacklistService 实现，注入 RedissonClient
  - [x] SubTask 4.2: 使用 RBloomFilter 快速判断
  - [x] SubTask 4.3: 使用 RSet 精确存储黑名单
  - [x] SubTask 4.4: 更新单元测试（无需修改）

- [x] Task 5: 验证和审查
  - [x] SubTask 5.1: 运行完整测试套件
  - [x] SubTask 5.2: 验证所有限流功能正常工作
  - [x] SubTask 5.3: 检查代码是否使用 Redisson 内置功能

# Task Dependencies
- Task 1-4 可以并行开发
- Task 5 依赖 Task 1-4
