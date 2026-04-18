### 数据库查询优化

> 当表不存在主键，也不存在非空的唯一列，则生成隐藏字段 DB_ROW_ID 6字节长度
> 
> 另外还存在隐藏字段 DB_RTX_ID 事务id； DB_ROW_PTR 回滚指针
> 
> redolog（数据页，偏移量，修改了什么）
>
> undolog（记录id 整行数据 修改列的值） 
>
> binlog（服务器二进制日志）

| 对比维度 | 聚簇索引 | 非聚簇索引（二级索引） |
| ---- | ---- | ---- |
| 存储结构 | 数据与索引物理存储在一起，叶子节点存储完整数据行 | 索引与数据分开存储，叶子节点存储聚簇索引键值（指针） |
| 物理顺序 | 决定数据物理存储顺序 | 不影响数据物理存储顺序，仅维护逻辑顺序 |
| 数量限制 | 一张表只能有一个 | 一张表可以有多个（通常建议不超过 5 个） |
| 查询效率 | 更高，无需回表 | 较低，需通过聚簇索引键值回表查询数据 |
| 写入性能 | 较低，可能引发页分裂 | 较高，仅需维护索引结构，不影响数据物理存储 |
| 适用场景 | 范围查询、排序、主键查询 | 等值查询、频繁查询的非主键列 |
1. 索引优化

> 单列索引
>
>  组合索引
>
>  最左匹配原则

2. 查询优化

> 减少查询字段，不要使用`select *`
>
> 使用连表查询替代子查询
>
> 避免使用 `like %xxx` （这种如果走覆盖索引那么也能走索引）
>
> 拆分大表（拆分大字段，非必要字段，低频字段）
>
> 减少冗余字段（同时存了userId 和 userName）
>
> 避免全表扫描（没走索引 使用max(column),隐式类型转换 notin&in&!=可能导致失效 EXPLAIN ）
>
>频繁更新的字段不要创建索引
>
> 禁止 or的一侧使用非索引字段
> 
> 数据量小的表，不走索引（成本分析）
> 
> 数据量过大的表（千万级） 因为存在大量回表 而放弃索引

3. 使用缓存

> 使用缓存（redis）减少数据库压力

4. 使用分区表

> 按行分区
> 分区键的选择（日期、数值范围、时序数据）支持多列
>
> 分区表中：主键 / 唯一索引必须包含所有分区列

```mysql
CREATE TABLE orders
(
    id         INT  NOT NULL,
    order_date DATE NOT NULL,
    amount     DECIMAL(10, 2)
) ENGINE = InnoDB
    PARTITION BY RANGE (TO_DAYS(order_date)) (
        PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
        PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
        PARTITION p_future VALUES LESS THAN MAXVALUE
        );

CREATE TABLE users
(
    id     INT,
    region VARCHAR(20)
) ENGINE = InnoDB
    PARTITION BY LIST (region) (
        PARTITION p_north VALUES IN ('北京','天津','河北'),
        PARTITION p_south VALUES IN ('上海','江苏','浙江'),
        PARTITION p_other VALUES IN (DEFAULT)
        );

CREATE TABLE logs
(
    id       BIGINT,
    log_time DATETIME
) ENGINE = InnoDB
    PARTITION BY HASH (id)
        PARTITIONS 8; -- 使用hash值作为key，分8区
```

5. 硬件优化

> 使用RAID
>
> 使用SSD

6. 数据库慢查询

> 开启数据库慢查询，并设置阈值
>
> 定时查看log文件分析

### 数据库执行计划

```mysql
EXPLAIN
SELECT *
FROM orders
WHERE order_date = '2024-01-01';

```

| 字段名           | 	核心含义       | 	优化关注点                   |
|---------------|-------------|--------------------------|
| id            | 	查询执行的顺序编号	 | id 越大越先执行；id 相同，从上到下执行   |
| select_type   | 	查询类型	      | 简单查询 / 子查询 / 联合查询，简单查询最优 |
| table         | 	当前查询访问的表	  | 关联查询时看表顺序                |
| type          | 	访问类型（最重要）	 | 性能从优到差，决定 SQL 快慢         |
| possible_keys | 	可能用到的索引    | 	列出候选索引                  |
| key           | 	实际使用的索引	   | 为空 = 没用到索引               |
| rows          | 	预估扫描的行数	   | 数值越小越好                   |
| Extra         | 	额外关键信息	    | 包含索引、排序、分区、临时表提示         |


### 优化mysql表结构
1. 索引列的长度尽量小
2. 选中离散度高的列（值的变化大，反例：性别），否则过滤的效果差 优化器可能基于成本分析不走索引
3. 前缀索引（截取）`CREATE INDEX idx_email ON users(email(10)); `
4. 只为搜索，排序，分组的列创建索引


### 如何避免死锁

### 优化大量数据插入
1. `INSERT INTO table values（），（）`
2. 使用load data方法，直接在服务器上导入csv文件

### 大量数据写入会出现什么问题
1. 高cpu和硬盘占用，系统变慢
2. 磁盘空间不足
3. 日志文件短时间内变得巨大
4. 主从同步延迟

### 锁
1. 只有命中事务，才能实现行级锁
2. 锁表
3. 锁行 （共享锁 别的事务可以读取，普通读看历史undolog，快照读，加锁读看最新，当前读） （排他锁）
4. 间隙锁（range）
5. 临键锁 锁定某行以及后面的全部行 （默认值）

### 事务隔离级别
1. 读未提交 可以直接读取别的事务中修改的数据
2. 读已提交 只能读取别的使用提交的数据
3. 可重复读 不会读取别的事务阶段性提交的数据，默认值 （一个事物可提交多次）
4. 串行化  禁止并发事务

### 事务特性
**ACID**
1. 原子性 事务操作要么全成功，要么全失败，基于undolog
2. 一致性 完整性约束不会破坏，主键，唯一索引，外键等
3. 隔离性 多个并发事务同时执行的时候，一个事物的执行不会被其他事务干扰 锁+MVVC
4. 对数据的修改是永久性，哪怕是数据库崩溃也能恢复 redolog

