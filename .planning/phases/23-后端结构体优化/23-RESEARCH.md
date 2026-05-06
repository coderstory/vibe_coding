# Phase 23: 后端结构体优化 - Research

**Researched:** 2026-05-07
**Domain:** Java 后端代码结构与目录重构
**Confidence:** HIGH

## Summary

Phase 23 包含 6 项需求（BAC-04~BAC-10），核心是 3 类工作：
1. **拆分上帝类** — RocketMQAdminServiceImpl（1098行）
2. **类型重构** — type-first 目录重构 + DTO/VO/XML 清理 + Service 方法清理
3. **工具类合并** — 验证后确认无需合并

## Key Findings

### 1. RocketMQAdminServiceImpl 拆分方案

**当前接口（RocketMQAdminService）方法分组：**

| 模块 | 方法 | 占比 |
|------|------|------|
| Topic | getTopicList, getTopicDetail, createTopic, deleteTopic | ~25% |
| Consumer Group | getConsumerGroupList, getConsumerGroupDetail, resetConsumerOffset, deleteConsumerGroup | ~25% |
| Message | getMessageList, getMessageDetail, getMessageTrace, sendMessage | ~30% |
| Cluster/Monitor | getClusterOverview, getBrokerStatusList, getTopicBacklogList, getBrokerMetrics | ~20% |

**拆分策略：** 
- 保留 `RocketMQAdminService` 接口不分拆（Controller 只依赖一个接口）
- 实现类拆为 4 个委托类，`RocketMQAdminServiceImpl` 作为 Facade 注入 4 个委托类并委托调用
- 或者直接分拆接口，Controller 按需注入

**推荐方案：** Facade 模式 — 接口不变，实现拆为 4 个内部 Service，原类作为 Facade 委托

### 2. type-first 目录重构方案

**当前结构（domain-first）：**
```
{domain}/controller/XxxController.java     → package {domain}.controller
{domain}/service/XxxService.java           → package {domain}.service
{domain}/service/impl/XxxServiceImpl.java  → package {domain}.service.impl
{domain}/mapper/XxxMapper.java             → package {domain}.mapper
{domain}/entity/Xxx.java                   → package {domain}.entity
```

**目标结构（type-first）：**
```
controller/{domain}/XxxController.java     → package controller.{domain}
service/{domain}/XxxService.java           → package service.{domain}
service/{domain}/impl/XxxServiceImpl.java  → package service.{domain}.impl
mapper/{domain}/XxxMapper.java             → package mapper.{domain}
entity/{domain}/Xxx.java                   → package entity.{domain}
```

**操作步骤（每个域）：**
1. `mkdir -p controller/{domain} service/{domain} ...`
2. `git mv` 每个文件到新目录
3. 用 sed 更新 `package` 声明
4. 全局替换 import 引用
5. `./gradlew.bat build` 验证

**影响范围：** 全部 10 个域，涉及 ~121 个 Java 文件

### 3. DTO/VO 清理

| 类 | 引用情况 | 判断 |
|----|---------|------|
| user/dto/UserVO.java | UserController + UserService 使用 | 保留 |
| seckill/dto/SeckillRequest.java | SeckillController + SeckillService 使用 | 保留 |
| seckill/dto/SeckillResponse.java | SeckillController + SeckillService 使用 | 保留 |
| seckill/vo/ActivityDetailVO.java | SeckillActivityController + ActivityService 使用 | 保留 |
| shared/vo/ApiResponse.java | 全局使用 | 保留 |
| shared/vo/ResultResponse.java | 需要检查 | 待确认 |

### 4. Mapper XML 检查

5 个 XML 文件均为 MyBatis Plus 自定义查询，需逐文件检查是否有冗余映射。

### 5. Service 方法清理

需对每个 Service 实现做全局搜索，确认每个 public 方法至少有一个调用者。

## Key Decisions

- RocketMQ 拆分：Facade 模式（接口不变）
- 目录重构：controller/service/mapper/entity 全量移动
- Service 方法：严格标准，无任何调用才删

## Research Complete

Ready for planning.
