---
plan: 27-02
phase: 27-注释标准定义
status: complete
created: 2026-05-08
---

## TODO 回溯处理报告

### 搜索范围

| 范围 | 路径 | 文件类型 |
|------|------|----------|
| 后端 Java | springboot/src/ (排除 test/) | *.java |
| 前端 TS/Vue/JS | app-vue/src/ | *.ts, *.vue, *.js |
| 配置 YAML | springboot/src/main/resources/ | *.yaml, *.yml |
| Gradle 构建 | springboot/ | build.gradle.kts |
| Lint 配置 | app-vue/ | eslint.config.js, stylelint.config.js |

### 分类结果

#### A 类 — 有效 TODO + 已有 Issue 引用

无。

#### B 类 — 有效 TODO + 无 Issue（需要更新）

| # | 文件 | 行号 | 原文 | 分配 Issue | 更新后格式 |
|---|------|------|------|-----------|-----------|
| B-1 | springboot/src/main/java/cn/coderstory/springboot/service/seckill/impl/ReservationNotifyServiceImpl.java | 111 | `// TODO: 实现实际的提醒通知逻辑` | `#TODO-1` | `// TODO(#TODO-1): 实现实际的提醒通知逻辑` |
| B-2 | app-vue/src/components/layout/AppHeader.vue | 41 | `// TODO: 跳转到个人中心` | `#TODO-2` | `// TODO(#TODO-2): 跳转到个人中心` |

#### C 类 — 已完成/过期 TODO

无。

#### D 类 — 含义不明确

无。

### 汇总

| 类别 | 数量 | 处理方式 |
|------|------|----------|
| A — 已有 Issue 引用 | 0 | 保持不变 |
| B — 有效 + 无 Issue | 2 | 更新为 `TODO(#TODO-N):` 格式 |
| C — 已完成/过期 | 0 | 删除 |
| D — 含义不明确 | 0 | 标记待确认 |
| **合计** | **2** | |
