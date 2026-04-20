# 秒杀系统测试指南

> 本文档描述秒杀系统的完整测试流程，包括环境准备、测试用例和验证方法。

---

## 一、环境准备

### 1.1 基础环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21+ | 后端运行环境 |
| Node.js | 20+ | 前端运行环境 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 8.0+ | 缓存和分布式锁 |
| RocketMQ | 5.3.2 | 消息队列 |

### 1.2 启动服务

#### 1.2.1 启动 MySQL

```bash
# 使用 Docker 启动 MySQL
docker run -d \
  --name mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=admin_system \
  mysql:8.0
```

#### 1.2.2 启动 Redis

```bash
# 使用 Docker 启动 Redis
docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:8.0
```

#### 1.2.3 启动 RocketMQ

```bash
# 使用 Docker 启动 RocketMQ
docker run -d \
  --name rocketmq \
  -p 9876:9876 \
  -p 10911:10911 \
  -p 10909:10909 \
  -e ROCKETMQ_STOPPED=true \
  apache/rocketmq:5.3.2 \
  sh mqnamesrv

# 启动 Broker
docker run -d \
  --name rocketmq-broker \
  -p 10911:10911 \
  -p 10909:10909 \
  --link rocketmq \
  -e NAMESRV_ADDR=rocketmq:9876 \
  apache/rocketmq:5.3.2 \
  sh mqbroker -c /opt/rocketmq/conf/broker.conf
```

#### 1.2.4 启动后端服务

```bash
cd springboot

# 编译项目
.\mvnw.cmd clean package -DskipTests

# 启动应用
java -jar target/springboot-0.0.1-SNAPSHOT.jar
```

#### 1.2.5 启动前端服务

```bash
cd app-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

### 1.3 验证服务状态

```bash
# 验证 MySQL
mysql -h localhost -u root -p123456 -e "SHOW DATABASES;"

# 验证 Redis
redis-cli ping
# 期望输出：PONG

# 验证后端 API
curl http://localhost:8080/api/monitor/metrics

# 验证前端
curl http://localhost:5173
```

---

## 二、测试用例

### 2.1 单元测试

#### 2.1.1 运行所有单元测试

```bash
cd springboot

# 运行所有单元测试
.\mvnw.cmd test

# 运行特定测试类
.\mvnw.cmd test -Dtest=SeckillServiceTest

# 运行特定测试方法
.\mvnw.cmd test -Dtest=SeckillServiceTest#testNormalSeckill
```

#### 2.1.2 预期结果

```
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 2.2 秒杀功能测试

#### 2.2.1 测试准备

1. 登录管理后台，创建秒杀活动
2. 设置活动库存为 10 件
3. 开启活动

#### 2.2.2 测试用例表

| 用例编号 | 用例名称 | 测试步骤 | 预期结果 |
|---------|---------|---------|---------|
| TC-001 | 正常秒杀流程 | 1. 进入秒杀活动详情<br>2. 点击立即抢购<br>3. 等待 SSE 通知 | 返回抢购成功，库存减 1 |
| TC-002 | 库存不足 | 1. 库存为 0<br>2. 点击立即抢购 | 返回"库存不足" |
| TC-003 | 重复请求 | 1. 快速点击抢购按钮两次 | 第二次返回"重复请求" |
| TC-004 | 活动未开始 | 1. 活动状态为"未开始"<br>2. 点击立即抢购 | 返回"活动未开始" |
| TC-005 | 活动已结束 | 1. 活动状态为"已结束"<br>2. 点击立即抢购 | 返回"活动已结束" |
| TC-006 | 签名验证 | 1. 使用错误签名抢购 | 返回"签名无效" |
| TC-007 | 无签名抢购 | 1. 不传签名参数<br>2. 点击立即抢购 | 正常抢购成功 |
| TC-008 | 限量购买 | 1. 抢购 2 次同一商品（限购 1 件） | 第二次返回"已限购" |

#### 2.2.3 测试脚本（使用 curl）

```bash
# 获取当前用户 token（假设已登录）
TOKEN="your-jwt-token"

# 1. 获取秒杀签名
curl -X GET "http://localhost:8080/api/seckill/sign/1" \
  -H "Authorization: Bearer $TOKEN"

# 2. 执行秒杀
curl -X POST "http://localhost:8080/api/seckill/buy" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "goodsId": 1,
    "activityId": 1,
    "sign": "xxx",
    "timestamp": 1713600000000,
    "idempotentKey": "test_key_123"
  }'

# 3. 查询秒杀结果
curl -X GET "http://localhost:8080/api/seckill/result/{queueId}" \
  -H "Authorization: Bearer $TOKEN"

# 4. 查询库存
curl -X GET "http://localhost:8080/api/seckill/stock/1" \
  -H "Authorization: Bearer $TOKEN"
```

### 2.3 库存扣减测试

#### 2.3.1 测试场景

| 用例编号 | 测试场景 | 验证方法 |
|---------|---------|---------|
| ST-001 | Redis 库存扣减 | 扣减后查询 Redis：`GET seckill:stock:1` |
| ST-002 | 数据库库存扣减 | 扣减后查询数据库：`SELECT * FROM stock WHERE goods_id = 1` |
| ST-003 | 库存回滚 | 订单取消后验证库存恢复 |
| ST-004 | 并发扣减一致性 | 使用 JMeter 模拟 100 并发 |

#### 2.3.2 Redis 验证命令

```bash
# 连接 Redis
redis-cli

# 查看库存
GET seckill:stock:1

# 查看限流计数器
GET seckill:qps:1

# 查看幂等键
GET seckill:idempotent:test_key_123

# 查看并发计数
GET seckill:processing:count
```

### 2.4 分布式锁测试

#### 2.4.1 测试场景

| 用例编号 | 测试场景 | 预期结果 |
|---------|---------|---------|
| LT-001 | 单机获取锁 | 成功获取锁 |
| LT-002 | 锁自动释放 | 持有超时后自动释放 |
| LT-003 | 重复释放 | 安全处理，不抛异常 |
| LT-004 | 并发竞争 | 只有一个线程获取锁成功 |

### 2.5 MQ 消息测试

#### 2.5.1 验证消息发送

```bash
# 查看 RocketMQ 控制台
# http://localhost:8080

# 使用 mqadmin 查看消息
cd rocketmq/bin

# 查看消费者
./mqadmin consumerList -n localhost:9876

# 查看 Topic
./mqadmin topicList -n localhost:9876

# 查看消息
./mqadmin queryMsgById -i <msgId> -n localhost:9876
```

### 2.6 SSE 实时通知测试

#### 2.6.1 测试步骤

1. 发起秒杀请求，获取 queueId
2. 使用浏览器或 Postman 建立 SSE 连接
3. 观察实时推送结果

#### 2.6.2 Postman 测试

```
# SSE 连接地址
GET http://localhost:8080/api/seckill/subscribe/{queueId}

# 预期收到事件：
event: connected
data: SSE 连接已建立，queueId: xxx

event: seckill_result
data: {"status": 1, "message": "抢购成功", "orderId": 123}
```

---

## 三、性能测试

### 3.1 压力测试脚本（使用 Apache Bench）

```bash
# 安装 Apache Bench
# Windows: 下载 Apache for Windows
# Linux: sudo apt install apache2-utils

# 测试 QPS
ab -n 1000 -c 100 -H "Authorization: Bearer $TOKEN" \
  -T "application/json" \
  -p seckill-request.json \
  http://localhost:8080/api/seckill/buy
```

### 3.2 JMeter 测试计划

创建 JMeter 测试计划，包含：
- 线程组：100 线程，循环 10 次
- HTTP 请求：秒杀接口
- 汇总报告：QPS、响应时间

### 3.3 性能指标

| 指标 | 目标值 | 说明 |
|------|-------|------|
| QPS | > 500 | 每秒处理请求数 |
| 成功率 | > 99% | 抢购成功率 |
| P99 响应时间 | < 200ms | 99 分位响应时间 |
| 库存准确率 | 100% | 不超卖、不少卖 |

---

## 四、异常场景测试

### 4.1 Redis 不可用

1. 停止 Redis 服务
2. 尝试抢购
3. 验证：返回友好错误提示，不影响用户

### 4.2 RocketMQ 不可用

1. 停止 RocketMQ 服务
2. 尝试抢购
3. 验证：
   - Redis 库存已扣减
   - 返回友好错误提示
   - 依赖定时对账恢复

### 4.3 MySQL 不可用

1. 停止 MySQL 服务
2. 尝试抢购
3. 验证：返回友好错误提示

### 4.4 网络超时

1. 使用抓包工具模拟网络延迟
2. 验证：
   - 请求有合理超时时间
   - 不出现死锁
   - 幂等性正常

---

## 五、回归测试

### 5.1 每次发布前必须通过的测试

1. ✅ 所有单元测试通过
2. ✅ 正常秒杀流程测试
3. ✅ 库存一致性验证
4. ✅ 订单超时取消测试

### 5.2 回归测试清单

| 序号 | 功能 | 测试结果 | 测试人 | 测试时间 |
|-----|------|---------|-------|---------|
| 1 | 秒杀抢购 | ☐ | | |
| 2 | 库存扣减 | ☐ | | |
| 3 | 订单创建 | ☐ | | |
| 4 | 订单超时取消 | ☐ | | |
| 5 | 库存回滚 | ☐ | | |
| 6 | 限流生效 | ☐ | | |
| 7 | 幂等性 | ☐ | | |
| 8 | SSE 通知 | ☐ | | |

---

## 六、常见问题排查

### 6.1 抢购失败常见原因

| 问题 | 可能原因 | 解决方法 |
|------|---------|---------|
| 库存为 0 | Redis 未预热 | 调用预热接口 |
| 签名无效 | 时间戳过期 | 重新获取签名 |
| 重复请求 | 幂等键未变 | 生成新的幂等键 |
| 签名无效 | 使用了旧签名 | 重新获取签名 |

### 6.2 日志查看

```bash
# 查看后端日志
tail -f springboot/logs/application.log

# 查看 RocketMQ 日志
tail -f rocketmq/logs/proxy.log
```

---

> 文档版本：1.0
> 最后更新：2026-04-20
