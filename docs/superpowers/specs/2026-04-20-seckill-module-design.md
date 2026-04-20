# 秒杀系统模块设计方案

> 基于 `2026-04-20-seckill-system-design.md` 规格说明的模块实现设计

## 概述

将秒杀系统集成到现有 vibe coding 项目（springboot + app-vue），采用 Spring Boot + RocketMQ + Redis 技术栈。

---

## 项目结构

```
vibe coding/
├── springboot/                              # 后端
│   └── src/main/java/cn/coderstory/springboot/
│       ├── seckill/                         # 秒杀模块
│       │   ├── controller/
│       │   │   ├── SeckillController.java   # 抢购接口
│       │   │   ├── ActivityController.java   # 活动管理
│       │   │   └── ReservationController.java # 预约管理
│       │   ├── service/
│       │   │   ├── SeckillService.java      # 抢购核心
│       │   │   ├── ActivityService.java     # 活动服务
│       │   │   ├── SignService.java         # 签名服务
│       │   │   └── PreheatService.java      # 数据预热
│       │   ├── dto/
│       │   │   ├── SeckillRequest.java
│       │   │   └── SeckillResponse.java
│       │   ├── entity/
│       │   │   ├── SeckillActivity.java
│       │   │   ├── SeckillGoods.java
│       │   │   ├── SeckillQueue.java
│       │   │   ├── SeckillOrder.java
│       │   │   ├── SeckillReservation.java
│       │   │   ├── SeckillLog.java
│       │   │   └── IpBlacklist.java
│       │   └── config/
│       │       └── SeckillConfig.java
│       │
│       ├── order/                           # 订单模块
│       │   ├── controller/
│       │   │   ├── OrderController.java
│       │   │   └── CartController.java
│       │   ├── service/
│       │   │   ├── OrderService.java
│       │   │   ├── CartService.java
│       │   │   └── TimeoutCancelService.java # 超时取消
│       │   └── entity/
│       │       ├── Order.java
│       │       └── Cart.java
│       │
│       ├── stock/                           # 库存模块
│       │   ├── service/
│       │   │   ├── StockService.java
│       │   │   └── ReconcileService.java   # 对账服务
│       │   ├── consumer/
│       │   │   └── StockConsumer.java      # 库存消息消费
│       │   └── entity/
│       │       └── Stock.java
│       │
│       ├── mq/                              # 消息队列
│       │   └── producer/
│       │       └── OrderTransactionProducer.java # 事务消息
│       │
│       ├── monitor/                         # 监控模块
│       │   ├── controller/
│       │   │   └── MonitorController.java
│       │   └── service/
│       │       └── MonitorService.java
│       │
│       ├── limiter/                         # 限流器
│       │   ├── QpsLimiter.java
│       │   ├── ConcurrencyLimiter.java
│       │   └── IpRateLimiter.java
│       │
│       └── security/                        # 风控
│           ├── BlacklistService.java
│           └── IdempotentService.java
│
└── app-vue/                                 # 前端
    └── src/
        ├── api/
        │   ├── seckill.ts
        │   ├── activity.ts
        │   ├── order.ts
        │   ├── cart.ts
        │   └── monitor.ts
        │
        └── views/
            ├── seckill/
            │   ├── SeckillIndex.vue         # 秒杀首页
            │   ├── SeckillDetail.vue        # 商品详情
            │   ├── SeckillRecord.vue        # 抢购记录
            │   └── SeckillCart.vue          # 秒杀购物车
            ├── order/
            │   ├── OrderConfirm.vue         # 订单确认
            │   └── OrderList.vue            # 订单列表
            └── monitor/
                └── MonitorDashboard.vue     # 监控大盘
```

---

## 数据库迁移（Flyway）

当前最新版本：V7

| 版本 | 文件 | 说明 |
|------|------|------|
| V8 | `seckill_activity.sql` | 秒杀活动表 |
| V9 | `seckill_goods.sql` | 秒杀商品表 |
| V10 | `seckill_queue.sql` | 秒杀排队表 |
| V11 | `seckill_order.sql` | 秒杀订单表 |
| V12 | `seckill_reservation.sql` | 预约表 |
| V13 | `stock.sql` | 库存表 |
| V14 | `cart.sql` | 购物车表 |
| V15 | `ip_blacklist.sql` | IP黑名单表 |
| V16 | `seckill_log.sql` | 请求日志表 |

---

## 模块职责

### seckill（秒杀模块）
- 抢购入口、签名生成/校验
- 活动管理、预约管理
- 数据预热（活动/库存/预约用户）

### order（订单模块）
- 订单创建、支付、取消
- 购物车管理
- 超时取消定时任务

### stock（库存模块）
- 库存扣减、回滚
- 对账定时任务
- MQ 消息消费

### mq（消息队列）
- RocketMQ 事务消息生产者
- 保证订单与库存一致性

### monitor（监控模块）
- 实时 QPS、成功率、库存监控
- 告警中心

### limiter（限流器）
- QPS 限流（滑动窗口）
- 并发限制
- IP 限流

### security（风控）
- 黑名单检测
- 幂等保证

---

## 模块调用关系

```
用户请求
    │
    ▼
┌─────────────────────────────────────────┐
│         SeckillController               │
│  限流检查 → 签名校验 → 幂等检查         │
└─────────────────────────────────────────┘
    │                        │
    ▼                        ▼ (MQ半消息)
┌─────────┐          ┌─────────────────┐
│  Redis  │          │ OrderService    │
│  原子扣减│          │ (本地事务)       │
└─────────┘          └─────────────────┘
                            │
                            ▼ (COMMITTED)
                     ┌─────────────────┐
                     │ StockConsumer   │
                     │ (扣减数据库库存) │
                     └─────────────────┘
```

---

## 技术栈

| 组件 | 技术 |
|------|------|
| 后端框架 | Spring Boot 4.0.5 + Java 21 |
| 消息队列 | RocketMQ 5.3.2 |
| 缓存 | Redis 8.0+ |
| 数据库 | MySQL 8.0+ |
| 前端 | Vue 3.5 + Vite 8 |
| 数据库迁移 | Flyway |

---

## 核心实现要点

### 1. Redis 原子扣库存
使用 Lua 脚本保证原子性，防止超卖

### 2. RocketMQ 事务消息
半消息机制保证本地事务与消息一致性

### 3. 三层防护
Redis 原子扣减 → RocketMQ 削峰 → 数据库乐观锁

### 4. SSE 实时推送
抢购结果通过 Server-Sent Events 异步通知用户

### 5. 数据预热
活动开始前 5 分钟将库存/活动信息预热到 Redis

---

> 设计版本：1.0
> 创建日期：2026-04-20
