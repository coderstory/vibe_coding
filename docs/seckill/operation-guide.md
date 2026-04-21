# 秒杀系统运维技术文档

> 本文档面向运维人员和技术支持，涵盖系统架构、故障检测、问题排查和应急处理。

---

## 一、系统概述

### 1.1 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户端 (Browser)                        │
└─────────────────────────────────────────────────────────────────┘
                               ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot 后端 (8080)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │限流器     │  │签名校验    │  │风控检测  │  │秒杀服务  │        │
│  │QpsLimiter│ │SignService│ │Blacklist │  │SeckillService│     │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
└─────────────────────────────────────────────────────────────────┘
           ↓                    ↓                    ↓
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Redis (6379)  │    │  RocketMQ (9876)│    │   MySQL (3306)  │
│  ┌───────────┐  │    │  ┌───────────┐  │    │  ┌───────────┐  │
│  │库存缓存    │  │    │  │事务消息   │  │    │  │订单数据   │  │
│  │限流计数    │  │    │  │半消息     │  │    │  │活动数据   │  │
│  │幂等键      │  │    │  └───────────┘  │    │  │商品数据   │  │
│  │黑名单      │  │    └─────────────────┘    └─────────────────┘  
│  └───────────┘  │                                               
└─────────────────┘                                               
```

### 1.2 技术栈

| 组件 | 版本 | 端口 | 作用 |
|------|------|------|------|
| Spring Boot | 4.0.5 | 8080 | 后端服务 |
| Vue 3 | 3.5+ | 5173 | 前端服务 |
| Redis | 8.0+ | 6379 | 缓存、限流、分布式锁 |
| RocketMQ | 5.3.2 | 9876/10911 | 消息队列、事务消息 |
| MySQL | 8.0+ | 3306 | 持久化存储 |

### 1.3 核心进程

```bash
# 后端服务
springboot-0.0.1-SNAPSHOT.jar  (PID: xxxx)

# 依赖服务
redis-server.exe              (PID: xxxx)
mysqld.exe                    (PID: xxxx)
mqnamesrv.exe                (PID: xxxx)
mqbroker.exe                 (PID: xxxx)
```

---

## 二、核心业务流程

### 2.1 抢购时序图

```
用户          后端            Redis           MQ              数据库
 │             │               │              │                │
 │──点击抢购──▶│               │              │                │
 │             │──签名验证─────▶│              │                │
 │             │──幂等检查─────▶│              │                │
 │             │──QPS限流─────▶│              │                │
 │             │──并发限流─────▶│              │                │
 │             │──活动校验─────▶│              │                │
 │             │──原子扣库存────────────────▶│                │
 │             │               │              │──发送半消息───▶│
 │◀─返回queueId─┤               │              │                │
 │             │               │              │◀─执行本地事务──│
 │             │               │              │──提交消息────▶│
 │             │               │              │◀─创建订单─────│
 │             │               │              │                │
 │◀─SSE通知───│◀────────SSE通知────────────│                │
 │  (成功)     │               │              │                │
```

### 2.2 SSE 通知流程（已修复）

**问题**：早期实现中，前端在收到 `queueId` 后才建立 SSE 连接，导致通知丢失。

**修复后的流程**：
```
前端生成 queueId
       ↓
前端先建立 SSE 连接（/api/seckill/subscribe/{queueId}）
       ↓
前端调用 API 携带 queueId
       ↓
后端验证 queueId 存在
       ↓
处理秒杀（扣库存、发MQ）
       ↓
订单成功后 → sseService.sendSuccess(queueId)
库存不足时 → sseService.sendFailed(queueId)
       ↓
前端 SSE 收到通知
```

### 2.3 库存扣减流程（已修复）

**问题**：早期实现中，库存被重复扣减（`SeckillServiceImpl` 扣一次，`StockConsumer` 再扣一次）。

**修复后的流程**：
```
SeckillServiceImpl.seckill()
       ↓
  deductStockInRedis()  ← 直接扣 Redis 库存
       ↓
  orderTransactionProducer.sendOrderCreateMsg()  ← 只发订单消息
       ↓
OrderTransactionListener.executeLocalTransaction()
       ↓
  创建订单 + SSE 通知成功（不扣库存）
```

---

## 三、关键设计点

### 3.1 多层防护机制

| 层级 | 组件 | 作用 |
|------|------|------|
| 第一层 | QpsLimiter | 滑动窗口算法，每商品每秒最多 1000 请求 |
| 第二层 | ConcurrencyLimiter | 信号量控制，最大 500 并发处理 |
| 第三层 | IdempotentService | Redis SETNX 保证幂等性 |
| 第四层 | SignService | HMAC-SHA256 签名防篡改 |
| 第五层 | DistributedLockService | Redisson 分布式锁保证并发安全 |

### 3.2 Redis Key 设计

| Key 模式 | 类型 | 说明 | TTL |
|----------|------|------|-----|
| `seckill:stock:{goodsId}` | String | 商品库存 | 活动结束+1小时 |
| `seckill:lock:stock:{goodsId}` | Hash | 库存分布式锁 | 10秒 |
| `seckill:lock:activity:{activityId}` | Hash | 活动分布式锁 | 10秒 |
| `seckill:qps:{goodsId}` | Sorted Set | QPS 限流计数 | 1秒 |
| `seckill:idempotent:{key}` | String | 幂等性标记 | 10分钟 |
| `seckill:sign:{sign}` | String | 签名使用记录 | 5分钟 |

### 3.3 锁 Key 冲突修复

**问题**：`seckill:stock:{goodsId}` 既用于存储库存数据（String），又用于分布式锁（Hash），导致数据被覆盖。

**修复**：
```java
// 库存数据 Key
private static final String STOCK_KEY_PREFIX = "seckill:stock:";  // String 类型

// 库存锁 Key（前缀区分）
private static final String STOCK_LOCK_PREFIX = "seckill:lock:stock:";  // Hash 类型
```

---

## 四、故障检测与处理

### 4.1 健康检查接口

```bash
# 后端健康检查
curl http://localhost:8080/api/monitor/metrics

# 响应示例
{
  "code": 200,
  "data": {
    "concurrentCount": 10,
    "qpsKeys": 100,
    "timestamp": 1713600000000
  }
}

# SSE 连接状态检查
curl http://localhost:8080/api/seckill/subscribe/status/{queueId}

# 响应示例
{
  "queueId": "xxx",
  "online": true,
  "message": "连接在线"
}
```

### 4.2 常见故障排查

#### 4.2.1 抢购接口返回 500

**排查步骤**：

1. **检查 Redis 连接**
```bash
redis-cli ping
# 期望：PONG

redis-cli get seckill:stock:1
# 期望：库存数量（如 100）
```

2. **检查数据库连接**
```bash
mysql -h localhost -u root -p123456 -e "SELECT * FROM admin_system.seckill_activity WHERE id=1;"
```

3. **查看日志**
```bash
# Windows
type springboot\logs\application.log | findstr "ERROR"

# Linux
tail -f springboot/logs/application.log | grep ERROR
```

4. **常见原因**

| 错误信息 | 可能原因 | 解决方案 |
|----------|----------|----------|
| `Redis connection refused` | Redis 未启动 | 重启 Redis |
| `MQ connection failed` | RocketMQ 未启动 | 重启 RocketMQ |
| `Table 'seckill_activity' doesn't exist` | 数据库未迁移 | 执行 Flyway 迁移 |

#### 4.2.2 SSE 通知失败

**排查步骤**：

1. **检查 SSE 连接**
```bash
curl http://localhost:8080/api/seckill/subscribe/status/{queueId}
```

2. **检查 SSE 服务日志**
```bash
# 搜索 SSE 相关日志
findstr "SSE" springboot/logs/application.log
```

3. **常见原因**

| 问题 | 可能原因 | 解决方案 |
|------|----------|----------|
| 前端未收到通知 | 前端未建立 SSE 连接 | 检查前端代码，确保先建立连接 |
| 通知丢失 | queueId 不匹配 | 确保 API 调用时传递正确的 queueId |
| 连接超时 | 超时时间太短 | 调整 `seckill.sse.timeout` 配置 |

#### 4.2.3 库存超卖

**排查步骤**：

1. **检查 Redis 库存**
```bash
redis-cli get seckill:stock:{goodsId}
```

2. **检查数据库订单数量**
```sql
SELECT COUNT(*) FROM seckill_order WHERE goods_id = ? AND status = 1;
```

3. **检查是否使用 Lua 脚本扣减**
```java
// 正确的 Lua 脚本扣减
Long remaining = redisTemplate.execute(
    RedisScript.of(DEDUCT_STOCK_LUA, Long.class),
    Collections.singletonList(stockKey),
    "1"
);
```

4. **常见原因**

| 原因 | 解决方案 |
|------|----------|
| 未使用 Lua 脚本原子扣减 | 修改为 Lua 脚本扣减 |
| 多节点并发扣减未加锁 | 添加分布式锁 |
| MQ 重复消费 | 实现幂等消费 |

#### 4.2.4 订单创建失败

**排查步骤**：

1. **检查 MQ 消息**
```bash
# 查看 RocketMQ 控制台
http://localhost:8081

# 使用 mqadmin 查看消息
.\mqadmin queryMsgById -i <msgId> -n localhost:9876
```

2. **检查本地事务状态**
```bash
# 搜索事务相关日志
findstr "executeLocalTransaction" springboot/logs/application.log
```

3. **常见原因**

| 错误信息 | 可能原因 | 解决方案 |
|----------|----------|----------|
| `Transaction failed` | 本地事务执行失败 | 检查数据库连接 |
| `Duplicate order` | 订单号重复 | 使用 UUID 生成订单号 |
| `Goods not found` | 商品不存在 | 检查商品数据 |

### 4.3 性能问题排查

#### 4.3.1 响应时间过长

1. **检查 QPS 限流**
```bash
redis-cli zcard seckill:qps:1
```

2. **检查并发数**
```bash
redis-cli get seckill:processing:count
```

3. **检查数据库慢查询**
```sql
-- 开启慢查询日志
SHOW VARIABLES LIKE 'slow_query_log';

-- 查看慢查询
SHOW VARIABLES LIKE 'long_query_time';
```

#### 4.3.2 Redis CPU 占用高

1. **检查大 Key**
```bash
redis-cli --bigkeys
```

2. **检查热 Key**
```bash
redis-cli --hotkeys
```

3. **优化建议**

| 问题 | 解决方案 |
|------|----------|
| QPS Key 过多 | 减少限流 key 数量 |
| 大 Key 操作 | 将大 Key 拆分 |
| 热 Key 集中 | 增加 Redis 集群节点 |

---

## 五、应急处理

### 5.1 服务宕机应急

#### 5.1.1 Redis 宕机

**表现**：抢购接口返回 `Redis connection failed`

**处理步骤**：
```bash
# 1. 重启 Redis
redis-server --daemonize yes

# 2. 验证连接
redis-cli ping

# 3. 检查库存数据
redis-cli get seckill:stock:1

# 4. 如数据丢失，从数据库恢复
# （需要后端支持库存预热接口）
```

#### 5.1.2 RocketMQ 宕机

**表现**：订单创建失败，日志显示 `MQ connection failed`

**处理步骤**：
```bash
# 1. 重启 RocketMQ NameServer
# 2. 重启 RocketMQ Broker
# 3. 检查消息积压
.\mqadmin msgCount -n localhost:9876 -t seckill_order_create

# 4. 处理积压消息
# （MQ 会自动重试，无需手动处理）
```

#### 5.1.3 MySQL 宕机

**表现**：接口返回 500，日志显示 `Communications link failure`

**处理步骤**：
```bash
# 1. 重启 MySQL
# 2. 检查连接
mysql -h localhost -u root -p123456 -e "SELECT 1;"

# 3. 检查数据完整性
mysql -h localhost -u root -p123456 -e "CHECK TABLE admin_system.seckill_order;"

# 4. 如有损坏，执行修复
mysql -h localhost -u root -p123456 -e "REPAIR TABLE admin_system.seckill_order;"
```

### 5.2 数据不一致应急

#### 5.2.1 Redis 与数据库库存不一致

**表现**：Redis 库存为 0，但数据库有库存；或相反

**处理步骤**：
```java
// 1. 查询数据库实际库存
int dbStock = goodsMapper.selectById(goodsId).getAvailableStock();

// 2. 查询 Redis 库存
int redisStock = Integer.parseInt(redisTemplate.opsForValue().get("seckill:stock:" + goodsId));

// 3. 以数据库为准，更新 Redis
redisTemplate.opsForValue().set("seckill:stock:" + goodsId, String.valueOf(dbStock));

// 4. 记录差异日志用于分析
log.warn("库存不一致已修复: goodsId={}, dbStock={}, redisStock={}", goodsId, dbStock, redisStock);
```

#### 5.2.2 订单状态与库存状态不一致

**表现**：订单已创建，但库存未扣减；或相反

**处理步骤**：
```sql
-- 1. 查询未同步的订单
SELECT o.* FROM seckill_order o
LEFT JOIN stock s ON o.goods_id = s.goods_id
WHERE o.status = 1 AND s.available_stock > 0;

-- 2. 查询库存已扣但无订单的记录
SELECT s.* FROM stock s
LEFT JOIN seckill_order o ON s.goods_id = o.goods_id
WHERE o.id IS NULL AND s.available_stock < s.total_stock;
```

### 5.3 限流/风控应急

#### 5.3.1 误限流

**表现**：正常用户被限流

**处理步骤**：
```bash
# 1. 检查限流配置
grep -A5 "limiter:" springboot/src/main/resources/application.yaml

# 2. 临时提高限流阈值（需重启）
# seckill:
#   limiter:
#     qps: 2000  # 临时提高

# 3. 清空限流计数器
redis-cli DEL seckill:qps:{goodsId}
```

#### 5.3.2 黑名单误封

**处理步骤**：
```bash
# 1. 查询黑名单
redis-cli KEYS "seckill:blacklist:*"

# 2. 移除误封 IP
redis-cli DEL seckill:blacklist:ip:192.168.1.100

# 3. 数据库黑名单移除
DELETE FROM ip_blacklist WHERE ip_address = '192.168.1.100';
```

---

## 六、监控与告警

### 6.1 核心监控指标

| 指标 | 正常范围 | 告警阈值 | 处理方式 |
|------|----------|----------|----------|
| QPS | < 800 | > 800 | 检查限流配置 |
| 抢购成功率 | > 90% | < 90% | 检查库存/风控 |
| SSE 在线数 | - | > 5000 | 扩容 |
| MQ 积压 | < 1000 | > 1000 | 增加消费者 |
| Redis CPU | < 70% | > 70% | 减少 key/扩容 |
| 数据库连接 | < 80% | > 80% | 连接池调优 |

### 6.2 日志关键字监控

```bash
# 监控错误日志
grep -E "ERROR|Exception|FAILED" springboot/logs/application.log

# 监控秒杀相关日志
grep "SeckillService" springboot/logs/application.log

# 监控 SSE 相关日志
grep "SseEmitter" springboot/logs/application.log

# 监控 MQ 相关日志
grep "RocketMQ" springboot/logs/application.log
```

### 6.3 日志规范

```java
// 结构化日志格式
log.info("秒杀请求|userId={}|goodsId={}|queueId={}|result={}|elapsed={}ms",
    userId, goodsId, queueId, result, elapsed);

// 关键路径埋点
log.info("开始扣减库存|goodsId={}|stockKey={}", goodsId, stockKey);
log.info("库存扣减成功|goodsId={}|remaining={}", goodsId, remaining);
log.info("发送MQ消息|queueId={}|topic={}", queueId, topic);
```

---

## 七、配置参考

### 7.1 application.yaml 核心配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000ms

rocketmq:
  name-server: localhost:9876
  producer:
    group: seckill_producer_group

seckill:
  limiter:
    qps: 1000          # 每秒允许请求数
    concurrency: 500   # 最大并发数
  lock:
    wait-time: 10      # 锁等待时间（秒）
    lease-time: 30     # 锁持有时间（秒）
  sse:
    timeout: 300000    # SSE 超时时间（毫秒）
```

### 7.2 Redis 重要配置

```bash
# 最大内存
maxmemory 2gb

# 淘汰策略
maxmemory-policy allkeys-lru

# 开启 AOF
appendonly yes
appendfsync everysec
```

---

## 八、版本历史

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-04-21 | 初始版本，包含架构、故障排查、应急处理 |

---

> 文档版本：1.0
> 最后更新：2026-04-21
