# 秒杀系统设计规格说明

> 这是一个完整的秒杀系统设计文档，包含详细的业务逻辑和原理说明，用于学习参考。

## 概述

一个完整的秒杀系统，支持高并发场景下的商品限时抢购，采用微服务架构。

### 核心特性

- 单商品限时秒杀（先到先得）
- 三层防护：Redis原子扣减 → RocketMQ削峰 → 数据库乐观锁
- RocketMQ 半消息（事务消息）保证最终一致性
- 异步通知（SSE实时推送）
- 微服务架构：订单服务 + 库存服务
- 购物车流程
- 安全风控：验证码、IP限流、黑名单
- 运营功能：预约、活动提醒、排行榜
- 完整幂等保证

---

## 第一章：系统架构

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                         用户端                                  │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐              │
│  │秒杀列表 │→ │商品详情 │→ │购物车   │→ │订单列表 │              │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘              │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                      API 网关（Zuul/Gateway）                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │限流器   │  │签名校验  │  │风控检测  │  │路由转发  │              │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      秒杀服务 (Seckill Service)                   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │活动管理  │  │抢购接口  │  │预约管理  │  │签名服务  │              │
│  │          │  │QPS限制  │  │资格校验  │  │          │              │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘              │
└─────────────────────────────────────────────────────────────────┘
                    ↓                          ↓
         ┌─────────────────┐        ┌─────────────────┐
         │   订单服务      │        │   库存服务      │
         │ (Order Service) │        │ (Stock Service) │
         │                 │        │                 │
         │  ·购物车管理    │   MQ   │  ·库存预占     │
         │  ·订单创建    │←──────→│  ·库存扣减     │
         │  ·订单支付    │  半消息 │  ·库存回滚     │
         │  ·订单查询    │        │  ·库存对账     │
         └─────────────────┘        └─────────────────┘
                    ↓
         ┌─────────────────────────────────────────┐
         │            消息队列 (RocketMQ)            │
         │  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
         │  │seckill │  │ order  │  │  stock  │  │
         │  │ order  │  │  pay   │  │  sync   │  │
         │  └─────────┘  └─────────┘  └─────────┘  │
         └─────────────────────────────────────────┘
                              ↓
         ┌─────────────────────────────────────────┐
         │            缓存层 (Redis)               │
         │  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
         │  │库存缓存 │  │排队状态 │  │QPS计数  │  │
         │  │限流器  │  │签名记录 │  │黑名单   │  │
         │  └─────────┘  └─────────┘  └─────────┘  │
         └─────────────────────────────────────────┘
                              ↓
         ┌─────────────────────────────────────────┐
         │            数据库层 (MySQL)              │
         │  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
         │  │秒杀活动 │  │ 订单表  │  │ 库存表  │  │
         │  │秒杀商品 │  │购物车表 │  │ 用户表  │  │
         │  └─────────┘  └─────────┘  └─────────┘  │
         └─────────────────────────────────────────┘
```

### 1.2 核心设计原则

#### 1.2.1 缓存思想

**原理**：在秒杀系统中，数据库是最慢的存储，Redis 是最快的。设计时应遵循"读写分离"原则。

```
读请求 → Redis（有则直接返回）
              ↓（无）
           数据库（查询后写入缓存）

写请求 → 数据库事务
              ↓
         写入缓存保证一致性
```

**实现要点**：
1. 活动信息、库存数量预热到 Redis
2. 抢购时先扣 Redis 库存，异步同步数据库
3. 热点数据使用 Redis 集群保证高可用

#### 1.2.2 异步化思想

**原理**：同步操作慢，异步操作快。将非核心路径异步化，减少用户等待时间。

```
同步路径：用户点击 → 等待处理 → 返回结果（慢）
异步路径：用户点击 → 立即返回 → 后台处理 → SSE通知（快）
```

**实现要点**：
1. 抢购请求入队，立即返回排队ID
2. 后台异步处理订单
3. SSE/WebSocket 推送结果

#### 1.2.3 削峰填谷

**原理**：瞬时流量大，但处理能力有限。用消息队列平滑流量。

```
瞬时流量：|████████|        10000 QPS
处理能力：|----|----|----|----|----|  2000 QPS
队列缓冲： 队列缓冲区，平滑处理
```

---

## 第二章：业务流程

### 2.1 完整用户流程

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            用户完整购物流程                                │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [1. 预约阶段]                                                           │
│      ↓                                                                  │
│  查看秒杀预告 → 预约秒杀 → 收到开始提醒                                    │
│                                                                          │
│  [2. 抢购阶段]                                                           │
│      ↓                                                                  │
│  活动开始 → 获取秒杀资格（签名） → 点击抢购 → 排队中... → SSE通知          │
│      ↓                                    ↓                             │
│  抢购成功                          抢购失败（返回库存不足）                │
│      ↓                                                                  │
│  [3. 支付阶段]                                                           │
│      ↓                                                                  │
│  加入购物车 → 生成订单 → 库存校验 → 支付订单                              │
│      ↓（15分钟未支付）                                                  │
│  自动取消，回滚库存                                                     │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### 2.2 抢购时序图

```
┌─────┐    ┌─────┐    ┌─────┐    ┌─────┐    ┌─────┐
│用户 │    │ API │    │Redis│    │ MQ  │    │DB   │
└──┬──┘    └──┬──┘    └──┬──┘    └──┬──┘    └──┬──┘
   │          │          │          │          │
   │ 1.获取资格│          │          │          │
   │──────────→│          │          │          │
   │          │ 2.校验资格│          │          │
   │          │──────────→│          │          │
   │          │ 3.生成签名│          │          │
   │          │──────────→│          │          │
   │ 4.返回签名│          │          │          │
   │←─────────│          │          │          │
   │          │          │          │          │
   │ 5.抢购请求│          │          │          │
   │──────────→│          │          │          │
   │          │ 6.校验签名│          │          │
   │          │──────────→│          │          │
   │          │          │ 7.原子扣库存│          │
   │          │          │───────────→│          │
   │          │          │ 8.返回结果│          │
   │          │←─────────│          │          │
   │          │          │          │ 9.发送半消息│
   │          │          │          │───────────→
   │          │ 10.返回排队ID│          │
   │←─────────│          │          │          │
   │          │          │          │11.执行本地事务│
   │          │          │          │───────────→│
   │          │          │          │12.更新订单状态│
   │          │          │          │←───────────│
   │          │          │          │13.提交/回滚 │
   │          │          │          │14.同步库存  │
   │          │          │          │───────────→│
   │          │          │          │15.SSE通知   │
   │ 16.SSE通知│          │          │←───────────│
   │←─────────│          │          │          │
   │          │          │          │          │
```

### 2.3 MQ 半消息事务流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    RocketMQ 事务消息流程                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  Producer（订单服务）                                             │
│       │                                                           │
│       ├──→ 发送半消息（PREPARED）                                 │
│       │                                                           │
│       ├── 执行本地事务（创建订单）                                 │
│       │                                                           │
│       ├── 提交/回滚消息（COMMITTED/ROLLBACK）                      │
│       │                                                           │
│       └── 等待MQ回调确认                                          │
│                                                                   │
│  MQ Broker                                                        │
│       │                                                           │
│       ├── 存储半消息                                              │
│       │                                                           │
│       ├── 向Producer确认事务状态                                  │
│       │                                                           │
│       └── 投递消息给Consumer                                       │
│                                                                   │
│  Consumer（库存服务）                                             │
│       │                                                           │
│       ├── 接收消息                                                │
│       │                                                           │
│       ├── 处理库存扣减                                            │
│       │                                                           │
│       └── 返回消费成功                                            │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

**为什么使用半消息？**

1. **本地事务与消息一致性**：如果本地事务失败，消息不会被消费
2. **防止超卖**：库存扣减和订单创建在同一个事务中
3. **消息可靠性**：消息一定会被投递（除非事务回滚）

---

## 第三章：数据库设计

### 3.1 ER图

```
┌─────────────────┐       ┌─────────────────┐
│  seckill_activity│       │   seckill_goods │
├─────────────────┤       ├─────────────────┤
│ id (PK)         │──1:N──│ activity_id (FK) │
│ name            │       │ id (PK)         │
│ start_time      │       │ name            │
│ end_time        │       │ original_price  │
│ status          │       │ seckill_price  │
└─────────────────┘       │ stock          │
                          │ sold           │
                          └─────────────────┘
                                 │
                                 │ 1:N
                                 ↓
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   seckill_order │       │  seckill_queue  │       │     stock       │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id (PK)         │       │ queue_id (PK)   │       │ goods_id (PK)   │
│ order_no        │       │ user_id (FK)   │       │ total_stock    │
│ user_id (FK)   │──N:1─→│ goods_id (FK)  │       │ available_stock│
│ goods_id (FK)   │       │ status         │       │ locked_stock   │
│ price           │       │ create_time    │       │ version        │
│ status          │       └─────────────────┘       └─────────────────┘
│ queue_id (FK)   │
│ ...             │       ┌─────────────────┐
└─────────────────┘       │     cart        │
                        ├─────────────────┤
                        │ id (PK)         │
                        │ user_id (FK)    │
                        │ goods_id (FK)   │
                        │ quantity        │
                        │ create_time     │
                        └─────────────────┘
```

### 3.2 表结构详解

#### 3.2.1 秒杀活动表

```sql
-- 秒杀活动表
-- 作用：管理秒杀活动的基本信息和时间范围

CREATE TABLE seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',

    -- 基本信息
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',

    -- 时间信息（核心：活动窗口）
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',

    -- 状态：0-未开始 1-进行中 2-已结束
    status TINYINT DEFAULT 0 COMMENT '活动状态',

    -- 限购规则
    per_limit INT DEFAULT 1 COMMENT '每人限购数量',

    -- 风控设置
    enable_captcha BOOLEAN DEFAULT TRUE COMMENT '是否启用验证码',
    enable_ip_limit BOOLEAN DEFAULT TRUE COMMENT '是否启用IP限制',

    -- 签名密钥（用于接口加密）
    sign_key VARCHAR(64) COMMENT '活动签名密钥',

    -- 审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) COMMENT='秒杀活动表';
```

**设计原理**：
- `start_time/end_time`：控制活动窗口，只有在窗口内才能抢购
- `sign_key`：每个活动有独立的签名密钥，防止跨活动刷单
- `per_limit`：每人限购数量

#### 3.2.2 秒杀商品表

```sql
-- 秒杀商品表
-- 作用：管理秒杀商品信息

CREATE TABLE seckill_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',

    -- 关联活动
    activity_id BIGINT NOT NULL COMMENT '所属活动ID',

    -- 商品信息
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    image_url VARCHAR(500) COMMENT '商品图片',

    -- 价格体系
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',

    -- 库存信息（核心：库存是秒杀的命脉）
    total_stock INT NOT NULL COMMENT '总库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    locked_stock INT DEFAULT 0 COMMENT '已锁定库存(预占)',
    sold_count INT DEFAULT 0 COMMENT '已售数量',

    -- 版本号（乐观锁）
    version INT DEFAULT 0 COMMENT '版本号',

    -- 审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (activity_id) REFERENCES seckill_activity(id)
) COMMENT='秒杀商品表';
```

**设计原理**：
- `total_stock/available_stock/locked_stock` 三级库存体系：
  - `total_stock`：不变，记录总库存
  - `available_stock`：实时可用库存
  - `locked_stock`：预占库存（用户抢购成功但未支付）
- `version`：乐观锁，防止并发更新

#### 3.2.3 秒杀排队表

```sql
-- 秒杀排队记录表
-- 作用：记录用户的抢购排队状态

CREATE TABLE seckill_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- 排队唯一标识
    queue_id VARCHAR(64) NOT NULL UNIQUE COMMENT '排队ID(UUID)',

    -- 关联信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    order_id BIGINT COMMENT '关联订单ID',

    -- 状态：0-排队中 1-成功 2-失败
    status TINYINT DEFAULT 0 COMMENT '状态',
    fail_reason VARCHAR(200) COMMENT '失败原因',

    -- 签名信息
    sign VARCHAR(128) NOT NULL COMMENT '请求签名',
    sign_expire_time DATETIME NOT NULL COMMENT '签名过期时间',

    -- 幂等键
    idempotent_key VARCHAR(64) NOT NULL UNIQUE COMMENT '幂等键',

    -- 时间戳
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 复合索引加速查询
    INDEX idx_user_goods (user_id, goods_id),
    INDEX idx_status (status),
    INDEX idx_idempotent (idempotent_key)
) COMMENT='秒杀排队记录表';
```

**设计原理**：
- `queue_id`：唯一标识，用于SSE推送
- `idempotent_key`：`userId + goodsId + timestamp`，保证幂等
- `sign`：签名验证请求合法性

#### 3.2.4 秒杀订单表

```sql
-- 秒杀订单表
-- 作用：记录秒杀生成的订单

CREATE TABLE seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- 订单号（业务ID）
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',

    -- 关联信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    queue_id VARCHAR(64) COMMENT '排队ID',

    -- 商品快照
    goods_name VARCHAR(200) NOT NULL COMMENT '商品名称(快照)',
    goods_image VARCHAR(500) COMMENT '商品图片(快照)',

    -- 金额
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价(快照)',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价(快照)',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',

    -- 数量
    quantity INT DEFAULT 1 COMMENT '购买数量',

    -- 状态机
    -- 0-待支付 1-已支付 2-已取消 3-已超时 4-已退款
    status TINYINT DEFAULT 0 COMMENT '订单状态',

    -- 支付信息
    pay_time DATETIME COMMENT '支付时间',
    pay_method VARCHAR(20) COMMENT '支付方式',

    -- MQ事务状态
    mq_status TINYINT DEFAULT 0 COMMENT 'MQ事务状态: 0-未确认 1-已确认 2-已回滚',

    -- 审计字段
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 索引
    INDEX idx_user (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='秒杀订单表';
```

**设计原理**：
- `goods_name/goods_image`：商品快照，避免商品信息变更影响订单
- `mq_status`：事务消息状态，确保订单和库存一致性
- `order_no`：业务订单号，方便对账

#### 3.2.5 秒杀请求日志表

```sql
-- 秒杀请求日志表（分析用）
-- 作用：记录所有秒杀请求，用于数据分析和风控

CREATE TABLE seckill_request_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- 请求标识
    request_id VARCHAR(64) NOT NULL COMMENT '请求ID',
    queue_id VARCHAR(64) COMMENT '排队ID',

    -- 用户信息
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent TEXT COMMENT '浏览器UA',

    -- 商品信息
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',

    -- 请求信息
    request_time DATETIME NOT NULL COMMENT '请求时间',
    response_time DATETIME COMMENT '响应时间',
    elapsed_ms INT COMMENT '耗时(毫秒)',

    -- 结果
    result TINYINT COMMENT '结果: 0-排队中 1-成功 2-库存不足 3-签名无效 4-已限购',
    fail_reason VARCHAR(200) COMMENT '失败原因',

    -- 风控信息
    captcha_verified BOOLEAN DEFAULT FALSE COMMENT '验证码是否通过',
    ip_blacklisted BOOLEAN DEFAULT FALSE COMMENT '是否IP黑名单',

    -- 签名信息
    sign VARCHAR(128) COMMENT '请求签名',

    -- 幂等键
    idempotent_key VARCHAR(64) COMMENT '幂等键',

    -- 时间戳索引
    INDEX idx_time (request_time),
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id),
    INDEX idx_result (result)
) COMMENT='秒杀请求日志表';
```

#### 3.2.6 预约表

```sql
-- 秒杀预约表
-- 作用：用户预约秒杀活动，活动开始前提醒

CREATE TABLE seckill_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',

    -- 预约时间
    reserve_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    -- 预约状态
    status TINYINT DEFAULT 0 COMMENT '状态: 0-预约 1-已提醒 2-已过期',

    -- 通知记录
    notified BOOLEAN DEFAULT FALSE COMMENT '是否已发送提醒',
    notify_time DATETIME COMMENT '提醒时间',

    -- 唯一约束：每人每个活动只能预约一次
    UNIQUE KEY uk_user_activity (user_id, activity_id),

    INDEX idx_activity (activity_id),
    INDEX idx_notify (status, notified)
) COMMENT='秒杀预约表';
```

#### 3.2.7 IP黑名单表

```sql
-- IP黑名单表
-- 作用：封禁恶意IP

CREATE TABLE ip_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    ip_address VARCHAR(50) NOT NULL UNIQUE COMMENT 'IP地址',
    reason VARCHAR(200) COMMENT '封禁原因',
    expire_time DATETIME COMMENT '过期时间(NULL表示永久)',

    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_expire (expire_time)
) COMMENT='IP黑名单';
```

### 3.3 库存服务表

```sql
-- 库存表（库存服务独立）
CREATE TABLE stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL UNIQUE COMMENT '商品ID',

    -- 库存三级分类
    total_stock INT NOT NULL COMMENT '总库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    locked_stock INT DEFAULT 0 COMMENT '已锁定库存',

    -- 乐观锁版本号
    version INT DEFAULT 0,

    -- MQ消息对账
    mq_deduct_count INT DEFAULT 0 COMMENT 'MQ已扣减次数',
    mq_rollback_count INT DEFAULT 0 COMMENT 'MQ已回滚次数',

    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='库存表';
```

---

## 第四章：Redis设计

### 4.1 Key设计规范

| Key模式 | 类型 | 说明 | TTL |
|---------|------|------|-----|
| `seckill:stock:{goodsId}` | String | 商品库存 | 活动结束 |
| `seckill:queue:{queueId}` | Hash | 排队状态 | 30分钟 |
| `seckill:user:{userId}:{goodsId}` | String | 用户限购标记 | 活动结束 |
| `seckill:sign:{sign}` | String | 签名使用记录 | 5分钟 |
| `seckill:qps:{goodsId}` | String | QPS计数器 | 1秒 |
| `seckill:concurrent` | String | 并发计数器 | - |
| `seckill:blacklist:ip:{ip}` | String | IP黑名单 | 动态 |
| `seckill:reservation:{activityId}` | Set | 预约用户集合 | 活动结束 |

### 4.2 Lua脚本：原子扣库存

```lua
--[[
    名称：原子扣库存脚本
    原理：利用Redis单线程保证原子性
    参数：
        KEYS[1] = 库存Key
        ARGV[1] = 扣减数量
    返回：
        1 = 成功，返回剩余库存
        0 = 库存不足
        -1 = Key不存在
]]

-- 检查Key是否存在
local stock = redis.call('GET', KEYS[1])
if not stock then
    return -1
end

-- 库存不足，直接返回
if tonumber(stock) < tonumber(ARGV[1]) then
    return 0
end

-- 扣减库存
local remaining = redis.call('DECRBY', KEYS[1], ARGV[1])

-- 返回剩余库存
return remaining
```

### 4.3 Lua脚本：QPS限流

```lua
--[[
    名称：滑动窗口限流
    原理：使用ZSet实现滑动窗口
    参数：
        KEYS[1] = 限流Key
        ARGV[1] = 当前时间戳(毫秒)
        ARGV[2] = 窗口大小(毫秒)
        ARGV[3] = 最大请求数
]]

local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local limit = tonumber(ARGV[3])

-- 删除窗口外的数据
local old = now - window
redis.call('ZREMRANGEBYSCORE', key, '-inf', old)

-- 统计当前窗口内请求数
local count = redis.call('ZCARD', key)

if count >= limit then
    return 0  -- 限流
end

-- 添加新请求
redis.call('ZADD', key, now, now .. ':' .. math.random())

-- 设置过期时间
redis.call('PEXPIRE', key, window)

return 1  -- 通过
```

### 4.4 签名服务设计

```java
/**
 * 签名服务
 *
 * 原理：
 * 1. HMAC-SHA256生成签名
 * 2. Redis记录签名使用状态
 * 3. 一次性使用，防止重放攻击
 */
@Service
public class SignService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成签名
     *
     * @param userId 用户ID
     * @param goodsId 商品ID
     * @param activitySignKey 活动密钥
     * @return 签名信息
     */
    public SignResult generateSign(Long userId, Long goodsId, String activitySignKey) {
        long timestamp = System.currentTimeMillis();

        // 签名内容：userId + goodsId + timestamp
        String content = userId + ":" + goodsId + ":" + timestamp;
        String sign = HmacSHA256(content, activitySignKey);

        // 存储签名，5分钟过期
        String signKey = "seckill:sign:" + sign;
        redisTemplate.opsForValue().set(signKey, "1", 5, TimeUnit.MINUTES);

        return new SignResult(sign, timestamp);
    }

    /**
     * 验证签名
     *
     * @param sign 签名
     * @param timestamp 时间戳
     * @return 验证结果
     */
    public boolean verifySign(String sign, long timestamp) {
        // 检查签名是否已使用
        String signKey = "seckill:sign:" + sign;
        Boolean used = redisTemplate.hasKey(signKey);
        if (Boolean.TRUE.equals(used)) {
            return false;  // 签名已使用
        }

        // 检查时间戳是否过期(5分钟)
        if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
            return false;
        }

        return true;
    }
}
```

---

## 第五章：RocketMQ设计

### 5.1 Topic和Tag

| Topic | Tag | 说明 | 消息类型 |
|-------|-----|------|---------|
| `seckill-order` | `create` | 创建订单 | 事务消息 |
| `seckill-order` | `pay` | 支付确认 | 普通消息 |
| `seckill-order` | `timeout` | 超时取消 | 延迟消息 |
| `seckill-stock` | `deduct` | 扣减库存 | 普通消息 |
| `seckill-stock` | `rollback` | 回滚库存 | 普通消息 |
| `seckill-notify` | `email` | 邮件通知 | 普通消息 |
| `seckill-notify` | `sms` | 短信通知 | 普通消息 |

### 5.2 消息格式

#### 创建订单消息

```json
{
    "queueId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": 12345,
    "goodsId": 100,
    "activityId": 1,
    "quantity": 1,
    "seckillPrice": 99.00,
    "orderNo": "SK20260420120000001",
    "timestamp": 1713600000000
}
```

#### 超时取消消息

```json
{
    "orderId": 12345,
    "orderNo": "SK20260420120000001",
    "userId": 12345,
    "goodsId": 100,
    "quantity": 1,
    "delayTime": 900000,
    "timestamp": 1713600000000
}
```

### 5.3 事务消息实现

```java
/**
 * 订单服务 - 事务消息生产者
 *
 * 原理：
 * 1. 发送半消息到MQ
 * 2. 执行本地事务
 * 3. 提交或回滚消息
 *
 * 事务状态机：
 * START --> PREPARED --> COMMITTED/ROLLBACK
 */
@Service
public class OrderTransactionProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void createOrder(OrderCreateDTO dto) {
        // 1. 发送半消息
        TransactionSendResult result = rocketMQTemplate.sendMessageInTransaction(
            "seckill-order:create",
            MessageBuilder.withPayload(JSON.toJSONString(dto))
            .setHeader("queueId", dto.getQueueId())
            .build()
        );

        // 2. 本地事务会自动执行
        // 3. 根据本地事务结果提交或回滚
    }

    @RocketMQTransactionListener
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            OrderCreateDTO dto = JSON.parseObject(msg.getPayload(), OrderCreateDTO.class);

            // 执行本地事务：创建订单
            orderService.createOrderInTransaction(dto);

            // 提交消息
            return LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            log.error("本地事务执行失败", e);
            // 回滚消息
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }
}
```

### 5.4 库存服务消费者

```java
/**
 * 库存服务 - 消息消费者
 *
 * 原理：
 * 1. 接收MQ消息
 * 2. 执行库存扣减/回滚
 * 3. 保证幂等
 */
@Component
@Slf4j
public class StockConsumer {

    @Autowired
    private StockService stockService;

    @RocketMQMessageListener(
        topic = "seckill-stock",
        consumerGroup = "stock-consumer-group"
    )
    public void onMessage(String message) {
        try {
            StockMessage msg = JSON.parseObject(message, StockMessage.class);

            if ("deduct".equals(msg.getAction())) {
                stockService.deductStock(msg);
            } else if ("rollback".equals(msg.getAction())) {
                stockService.rollbackStock(msg);
            }
        } catch (Exception e) {
            log.error("处理库存消息失败", e);
            // 返回NACK，等待重试
            throw e;
        }
    }
}
```

---

## 第六章：安全设计

### 6.1 限流设计

#### 6.1.1 QPS限制（每秒1000请求）

**原理**：滑动窗口算法，控制瞬时并发

```java
@Service
public class QpsLimiter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int QPS_LIMIT = 1000;

    /**
     * 尝试获取令牌
     *
     * @param goodsId 商品ID
     * @return true=允许, false=限流
     */
    public boolean tryAcquire(Long goodsId) {
        String key = "seckill:qps:" + goodsId;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            // 设置1秒过期
            redisTemplate.expire(key, 1, TimeUnit.SECONDS);
        }

        return count <= QPS_LIMIT;
    }
}
```

#### 6.1.2 并发处理限制（最多2000并发）

**原理**：Semaphore + Redis 计数器

```java
@Service
public class ConcurrencyLimiter {

    private static final int MAX_CONCURRENT = 2000;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean tryAcquire() {
        // 使用Redis原子递增
        Long count = redisTemplate.opsForValue().increment("seckill:processing:count");

        if (count != null && count <= MAX_CONCURRENT) {
            return true;
        }

        // 超出限制，回退
        redisTemplate.opsForValue().decrement("seckill:processing:count");
        return false;
    }

    public void release() {
        redisTemplate.opsForValue().decrement("seckill:processing:count");
    }
}
```

### 6.2 风控设计

#### 6.2.1 IP限流

```java
@Service
public class IpRateLimiter {

    // 同一IP每秒最多10次请求
    private static final int IP_QPS = 10;

    public boolean isIpAllowed(String ip) {
        // 检查黑名单
        if (isBlacklisted(ip)) {
            return false;
        }

        // 检查QPS
        String key = "seckill:ip:qps:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.SECONDS);
        }

        return count <= IP_QPS;
    }
}
```

#### 6.2.2 黑名单检测

```java
@Service
public class BlacklistService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 检查IP是否在黑名单
     */
    public boolean isBlacklisted(String ip) {
        String key = "seckill:blacklist:ip:" + ip;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 添加IP到黑名单
     */
    public void addToBlacklist(String ip, Duration duration) {
        String key = "seckill:blacklist:ip:" + ip;
        redisTemplate.opsForValue().set(key, "1", duration);
    }
}
```

### 6.3 幂等设计

```java
@Service
public class IdempotentService {

    /**
     * 检查并标记请求为已处理
     *
     * @param idempotentKey 幂等键
     * @param expireTime 过期时间
     * @return true=新请求, false=重复请求
     */
    public boolean tryAcquire(String idempotentKey, Duration expireTime) {
        String key = "seckill:idempotent:" + idempotentKey;

        // SETNX 保证原子性
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, "1", expireTime);

        return Boolean.TRUE.equals(success);
    }
}
```

---

## 第七章：最终一致性

### 7.1 对账机制

```java
/**
 * 库存对账定时任务
 *
 * 原理：
 * 1. 统计MQ扣减消息数量
 * 2. 对比数据库订单数量
 * 3. 不一致则记录告警
 */
@Scheduled(fixedRate = 60000) // 每分钟
public void reconcileStock() {
    List<Stock> stocks = stockMapper.selectList(null);

    for (Stock stock : stocks) {
        // MQ扣减次数
        Integer mqDeduct = stock.getMqDeductCount();

        // 实际订单数量
        Integer orderCount = orderMapper.countByGoodsId(stock.getGoodsId());

        // 预期可用库存 = 总库存 - MQ已扣减 + MQ已回滚
        Integer expected = stock.getTotalStock() - mqDeduct + stock.getMqRollbackCount();

        if (!expected.equals(stock.getAvailableStock())) {
            log.warn("库存对账不一致: goodsId={}, expected={}, actual={}",
                stock.getGoodsId(), expected, stock.getAvailableStock());
            // 发送告警
            alertService.send("库存不一致", stock.getGoodsId());
        }
    }
}
```

### 7.2 超时回滚

```java
/**
 * 订单超时取消定时任务
 *
 * 原理：
 * 1. 查询待支付订单
 * 2. 超过15分钟未支付
 * 3. 回滚库存
 * 4. 更新订单状态
 */
@Scheduled(fixedDelay = 10000) // 每10秒
public void cancelTimeoutOrders() {
    // 查询15分钟前的待支付订单
    List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(
        15, TimeUnit.MINUTES.toMinutes(1));

    for (Order order : timeoutOrders) {
        try {
            // 回滚库存
            stockService.rollbackStock(order.getGoodsId(), order.getQuantity());

            // 更新订单状态
            orderService.updateStatus(order.getId(), OrderStatus.TIMEOUT);

            // 发送MQ消息
            mqProducer.sendRollback(order);
        } catch (Exception e) {
            log.error("超时取消订单失败", order.getId(), e);
        }
    }
}
```

---

## 第八章：前端设计

### 8.1 页面结构

| 页面 | 路由 | 功能 |
|------|------|------|
| 秒杀首页 | `/seckill` | 活动列表、预约 |
| 商品详情 | `/seckill/:id` | 商品信息、抢购按钮 |
| 购物车 | `/cart` | 购物车列表 |
| 订单确认 | `/order/confirm` | 订单确认、支付 |
| 订单列表 | `/order/list` | 历史订单 |
| 秒杀战绩 | `/seckill/record` | 参与记录、排行榜 |

### 8.2 秒杀首页

```vue
<template>
  <div class="seckill-list">
    <!-- 秒杀倒计时 -->
    <div v-if="nextActivity" class="countdown">
      <div class="label">距离开始</div>
      <div class="time">
        {{ formatTime(countdown) }}
      </div>
    </div>

    <!-- 活动列表 -->
    <div class="activity-list">
      <div v-for="activity in activities" :key="activity.id" class="activity-card">
        <img :src="activity.image" />
        <div class="info">
          <h3>{{ activity.name }}</h3>
          <div class="price">
            <span class="seckill-price">¥{{ activity.seckillPrice }}</span>
            <span class="original-price">¥{{ activity.originalPrice }}</span>
          </div>
          <el-button
            v-if="activity.canReserve"
            @click="handleReserve(activity)">
            预约提醒
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>
```

### 8.3 抢购流程

```javascript
// 抢购流程
async function handleSeckill(goodsId) {
  // 1. 获取秒杀资格（签名）
  const { sign, timestamp } = await getSeckillSign(goodsId)

  // 2. 调用抢购接口
  const result = await seckillApi.seckill({
    goodsId,
    sign,
    timestamp
  })

  // 3. 建立SSE连接
  const eventSource = new EventSource(`/api/seckill/subscribe?queueId=${result.queueId}`)

  // 4. 监听结果
  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data)
    if (data.status === 'success') {
      ElMessage.success('抢购成功！')
      // 跳转到购物车
      router.push('/cart')
    } else if (data.status === 'failed') {
      ElMessage.error(data.reason)
    }
  }
}
```

---

## 第九章：监控设计

### 9.1 核心监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| QPS | 每秒请求数 | >800 |
| 成功率 | 抢购成功率 | <90% |
| 库存准确率 | 库存一致性 | <100% |
| 响应时间P99 | 99分位响应时间 | >500ms |
| MQ积压 | 消息队列积压数 | >1000 |

### 9.2 日志规范

```java
// 结构化日志
log.info("抢购请求|userId={}|goodsId={}|result={}|elapsed={}ms",
    userId, goodsId, result, elapsed);

// 关键路径埋点
Tracer.startSpan("seckill-deduct-stock");
```

### 监控大盘页面

#### 功能需求

| 功能 | 说明 |
|------|------|
| 实时QPS | 每秒请求数、峰值 |
| 成功率 | 抢购成功率、失败率 |
| 库存监控 | 实时库存、已售比例 |
| 活动热度 | 活动参与人数、排队情况 |
| 订单流水 | 实时订单状态分布 |
| 告警中心 | 异常告警、限流记录 |

#### 页面布局

```vue
<template>
  <div class="monitor-dashboard">
    <!-- 顶部指标卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>实时QPS</template>
          <div class="metric-value">{{ metrics.qps }}</div>
          <div class="metric-trend">
            <trend-chart :data="qpsHistory" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <template #header>抢购成功率</template>
          <div class="metric-value success-rate">
            {{ metrics.successRate }}%
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <template #header>库存预警</template>
          <el-progress
            :percentage="metrics.stockPercentage"
            :status="metrics.stockPercentage > 80 ? 'warning' : 'success'" />
          <div class="metric-detail">
            已售 {{ metrics.sold }} / 总库存 {{ metrics.total }}
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card>
          <template #header>排队情况</template>
          <div class="metric-value">{{ metrics.queueSize }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时数据图表 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>QPS趋势图</template>
          <line-chart :data="qpsData" :config="qpsConfig" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>订单状态分布</template>
          <pie-chart :data="orderStatusData" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动列表 -->
    <el-card>
      <template #header>活动监控</template>
      <el-table :data="activities">
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="qps" label="当前QPS" />
        <el-table-column prop="successRate" label="成功率" />
        <el-table-column prop="sold" label="已售" />
      </el-table>
    </el-card>
  </div>
</template>
```

#### 核心指标API

| 接口 | 说明 |
|------|------|
| `GET /api/monitor/metrics` | 获取实时指标 |
| `GET /api/monitor/qps-history` | QPS历史数据 |
| `GET /api/monitor/activities` | 活动监控数据 |
| `GET /api/monitor/alerts` | 告警列表 |

#### WebSocket推送

```javascript
// 实时推送监控数据
const ws = new WebSocket('ws://localhost:8080/api/monitor/stream')

ws.onmessage = (event) => {
  const data = JSON.parse(event.data)
  metrics.value = data
}
```

### 预热机制

#### 为什么需要预热？

**原理**：秒杀开始前，大量请求同时涌入数据库查询库存，导致数据库压力过大。预热将热点数据提前加载到 Redis，避免秒杀开始时的数据库冲击。

```
未预热：
秒杀开始 → 10万请求 → 同时查询数据库 → 数据库崩溃 ❌

已预热：
秒杀开始 → 10万请求 → 查询Redis → 数据库安全 ✅
```

#### 预热时机

| 时机 | 触发条件 |
|------|---------|
| 定时任务 | 秒杀开始前5分钟 |
| 手动触发 | 管理员操作 |
| 活动创建时 | 创建秒杀活动后 |

#### 预热内容

```java
/**
 * 数据预热服务
 *
 * 原理：
 * 1. 活动信息预热
 * 2. 库存预热到Redis
 * 3. 预约用户预热到Redis Set
 */
@Service
public class DataPreheatService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 预热单个活动
     *
     * @param activityId 活动ID
     */
    public void preheatActivity(Long activityId) {
        // 1. 预热活动信息
        preheatActivityInfo(activityId);

        // 2. 预热商品库存
        preheatGoodsStock(activityId);

        // 3. 预热预约用户
        preheatReservations(activityId);

        // 4. 预热限购标记
        preheatUserLimits(activityId);
    }

    /**
     * 预热活动基本信息
     */
    private void preheatActivityInfo(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);

        // 活动开关状态
        String key = "seckill:activity:" + activityId;
        redisTemplate.opsForHash().putAll(key, Map.of(
            "status", activity.getStatus(),
            "startTime", activity.getStartTime().toString(),
            "endTime", activity.getEndTime().toString()
        );
    }

    /**
     * 预热商品库存到Redis
     *
     * 原理：
     * 库存是关键数据，必须保证原子性
     * 使用 String 类型存储库存，Lua脚本保证原子扣减
     */
    private void preheatGoodsStock(Long activityId) {
        List<Goods> goodsList = goodsMapper.selectByActivityId(activityId);

        for (Goods goods : goodsList) {
            String stockKey = "seckill:stock:" + goods.getId();

            // 设置库存到Redis
            redisTemplate.opsForValue().set(stockKey, goods.getAvailableStock());

            // 设置过期时间（活动结束后1小时自动清理
            redisTemplate.expire(stockKey,
                Duration.between(
                    activity.getEndTime().toInstant(ZoneOffset.ofHours(8),
                    Instant.now().plusSeconds(3600)
                ));
        }
    }

    /**
     * 预热预约用户集合
     *
     * 用途：快速判断用户是否有资格抢购
     */
    private void preheatReservations(Long activityId) {
        // 预约用户ID列表
        List<Long> userIds = reservationMapper.selectUserIdsByActivityId(activityId);

        String reservationKey = "seckill:reservation:" + activityId;
        redisTemplate.delete(reservationKey);
        redisTemplate.opsForSet().add(reservationKey, userIds.toArray());
    }
}
```

### 预热定时任务

```java
/**
 * 预热定时任务
 *
 * 原理：
 * 1. 活动开始前5分钟自动预热
 * 2. 防止人工操作遗漏
 */
@Scheduled(cron = "0 */5 * * * *")
public void scheduledPreheat() {
    // 查询5分钟内开始的活动
    List<Activity> activities = activityMapper.selectStartingSoon(5, TimeUnit.MINUTES);

    for (Activity activity : activities) {
        try {
            preheatService.preheatActivity(activity.getId());
            log.info("预热活动成功: {}", activity.getId());
        } catch (Exception e) {
            log.error("预热活动失败: {}", activity.getId(), e);
        }
    }
}
```

---

## 第十章：环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21+ | 后端服务 |
| Node.js | 20+ | 前端服务 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 8.0+ | 缓存、限流 |
| RocketMQ | 5.3.2 | 消息队列 |
| Vue | 3.5+ | 前端框架 |
| Spring Boot | 4.0.5 | 后端框架 |

---

## 附录A：核心原理速查

### 乐观锁 vs 悲观锁

| 特性 | 乐观锁 | 悲观锁 |
|------|--------|--------|
| 实现 | version字段 | SELECT FOR UPDATE |
| 适用场景 | 冲突少 | 冲突多 |
| 性能 | 好 | 一般 |
| 实现方式 | 更新时检查version | 锁定读 |

### 限流算法

| 算法 | 原理 | 优点 | 缺点 |
|------|------|------|------|
| 计数器 | 单位时间计数 | 简单 | 不平滑 |
| 滑动窗口 | 时间窗口内计数 | 平滑 | 实现复杂 |
| 令牌桶 | 匀速发放令牌 | 支持突发 | - |
| 漏桶 | 匀速消费 | 平滑 | - |

### 消息队列对比

| MQ | 吞吐量 | 顺序消息 | 事务消息 | 延迟消息 |
|----|--------|---------|---------|---------|
| RocketMQ | 高 | 支持 | 支持 | 支持 |
| Kafka | 最高 | 支持 | 不支持 | 不支持 |
| RabbitMQ | 中 | 支持 | 支持 | 支持 |

---

> 文档版本：1.0
> 最后更新：2026-04-20
