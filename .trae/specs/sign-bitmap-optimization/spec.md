# SignService Redis Bitmap 优化规范

## Why
当前 SignServiceImpl 使用 Redis String 类型存储已验证的签名，每个签名占用一个独立的 key。在高并发秒杀场景下：
- 内存占用高：每个签名一个 key
- 查询效率低：需要多次 Redis 操作（hasKey + set）

使用 Redisson 内置的 RBitmap 可以将签名字符串映射为 bit 位置，实现 O(1) 查找和极低的内存占用，且无需自己实现 Bitmap 操作逻辑。

## What Changes
- 使用 Redisson RBitmap 替代 String 存储已使用的签名
- 签名字符串通过 MD5 哈希后取指定位数映射为 bit 位置
- Bitmap key 包含活动 ID，支持不同活动独立的签名状态
- 设置 Bitmap key 的过期时间为秒杀活动时长
- 保持 HMAC-SHA256 签名生成逻辑不变

## Impact
- Affected specs: SignService
- Affected code: 
  - `SignServiceImpl.java` - 核心实现，使用 RedissonClient.getBitMap()
  - `SignService.java` - 接口（需要调整方法签名添加 activityId）
  - `SignServiceTest.java` - 单元测试（需更新参数和新增测试用例）

## 技术方案

### Redisson RBitmap 使用
```java
// 获取 Bitmap
RBitmap bitmap = redissonClient.getBitMap("seckill:sign:bitmap:" + activityId);

// 设置 bit（标记签名已使用）
bitmap.setBit(bitOffset, true);

// 检查 bit（检查签名是否已使用）
boolean used = bitmap.getBit(bitOffset);

// 设置过期时间
bitmap.expire(Duration.ofHours(活动时长小时数));
```

### 签名字符串到 Bit 位置映射
- 对签名字符串计算 MD5 哈希（128 位）
- 取 MD5 的低 32 位作为 bit 偏移量（最大 2^32 ≈ 42 亿）
- 不同签名映射到不同 bit 位置，碰撞概率极低

## ADDED Requirements

### Requirement: Bitmap 签名存储
系统 SHALL 使用 Redisson RBitmap 存储已验证的签名记录。

#### Scenario: 高效签名验证
- **Given** 用户提交签名进行验证
- **When** 调用 verifySign 方法
- **Then** 系统使用 RBitmap.setBit 标记签名已使用，下次验证返回 false

### Requirement: 签名到 Bit 映射
系统 SHALL 将签名字符串映射为 Bitmap 中的唯一 bit 位置。

#### Scenario: 签名字符串映射
- **Given** 签名字符串 "abc123..."
- **When** 需要设置或检查签名
- **Then** 系统计算 MD5(sign) 并取低 32 位作为 bit 偏移量

### Requirement: 活动级 Bitmap 管理
系统 SHALL 为每个秒杀活动创建独立的 RBitmap。

#### Scenario: 多活动隔离
- **Given** 活动 A 和活动 B 同时进行
- **When** 用户验证签名
- **Then** 系统使用活动专属的 Bitmap key，签名状态互不影响

### Requirement: Bitmap 过期时间
系统 SHALL 为 RBitmap 设置过期时间等于秒杀活动时长。

#### Scenario: 自动清理
- **Given** 秒杀活动持续 2 小时
- **When** 首次验证签名时创建 Bitmap
- **Then** Bitmap key 在活动结束后自动过期释放内存

## MODIFIED Requirements

### Requirement: verifySign 方法签名调整
**变更内容**：添加 activityId 参数用于 Bitmap key 区分，添加 duration 参数用于设置过期时间
- **Before**: `boolean verifySign(String sign, long timestamp)`
- **After**: `boolean verifySign(String sign, long timestamp, Long activityId, Duration duration)`

## REMOVED Requirements

### Requirement: String 类型签名存储
**Reason**: 使用 Redisson RBitmap 方案更高效，无需自己实现 Bitmap 操作
**Migration**: 旧接口不再支持，所有签名验证必须通过新接口

## 测试变更说明

### 需要修改的现有测试
1. **SignServiceTest.java** 中所有调用 `verifySign` 的测试用例
   - 添加 `activityId` 参数（如 `1L`）
   - 添加 `duration` 参数（如 `Duration.ofHours(2)`）

### 需要修改的测试清理逻辑
- `tearDown` 方法需要清理 Bitmap key，格式为 `seckill:sign:bitmap:{activityId}`
- 使用 `redisTemplate.delete()` 清理

### 需要新增的测试用例
1. **不同活动签名隔离测试**
   - 验证活动 A 的签名不会影响活动 B 的验证结果

2. **Bitmap 过期时间测试**
   - 验证 Bitmap key 设置了正确的过期时间

3. **MD5 映射唯一性测试**
   - 验证不同签名映射到不同的 bit 位置（边界情况）
