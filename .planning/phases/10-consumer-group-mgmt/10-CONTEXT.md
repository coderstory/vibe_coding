# Phase 10: Consumer Group 管理 - 实现上下文

## 概述

| 字段 | 值 |
|------|-----|
| Phase | 10 |
| Domain | RocketMQ Consumer Group 查看与位点重置 |
| 依赖 | Phase 9 (Topic 管理) |
| 状态 | 已讨论 |

---

## 继承决策

### 技术栈
- **后端**: `rocketmq-client-apache` Apache RocketMQ Client 5.x
- **前端**: Vue 3 + Element Plus (与 Phase 9 一致)
- **API 路径**: `/api/rocketmq/*` (与 Phase 9 一致)
- **组件模式**: el-table、el-dialog、el-tag、el-tabs

### 现有组件参考
- `app-vue/src/views/rocketmq/TopicList.vue` — Group 列表复用相同模式
- `springboot/src/main/java/cn/coderstory/springboot/service/RocketMQAdminService.java` — Admin API 封装

---

## 实现决策

### 1. Group 列表展示

#### API 端点
```
GET /api/rocketmq/consumer-groups
```

#### 响应字段
| 字段 | 类型 | 描述 |
|------|------|------|
| group | String | Consumer Group 名称 |
| groupType | String | 类型: `BROADCASTING` / `CLUSTERING` |
| status | String | 状态: `OK` / `REBALANCE_NOT_INIT` / `OFFLINE` |
| consumerCount | Integer | 在线消费者数量 |
| accumulatedDiff | Long | 消息堆积量 |

#### 前端表格列
1. **Group 名称** — 链接，点击进入详情
2. **类型** — el-tag (`广播` / `集群`)
3. **状态** — el-tag (`正常` / `重试中` / `离线`)
4. **消费者数** — 数字
5. **堆积量** — 数字 (万为单位显示)

#### 状态标签映射
| 状态值 | 标签类型 | 颜色 |
|--------|----------|------|
| OK | 正常 | success (绿色) |
| REBALANCE_NOT_INIT | 重试中 | warning (橙色) |
| OFFLINE | 离线 | danger (红色) |

---

### 2. Group 详情展示

#### API 端点
```
GET /api/rocketmq/consumer-groups/{group}/detail
```

#### 详情 Tab 结构

**Tab 1: 消费进度 (ConsumeProgress)**
- 表格展示每个 Topic 的消费进度
- 列: Topic 名称、队列 ID、消费位点、存储位点、堆积量
- 使用 el-table 的 `max-height` 固定高度

**Tab 2: 订阅关系 (Subscription)**
- 表格展示订阅的 Topic 列表
- 列: Topic 名称、过滤表达式、起始位置

**Tab 3: 位点信息 (CurrentOffset)**
- 卡片形式展示各队列消费状态
- 显示消费者在线状态和最后更新时间

---

### 3. 位点重置功能

#### 重置方式
仅支持 **时间戳重置** — 将消费位点重置到指定时间点

#### API 端点
```
POST /api/rocketmq/consumer-groups/{group}/reset-offset
```

#### 请求参数
```json
{
  "topic": "string",
  "timestamp": 1715000000000,
  "force": false
}
```

#### 前端交互
1. 在 Group 详情页面添加「重置位点」按钮
2. 点击后弹出 el-dialog
3. 选择 Topic (el-select)
4. 选择时间 (el-date-picker 支持选择日期和时间)
5. 确认重置 (force 复选框可选)

#### 约束
- 仅对 `CLUSTERING` 类型 Group 生效
- 广播模式不支持位点重置

---

### 4. 组件文件清单

| 用途 | 文件路径 |
|------|----------|
| 列表页面 | `app-vue/src/views/rocketmq/ConsumerGroupList.vue` |
| 详情弹窗 | `app-vue/src/views/rocketmq/ConsumerGroupDetail.vue` |
| 位点重置弹窗 | `app-vue/src/views/rocketmq/ResetOffsetDialog.vue` |
| 后端 Controller | `springboot/.../controller/RocketMQController.java` (扩展) |
| 后端 Service | `springboot/.../service/RocketMQAdminService.java` (扩展) |
| 响应 VO | `springboot/.../vo/ConsumerGroupVO.java` |

---

### 5. 路由配置

```typescript
// app-vue/src/router/index.ts
{
  path: '/rocketmq/consumer-groups',
  name: 'ConsumerGroupList',
  component: () => import('@/views/rocketmq/ConsumerGroupList.vue')
}
```

---

## 风险与约束

### 不实现的功能
- ~~按位点重置~~ — 仅支持时间戳重置（简化实现）
- ~~广播模式位点重置~~ — 广播不支持重置
- ~~实时消费监控~~ — 仅展示静态快照

### 已知限制
- 堆积量统计可能有延迟（依赖 RocketMQ Broker 统计）
- 重置位点需要 Broker 支持（默认开启）

---

## 验证方式

1. **列表页**: 访问 `/rocketmq/consumer-groups` 显示所有 Group
2. **详情页**: 点击 Group 名称打开详情 Tab
3. **重置功能**: 选择 Topic + 时间后提交，验证位点变化
