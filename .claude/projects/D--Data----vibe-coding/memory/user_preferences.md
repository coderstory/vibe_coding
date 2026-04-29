---
name: user_preferences
description: User's development preferences and patterns
type: user
---

## 构建偏好

- **Gradle**: 优先使用 Gradle 构建项目
- **Spring Boot 版本**: Spring Boot 4.1.0-RC1 测试中遇到依赖解析问题时会考虑回滚到 4.0.5


## 工作方式

- **执行策略**: 长时间任务使用 subagent 并行执行，不阻塞主会话
- **验证偏好**: 要求用证据（命令输出）支撑断言，不要空口声称成功
- **问题处理**: 遇到 build 失败时，先尝试诊断根因而非立即回滚
- **目录偏好**: 执行任何命令前先确认当前目录是否正确，切换到正确目录后再执行

## 调试偏好

- **Gradle 构建调试**: 先用 `dependencies --configuration` 查看实际解析的依赖，定位 BOM 是否生效
- **Spring Boot 4.1 特有**: `spring-boot-starter-aop` 在 SB4.1 BOM 中可能不存在，需用 `org.springframework:spring-aop:7.0.7` 直接引用
- **AspectJ 依赖**: 仅 `spring-aop` 不够，还需要 `aspectjweaver` 来提供 `org.aspectj.lang` 包

## Git 规范

- **提交信息**: 必须使用中文撰写 commit message

## 反馈风格

- 简洁直接，不要总结
- 发现问题立即告知，不要等到最后
