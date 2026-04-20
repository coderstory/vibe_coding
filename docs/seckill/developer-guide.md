# 秒杀系统开发手册

## 1. 系统架构

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户请求                                 │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                      第一层：Redis限流                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │  QPS限流器   │  │ 并发限流器    │  │  IP限流器    │            │
│  │              │  │              │  │              │            │
│  │ 滑动窗口算法 │  │ 信号量控制    │  │ 令牌桶算法   │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                      第二层：风控服务                             │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │  黑名单服务  │  │ 幂等性服务    │                            │
│  │              │  │              │                            │
│  │ Redis Set    │  │ Redis String │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                      第三层：秒杀服务                             │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │ 签名服务     │  │ 秒杀服务     │                            │
│  │              │  │              │                            │
│  │ MD5签名验证  │  │ Redis原子扣减 │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                 第四层：RocketMQ消息队列                         │
│  ┌──────────────────────────────────────────────────┐           │
│  │              事务消息处理                         │           │
│  │  ┌────────────────┐  ┌────────────────┐        │           │
│  │  │ 生产者          │  │ 事务监听器      │        │           │
│  │  │                 │  │                 │        │           │
│  │  │ 发送半消息      │  │ 执行本地事务    │        │           │
│  │  │ 执行本地事务    │  │ 确认/回滚      │        │           │
│  │  └────────────────┘  └────────────────┘        │           │
│  └──────────────────────────────────────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
                                ↓
┌─────────────────────────────────────────────────────────────────┐
│                      第五层：数据库                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │
│  │ 订单服务     │  │ 库存服务     │  │ 活动服务     │            │
│  │              │  │              │  │              │            │
│  │ 乐观锁保障   │  │ 乐观锁保障   │  │ 状态管理     │            │
│  └──────────────┘  └──────────────┘  └──────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 4.0.5 |
| 数据库 | MySQL | 8.0+ |
| ORM框架 | MyBatis Plus | 最新 |
| 缓存 | Redis | 8.0+ |
| 消息队列 | RocketMQ | 5.3.2 |
| 前端框架 | Vue 3 | 3.5 |
| 构建工具 | Vite | 8 |

## 2. 核心模块说明

### 2.1 限流模块 (limiter)

**位置：** `cn.coderstory.springboot.limiter`

**组件：**

| 类名 | 功能 | 算法 |
|------|------|------|
| QpsLimiter | QPS限流 | 滑动窗口 |
| ConcurrencyLimiter | 并发数限流 | 信号量 |
| IpRateLimiter | IP限流 | 令牌桶 |

**源码位置：**
- `springboot/src/main/java/cn/coderstory/springboot/limiter/QpsLimiter.java`
- `springboot/src/main/java/cn/coderstory/springboot/limiter/ConcurrencyLimiter.java`
- `springboot/src/main/java/cn/coderstory/springboot/limiter/IpRateLimiter.java`

### 2.2 风控模块 (security)

**位置：** `cn.coderstory.springboot.security`

**组件：**

| 类名 | 功能 | 数据结构 |
|------|------|----------|
| BlacklistService | 黑名单管理 | Redis Set |
| IdempotentService | 幂等性保证 | Redis String |

**源码位置：**
- `springboot/src/main/java/cn/coderstory/springboot/security/BlacklistService.java`
- `springboot/src/main/java/cn/coderstory/springboot/security/IdempotentService.java`

### 2.3 秒杀模块 (seckill)

**位置：** `cn.coderstory.springboot.seckill`

**组件：**

| 类名 | 功能 | 职责 |
|------|------|------|
| SignService | 签名服务 | 生成和验证请求签名 |
| SeckillService | 秒杀服务 | 核心秒杀逻辑 |
| ActivityService | 活动服务 | 活动管理 |
| PreheatService | 预热服务 | 活动预热和缓存预加载 |

**源码位置：**
- `springboot/src/main/java/cn/coderstory/springboot/seckill/service/SignService.java`
- `springboot/src/main/java/cn/coderstory/springboot/seckill/service/SeckillService.java`
- `springboot/src/main/java/cn/coderstory/springboot/seckill/service/ActivityService.java`
- `springboot/src/main/java/cn/coderstory/springboot/seckill/service/PreludeService.java`

### 2.4 库存模块 (stock)

**位置：** `cn.coderstory.springboot.stock`

**组件：**

| 类名 | 功能 | 原理 |
|------|------|------|
| StockService | 库存服务 | Redis原子扣减 + 数据库乐观锁 |
| StockConsumer | 库存消费者 | RocketMQ消息处理 |

**源码位置：**
- `springboot/src/main/java/cn/coderstory/springboot/stock/service/StockService.java`
- `springboot/src/main/java/cn/coderstory/springboot/stock/consumer/StockConsumer.java`

### 2.5 消息队列模块 (mq)

**位置：** `cn.coderstory.springboot.mq`

**组件：**

| 类名 | 功能 | 说明 |
|------|------|------|
| OrderTransactionProducer | 订单事务生产者 | 发送RocketMQ事务消息 |
| OrderTransactionListener | 订单事务监听器 | 执行本地事务和状态回查 |

**源码位置：**
- `springboot/src/main/java/cn/coderstory/springboot/mq/producer/OrderTransactionProducer.java`
- `springboot/src/main/java/cn/coderstory/springboot/mq/consumer/OrderTransactionListener.java`

## 3. 数据库表结构

### 3.1 秒杀活动表 (seckill_activity)

```sql
CREATE TABLE seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description TEXT COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未开始 1-进行中 2-已结束',
    per_limit INT DEFAULT 1 COMMENT '每人限购数量',
    total_stock INT DEFAULT 0 COMMENT '总库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3.2 秒杀商品表 (seckill_goods)

```sql
CREATE TABLE seckill_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    original_price DECIMAL(10,2) COMMENT '原价',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    stock INT DEFAULT 0 COMMENT '库存',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3.3 订单表 (seckill_order)

```sql
CREATE TABLE seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    quantity INT DEFAULT 1 COMMENT '购买数量',
    price DECIMAL(10,2) NOT NULL COMMENT '成交价格',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已取消 3-超时取消',
    queue_id VARCHAR(64) COMMENT '队列ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3.4 库存表 (stock)

```sql
CREATE TABLE stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    available_stock INT DEFAULT 0 COMMENT '可用库存',
    locked_stock INT DEFAULT 0 COMMENT '锁定库存',
    version INT DEFAULT 0 COMMENT '乐观锁版本',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3.5 预约表 (seckill_reservation)

```sql
CREATE TABLE seckill_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    reserve_time DATETIME NOT NULL COMMENT '预约时间',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-有效 1-已通知 2-已过期',
    notified TINYINT DEFAULT 0 COMMENT '是否已通知',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3.6 排队表 (seckill_queue)

```sql
CREATE TABLE seckill_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '队列ID',
    queue_id VARCHAR(64) NOT NULL UNIQUE COMMENT '队列编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-排队中 1-处理中 2-成功 3-失败',
    result TEXT COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 4. API接口文档

### 4.1 秒杀接口

#### 抢购接口
```
POST /api/seckill/buy
Headers:
  X-User-Id: {userId}

Request Body:
{
    "goodsId": 1,
    "activityId": 1,
    "sign": "xxx",
    "timestamp": 1713600000000,
    "idempotentKey": "xxx"
}

Response:
{
    "code": 200,
    "message": "success",
    "data": {
        "queueId": "xxx",
        "status": 0,
        "message": "排队中"
    }
}
```

#### 查询结果
```
GET /api/seckill/result/{queueId}

Response:
{
    "code": 200,
    "message": "success",
    "data": {
        "queueId": "xxx",
        "status": 1,
        "message": "抢购成功"
    }
}
```

### 4.2 活动接口

#### 获取活动详情
```
GET /api/activity/{id}

Response:
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "name": "秒杀活动",
        "description": "活动描述",
        "startTime": "2026-04-20 10:00:00",
        "endTime": "2026-04-20 12:00:00",
        "status": 1,
        "perLimit": 1
    }
}
```

#### 开启活动
```
POST /api/activity/{id}/start

Response:
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 4.3 订单接口

#### 我的订单
```
GET /api/order/my
Headers:
  X-User-Id: {userId}

Response:
{
    "code": 200,
    "message": "success",
    "data": [...]
}
```

#### 支付订单
```
POST /api/order/{orderNo}/pay

Response:
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 4.4 监控接口

#### 获取监控指标
```
GET /api/monitor/metrics

Response:
{
    "code": 200,
    "message": "success",
    "data": {
        "concurrentCount": 10,
        "qpsKeys": 100,
        "timestamp": 1713600000000
    }
}
```

## 5. 配置说明

### 5.1 Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 3000ms
```

### 5.2 RocketMQ配置

```yaml
rocketmq:
  name-server: localhost:9876
  producer:
    group: seckill_producer_group
```

### 5.3 限流配置

```yaml
seckill:
  limiter:
    qps: 1000          # 每秒允许请求数
    concurrency: 500   # 最大并发数
    ip-rate: 100       # 每IP每秒请求数
```

## 6. 部署指南

### 6.1 环境要求

- JDK 21+
- Node.js 20+
- MySQL 8.0+
- Redis 8.0+
- RocketMQ 5.3.2

### 6.2 后端部署

```bash
cd springboot
.\mvnw.cmd package -DskipTests
java -jar target/springboot-0.0.1-SNAPSHOT.jar
```

### 6.3 前端部署

```bash
cd app-vue
npm run build
```

构建产物在 `dist` 目录。

### 6.4 数据库迁移

Flyway自动执行，迁移脚本位置：
```
springboot/src/main/resources/db/migration/
```

---

**版本：** 1.0.0
**更新日期：** 2026-04-20