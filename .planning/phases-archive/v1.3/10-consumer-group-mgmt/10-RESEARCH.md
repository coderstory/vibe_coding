# Phase 10 Research: Consumer Group 管理

**Phase:** 10
**Domain:** RocketMQ Consumer Group 查看与位点重置
**Mode:** ecosystem
**Confidence:** HIGH
**Date:** 2026-04-28

---

## Executive Summary

Phase 10 实现 Consumer Group 管理功能，复用 Phase 9 的技术栈（rocketmq-client-apache 5.x + DefaultMQAdminExt）。核心是 Consumer Group 列表查询、消费进度查看、时间戳位点重置。

---

## Standard Stack

| 组件 | 选择 | 理由 |
|------|------|------|
| Admin SDK | `rocketmq-tools` DefaultMQAdminExt | Phase 9 已采用，一致性 |
| 前端框架 | Vue 3 + Element Plus | 与 Phase 9, TopicList.vue 一致 |
| API 路径 | `/api/rocketmq/consumer-groups/*` | RESTful 设计 |

### 后端依赖 (pom.xml)

```xml
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-tools</artifactId>
    <version>5.3.2</version>
</dependency>
```

---

## Architecture Patterns

### 后端分层

```
RocketMQController (REST API)
    ↓
RocketMQAdminService (接口)
    ↓
RocketMQAdminServiceImpl (实现)
    ↓
DefaultMQAdminExt (RocketMQ Admin API)
```

### API 端点设计

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/rocketmq/consumer-groups` | 列表查询 |
| GET | `/api/rocketmq/consumer-groups/{group}` | Group 详情 |
| POST | `/api/rocketmq/consumer-groups/{group}/reset-offset` | 重置位点 |

### 前端组件结构

```
ConsumerGroupList.vue (列表页)
    ↓ 点击行
ConsumerGroupDetail.vue (详情弹窗, el-tabs)
    ↓ Tab: 重置位点
ResetOffsetDialog.vue (重置对话框)
```

---

## Don't Hand-Roll

1. **MQAdmin 实例管理** - 使用 Spring Bean 管理生命周期，不要每次 new
2. **位点重置逻辑** - RocketMQ Admin API 提供 `resetOffsetByTimestamp()`，不要自己计算
3. **Consumer Group 状态检测** - 使用 `getConsumeStats()` 获取实时状态

---

## Common Pitfalls

### 1. 广播模式不支持位点重置

**问题:** 调用 `resetOffsetByTimestamp()` 对 BROADCASTING 类型 Group 会失败

**解决:** 重置前检查 `groupType`，仅对 CLUSTERING 模式执行重置

```java
// 检查 Group 类型
SubscriptionGroupConfig config = admin.examineSubscriptionGroupConfig(topic, group);
if ("BROADCASTING".equals(config.getGroupType())) {
    throw new BusinessException("广播模式不支持位点重置");
}
```

### 2. 时间戳格式要求

**问题:** `resetOffsetByTimestamp()` 需要毫秒级时间戳

**解决:** 前端 el-date-picker 获取的时间已经是毫秒级，后端直接使用

```java
long timestamp = request.getTimestamp(); // 毫秒
admin.resetOffsetByTimestamp(topicName, consumerGroup, timestamp);
```

### 3. 消费者不在线时的重置

**问题:** 所有消费者实例都必须在线才能重置

**解决:** 重置前检查 `consumerCount > 0`，如果为 0 提示用户

### 4. Topic 名称编码

**问题:** Topic 名称可能包含特殊字符

**解决:** 使用 `encodeURIComponent()` 在 URL 中编码，前端和后端都要处理

---

## API Methods Reference (DefaultMQAdminExt)

### 获取所有 Consumer Group

```java
public Set<String> fetchAllSubscriptionGroups() throws Exception
```

### 获取单个 Group 详情

```java
public SubscriptionGroupConfig examineSubscriptionGroupConfig(
    String topic,    // 指定 topic，否则获取不到消费进度
    String group
) throws Exception
```

### 获取消费进度

```java
public ConsumeStats getConsumeStats(
    String topic,
    String group,
    long timeout // milliseconds
) throws Exception

// 返回字段:
// - consumerList: 在线消费者列表
// - totalDiff: 堆积量
// - offsetTable: 各队列的位点信息
```

### 重置位点（时间戳）

```java
public void resetOffsetByTimestamp(
    String topic,
    String consumerGroup,
    long timestamp
) throws Exception
```

---

## Code Examples

### 后端: Consumer Group 列表

```java
@Override
public List<Map<String, Object>> getConsumerGroupList(String keyword) {
    Set<String> groups = defaultMQAdminExt.fetchAllSubscriptionGroups();
    List<Map<String, Object>> result = new ArrayList<>();

    for (String group : groups) {
        if (keyword != null && !group.toLowerCase().contains(keyword.toLowerCase())) {
            continue;
        }

        Map<String, Object> item = new HashMap<>();
        item.put("group", group);

        try {
            // 获取消费统计
            ConsumeStats stats = defaultMQAdminExt.getConsumeStats(null, group, 3000);
            item.put("consumerCount", stats.getConsumerList().size());
            item.put("accumulatedDiff", stats.getTotalDiff());
        } catch (Exception e) {
            item.put("consumerCount", 0);
            item.put("accumulatedDiff", 0L);
        }

        // 获取 Group 配置（类型、状态）
        try {
            SubscriptionGroupConfig config = defaultMQAdminExt.examineSubscriptionGroupConfig(null, group);
            item.put("groupType", config.getGroupType() == 0 ? "BROADCASTING" : "CLUSTERING");
            item.put("status", "OK"); // 需要根据实际情况判断
        } catch (Exception e) {
            item.put("groupType", "UNKNOWN");
            item.put("status", "OFFLINE");
        }

        result.add(item);
    }
    return result;
}
```

### 后端: 位点重置

```java
@Override
public void resetConsumerOffset(String topic, String group, long timestamp) {
    // 1. 检查 Group 类型
    SubscriptionGroupConfig config = admin.examineSubscriptionGroupConfig(topic, group);
    if (config.getGroupType() == 0) { // BROADCASTING
        throw new BusinessException("广播模式不支持位点重置");
    }

    // 2. 执行重置
    admin.resetOffsetByTimestamp(topic, group, timestamp);
    log.info("位点重置成功: topic={}, group={}, timestamp={}", topic, group, timestamp);
}
```

### 前端: 时间选择器

```typescript
// ResetOffsetDialog.vue
const handleReset = async () => {
  if (!selectedTopic.value || !resetTime.value) {
    ElMessage.warning('请选择 Topic 和时间')
    return
  }

  try {
    await resetConsumerOffset(selectedTopic.value, groupName.value, resetTime.value)
    ElMessage.success('位点重置成功')
    detailDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '重置失败')
  }
}
```

---

## Verification Checklist

- [ ] Consumer Group 列表正确显示（名称、类型、状态、消费者数、堆积量）
- [ ] 详情页 Tab 切换正常（消费进度/订阅关系/位点信息）
- [ ] CLUSTERING 类型可以正常重置位点
- [ ] BROADCASTING 类型重置时返回友好错误
- [ ] 所有 API 响应格式为 `ApiResponse<T>`
- [ ] 前端处理了空状态和加载状态

---

## Confidence Assessment

| Area | Confidence | Notes |
|------|-------------|-------|
| Admin API 方法 | HIGH | DefaultMQAdminExt 是稳定 API |
| 前端组件模式 | HIGH | 复用 TopicList.vue 模式 |
| 位点重置逻辑 | MEDIUM | 需要 Broker 在线 |
| 状态判断 | MEDIUM | RocketMQ 状态定义可能有版本差异 |

---

## Next Step: Plan Phase

Proceed to `/gsd-plan-phase 10` with this research as input.