---
name: performance-optimization
description: >-
  性能优化检查清单。当遇到性能问题、进行性能优化、代码审查时使用。
---

# 性能优化检查清单

本技能提供 RobitCode 项目的性能优化指南，帮助开发者识别和解决性能问题。

---

## 一、数据库优化

### 1.1 索引优化

| 检查项 | 要求 | 影响 |
|--------|------|------|
| 查询条件字段 | WHERE、JOIN、ORDER BY 字段建索引 | 🔴 高 |
| 复合索引顺序 | 遵循最左前缀原则 | 🟡 中 |
| 索引覆盖率 | 高频查询使用覆盖索引 | 🟡 中 |
| 冗余索引 | 删除重复或无用索引 | 🟢 低 |

```sql
-- ✅ 正确：复合索引支持多种查询
CREATE INDEX idx_user_dept_status ON sys_user(department_id, status);

-- ❌ 错误：单独索引无法支持组合查询
CREATE INDEX idx_user_dept ON sys_user(department_id);
CREATE INDEX idx_user_status ON sys_user(status);
```

### 1.2 查询优化

| 问题 | 解决方案 |
|------|----------|
| N+1 查询 | 使用 JOIN 或批量查询 |
| 全表扫描 | 添加索引或限制返回行数 |
| 大量数据返回 | 分页查询，只查需要的字段 |
| 复杂子查询 | 改写为 JOIN 或使用临时表 |

```java
// ✅ 正确：批量查询避免 N+1
@Select("SELECT * FROM sys_user WHERE department_id IN (${deptIds})")
List<User> findByDepartmentIds(@Param("deptIds") List<Long> deptIds);

// ❌ 错误：循环查询 N+1
for (Long deptId : deptIds) {
    List<User> users = userMapper.findByDepartmentId(deptId);
}
```

### 1.3 分页优化

```sql
-- ✅ 正确：使用游标分页（大数据量）
SELECT * FROM sys_user WHERE id > #{lastId} ORDER BY id LIMIT 20;

-- ⚠️ 注意：OFFSET 分页在大偏移量时性能差
SELECT * FROM sys_user ORDER BY id LIMIT 20 OFFSET 100000;
```

---

## 二、缓存优化

### 2.1 缓存策略

| 数据类型 | 缓存策略 | TTL |
|----------|----------|-----|
| 用户信息 | Cache-Aside | 30 分钟 |
| 菜单数据 | Write-Through | 1 小时 |
| 字典数据 | Read-Through | 24 小时 |
| 统计数据 | Write-Behind | 5 分钟 |

### 2.2 缓存实现

```java
// 使用 Spring Cache
@Cacheable(value = "users", key = "#id")
public UserVO getUser(Long id) {
    return userMapper.selectById(id);
}

@CacheEvict(value = "users", key = "#user.id")
public void updateUser(User user) {
    userMapper.updateById(user);
}

// 多级缓存
@Cacheable(
    cacheNames = {"local", "redis"},
    key = "#id",
    cacheManager = "multiLevelCacheManager"
)
public UserVO getUser(Long id) { ... }
```

### 2.3 缓存问题处理

| 问题 | 解决方案 |
|------|----------|
| 缓存穿透 | 布隆过滤器或缓存空值 |
| 缓存击穿 | 热点数据永不过期 + 互斥锁 |
| 缓存雪崩 | 随机过期时间 + 多级缓存 |
| 缓存一致性 | 延迟双删 + 最终一致性 |

---

## 三、代码优化

### 3.1 算法复杂度

| 场景 | 要求 | 优化方式 |
|------|------|----------|
| 列表遍历 | O(n) | 使用 Map 替代嵌套循环 |
| 查找操作 | O(1) | 使用 HashSet/HashMap |
| 排序操作 | O(n log n) | 使用标准库排序 |
| 字符串拼接 | O(n) | 使用 StringBuilder |

```java
// ✅ 正确：使用 Map 优化查找
Map<Long, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));
for (Order order : orders) {
    User user = userMap.get(order.getUserId());
}

// ❌ 错误：嵌套循环 O(n*m)
for (Order order : orders) {
    for (User user : users) {
        if (user.getId().equals(order.getUserId())) {
            // ...
        }
    }
}
```

### 3.2 集合优化

| 场景 | 推荐集合 | 原因 |
|------|----------|------|
| 频繁插入删除 | LinkedList | 插入删除 O(1) |
| 随机访问 | ArrayList | 访问 O(1) |
| 线程安全列表 | CopyOnWriteArrayList | 读多写少 |
| 去重 | LinkedHashSet | 保持顺序 |
| 键值存储 | HashMap | 快速查找 |

### 3.3 字符串优化

```java
// ✅ 正确：使用 StringBuilder
StringBuilder sb = new StringBuilder();
for (String item : items) {
    sb.append(item).append(",");
}

// ❌ 错误：字符串拼接创建大量对象
String result = "";
for (String item : items) {
    result += item + ",";
}

// ✅ 正确：使用 String.join
String result = String.join(",", items);
```

---

## 四、异步处理

### 4.1 异步场景

| 场景 | 处理方式 |
|------|----------|
| 发送通知 | 异步消息队列 |
| 生成报表 | 异步任务 + 轮询 |
| 批量导入 | 异步处理 + 进度查询 |
| 日志记录 | 异步写入 |

### 4.2 异步实现

```java
// 使用 @Async
@Async
public CompletableFuture<Void> sendNotification(Notification notification) {
    // 异步发送通知
    notificationService.send(notification);
    return CompletableFuture.completedFuture(null);
}

// 使用线程池
@Service
public class AsyncTaskService {

    @Resource
    private ThreadPoolTaskExecutor asyncExecutor;

    public void processAsync(List<Data> dataList) {
        asyncExecutor.execute(() -> {
            dataList.forEach(this::process);
        });
    }
}
```

### 4.3 线程池配置

```java
@Configuration
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 五、前端优化

### 5.1 加载优化

| 优化项 | 方法 |
|--------|------|
| 代码分割 | 路由懒加载、动态 import |
| 资源压缩 | Gzip、图片压缩 |
| 缓存利用 | 长期缓存、CDN |
| 预加载 | preload、prefetch |
| 懒加载 | 图片懒加载、组件懒加载 |

```typescript
// 路由懒加载
const routes = [
  {
    path: '/workflow',
    component: () => import('@/views/workflow/index.vue')
  }
];

// 组件懒加载
const HeavyComponent = defineAsyncComponent(() =>
  import('@/components/HeavyComponent.vue')
);
```

### 5.2 渲染优化

| 优化项 | 方法 |
|--------|------|
| 减少重绘 | 批量更新、虚拟滚动 |
| 避免不必要的渲染 | v-memo、shouldComponentUpdate |
| 计算属性缓存 | computed 代替 methods |
| 大列表渲染 | 虚拟滚动（vue-virtual-scroller） |

### 5.3 网络优化

| 优化项 | 方法 |
|--------|------|
| 请求合并 | 批量接口、GraphQL |
| 数据压缩 | Gzip、Brotli |
| 缓存策略 | localStorage、IndexedDB |
| 防抖节流 | debounce、throttle |

---

## 六、监控与诊断

### 6.1 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 页面加载时间 | < 3s | 首屏渲染完成 |
| API 响应时间 | < 200ms | P95 |
| 数据库查询 | < 100ms | 单次查询 |
| 内存使用 | < 80% | JVM 堆内存 |
| CPU 使用 | < 70% | 平均使用率 |

### 6.2 诊断工具

| 工具 | 用途 |
|------|------|
| Arthas | Java 在线诊断 |
| JProfiler | JVM 性能分析 |
| Chrome DevTools | 前端性能分析 |
| MySQL EXPLAIN | SQL 执行计划 |
| Redis Monitor | Redis 性能监控 |

### 6.3 慢查询日志

```sql
-- MySQL 慢查询配置
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 1;
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';
```

---

## 七、性能检查清单

### 7.1 代码层面

- [ ] 避免 N+1 查询
- [ ] 使用批量操作代替循环
- [ ] 合理使用缓存
- [ ] 大文件使用流式处理
- [ ] 避免在循环中创建对象

### 7.2 数据库层面

- [ ] 索引覆盖高频查询
- [ ] 避免 SELECT *
- [ ] 使用分页查询
- [ ] 定期分析慢查询
- [ ] 合理使用事务

### 7.3 前端层面

- [ ] 路由懒加载
- [ ] 图片压缩和懒加载
- [ ] 使用虚拟滚动
- [ ] 防抖节流
- [ ] 合理使用缓存

---

## 八、性能优化流程

1. **建立基准**：记录当前性能指标
2. **识别瓶颈**：通过监控和诊断找到问题
3. **分析原因**：确定优化方向
4. **实施优化**：小步快跑，逐步优化
5. **验证效果**：对比优化前后的指标
6. **持续监控**：防止性能回归
