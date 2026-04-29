---
status: complete
phase: 10-consumer-group-mgmt
source: 10-01-SUMMARY.md
started: 2026-04-28T17:00:00Z
updated: 2026-04-28T18:50:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Backend API - Consumer Group List
expected: |
  后端 API 响应正确格式的数据：
  GET /api/rocketmq/consumer-groups?keyword=xxx
  返回 { records: [...], total: N } 格式
  每条记录包含: group, groupType, status, consumerCount, accumulatedDiff
result: pass

### 2. Backend API - Consumer Group Detail
expected: |
  GET /api/rocketmq/consumer-groups/{group}
  返回 Group 详细信息，包含:
  - group, groupType, status, consumerCount, totalDiff
  - offsetTable (Topic-QueueId -> offset 映射)
result: pass

### 3. Backend API - Reset Consumer Offset
expected: |
  POST /api/rocketmq/consumer-groups/{group}/reset-offset
  Body: { topic: "xxx", timestamp: N }
  返回成功消息，仅 CLUSTERING 类型可调用
  BROADCASTING 类型返回错误
result: pass

### 4. Frontend - Consumer Group List Page
expected: |
  访问 /rocketmq/consumer-groups
  显示:
  - 搜索表单（Group 名称输入框、查询按钮、重置按钮）
  - 表格（序号、Group名称、类型、状态、消费者数、堆积量、操作列）
  - 类型标签: 广播(蓝色)/集群(紫色)
  - 状态标签: 正常(绿色)/重试中(橙色)/离线(红色)
result: pass

### 5. Frontend - Consumer Group Detail Dialog
expected: |
  点击 Group 名称打开详情弹窗
  显示 3 个 Tab:
  - 消费进度（Topic/队列/位点/堆积）
  - 订阅关系（Topic/过滤表达式/起始位置）
  - 位点信息（各队列消费状态卡片）
  - CLUSTERING 类型显示"重置位点"按钮
  - BROADCASTING 类型不显示"重置位点"按钮
result: pass

### 6. Frontend - Reset Offset Dialog
expected: |
  点击"重置位点"按钮
  弹窗包含:
  - Topic 下拉选择器
  - 日期时间选择器
  - 确认/取消按钮
  提交后调用 resetConsumerOffset API
result: pass

### 7. Compilation - Backend
expected: |
  后端编译成功:
  cd springboot; mvn compile
  无语法错误、无类型错误
result: pass

### 8. Compilation - Frontend
expected: |
  前端构建成功:
  cd app-vue; npm run build
  无 TypeScript 错误
result: pass

## Summary

total: 8
passed: 8
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps

[none yet]