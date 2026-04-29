# Phase 11 Research: 消息管理

**Phase:** 11 — 消息管理
**Goal:** 实现消息查询、详情查看和轨迹追踪
**Mode:** ecosystem
**Date:** 2026-04-29
**Confidence:** High

---

## Standard Stack

### Backend (Spring Boot 4.0.5)
- **RocketMQ Client:** `rocketmq-tools` → `DefaultMQAdminExt` (已有基础设施)
- **消息查询 API:** `defaultMQAdminExt.queryMessage(topic, msgId, maxMsg, beginTimestamp, endTimestamp)`
- **消息轨迹:** RocketMQ 5.x 内置轨迹追踪，使用 `MQAdminExt.searchOffset` + `MQClientInstance` 组合
- **消息存储:** 基于 Broker 存储，通过 Name Server 路由

### Frontend (Vue 3 + TypeScript)
- **Element Plus:** `el-table`, `el-dialog`, `el-date-picker` 用于消息列表和详情
- **状态管理:** Pinia (如需缓存)
- **API 层:** `src/api/rocketmq.ts` (已有模式，扩展消息相关接口)

---

## Architecture Patterns

### Backend Pattern: 扩展 RocketMQAdminService

```
RocketMQAdminService (interface)
    └── RocketMQAdminServiceImpl
            ├── getTopicList() ✓ 已实现
            ├── getConsumerGroupList() ✓ 已实现
            └── [NEW] getMessageList(topic, startTime, endTime, maxMsg)
            └── [NEW] getMessageDetail(msgId, topic)
            └── [NEW] getMessageTrace(msgId, topic)
```

**关键 API:**
1. **消息查询:** `DefaultMQAdminExt.queryMessage(topic, msgId, maxMsg, beginTime, endTime)`
   - 按时间范围查询消息，返回 `QueryResult` 包含 `MessageExt` 列表
   - 需要 Broker 地址 (通过 `getFirstBrokerAddr()` 获取)

2. **消息详情:** 从 `QueryResult` 获取 `MessageExt` 对象
   - 包含: msgId, body, keys, tags, properties, timestamp, topic, queueId

3. **消息轨迹:** RocketMQ 5.x 内置轨迹
   - `DefaultMQAdminExt.searchOffset(topic, queueId, timestamp)` 获取物理位点
   - 组合消费进度信息重建轨迹

### Frontend Pattern: 消息管理页面

```
views/rocketmq/
├── MessageList.vue          # 消息列表页 (新)
├── MessageDetail.vue       # 消息详情弹窗 (新)
└── MessageTrace.vue        # 消息轨迹页 (新)
```

**交互流程:**
1. 用户选择 Topic + 时间范围 → 调用 `getMessageList`
2. 点击消息行 → 弹出 `MessageDetail.vue` 显示消息详情
3. 点击"轨迹"按钮 → 调用 `getMessageTrace` 显示生产→消费链路

---

## Don't Hand-Roll

| 场景 | 原因 |
|------|------|
| **消息查询分页** | 大 Topic 消息量大，使用 `maxMsg` 限制每次返回量，分页加载 |
| **轨迹时间计算** | 涉及多个时间点计算，不要自己计算，用 RocketMQ 内置 API |
| **大消息内容展示** | 消息 body 可能很大，前端做截断显示 |

---

## Common Pitfalls

### 1. 消息查询性能问题
- **问题:** 大 Topic 查询返回消息过多，导致 OOM 或超时
- **解决:** 强制 `maxMsg` 参数限制单次查询量 (建议 ≤ 100)，前端做分页

### 2. 时间范围边界
- **问题:** 时间范围跨度过大，查询慢
- **解决:** 前端限制最大时间范围 (如 7 天)，后端校验

### 3. 轨迹数据缺失
- **问题:** 消息可能被消费后，轨迹信息不完整
- **解决:**
  - 检查 Broker 是否开启 trace (`trace.topic`)
  - 轨迹只展示已记录的数据，避免显示空状态

### 4. 特殊字符处理
- **问题:** topic 名称可能包含特殊字符，URL 编码
- **解决:** 前端 `encodeURIComponent(topic)`，后端 `URLDecoder.decode()`

---

## Implementation Plan

### Backend (RocketMQAdminService)

```java
// 1. 消息列表查询
List<Map<String, Object>> getMessageList(String topic, long startTime, long endTime, int maxMsg);

// 2. 消息详情
Map<String, Object> getMessageDetail(String topic, String msgId);

// 3. 消息轨迹
Map<String, Object> getMessageTrace(String topic, String msgId);
```

### Controller (RocketMQController)

```
GET  /api/rocketmq/messages?topic=xxx&startTime=xxx&endTime=xxx&maxMsg=100
GET  /api/rocketmq/messages/{topic}/{msgId}
GET  /api/rocketmq/messages/{topic}/{msgId}/trace
```

### Frontend API (rocketmq.ts)

```typescript
interface MessageVO {
  msgId: string
  topic: string
  body: string
  keys: string
  tags: string
  properties: Record<string, string>
  timestamp: number
  queueId: number
  queueOffset: number
}

interface MessageTraceVO {
  produceTime: number
  consumeTime: number[]
  consumeGroup: string[]
  status: string
}

export function getMessageList(topic: string, startTime: number, endTime: number, maxMsg?: number)
export function getMessageDetail(topic: string, msgId: string)
export function getMessageTrace(topic: string, msgId: string)
```

---

## Code Examples

### Backend: 消息查询实现

```java
@Override
public List<Map<String, Object>> getMessageList(String topic, long startTime, long endTime, int maxMsg) {
    String brokerAddr = getFirstBrokerAddr();
    if (brokerAddr == null) {
        throw BusinessException.badRequest("未找到可用的 Broker");
    }

    try {
        QueryResult queryResult = defaultMQAdminExt.queryMessage(
            topic,
            "*", // msgId, * means all
            maxMsg > 0 ? maxMsg : 100,
            startTime,
            endTime
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (MessageExt msg : queryResult.getMessageList()) {
            Map<String, Object> item = new HashMap<>();
            item.put("msgId", msg.getMsgId());
            item.put("topic", msg.getTopic());
            item.put("body", new String(msg.getBody()));
            item.put("keys", msg.getKeys());
            item.put("tags", msg.getTags());
            item.put("timestamp", msg.getStoreTimestamp());
            item.put("queueId", msg.getQueueId());
            item.put("queueOffset", msg.getQueueOffset());
            item.put("properties", msg.getProperties());
            result.add(item);
        }
        return result;
    } catch (Exception e) {
        log.error("查询消息列表失败: topic={}", topic, e);
        throw BusinessException.badRequest("查询消息失败: " + e.getMessage());
    }
}
```

### Frontend: 消息列表组件

```vue
<template>
  <div class="message-list">
    <!-- 搜索条件 -->
    <el-form inline>
      <el-form-item label="Topic">
        <el-select v-model="form.topic" filterable>
          <el-option v-for="t in topics" :key="t" :label="t" :value="t" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker v-model="dateRange" type="datetimerange" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>

    <!-- 消息列表 -->
    <el-table :data="messages" @row-click="showDetail">
      <el-table-column prop="msgId" label="消息ID" width="200" />
      <el-table-column prop="tags" label="Tags" width="120" />
      <el-table-column prop="keys" label="Keys" width="150" />
      <el-table-column prop="timestamp" label="时间" width="180" :formatter="formatTime" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click.stop="showTrace(row)">轨迹</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      @current-change="loadMore"
    />

    <!-- 详情弹窗 -->
    <MessageDetailDialog v-model="detailVisible" :message="currentMessage" />
  </div>
</template>
```

---

## Verification Checklist

- [ ] 消息列表查询返回正确数据结构
- [ ] 时间范围查询准确
- [ ] 消息详情包含完整字段 (msgId, body, keys, tags, properties)
- [ ] 轨迹显示生产→消费完整链路
- [ ] 大 Topic 分页处理正确
- [ ] 特殊字符 topic 名称正确编码
- [ ] 消息 body 为空/null 时不报错

---

## Dependencies

- Phase 9 (Topic 管理) — 已完成，提供 topic 列表选择
- RocketMQ Broker 需要开启 trace 功能

---

## Risk Assessment

| 风险 | 级别 | 缓解 |
|------|------|------|
| 大 Topic 查询性能 | 中 | maxMsg 限制 + 分页 |
| 轨迹数据不完整 | 低 | 检查 trace topic 配置 |
| 时间范围跨度过大 | 低 | 前端限制 7 天 |

**Overall Risk:** Low — 技术方案成熟，API 稳定