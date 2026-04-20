### 控制反转 依赖注入
1. 控制反转 （@Service @Controller @Bean）对象不再手动创建
2. 依赖注入 @Resoure 构造函数注入 等


### AOP
1. 代理模式
2. java动态代理，cglib
3. 运行时生成代理对象，在代理对象里嵌入增强逻辑。
4. 场景： 权限校验，接口日志，事务

### spring 中的设计模式
1.单例模式 bean默认都是单例的

2.工厂模式 beanfactory

3.观察者模式 applicationevent

4.责任链 aop 

### classloader
1. 双亲委派机制（先从parent查找，不同的classloader加载的类是不同的）
2. loadclass方法


### 创建线程方式

1. new Thread(()->xxxx).start;
2. new Thread(new FetureTask(()->xxx)).start;
3. Executors.newFixedThreadPool()

### 异常体系

1. 顶层是throwable
2. 第二层是exception和error
3. 第三层是runtimeexception

### 避免死锁
1. 注意加锁顺序
3. 设置等待时间
4. 检查死锁 （jstack 12345 | grep -A 20 "deadlock"）


### ReentrantLock
1. 条件锁，先锁定锁，然后锁定条件锁，此时锁释放
2. 确保大门不能并发
3. 如何生产者消费者模式，生产者生产的时候需要拿锁，然后同时通知正在等待的消费者，消费者消费的时候，先拿锁，没有商品则上条件锁。
4. 还有线程池，队列，数据库连接池等场景

### 分布式事务
 
1. 核心概念

TC (Transaction Coordinator)：事务协调器（Seata Server），独立部署，负责协调全局事务状态。

TM (Transaction Manager)：事务管理器（在业务代码中通过注解定义事务边界）。

RM (Resource Manager)：资源管理器（各微服务中的数据库）。

2. 最常用的 AT 模式（自动补偿）

AT 模式对代码侵入性极低，只需一个注解即可实现分布式事务。

实现步骤：

部署 Seata Server：下载并启动 Seata Server（TC），注册到 Nacos。

引入依赖：在微服务中引入 spring-cloud-starter-alibaba-seata。

配置代理数据源：Seata 会自动代理 DataSource，并需要在每个业务库中创建 undo_log表（用于生成回滚日志）。

添加注解：在事务发起方的方法上添加 @GlobalTransactional。


