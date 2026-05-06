---
name: common_pitfalls
description: 项目历史中反复出现的错误模式与修复方案
type: reference
originSessionId: a022f3ae-f37d-4f8a-ab92-32c0d326b4f6
---
## 1. Windows 中文路径编码问题

**触发场景**: Gradle 执行测试，项目路径包含中文（如 "桌面"）
**错误信息**: `ClassNotFoundException`（测试类明明存在）
**根因**: Windows 中文编码问题，bash shell 传递给 JVM 的路径编码不正确
**修复**: `build.gradle.kts` 中配置 `-Dfile.encoding=GBK`
**如何避免**: 新加测试配置或 JVM 参数时，保留 `file.encoding=GBK` 设置

## 2. localhost DNS 解析失败

**触发场景**: 应用启动/数据库连接/RocketMQ 连接
**错误信息**: `UnknownHostException: localhost` 或数据库连接超时
**根因**: Windows DNS 解析 intermittent 失败
**修复**: 所有配置文件和代码中使用 `127.0.0.1` 替代 `localhost`
**如何避免**: 新配置数据库、Redis、RocketMQ 连接时，用 IP 而非主机名

## 3. RocketMQ 5.x API 包路径变更

**触发场景**: 引入新版本 RocketMQ 依赖或使用新 API
**错误信息**: `ClassNotFoundException`，特定类找不到
**常见变更**:
- `TopicList` → `remoting.protocol.body.TopicList`
- `TopicRouteData` → `remoting.protocol.route.TopicRouteData`
- `MessageQueue` 构造使用 `brokerName` 而非 IP 地址
**如何避免**: 升级 RocketMQ 版本后检查 API 包路径

## 4. Edit 工具跨段落匹配误删

**触发场景**: 使用 Edit 工具修改文件，`old_string` 不唯一
**后果**: 误删文件中其他匹配内容
**修复**: 无——内容丢失需从 git 恢复
**如何避免**:
- 编辑前先完整读取文件
- 使用最小唯一字符串定位
- 不跨段落匹配
- 不确定时直接用 Write 重写整个文件

## 5. 依赖版本对齐问题

**触发场景**: 升级 Spring Boot 版本或添加新依赖
**错误信息**: Bean 注入失败、编译错误、运行时异常
**常见案例**:
- Lombok 版本与 Spring Boot 不兼容
- `spring-aop` 版本与 Spring Boot 版本不匹配
- `spring-boot-starter-webmvc` 与 `spring-boot-starter-web` 混用
**如何避免**: 使用 `libs.versions.toml` 统一管理版本号

## 6. GSD 命令语法混淆

**触发场景**: 在 Claude Code 中使用 GSD 命令
**错误**: `Unknown command: /gsd-plan-phase`
**根因**: 正确语法使用冒号 `/gsd:plan-phase`（或 `/gsd-plan-phase` 取决于 CLI 版本）
**正确用法**: `gsd:plan-phase`, `gsd:execute-phase`, `gsd:discuss-phase`

## 7. Gradle 构建缓存问题

**触发场景**: 修改代码后构建仍使用缓存导致不一致
**修复**: `./gradlew.bat clean build` 或删除 `build/` 目录
**注意**: Gradle 9.x 缓存机制更强，必要时使用 `--no-build-cache`

## 8. SSE 连接时序问题（秒杀系统）

**触发场景**: 秒杀系统的 SSE 通知
**问题**: 先建立 SSE 连接再发请求 vs 先发请求再连接 SSE 的时序竞争
**修复**: 前端先生成 `queueId`，再建立 SSE 连接，解决时序问题
**如何避免**: SSE 相关功能注意连接建立与消息发送的顺序
