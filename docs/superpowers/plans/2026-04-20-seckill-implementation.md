# 秒杀系统实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在现有 vibe coding 项目中实现完整的秒杀系统，支持高并发抢购

**架构：** 微服务风格但集成在同一项目，Redis 原子扣库存 + RocketMQ 事务消息 + 三层防护

**技术栈：** Spring Boot 4.0.5 + RocketMQ 5.3.2 + Redis + MySQL + Vue 3.5

---

## 文件结构概览

### 后端（springboot）

| 目录 | 文件 | 职责 |
|------|------|------|
| `db/migration/` | V8~V16.sql | 9个数据库迁移脚本 |
| `seckill/entity/` | 7个实体类 | 活动、商品、订单、排队等 |
| `seckill/dto/` | SeckillRequest/Response | 抢购请求响应DTO |
| `seckill/service/` | 4个服务 | 抢购、活动、签名、预热 |
| `seckill/controller/` | 3个控制器 | 秒杀、活动、预约接口 |
| `order/entity/` | Order, Cart | 订单、购物车实体 |
| `order/service/` | 3个服务 | 订单、购物车、超时取消 |
| `order/controller/` | 2个控制器 | 订单、购物车接口 |
| `stock/entity/` | Stock | 库存实体 |
| `stock/service/` | 2个服务 | 库存管理、对账 |
| `stock/consumer/` | StockConsumer | MQ消息消费 |
| `mq/producer/` | OrderTransactionProducer | 事务消息生产者 |
| `monitor/controller/` | MonitorController | 监控API |
| `monitor/service/` | MonitorService | 监控指标收集 |
| `limiter/` | 3个限流器 | QPS、并发、IP限流 |
| `security/` | 2个服务 | 黑名单、幂等 |

### 前端（app-vue）

| 目录 | 文件 | 职责 |
|------|------|------|
| `api/` | seckill.ts, activity.ts, order.ts, cart.ts, monitor.ts | API服务 |
| `views/seckill/` | 4个页面 | 秒杀首页、详情、记录、购物车 |
| `views/order/` | 2个页面 | 订单确认、订单列表 |
| `views/monitor/` | MonitorDashboard.vue | 监控大盘 |

---

## 第一阶段：数据库迁移

### 任务 1：创建秒杀活动表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V8__seckill_activity.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    status TINYINT DEFAULT 0 COMMENT '活动状态: 0-未开始 1-进行中 2-已结束',
    per_limit INT DEFAULT 1 COMMENT '每人限购数量',
    enable_captcha BOOLEAN DEFAULT TRUE COMMENT '是否启用验证码',
    enable_ip_limit BOOLEAN DEFAULT TRUE COMMENT '是否启用IP限制',
    sign_key VARCHAR(64) COMMENT '活动签名密钥',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) COMMENT='秒杀活动表';
```

- [ ] **步骤 2：执行迁移验证**

运行：`cd springboot; .\mvnw.cmd flyway:migrate -Dflyway.target=V8`
预期：Migration V8__seckill_activity.sql successful

### 任务 2：创建秒杀商品表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V9__seckill_goods.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS seckill_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    stock INT NOT NULL COMMENT '总库存',
    sold INT DEFAULT 0 COMMENT '已售',
    image_url VARCHAR(500) COMMENT '商品图片',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_activity (activity_id),
    FOREIGN KEY (activity_id) REFERENCES seckill_activity(id)
) COMMENT='秒杀商品表';
```

- [ ] **步骤 2：执行迁移验证**

运行：`cd springboot; .\mvnw.cmd flyway:migrate -Dflyway.target=V9`
预期：Migration V9__seckill_goods.sql successful

### 任务 3：创建秒杀排队表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V10__seckill_queue.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS seckill_queue (
    queue_id VARCHAR(64) PRIMARY KEY COMMENT '队列ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-排队中 1-成功 2-失败',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='秒杀排队表';
```

- [ ] **步骤 2：执行迁移验证**

### 任务 4：创建秒杀订单表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V11__seckill_order.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    quantity INT DEFAULT 1 COMMENT '购买数量',
    price DECIMAL(10,2) NOT NULL COMMENT '购买价格',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待支付 1-已支付 2-已取消 3-超时取消',
    queue_id VARCHAR(64) COMMENT '队列ID',
    payment_time DATETIME COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='秒杀订单表';
```

- [ ] **步骤 2：执行迁移验证**

### 任务 5：创建预约表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V12__seckill_reservation.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS seckill_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    reserve_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '状态: 0-预约 1-已提醒 2-已过期',
    notified BOOLEAN DEFAULT FALSE COMMENT '是否已发送提醒',
    notify_time DATETIME COMMENT '提醒时间',
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_activity (activity_id),
    INDEX idx_notify (status, notified)
) COMMENT='秒杀预约表';
```

- [ ] **步骤 2：执行迁移验证**

### 任务 6：创建库存表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V13__stock.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL UNIQUE COMMENT '商品ID',
    total_stock INT NOT NULL COMMENT '总库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    locked_stock INT DEFAULT 0 COMMENT '已锁定库存',
    version INT DEFAULT 0 COMMENT '乐观锁版本',
    mq_deduct_count INT DEFAULT 0 COMMENT 'MQ已扣减次数',
    mq_rollback_count INT DEFAULT 0 COMMENT 'MQ已回滚次数',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='库存表';
```

- [ ] **步骤 2：执行迁移验证**

### 任务 7：创建购物车表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V14__cart.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT DEFAULT 1 COMMENT '购买数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_goods (user_id, goods_id),
    INDEX idx_user (user_id)
) COMMENT='购物车表';
```

- [ ] **步骤 2：执行迁移验证**

### 任务 8：创建IP黑名单表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V15__ip_blacklist.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS ip_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_address VARCHAR(50) NOT NULL UNIQUE COMMENT 'IP地址',
    reason VARCHAR(200) COMMENT '封禁原因',
    expire_time DATETIME COMMENT '过期时间(NULL表示永久)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_expire (expire_time)
) COMMENT='IP黑名单';
```

- [ ] **步骤 2：执行迁移验证**

### 任务 9：创建秒杀日志表迁移

**文件：**
- 创建：`springboot/src/main/resources/db/migration/V16__seckill_log.sql`

- [ ] **步骤 1：编写迁移脚本**

```sql
CREATE TABLE IF NOT EXISTS seckill_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    goods_id BIGINT COMMENT '商品ID',
    activity_id BIGINT COMMENT '活动ID',
    request_time DATETIME NOT NULL COMMENT '请求时间',
    response_time DATETIME COMMENT '响应时间',
    elapsed_ms INT COMMENT '耗时(毫秒)',
    result TINYINT COMMENT '结果: 0-排队中 1-成功 2-库存不足 3-签名无效 4-已限购',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    captcha_verified BOOLEAN DEFAULT FALSE COMMENT '验证码是否通过',
    ip_blacklisted BOOLEAN DEFAULT FALSE COMMENT '是否IP黑名单',
    sign VARCHAR(128) COMMENT '请求签名',
    idempotent_key VARCHAR(64) COMMENT '幂等键',
    INDEX idx_time (request_time),
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id),
    INDEX idx_result (result)
) COMMENT='秒杀请求日志表';
```

- [ ] **步骤 2：执行迁移验证**

---

## 第二阶段：后端实体类

### 任务 10：创建秒杀相关实体

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/SeckillActivity.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/SeckillGoods.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/SeckillQueue.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/SeckillOrder.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/SeckillReservation.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/SeckillLog.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/entity/IpBlacklist.java`

- [ ] **步骤 1：创建 SeckillActivity.java**

```java
package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seckill_activity")
public class SeckillActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer perLimit;
    private Boolean enableCaptcha;
    private Boolean enableIpLimit;
    private String signKey;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **步骤 2：创建 SeckillGoods.java**

```java
package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_goods")
public class SeckillGoods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String name;
    private BigDecimal originalPrice;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer sold;
    private String imageUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **步骤 3：创建 SeckillQueue.java**

```java
package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seckill_queue")
public class SeckillQueue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String queueId;
    private Long userId;
    private Long goodsId;
    private Integer status;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **步骤 4：创建 SeckillOrder.java**

```java
package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private Long activityId;
    private Integer quantity;
    private BigDecimal price;
    private Integer status;
    private String queueId;
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **步骤 5：创建其他实体类**

创建 `SeckillReservation.java`、`SeckillLog.java`、`IpBlacklist.java`

- [ ] **步骤 6：运行构建验证**

运行：`cd springboot; .\mvnw.cmd compile`
预期：BUILD SUCCESS

### 任务 11：创建订单和库存实体

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/entity/Order.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/entity/Cart.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/stock/entity/Stock.java`

- [ ] **步骤 1：创建 Order.java**

```java
package cn.coderstory.springboot.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long goodsId;
    private Long activityId;
    private Integer quantity;
    private BigDecimal price;
    private Integer status;
    private String queueId;
    private LocalDateTime paymentTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **步骤 2：创建 Cart.java**

```java
package cn.coderstory.springboot.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

- [ ] **步骤 3：创建 Stock.java**

```java
package cn.coderstory.springboot.stock.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("stock")
public class Stock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goodsId;
    private Integer totalStock;
    private Integer availableStock;
    private Integer lockedStock;
    private Integer version;
    private Integer mqDeductCount;
    private Integer mqRollbackCount;
    private LocalDateTime updateTime;
}
```

- [ ] **步骤 4：运行构建验证**

---

## 第三阶段：限流和风控

### 任务 12：创建限流器

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/limiter/QpsLimiter.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/limiter/ConcurrencyLimiter.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/limiter/IpRateLimiter.java`

- [ ] **步骤 1：创建 QpsLimiter.java**

```java
package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QpsLimiter {
    private final StringRedisTemplate redisTemplate;
    private static final int QPS_LIMIT = 1000;

    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        local old = now - window
        redis.call('ZREMRANGEBYSCORE', key, '-inf', old)
        local count = redis.call('ZCARD', key)
        if count >= limit then
            return 0
        end
        redis.call('ZADD', key, now, now .. ':' .. math.random())
        redis.call('PEXPIRE', key, window)
        return 1
        """;

    public boolean tryAcquire(Long goodsId) {
        String key = "seckill:qps:" + goodsId;
        long now = System.currentTimeMillis();
        Long result = redisTemplate.execute(
            RedisScript.of(LUA_SCRIPT, Long.class),
            Collections.singletonList(key),
            String.valueOf(now), "1000", String.valueOf(QPS_LIMIT)
        );
        return result != null && result == 1;
    }
}
```

- [ ] **步骤 2：创建 ConcurrencyLimiter.java**

```java
package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcurrencyLimiter {
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_CONCURRENT = 2000;

    public boolean tryAcquire() {
        Long count = redisTemplate.opsForValue().increment("seckill:processing:count");
        if (count != null && count <= MAX_CONCURRENT) {
            return true;
        }
        if (count != null) {
            redisTemplate.opsForValue().decrement("seckill:processing:count");
        }
        return false;
    }

    public void release() {
        redisTemplate.opsForValue().decrement("seckill:processing:count");
    }
}
```

- [ ] **步骤 3：创建 IpRateLimiter.java**

```java
package cn.coderstory.springboot.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IpRateLimiter {
    private final StringRedisTemplate redisTemplate;
    private static final int IP_QPS = 10;

    public boolean isIpAllowed(String ip) {
        String blacklistKey = "seckill:blacklist:ip:" + ip;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            return false;
        }
        String qpsKey = "seckill:ip:qps:" + ip;
        Long count = redisTemplate.opsForValue().increment(qpsKey);
        if (count == 1) {
            redisTemplate.expire(qpsKey, 1, TimeUnit.SECONDS);
        }
        return count == null || count <= IP_QPS;
    }
}
```

- [ ] **步骤 4：运行构建验证**

### 任务 13：创建风控服务

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/security/BlacklistService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/security/IdempotentService.java`

- [ ] **步骤 1：创建 BlacklistService.java**

```java
package cn.coderstory.springboot.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BlacklistService {
    private final StringRedisTemplate redisTemplate;

    public boolean isBlacklisted(String ip) {
        String key = "seckill:blacklist:ip:" + ip;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void addToBlacklist(String ip, Duration duration) {
        String key = "seckill:blacklist:ip:" + ip;
        redisTemplate.opsForValue().set(key, "1", duration);
    }

    public void removeFromBlacklist(String ip) {
        String key = "seckill:blacklist:ip:" + ip;
        redisTemplate.delete(key);
    }
}
```

- [ ] **步骤 2：创建 IdempotentService.java**

```java
package cn.coderstory.springboot.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IdempotentService {
    private final StringRedisTemplate redisTemplate;

    public boolean tryAcquire(String idempotentKey, Duration expireTime) {
        String key = "seckill:idempotent:" + idempotentKey;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "1", expireTime);
        return Boolean.TRUE.equals(success);
    }
}
```

- [ ] **步骤 3：运行构建验证**

---

## 第四阶段：核心服务

### 任务 14：创建签名服务

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/SignService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/impl/SignServiceImpl.java`

- [ ] **步骤 1：创建 SignService.java**

```java
package cn.coderstory.springboot.seckill.service;

import lombok.Data;

public interface SignService {
    SignResult generateSign(Long userId, Long goodsId, String activitySignKey);

    boolean verifySign(String sign, long timestamp);

    @Data
    class SignResult {
        private final String sign;
        private final long timestamp;
    }
}
```

- [ ] **步骤 2：创建 SignServiceImpl.java**

```java
package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.service.SignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {
    private final StringRedisTemplate redisTemplate;
    private static final long SIGN_EXPIRE_MS = 5 * 60 * 1000;

    @Override
    public SignResult generateSign(Long userId, Long goodsId, String activitySignKey) {
        long timestamp = System.currentTimeMillis();
        String content = userId + ":" + goodsId + ":" + timestamp;
        String sign = hmacSHA256(content, activitySignKey);
        String signKey = "seckill:sign:" + sign;
        redisTemplate.opsForValue().set(signKey, "1", 5, TimeUnit.MINUTES);
        return new SignResult(sign, timestamp);
    }

    @Override
    public boolean verifySign(String sign, long timestamp) {
        if (System.currentTimeMillis() - timestamp > SIGN_EXPIRE_MS) {
            return false;
        }
        String signKey = "seckill:sign:" + sign;
        Boolean used = redisTemplate.hasKey(signKey);
        if (Boolean.TRUE.equals(used)) {
            return false;
        }
        redisTemplate.opsForValue().set(signKey, "used", 1, TimeUnit.MINUTES);
        return true;
    }

    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC计算失败", e);
        }
    }
}
```

- [ ] **步骤 3：运行构建验证**

### 任务 15：创建秒杀核心服务

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/dto/SeckillRequest.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/dto/SeckillResponse.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/SeckillService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/impl/SeckillServiceImpl.java`

- [ ] **步骤 1：创建 SeckillRequest.java**

```java
package cn.coderstory.springboot.seckill.dto;

import lombok.Data;

@Data
public class SeckillRequest {
    private Long goodsId;
    private Long activityId;
    private String sign;
    private Long timestamp;
    private String idempotentKey;
}
```

- [ ] **步骤 2：创建 SeckillResponse.java**

```java
package cn.coderstory.springboot.seckill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillResponse {
    private String queueId;
    private Integer status;
    private String message;

    public static SeckillResponse queued(String queueId) {
        return SeckillResponse.builder()
            .queueId(queueId)
            .status(0)
            .message("排队中")
            .build();
    }

    public static SeckillResponse success(String queueId) {
        return SeckillResponse.builder()
            .queueId(queueId)
            .status(1)
            .message("抢购成功")
            .build();
    }

    public static SeckillResponse failed(String message) {
        return SeckillResponse.builder()
            .status(2)
            .message(message)
            .build();
    }
}
```

- [ ] **步骤 3：创建 SeckillService.java**

```java
package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.dto.SeckillRequest;
import cn.coderstory.springboot.seckill.dto.SeckillResponse;

public interface SeckillService {
    SeckillResponse seckill(SeckillRequest request, Long userId);
}
```

- [ ] **步骤 4：创建 SeckillServiceImpl.java**

```java
package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.limiter.ConcurrencyLimiter;
import cn.coderstory.springboot.limiter.QpsLimiter;
import cn.coderstory.springboot.security.IdempotentService;
import cn.coderstory.springboot.seckill.dto.SeckillRequest;
import cn.coderstory.springboot.seckill.dto.SeckillResponse;
import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.seckill.service.SignService;
import cn.coderstory.springboot.seckill.service.SeckillService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {
    private final SeckillActivityMapper activityMapper;
    private final SeckillGoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;
    private final SignService signService;
    private final QpsLimiter qpsLimiter;
    private final ConcurrencyLimiter concurrencyLimiter;
    private final IdempotentService idempotentService;

    private static final String DEDUCT_STOCK_LUA = """
        local stock = redis.call('GET', KEYS[1])
        if not stock then return -1 end
        if tonumber(stock) < tonumber(ARGV[1]) then return 0 end
        return redis.call('DECRBY', KEYS[1], ARGV[1])
        """;

    @Override
    public SeckillResponse seckill(SeckillRequest request, Long userId) {
        String queueId = UUID.randomUUID().toString();

        if (!idempotentService.tryAcquire(request.getIdempotentKey(), Duration.ofMinutes(10))) {
            return SeckillResponse.failed("重复请求");
        }

        if (!qpsLimiter.tryAcquire(request.getGoodsId())) {
            return SeckillResponse.failed("请求过于频繁");
        }

        if (!concurrencyLimiter.tryAcquire()) {
            return SeckillResponse.failed("系统繁忙");
        }

        try {
            SeckillActivity activity = activityMapper.selectById(request.getActivityId());
            if (activity == null || activity.getStatus() != 1) {
                return SeckillResponse.failed("活动未开始");
            }

            if (!signService.verifySign(request.getSign(), request.getTimestamp())) {
                return SeckillResponse.failed("签名无效");
            }

            String stockKey = "seckill:stock:" + request.getGoodsId();
            Long remaining = redisTemplate.execute(
                RedisScript.of(DEDUCT_STOCK_LUA, Long.class),
                Collections.singletonList(stockKey),
                "1"
            );

            if (remaining == null || remaining < 0) {
                return SeckillResponse.failed("库存不足");
            }

            return SeckillResponse.queued(queueId);
        } finally {
            concurrencyLimiter.release();
        }
    }
}
```

- [ ] **步骤 5：运行构建验证**

### 任务 16：创建活动服务和预热服务

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/ActivityService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/PreheatService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/impl/ActivityServiceImpl.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/impl/PreludeServiceImpl.java`

- [ ] **步骤 1：创建 ActivityService.java**

```java
package cn.coderstory.springboot.seckill.service;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;

public interface ActivityService {
    SeckillActivity getActivity(Long activityId);
    boolean startActivity(Long activityId);
    boolean endActivity(Long activityId);
}
```

- [ ] **步骤 2：创建 PreheatService.java**

```java
package cn.coderstory.springboot.seckill.service;

public interface PreheatService {
    void preheatActivity(Long activityId);
}
```

- [ ] **步骤 3：创建 PreheatServiceImpl.java**

```java
package cn.coderstory.springboot.seckill.service.impl;

import cn.coderstory.springboot.seckill.entity.SeckillActivity;
import cn.coderstory.springboot.seckill.entity.SeckillGoods;
import cn.coderstory.springboot.seckill.mapper.SeckillActivityMapper;
import cn.coderstory.springboot.seckill.mapper.SeckillGoodsMapper;
import cn.coderstory.springboot.seckill.service.PreludeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreheatServiceImpl implements PreheatService {
    private final SeckillActivityMapper activityMapper;
    private final SeckillGoodsMapper goodsMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void preheatActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            log.warn("预热活动不存在: {}", activityId);
            return;
        }

        String activityKey = "seckill:activity:" + activityId;
        redisTemplate.opsForHash().put(activityKey, "status", String.valueOf(activity.getStatus()));
        redisTemplate.opsForHash().put(activityKey, "startTime", activity.getStartTime().toString());
        redisTemplate.opsForHash().put(activityKey, "endTime", activity.getEndTime().toString());

        var goodsList = goodsMapper.selectList(
            new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getActivityId, activityId)
        );

        for (SeckillGoods goods : goodsList) {
            String stockKey = "seckill:stock:" + goods.getId();
            redisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
            redisTemplate.expire(stockKey,
                Duration.between(activity.getEndTime().toInstant(ZoneOffset.ofHours(8)),
                    java.time.Instant.now().plusSeconds(3600)));
        }

        log.info("预热活动成功: {}", activityId);
    }
}
```

- [ ] **步骤 4：运行构建验证**

### 任务 17：创建库存服务和MQ消费者

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/stock/service/StockService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/stock/service/impl/StockServiceImpl.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/stock/consumer/StockConsumer.java`

- [ ] **步骤 1：创建 StockService.java**

```java
package cn.coderstory.springboot.stock.service;

public interface StockService {
    void deductStock(Long goodsId, Integer quantity);
    void rollbackStock(Long goodsId, Integer quantity);
}
```

- [ ] **步骤 2：创建 StockServiceImpl.java**

```java
package cn.coderstory.springboot.stock.service.impl;

import cn.coderstory.springboot.mapper.StockMapper;
import cn.coderstory.springboot.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final StockMapper stockMapper;

    @Override
    @Transactional
    public void deductStock(Long goodsId, Integer quantity) {
        int rows = stockMapper.deductStock(goodsId, quantity);
        if (rows == 0) {
            throw new RuntimeException("库存扣减失败");
        }
    }

    @Override
    @Transactional
    public void rollbackStock(Long goodsId, Integer quantity) {
        stockMapper.rollbackStock(goodsId, quantity);
    }
}
```

- [ ] **步骤 3：创建 StockConsumer.java**

```java
package cn.coderstory.springboot.stock.consumer;

import cn.coderstory.springboot.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "seckill-stock", consumerGroup = "stock-consumer-group")
public class StockConsumer {
    private final StockService stockService;

    public void onMessage(String message) {
        try {
            var msg = JSON.parseObject(message, StockMessage.class);
            if ("deduct".equals(msg.getAction())) {
                stockService.deductStock(msg.getGoodsId(), msg.getQuantity());
            } else if ("rollback".equals(msg.getAction())) {
                stockService.rollbackStock(msg.getGoodsId(), msg.getQuantity());
            }
        } catch (Exception e) {
            log.error("处理库存消息失败", e);
            throw e;
        }
    }

    @Data
    public static class StockMessage {
        private String action;
        private Long goodsId;
        private Integer quantity;
    }
}
```

- [ ] **步骤 4：运行构建验证**

### 任务 18：创建订单服务和超时取消

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/service/OrderService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/service/TimeoutCancelService.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/service/impl/OrderServiceImpl.java`

- [ ] **步骤 1：创建 OrderService.java**

```java
package cn.coderstory.springboot.order.service;

import cn.coderstory.springboot.order.entity.Order;

public interface OrderService {
    Order createOrder(Order order);
    void payOrder(String orderNo);
    void cancelOrder(Long orderId);
}
```

- [ ] **步骤 2：创建 TimeoutCancelService.java**

```java
package cn.coderstory.springboot.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeoutCancelService {
    private final OrderService orderService;

    @Scheduled(fixedDelay = 10000)
    public void cancelTimeoutOrders() {
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(15);
        for (Order order : timeoutOrders) {
            try {
                orderService.cancelOrder(order.getId());
            } catch (Exception e) {
                log.error("超时取消订单失败: {}", order.getId(), e);
            }
        }
    }
}
```

- [ ] **步骤 3：运行构建验证**

### 任务 19：创建MQ事务消息生产者

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/mq/producer/OrderTransactionProducer.java`

- [ ] **步骤 1：创建 OrderTransactionProducer.java**

```java
package cn.coderstory.springboot.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTransactionProducer {
    private final RocketMQTemplate rocketMQTemplate;

    public void sendCreateOrderMessage(OrderCreateDTO dto) {
        Message<String> message = MessageBuilder
            .withPayload(JSON.toJSONString(dto))
            .setHeader(RocketMQHeaders.TRANSACTION_ID, dto.getQueueId())
            .build();

        rocketMQTemplate.sendMessageInTransaction(
            "seckill-order:create",
            message,
            dto
        );
    }

    @RocketMQTransactionListener
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        try {
            OrderCreateDTO dto = (OrderCreateDTO) arg;
            orderService.createOrderInTransaction(dto);
            return LocalTransactionState.COMMIT_MESSAGE;
        } catch (Exception e) {
            log.error("本地事务执行失败", e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }
}
```

- [ ] **步骤 2：运行构建验证**

---

## 第五阶段：Controller层

### 任务 20：创建秒杀Controller

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/controller/SeckillController.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/controller/ActivityController.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/seckill/controller/ReservationController.java`

- [ ] **步骤 1：创建 SeckillController.java**

```java
package cn.coderstory.springboot.seckill.controller;

import cn.coderstory.springboot.seckill.dto.SeckillRequest;
import cn.coderstory.springboot.seckill.dto.SeckillResponse;
import cn.coderstory.springboot.seckill.service.SeckillService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {
    private final SeckillService seckillService;

    @PostMapping("/do")
    public ApiResponse<SeckillResponse> seckill(@RequestBody SeckillRequest request) {
        Long userId = getCurrentUserId();
        SeckillResponse response = seckillService.seckill(request, userId);
        return ApiResponse.success(response);
    }

    private Long getCurrentUserId() {
        return 1L;
    }
}
```

- [ ] **步骤 2：创建 ActivityController.java 和 ReservationController.java**

- [ ] **步骤 3：运行构建验证**

### 任务 21：创建订单和监控Controller

**文件：**
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/controller/OrderController.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/order/controller/CartController.java`
- 创建：`springboot/src/main/java/cn/coderstory/springboot/monitor/controller/MonitorController.java`

- [ ] **步骤 1：创建 OrderController.java**

```java
package cn.coderstory.springboot.order.controller;

import cn.coderstory.springboot.order.entity.Order;
import cn.coderstory.springboot.order.service.OrderService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ApiResponse<Order> getOrder(@PathVariable Long id) {
        return ApiResponse.success(orderService.getOrder(id));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> payOrder(@PathVariable Long id) {
        orderService.payOrder(id);
        return ApiResponse.success();
    }
}
```

- [ ] **步骤 2：创建 MonitorController.java**

```java
package cn.coderstory.springboot.monitor.controller;

import cn.coderstory.springboot.monitor.service.MonitorService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {
    private final MonitorService monitorService;

    @GetMapping("/metrics")
    public ApiResponse<Map<String, Object>> getMetrics() {
        return ApiResponse.success(monitorService.getMetrics());
    }
}
```

- [ ] **步骤 3：运行构建验证**

---

## 第六阶段：前端

### 任务 22：创建前端API

**文件：**
- 创建：`app-vue/src/api/seckill.ts`
- 创建：`app-vue/src/api/activity.ts`
- 创建：`app-vue/src/api/order.ts`
- 创建：`app-vue/src/api/cart.ts`
- 创建：`app-vue/src/api/monitor.ts`

- [ ] **步骤 1：创建 seckill.ts**

```typescript
import request from './request'

export interface SeckillRequest {
  goodsId: number
  activityId: number
  sign: string
  timestamp: number
  idempotentKey: string
}

export interface SeckillResponse {
  queueId: string
  status: number
  message: string
}

export const seckillApi = {
  doSeckill(data: SeckillRequest) {
    return request.post<SeckillResponse>('/api/seckill/do', data)
  },
  getSign(goodsId: number) {
    return request.get<{ sign: string; timestamp: number }>(`/api/seckill/sign/${goodsId}`)
  }
}
```

- [ ] **步骤 2：创建其他API文件**

- [ ] **步骤 3：运行构建验证**

运行：`cd app-vue; npm run build`
预期：BUILD SUCCESS

### 任务 23：创建秒杀页面

**文件：**
- 创建：`app-vue/src/views/seckill/SeckillIndex.vue`
- 创建：`app-vue/src/views/seckill/SeckillDetail.vue`
- 创建：`app-vue/src/views/seckill/SeckillRecord.vue`
- 创建：`app-vue/src/views/seckill/SeckillCart.vue`

- [ ] **步骤 1：创建 SeckillIndex.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { activityApi } from '@/api/activity'

const activities = ref([])

onMounted(async () => {
  const res = await activityApi.list()
  activities.value = res.data
})
</script>

<template>
  <div class="seckill-index">
    <h1>秒杀活动</h1>
    <div v-for="activity in activities" :key="activity.id" class="activity-card">
      <h3>{{ activity.name }}</h3>
      <p>{{ activity.description }}</p>
      <el-button @click="$router.push(`/seckill/${activity.id}`)">立即抢购</el-button>
    </div>
  </div>
</template>
```

- [ ] **步骤 2：创建其他页面**

- [ ] **步骤 3：运行构建验证**

### 任务 24：创建订单页面和监控页面

**文件：**
- 创建：`app-vue/src/views/order/OrderConfirm.vue`
- 创建：`app-vue/src/views/order/OrderList.vue`
- 创建：`app-vue/src/views/monitor/MonitorDashboard.vue`

- [ ] **步骤 1：创建 OrderList.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order'

const orders = ref([])

onMounted(async () => {
  const res = await orderApi.list()
  orders.value = res.data
})
</script>

<template>
  <div class="order-list">
    <h1>我的订单</h1>
    <el-table :data="orders">
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="createTime" label="创建时间" />
    </el-table>
  </div>
</template>
```

- [ ] **步骤 2：创建 MonitorDashboard.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { monitorApi } from '@/api/monitor'

const metrics = ref({
  qps: 0,
  successRate: 0,
  stockPercentage: 0
})

onMounted(async () => {
  const res = await monitorApi.getMetrics()
  metrics.value = res.data
})
</script>

<template>
  <div class="monitor-dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>实时QPS</template>
          <div class="metric-value">{{ metrics.qps }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>成功率</template>
          <div class="metric-value">{{ metrics.successRate }}%</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
```

- [ ] **步骤 3：运行构建验证**

---

## 执行方式

计划已完成并保存到 `docs/superpowers/plans/2026-04-20-seckill-implementation.md`。

两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

选择哪种方式？
