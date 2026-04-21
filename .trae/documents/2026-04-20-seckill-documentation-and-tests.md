# 秒杀系统文档与测试实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为秒杀系统生成使用手册、开发手册、功能注释，并完成前后端单元测试

**架构：** 通过完善文档和测试用例，确保秒杀系统的可用性和可维护性

**技术栈：** Spring Boot + Vue 3 + MyBatis Plus + Redis + RocketMQ

---

## 文件结构

本次计划涉及的文件：

**文档类：**
- `docs/seckill/user-guide.md` - 用户使用手册
- `docs/seckill/developer-guide.md` - 开发手册

**注释增强：**
- 后端核心服务类注释
- 前端组件注释

**测试类：**
- 后端测试：`springboot/src/test/java/cn/coderstory/springboot/...`
- 前端测试：`app-vue/src/api/__tests__/...`

---

## 任务列表

### 任务 1：生成用户使用手册

**文件：**
- 创建：`docs/seckill/user-guide.md`

- [ ] **步骤 1：编写用户使用手册**

创建完整的用户使用手册，包含：
- 系统概述和功能模块
- 秒杀活动浏览和参与流程
- 订单管理和支付流程
- 购物车使用
- 监控大盘使用说明
- 常见问题解答

---

### 任务 2：生成开发手册

**文件：**
- 创建：`docs/seckill/developer-guide.md`

- [ ] **步骤 1：编写开发手册**

创建完整的开发手册，包含：
- 系统架构设计
- 核心模块说明（限流、风控、秒杀、库存、MQ）
- 数据库表结构说明
- API接口文档
- 配置说明
- 部署指南

---

### 任务 3：功能模块注释增强

**文件：**
- 修改：`springboot/src/main/java/cn/coderstory/springboot/seckill/service/SeckillService.java` - 添加核心方法注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/limiter/QpsLimiter.java` - 添加限流原理注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/limiter/ConcurrencyLimiter.java` - 添加并发控制原理注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/security/BlacklistService.java` - 添加风控原理注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/security/IdempotentService.java` - 添加幂等性原理注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/stock/service/StockService.java` - 添加库存扣减原理注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/mq/producer/OrderTransactionProducer.java` - 添加MQ事务原理注释
- 修改：`springboot/src/main/java/cn/coderstory/springboot/mq/consumer/OrderTransactionListener.java` - 添加事务消息处理原理注释

- [ ] **步骤 1：为SeckillService添加核心方法注释和原理说明**

添加类级别JavaDoc和方法注释，包含：
- 类功能说明
- 秒杀流程原理（三层保护机制）
- 关键方法的工作原理

```java
/**
 * 秒杀服务 - 核心业务逻辑
 * 
 * 工作原理：
 * 1. 用户请求首先进行签名验证，防止请求篡改
 * 2. 通过Redis原子操作快速判断库存是否充足
 * 3. 发送RocketMQ事务消息，确保订单创建和库存扣减的最终一致性
 * 4. 用户进入排队队列，等待异步处理结果
 * 
 * 三层保护机制：
 * - 第一层：Redis原子扣减，高性能过滤大部分无效请求
 * - 第二层：RocketMQ消息队列，削峰填谷，控制并发
 * - 第三层：数据库乐观锁，最终数据一致性保障
 */
@Service
public class SeckillService { ... }
```

- [ ] **步骤 2：为限流服务添加原理注释**

为QpsLimiter和ConcurrencyLimiter添加详细注释

- [ ] **步骤 3：为风控服务添加原理注释**

为BlacklistService和IdempotentService添加详细注释

- [ ] **步骤 4：为库存服务添加原理注释**

为StockService添加详细注释

- [ ] **步骤 5：为MQ组件添加原理注释**

为OrderTransactionProducer和OrderTransactionListener添加详细注释

---

### 任务 4：后端单元测试

**文件：**
- 创建：`springboot/src/test/java/cn/coderstory/springboot/seckill/service/SeckillServiceTest.java`
- 创建：`springboot/src/test/java/cn/coderstory/springboot/seckill/service/SignServiceTest.java`
- 创建：`springboot/src/test/java/cn/coderstory/springboot/seckill/service/ActivityServiceTest.java`
- 创建：`springboot/src/test/java/cn/coderstory/springboot/limiter/QpsLimiterTest.java`
- 创建：`springboot/src/test/java/cn/coderstory/springboot/security/IdempotentServiceTest.java`
- 创建：`springboot/src/test/java/cn/coderstory/springboot/order/service/OrderServiceTest.java`

- [ ] **步骤 1：编写SeckillService单元测试**

```java
@SpringBootTest
class SeckillServiceTest {
    
    @Test
    void shouldValidateSignCorrectly() {
        // 测试签名验证逻辑
    }
    
    @Test
    void shouldRejectInvalidSign() {
        // 测试无效签名拒绝
    }
}
```

- [ ] **步骤 2：编写SignService单元测试**

- [ ] **步骤 3：编写ActivityService单元测试**

- [ ] **步骤 4：编写QpsLimiter单元测试**

- [ ] **步骤 5：编写IdempotentService单元测试**

- [ ] **步骤 6：编写OrderService单元测试**

- [ ] **步骤 7：执行后端测试**

运行：`cd springboot; .\mvnw.cmd test -Dtest=SeckillServiceTest,SignServiceTest,ActivityServiceTest,QpsLimiterTest,IdempotentServiceTest,OrderServiceTest`

---

### 任务 5：前端单元测试

**文件：**
- 创建：`app-vue/src/api/__tests__/seckill.test.ts`
- 创建：`app-vue/src/api/__tests__/activity.test.ts`
- 创建：`app-vue/src/api/__tests__/order.test.ts`

- [ ] **步骤 1：编写seckill API测试**

```typescript
import { describe, it, expect } from 'vitest'
import { seckillApi } from '../seckill'

describe('seckill API', () => {
  it('should define buy method', () => {
    expect(seckillApi.buy).toBeDefined()
  })
  
  it('should define getResult method', () => {
    expect(seckillApi.getResult).toBeDefined()
  })
})
```

- [ ] **步骤 2：编写activity API测试**

- [ ] **步骤 3：编写order API测试**

- [ ] **步骤 4：执行前端测试**

运行：`cd app-vue; npm run test`

---

## 执行方式选择

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

**选哪种方式？**