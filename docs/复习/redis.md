
### 指令
> NX 不存在才添加
> 
>XX 只更新，不存在则不操作
> 
>INCR 自增
> 
>CH 返回修改总数


### 数据结构

#### String
> 基于key-value存储
> 
> 缓存用户登入信息，配置信息等
> 
> 实现分布式锁 ```set nx```
> 
> 实现计数器 ```INCR DECR```
> 
> 实现session共享
> 

```java
// 缓存用户信息
redis.set("user:1001":"{\"name\":\"张三\"}");
//阅读量+1
redis.incr("page:view:1001");
```

#### hash
> map结构，存储对象

```java
redis.hset("user:1001","name","abc");
// 购物车 商品数量+1 key不存在自动创建 值不存在默认0 给负数就是减法 原子性
redis.hincrby("cart:1000":"product:1001",1);
```




#### list
> 基于 quicklist（快速列表）实现，而 quicklist 内部又组合了 ziplist（压缩列表） 和 双向链表，按插入顺序排序
> 实现最新文档，最近动态，搜索历史，浏览记录等

```java
redis.plush("user:favor":"apple");
//阻塞读取
redis.brpop("user:favor",10);
```

#### Set
> 无须，不重复
```java
redis.sadd("user:1001:follow","1002","1003","5555");
redis.sadd("user:1002:follow","1009","1003","5555");
// 求交集 共同关注
redis.sinter("user:1001:follow","user:1002:follow");
```

#### sorted set
> 也叫zset，有序，不重复，底层为每个元素关联一个分数score
> 
> 场景： 热搜榜，积分榜，热销榜
> 
> 范围查找，按分数
>
```redis
// 添加单条数据 100是分数
ZADD ranking 100 "张三"
// 同时添加多条数据
ZADD ranking 90 "李四" 80 "王五" 120 "赵六"
// 语法
ZADD key [NX|XX] [CH] [INCR] score member [score member ...]
```

### bitmaps

```REDIS
SETBIT key offset value
SETBIT user:sign 100 1   # 把第100位设为1
GETBIT user:sign 100 # 获取某一位的值
BITCOUNT user:sign # 统计有多少位是 1
BITCOUNT user:sign 0 1000  # 统计 0~1000 位
```

>   场景：  用户签到
```java
SETBIT sign:202505 10086 1
GETBIT sign:202505 10086
BITCOUNT sign:202505  # 当月总签到人次
```
> 场景： 用户在线状态、是否读消息、是否激活
> 

### HyperLogLog
> 专门用来做不精确的海量去重计数（比如统计 UV、日活、独立访客）。
```redis
PFADD key element [element ...]
```

### redis cell 限流器模块

```REIDS
CL.THROTTLE  key  最大容量  每秒请求数  时间窗口
```

### 电商秒杀

1. 用string 或者hash 存 库存，使用 DECR 原子扣减，防止超卖
2. 使用string限制用户访问频率，防止刷单
3. 秒杀结果临时存储，成功用户id存入set，快速过滤重复请求
4. 