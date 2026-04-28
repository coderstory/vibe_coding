# Oh My OpenCode 用户手册

**Oh My OpenCode (OmO)** — 面向 OpenCode 的 AI Agent 增强插件，提供多模型编排、并行后台 Agent、LSP/AST 工具链。

---

## 目录

1. [简介](#简介)
2. [安装配置](#安装配置)
3. [核心概念](#核心概念)
4. [Agent 系统](#agent-系统)
5. [核心命令](#核心命令)
6. [技能系统](#技能系统)
7. [配置指南](#配置指南)

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
| **后台 Agent** | 同时运行 5+ 个专家 Agent，保持上下文干净 |
| **内置 MCP** | Exa（搜索）、Context7（文档）、Grep.app（GitHub）默认开启 |
| **Ralph Loop** | 自我引用循环，100% 完成前绝不停止 |
| **Todo Enforcer** | Agent 空闲时强制拉回继续工作 |
| **Comment Checker** | 清除 AI 味的注释，代码如老手所写 |

### 订阅建议

仅需以下订阅之一即可流畅运行 ultrawork（本项目无关联，纯个人推荐）：

- [ChatGPT 订阅 ($20)](https://chatgpt.com/)
- [Kimi Code 订阅 ($0.99)](https://www.kimi.com/kimiplus/sale)
- [GLM Coding 套餐 ($10)](https://z.ai/subscribe)

---

## 安装配置

### 方式一：让 Agent 自动安装（推荐）

将以下内容发给 Agent：

```
Install and configure oh-my-opencode by following the instructions here:
https://raw.githubusercontent.com/code-yeongyu/oh-my-openagent/refs/heads/dev/docs/guide/installation.md
```

### 方式二：手动安装

1. 在 `opencode.json` 中添加插件：

```json
{
  "plugin": ["oh-my-openagent"]
}
```

2. 重启 OpenCode

### 验证安装

```bash
bunx oh-my-opencode doctor
```

该命令会验证插件注册、配置、模型和环境。

---

## 核心概念

### Agent 类别映射

当主 Agent 分配任务时，选择的是**类别**而非具体模型：

| 类别 | 用途 | 默认模型 |
|------|------|----------|
| `visual-engineering` | 前端、UI/UX、设计 | 根据配置 |
| `deep` | 深度自主调研与执行 | 根据配置 |
| `quick` | 单文件修改、错字修正 | 根据配置 |
| `ultrabrain` | 复杂硬核逻辑、架构决策 | GPT-5.4 xhigh |

### Hashline 编辑

每次读取文件时，每行末尾带有内容哈希：

```
11#VK| function hello() {
22#XJ|   return "world";
33#MB| }
```

编辑时通过 `LINE#ID` 引用。如果文件已修改，哈希不匹配则拒绝编辑，防止改错行。

### 意图门 (IntentGate)

在分类或行动前分析用户的真实意图，避免字面误解。

---

## Agent 系统

### 自律军团 (Discipline Agents)

#### Sisyphus
主指挥官 (`claude-opus-4-6` / `kimi-k2.5` / `glm-5`)

- 制定计划、分配任务给专家
- 以激进并行策略推动任务完成
- 永不半途而废

#### Hephaestus
自主深度工作者 (`gpt-5.4`)

- 只需目标，不需要具体做法
- 自动探索代码库、研究模式、端到端执行
- **"正牌工匠"** — 不需要手把手指导

#### Prometheus
战略规划师 (`claude-opus-4-6` / `kimi-k2.5` / `glm-5`)

- 访谈模式：在动手前先通过提问确定范围
- 构建详细计划后才开始编码
- 避免"Prompt 然后祈祷"式开发

### 内置 Agent

| Agent | 类型 | 功能 |
|-------|------|------|
| **Sisyphus** | 编排器 | 主 Agent，调度其他 Agent |
| **Prometheus** | 规划师 | 访谈式战略规划 |
| **Hephaestus** | 执行者 | 自主深度工作 |
| **Oracle** | 咨询师 | 架构决策、调试指导 |
| **Librarian** | 文档搜索 | 外部文档、OSS 示例 |
| **Explore** | 代码搜索 | 快速代码库 grep |
| **Metis** | 计划顾问 | 分析需求、识别歧义 |
| **Momus** | 计划评审 | 评估计划质量 |
| **Multimodal Looker** | 媒体分析 | PDF、图片、图表解读 |

### 后台 Agent

并行启动 5+ 个专家 Agent：

- 上下文保持干净
- 结果完成后汇总
- 不相互干扰

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

### 内置命令

| 命令 | 功能 |
|------|------|
| `/stop-continuation` | 停止所有续接机制 |
| `/handoff` | 创建会话交接文档 |
| `/remove-ai-slops` | 清除代码中的 AI 味 |
| `/playwright` | 浏览器自动化技能 |
| `/dev-browser` | 浏览器自动化 |
| `/frontend-ui-ux` | 设计优先 UI 实现 |
| `/git-master` | 原子提交、rebase 操作 |
| `/review-work` | 完成后交叉评审 |

---

## 技能系统

### 内置技能

| 技能 | 功能 |
|------|------|
| `playwright` | 浏览器自动化测试验证 |
| `git-master` | 原子提交、rebase、history 分析 |
| `frontend-ui-ux` | 设计感拉满的 UI 实现 |
| `brainstorming` | 创意头脑风暴 |
| `systematic-debugging` | 系统化调试 |
| `test-driven-development` | TDD 测试驱动开发 |
| `verification-before-completion` | 完成前验证 |
| `using-git-worktrees` | Git worktree 隔离 |
| `executing-plans` | 执行计划 |
| `writing-plans` | 编写计划 |
| `subagent-driven-development` | 子 Agent 驱动开发 |
| `finishing-a-development-branch` | 完成开发分支 |
| `receiving-code-review` | 接收代码审查 |
| `requesting-code-review` | 请求代码审查 |
| `dispatching-parallel-agents` | 并行 Agent 分派 |

### 自定义技能

添加位置：
- `.opencode/skills/*/SKILL.md` — 项目级
- `~/.config/opencode/skills/*/SKILL.md` — 用户级

技能自带：
- 领域调优的系统指令
- 按需加载的 MCP 服务器
- 作用域权限约束

### Skill-Embedded MCPs

技能携带自己的 MCP 服务器，按需启动，任务完成后销毁，不会撑爆上下文窗口。

---

## 配置指南

### 配置文件位置

- 全局：`~/.config/opencode/oh-my-opencode.json[c]`
- 项目级：`.opencode/oh-my-open-code.json[c]`
- 兼容旧名：`oh-my-openagent.json[c]`

### 基本配置结构

```json
{
  "agents": {
    "sisyphus": {
      "model": "claude-opus-4-6",
      "temperature": 0.7
    },
    "hephaestus": {
      "model": "gpt-5.4"
    },
    "prometheus": {
      "model": "claude-opus-4-6"
    }
  },
  "categories": {
    "visual-engineering": {
      "model": "claude-sonnet-4-6"
    },
    "ultrabrain": {
      "model": "gpt-5.4-xhigh"
    }
  },
  "mcpServers": {
    "websearch": {
      "enabled": true
    },
    "context7": {
      "enabled": true
    }
  },
  "disabled_hooks": [],
  "experimental": {
    "aggressive_truncation": true
  }
}
```

### 禁用匿名遥测

```bash
export OMO_SEND_ANONYMOUS_TELEMETRY=0
# 或
export OMO_DISABLE_POSTHOG=1
```

### 模型回退配置

`fallback_models` 支持混合配置：

```json
{
  "fallback_models": [
    "claude-opus-4-6",
    {
      "model": "claude-sonnet-4-6",
      "maxTokens": 100000
    },
    "gpt-5.4"
  ]
}
```

---

## 卸载

1. 从 `opencode.json` 的 plugin 数组中移除 `"oh-my-openagent"` 或 `"oh-my-opencode"`

2. 清除配置（可选）：
```bash
rm -f ~/.config/opencode/oh-my-openagent.jsonc
rm -f ~/.config/opencode/oh-my-opencode.jsonc
rm -f .opencode/oh-my-openagent.jsonc
rm -f .opencode/oh-my-open-code.jsonc
```

3. 验证：
```bash
opencode --version
```

---

## 快速参考

| 场景 | 命令 |
|------|------|
| 启动深度工作 | `/start-work` |
| 持续工作直到完成 | `/ulw-loop` |
| 快速激活 | `/ultrawork` |
| 生成 AGENTS.md | `/init-deep` |
| 智能重构 | `/refactor` |
| 浏览器自动化 | `/playwright` |
| 清除 AI 味 | `/remove-ai-slops` |
| 诊断问题 | `bunx oh-my-opencode doctor` |

---

*最后更新：2026-04-28*
