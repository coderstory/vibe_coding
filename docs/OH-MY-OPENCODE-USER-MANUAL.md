# Oh My OpenCode 用户手册

**Oh My OpenCode (OmO)** — 面向 OpenCode 的 AI Agent 增强插件，提供多模型编排、并行后台 Agent、LSP/AST 工具链。

> 官方文档：https://ohmyopenagent.com/zh/docs

---

## 目录

1. [简介](#简介)
2. [安装配置](#安装配置)
3. [配置文件](#配置文件)
4. [Agents](#agents)
5. [分类](#分类)
6. [Skills](#skills)
7. [后台任务](#后台任务)
8. [钩子](#钩子)
9. [MCPs](#mcps)
10. [浏览器自动化](#浏览器自动化)
11. [Tmux 集成](#tmux-集成)
12. [Git Master](#git-master)
13. [注释检查器](#注释检查器)
14. [实验性功能](#实验性功能)
15. [LSP 配置](#lsp-配置)
16. [环境变量](#环境变量)

---

## 简介

### 什么是 Oh My OpenCode？

OmO 是 OpenCode 的增强插件，将多个 AI 模型组合成一支完整的开发团队。各司其职，并行运转，永不停歇。

### 核心特性

| 特性 | 说明 |
|------|------|
| **Discipline Agents** | Sisyphus 调度 Hephaestus、Oracle、Librarian、Explore 并行工作 |
| **ultrawork / ulw** | 一键触发所有 Agent，任务完成前绝不停止 |
| **IntentGate** | 分析用户真实意图，避免字面误解 |
| **Hash-Anchored Edits** | 基于内容哈希的编辑工具，零错改行 |
| **LSP + AST-Grep** | IDE 级别精度的重构、搜索、诊断 |
| **后台 Agent** | 同时运行多个专家 Agent，保持上下文干净 |
| **内置 MCP** | Exa（搜索）、Context7（文档）、Grep.app（GitHub）默认开启 |
| **Ralph Loop** | 自我引用循环，100% 完成前绝不停止 |
| **Todo Enforcer** | Agent 空闲时强制拉回继续工作 |

### 订阅建议

仅需以下订阅之一即可流畅运行 ultrawork（本项目无关联，纯个人推荐）：

- [ChatGPT 订阅 ($20)](https://chatgpt.com/)
- [Kimi Code 订阅 ($0.99)](https://www.kimi.com/kimiplus/sale)
- [GLM Coding 套餐 ($10)](https://z.ai/subscribe)

---

## 安装配置

### 自动安装（推荐）

将以下内容发给 Agent：

```
Install and configure oh-my-opencode by following the instructions here:
https://raw.githubusercontent.com/code-yeongyu/oh-my-openagent/refs/heads/dev/docs/guide/installation.md
```

### 手动安装

1. 确保已安装 OpenCode
2. 在 `opencode.json` 中添加插件：

```json
{
  "plugin": ["oh-my-openagent"]
}
```

3. 重启 OpenCode

### 验证安装

```bash
bunx oh-my-opencode doctor
```

该命令会验证插件注册、配置、模型和环境。

---

## 配置文件

### 配置文件位置

| 级别 | 路径 |
|------|------|
| 项目级 | `.opencode/oh-my-openagent.json` |
| 用户级 | `~/.config/opencode/oh-my-openagent.json` |

支持 JSONC 格式，允许注释和尾随逗号。

### 快速配置示例

```json
{
  "$schema": "https://raw.githubusercontent.com/code-yeongyu/oh-my-openagent/master/assets/oh-my-openagent.schema.json",
  "agents": {
    "oracle": { "model": "openai/gpt-5.4", "variant": "high" },
    "explore": { "model": "github-copilot/grok-code-fast-1" }
  },
  "categories": {
    "quick": { "model": "opencode/gpt-5-nano" },
    "visual-engineering": { "model": "google/gemini-3.1-pro" }
  }
}
```

---

## Agents

### 内置 Agents

OmO 提供以下内置 Agent：

| Agent | 类型 | 功能 |
|-------|------|------|
| **Sisyphus** | 编排器 | 主 Agent，调度其他 Agent |
| **Hephaestus** | 执行者 | 自主深度工作 |
| **Prometheus** | 规划师 | 访谈式战略规划 |
| **Metis** | 计划顾问 | 分析需求、识别歧义 |
| **Momus** | 计划评审 | 评估计划质量 |
| **Oracle** | 咨询师 | 架构决策、调试指导 |
| **Librarian** | 文档搜索 | 外部文档、OSS 示例 |
| **Explore** | 代码搜索 | 快速代码库 grep |
| **Atlas** | 上下文管理 | 上下文注入优化 |
| **Multimodal Looker** | 媒体分析 | PDF、图片、图表解读 |
| **Sisyphus Junior** | 任务执行 | 专注任务执行 |

### Agent 配置选项

```json
{
  "agents": {
    "sisyphus": {
      "model": "claude-opus-4-6",
      "temperature": 0.7,
      "maxTokens": 100000,
      "tools": {
        "bash": "allow"
      }
    },
    "oracle": {
      "model": "openai/gpt-5.4",
      "variant": "high"
    },
    "explore": {
      "model": "github-copilot/grok-code-fast-1"
    }
  }
}
```

| 选项 | 类型 | 说明 |
|------|------|------|
| `model` | string | 模型标识符（如 `openai/gpt-4o`） |
| `variant` | string | 模型变体（`max`、`high`、`medium`、`low`） |
| `temperature` | number | 采样温度（0-2） |
| `top_p` | number | Top-p 采样（0-1） |
| `maxTokens` | number | 最大 token 数 |
| `prompt` | string | 完全覆盖系统提示词 |
| `prompt_append` | string | 在系统提示词后追加文本 |
| `disable` | boolean | 禁用此 Agent |
| `thinking` | object | 扩展思考配置 |
| `reasoningEffort` | string | 推理强度（`low`、`medium`、`high`、`xhigh`） |

### Agent 权限

| 权限 | 值 | 说明 |
|------|---|------|
| `edit` | `ask` / `allow` / `deny` | 文件编辑能力 |
| `bash` | `ask` / `allow` / `deny` | Bash 命令执行 |
| `webfetch` | `ask` / `allow` / `deny` | 网络请求能力 |
| `doom_loop` | `ask` / `allow` / `deny` | 无限循环覆盖 |
| `external_directory` | `ask` / `allow` / `deny` | 访问项目外部文件 |

---

## 分类

分类允许定义 Agent 可继承的共享配置。

### 默认分类

| 分类 | 默认模型 | 用途 |
|------|----------|------|
| `visual-engineering` | gemini-3.1-pro (high) | 前端、UI/UX、设计任务 |
| `ultrabrain` | gpt-5.3-codex (xhigh) | 深度逻辑推理 |
| `deep` | gpt-5.3-codex (medium) | 自主解决问题与深入研究 |
| `artistry` | gemini-3.1-pro (high) | 创意任务 |
| `quick` | claude-haiku-4-5 | 简单、快速任务 |
| `unspecified-low` | claude-sonnet-4-6 | 低投入一般任务 |
| `unspecified-high` | gpt-5.4 (high) | 高投入一般任务 |
| `writing` | gemini-3-flash | 文档和写作 |

### 分类配置选项

可用选项：`model`、`variant`、`temperature`、`top_p`、`maxTokens`、`thinking`、`reasoningEffort`、`textVerbosity`、`tools`、`prompt_append`、`is_unstable_agent`

---

## Skills

### 内置 Skills

- `playwright` — 完整浏览器自动化
- `agent-browser` — 轻量级浏览器 Agent
- `git-master` — 原子提交、rebase 操作

### 自定义 Skills

```json
{
  "skills": {
    "my-custom-skill": {
      "description": "A custom skill for specific tasks",
      "instructions": "Always use this skill when..."
    }
  }
}
```

添加位置：
- `.opencode/skills/*/SKILL.md` — 项目级
- `~/.config/opencode/skills/*/SKILL.md` — 用户级

---

## 后台任务

配置后台任务的并发限制：

```json
{
  "backgroundTasks": {
    "defaultConcurrency": 5,
    "staleTimeoutMs": 300000,
    "providerConcurrency": {
      "openai": 3,
      "anthropic": 5
    },
    "modelConcurrency": {
      "gpt-5.4": 2
    }
  }
}
```

优先级：`modelConcurrency` > `providerConcurrency` > `defaultConcurrency`

---

## 钩子

钩子允许在各个生命周期点扩展功能。

### 内置钩子列表

| 钩子 | 功能 |
|------|------|
| `agent-usage-reminder` | Agent 使用提醒 |
| `anthropic-context-window-limit-recovery` | 上下文窗口限制恢复 |
| `anthropic-effort` | Anthropic 努力级别 |
| `atlas` | 上下文管理 |
| `auto-slash-command` | 自动斜杠命令 |
| `auto-update-checker` | 自动更新检查 |
| `background-notification` | 后台通知 |
| `category-skill-reminder` | 分类技能提醒 |
| `claude-code-hooks` | Claude Code 兼容钩子 |
| `comment-checker` | 注释检查器 |
| `compaction-context-injector` | 压缩上下文注入 |
| `compaction-todo-preserver` | 压缩 Todo 保留 |
| `delegate-task-retry` | 委托任务重试 |
| `directory-agents-injector` | 目录 Agent 注入 |
| `directory-readme-injector` | 目录 README 注入 |
| `edit-error-recovery` | 编辑错误恢复 |
| `interactive-bash-session` | 交互式 Bash 会话 |
| `keyword-detector` | 关键词检测 |
| `non-interactive-env` | 非交互环境 |
| `prometheus-md-only` | Prometheus 仅 MD 模式 |
| `question-label-truncator` | 问题标签截断 |
| `ralph-loop` | Ralph 循环 |
| `rules-injector` | 规则注入 |
| `session-recovery` | 会话恢复 |
| `sisyphus-junior-notepad` | Sisyphus Junior 记事本 |
| `start-work` | 开始工作 |
| `stop-continuation-guard` | 停止续接保护 |
| `subagent-question-blocker` | 子 Agent 问题阻止 |
| `task-reminder` | 任务提醒 |
| `task-resume-info` | 任务恢复信息 |
| `tasks-todowrite-disabler` | 禁用 TodoWrite |
| `think-mode` | 思考模式 |
| `thinking-block-validator` | 思考块验证 |
| `unstable-agent-babysitter` | 不稳定 Agent 看护 |
| `write-existing-file-guard` | 写已存在文件保护 |

---

## MCPs

### 内置 MCP

| MCP | 驱动 | 功能 |
|-----|------|------|
| `websearch` | Exa | 高质量网络搜索 |
| `context7` | Context7 | 官方文档检索 |
| `grep_app` | Grep.app | GitHub 代码搜索 |

---

## 浏览器自动化

| 工具 | 说明 | 使用场景 |
|------|------|----------|
| `playwright` | 完整浏览器自动化（默认） | 测试、复杂交互 |
| `agent-browser` | 轻量级浏览器 Agent | 快速查询、简单抓取 |

---

## Tmux 集成

```json
{
  "tmux": {
    "enabled": true,
    "layout": "main-vertical",
    "main_pane_size": "70%"
  }
}
```

| 选项 | 类型 | 说明 |
|------|------|------|
| `enabled` | boolean | 启用 Tmux 集成 |
| `layout` | string | Tmux 窗口布局 |
| `main_pane_size` | string | 主面板大小 |

---

## Git Master

```json
{
  "gitMaster": {
    "commit_footer": "Generated by Oh My OpenCode",
    "include_co_authored_by": true
  }
}
```

| 选项 | 类型 | 说明 |
|------|------|------|
| `commit_footer` | string | 追加到提交消息的文本 |
| `include_co_authored_by` | boolean | 添加 Co-authored-by 标记 |

---

## 注释检查器

验证代码中的注释。在自定义提示词中使用 `{{comments}}` 占位符。

```json
{
  "comment-checker": {
    "custom_prompt": "Review these comments: {{comments}}"
  }
}
```

---

## 实验性功能

```json
{
  "experimental": {
    "aggressive_truncation": true,
    "auto_resume": true,
    "preemptive_compaction": true,
    "truncate_all_tool_outputs": true,
    "dynamic_context_pruning": true
  }
}
```

| 选项 | 类型 | 说明 |
|------|------|------|
| `aggressive_truncation` | boolean | 积极截断输出 |
| `auto_resume` | boolean | 自动恢复中断的任务 |
| `preemptive_compaction` | boolean | 在达到限制前压缩上下文 |
| `truncate_all_tool_outputs` | boolean | 截断所有工具输出 |
| `dynamic_context_pruning` | boolean | 动态上下文修剪 |

---

## LSP 配置

```json
{
  "lsp": {
    "typescript": {
      "command": "typescript-language-server",
      "extensions": [".ts", ".tsx"],
      "priority": 1,
      "env": {
        "TSS_LOG_LEVEL": "warning"
      }
    }
  }
}
```

| 选项 | 类型 | 说明 |
|------|------|------|
| `command` | string | LSP 服务器命令 |
| `extensions` | array | 匹配的文件扩展名 |
| `priority` | number | 服务器优先级 |
| `env` | object | 环境变量 |
| `initialization` | object | 初始化选项 |
| `disabled` | boolean | 禁用此 LSP |

---

## 环境变量

### OPENCODE_CONFIG_DIR

覆盖默认配置目录路径。

### OMO_SEND_ANONYMOUS_TELEMETRY

禁用匿名遥测：

```bash
export OMO_SEND_ANONYMOUS_TELEMETRY=0
# 或
export OMO_DISABLE_POSTHOG=1
```

---

## 核心命令

### /start-work
启动 Sisyphus 工作会话，从 Prometheus 计划开始。

```
/start-work
```

调用 Prometheus 进行访谈式规划，建立详细计划后再开始执行。

### /ulw-loop 或 /ralph-loop
启动自我引用循环，持续工作直到 100% 完成。

```
/ulw-loop
或
/ralph-loop
```

Agent 会自动追踪 Todo，任务完成前绝不停止。

### /ultrawork 或 /ulw
一键触发所有相关 Agent，全面激活。

```
/ultrawork
或
/ulw
```

### /init-deep
自动生成层级式 AGENTS.md 文件：

```
project/
├── AGENTS.md              ← 全局级架构
├── src/
│   ├── AGENTS.md          ← src 级规范
│   └── components/
│       └── AGENTS.md      ← 组件级说明
```

Agent 会自动读取相关上下文。

### /refactor
智能重构命令，结合 LSP 和 AST-Grep：

```
/refactor
```

提供：
- Workspace rename
- Pre-build diagnostics
- AST-aware rewrites

---

## 卸载

1. 从 `opencode.json` 的 plugin 数组中移除 `"oh-my-openagent"`

2. 清除配置（可选）：
```bash
rm -f ~/.config/opencode/oh-my-openagent.jsonc
rm -f .opencode/oh-my-openagent.jsonc
```

3. 验证：
```bash
opencode --version
```

---

## 快速参考

| 场景 | 命令/操作 |
|------|-----------|
| 启动深度工作 | `/start-work` |
| 持续工作直到完成 | `/ulw-loop` 或 `/ralph-loop` |
| 快速激活 | `/ultrawork` 或 `/ulw` |
| 生成 AGENTS.md | `/init-deep` |
| 智能重构 | `/refactor` |
| 浏览器自动化 | `/playwright` |
| 清除 AI 味 | `/remove-ai-slops` |
| 诊断问题 | `bunx oh-my-opencode doctor` |

---

## 相关资源

- 官方文档：https://ohmyopenagent.com/zh/docs
- GitHub：https://github.com/code-yeongyu/oh-my-openagent
- Discord：https://discord.gg/PUwSMR9XNk

---

*最后更新：2026-04-28*
