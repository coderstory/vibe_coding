---
name: user_preferences
description: User's development preferences and patterns
type: user
---


## 工作方式

- **执行策略**: 长时间任务使用 subagent 并行执行，不阻塞主会话
- **验证偏好**: 要求用证据（命令输出）支撑断言，不要空口声称成功
- **问题处理**: 遇到 build 失败时，先尝试诊断根因而非立即回滚
- **目录偏好**: 执行任何命令前先确认当前目录是否正确，切换到正确目录后再执行

## Git 规范

- **提交信息**: 必须使用中文撰写 commit message

## 编辑规范

- **Edit 工具**: 避免跨段落匹配，每段单独替换，用最小的唯一字符串作为 old_string
- **Edit 前**: 先读取完整文件，确认 old_string 精确匹配后再操作

## 反馈风格

- 简洁直接，不要总结
- 发现问题立即告知，不要等到最后
