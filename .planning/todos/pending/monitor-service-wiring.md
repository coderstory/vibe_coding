---
title: 监控模块：MonitorController 未使用 MonitorService
area: backend
date: 2026-05-07
resolves_phase: 23
priority: medium
---

## 问题

MonitorController 直接注入 `StringRedisTemplate` 实现 metrics，完全绕过了已存在的 `MonitorService`。

## 影响

- 前端 `MonitorDashboard.vue` 期望的 `qpsKeys` 字段后端未返回，永远显示 0
- 前端 API 定义的 `getGoodsStock(goodsId)` 无对应后端接口，会 404
- `MonitorService` 已有完整的 getMetrics()（含 qpsKeys）和 getGoodsStockInfo()，但未使用

## 修复方案

### 后端
- `MonitorController` 注入 `MonitorService` 替代 `StringRedisTemplate`
- `getMetrics()` 委托给 `monitorService.getMetrics()`
- 新增 `GET /api/monitor/stock/{goodsId}` 调用 `monitorService.getGoodsStockInfo(goodsId)`
- 删除 `MonitorController` 中直接的 Redis 操作代码
- 注意：`MonitorService.getMetrics()` 返回的 `concurrentCount` 是 Integer，前端 TypeScript 类型已定义为 `number`，兼容

### 前端
- `MonitorDashboard.vue` 已正确渲染 `qpsKeys`，后端修好后自动生效
- `getGoodsStock` API 已定义但 Dashboard 未使用，如需使用需添加相应 UI

## 验证标准

- `GET /api/monitor/metrics` 返回包含 `qpsKeys` 的完整指标
- `GET /api/monitor/stock/1` 返回商品库存信息
- 前端 Dashboard 显示正确的 QPS Key 数量（不再为 0）
