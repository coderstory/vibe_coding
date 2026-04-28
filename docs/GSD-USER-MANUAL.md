# GSD 用户手册

**Get Shit Done** — 为 AI 编程工具设计的分层项目规划框架，优化用于单人代理开发。

---

## 目录

1. [简介](#简介)
2. [快速开始](#快速开始)
3. [核心工作流](#核心工作流)
4. [命令参考](#命令参考)
5. [文件结构](#文件结构)
6. [常见工作流](#常见工作流)

---

## 简介

### 什么是 GSD？

GSD（Get Shit Done）是一个结构化的项目规划框架，通过分层阶段计划实现复杂项目的有序开发。

### 核心设计原则

- **分层规划**：项目 → 阶段 → 计划 → 任务
- **验证驱动**：每个阶段完成后验证目标达成
- **原子提交**：每个任务独立提交，便于追踪和回滚
- **工作树隔离**：并行计划在独立 git worktree 中执行，避免冲突

### 关键概念

| 概念 | 说明 |
|------|------|
| **Project** | 项目愿景和总体目标 |
| **Phase** | 阶段，一次开发迭代 |
| **Plan** | 计划，一个阶段内的具体实现方案 |
| **Wave** | 波次，并行执行的计划组 |
| **Checkpoint** | 检查点，需要人工交互的暂停点 |

---

## 快速开始

### 基础流程

```
/gsd-new-project        → 初始化项目
/gsd-plan-phase 1       → 为第一阶段创建计划
/gsd-execute-phase 1    → 执行第一阶段所有计划
```

### 更新 GSD

```bash
npx get-shit-done-cc@latest
```

---

## 核心工作流

### 1. 项目初始化 (/gsd-new-project)

创建新项目，包含完整的调研和规划流程：

```
用户问答 → (可选)领域调研 → 需求定义 → 路线图创建
```

**产出文件：**
- `PROJECT.md` — 项目愿景
- `config.json` — 工作流配置
- `research/` — 领域调研（如果选择）
- `REQUIREMENTS.md` — 需求清单（带 REQ-ID）
- `ROADMAP.md` — 阶段路线图
- `STATE.md` — 项目状态

### 2. 阶段讨论 (/gsd-discuss-phase N)

在规划前澄清阶段目标和边界：

- 捕获对阶段的想象和期望
- 创建 `CONTEXT.md` 记录用户决策
- 使用 `--batch` 批量提问模式

### 3. 阶段规划 (/gsd-plan-phase N)

为特定阶段创建详细执行计划：

- 生成 `phases/XX-name/XX-YY-PLAN.md`
- 拆分为具体可执行的任务
- 包含验证标准和成功衡量指标
- 支持多计划（`XX-01`、`XX-02`等）

### 4. 阶段执行 (/gsd-execute-phase N)

执行阶段内所有计划，波次并行：

```
Wave 1: 计划1, 计划2 (并行)
  ↓
检查点 (如有)
  ↓
Wave 2: 计划3 (等 Wave 1 完成)
  ↓
验证阶段目标
```

---

## 命令参考

### 项目管理

#### /gsd-new-project
初始化新项目。

```
/gsd-new-project
```

包含：深度问答 → (可选)调研 → 需求定义 → 路线图创建

#### /gsd-new-milestone
为新里程碑初始化。

```
/gsd-new-milestone "v2.0 功能"
/gsd-new-milestone --reset-phase-numbers "v2.0 功能"
```

#### /gsd-map-codebase
为现有代码库创建映射文档。

```
/gsd-map-codebase
```

用于棕地项目（已有代码），在 `new-project` 前运行。

---

### 阶段管理

#### /gsd-discuss-phase \<number\>
讨论阶段目标和边界。

```
/gsd-discuss-phase 2
/gsd-discuss-phase 2 --batch
```

#### /gsd-plan-phase \<number\>
创建阶段执行计划。

```
/gsd-plan-phase 1
/gsd-plan-phase 1 --prd path/to/prd.md  # PRD 快速路径
/gsd-plan-phase 1 --gaps  # 填补验证缺口
```

#### /gsd-execute-phase \<phase\>
执行阶段内所有计划。

```
/gsd-execute-phase 5
/gsd-execute-phase 5 --wave 2  # 只执行第二波
/gsd-execute-phase 5 --interactive  # 交互式执行
```

#### /gsd-complete-milestone \<version\>
完成里程碑并归档。

```
/gsd-complete-milestone 1.0.0
```

---

### 进度与状态

#### /gsd-progress
查看项目进度。

```
/gsd-progress
```

显示：进度条、当前阶段、最近工作、下一步建议

#### /gsd-resume-work
恢复之前的工作。

```
/gsd-resume-work
```

#### /gsd-pause-work
暂停工作并保存上下文。

```
/gsd-pause-work
```

---

### 快速任务

#### /gsd-quick
执行小任务的快速路径。

```
/gsd-quick
/gsd-quick --full  # 完整质量流程
/gsd-quick --research --validate
```

任务保存在 `.planning/quick/` 而非阶段目录。

#### /gsd-fast
内联执行琐碎任务。

```
/gsd-fast "fix typo in README"
/gsd-fast "add .env to gitignore"
```

无子代理、无计划文件，最多 3 个文件编辑。

---

### 调研与探索

#### /gsd-research-phase \<number\>
为阶段进行领域生态研究。

```
/gsd-research-phase 3
```

创建 `RESEARCH.md`，发现标准技术栈、架构模式、常见陷阱。

#### /gsd-spike
快速验证可行性。

```
/gsd-spike "can we stream LLM output over WebSockets?"
/gsd-spike --quick "test if pdfjs extracts tables"
```

创建 `.planning/spikes/` 目录，结果标记为 VALIDATED/INVALIDATED/PARTIAL。

#### /gsd-sketch
快速原型 UI/设计想法。

```
/gsd-sketch "dashboard layout for admin panel"
/gsd-sketch --quick "form card grouping"
```

生成 `.planning/sketches/` 下的 HTML 页面。

---

### 调试

#### /gsd-debug
系统性调试会话。

```
/gsd-debug "login button doesn't work"
/gsd-debug  # 恢复之前的会话
```

使用科学方法：证据 → 假设 → 测试。

---

### 验证与测试

#### /gsd-verify-work \<phase\#
通过对话进行用户验收测试。

```
/gsd-verify-work 3
```

从 SUMMARY.md 提取可测试交付物，逐一验证。

#### /gsd-audit-uat
跨阶段 UAT 审计。

```
/gsd-audit-uat
```

扫描所有待处理、跳过、阻塞的测试项。

---

### 发布

#### /gsd-ship
创建 PR。

```
/gsd-ship 4
/gsd-ship 4 --draft
```

前置条件：阶段已验证、`gh` CLI 已安装并认证。

#### /gsd-pr-branch
创建干净的 PR 分支。

```
/gsd-pr-branch
/gsd-pr-branch main
```

过滤掉 `.planning/` 相关的提交。

---

### 路线图管理

#### /gsd-add-phase
添加新阶段。

```
/gsd-add-phase "添加管理后台"
```

追加到 ROADMAP.md 末尾。

#### /gsd-insert-phase
插入紧急阶段。

```
/gsd-insert-phase 7 "修复关键认证漏洞"
```

创建中间阶段（如 7.1）。

#### /gsd-remove-phase
删除阶段。

```
/gsd-remove-phase 17
```

仅适用于未开始的阶段。

---

### 笔记与待办

#### /gsd-note
快速笔记。

```
/gsd-note refactor the hook system
/gsd-note list
/gsd-note promote 3
/gsd-note --global cross-project idea
```

#### /gsd-add-todo
从对话中捕获待办。

```
/gsd-add-todo
/gsd-add-todo Add auth token refresh
```

保存到 `.planning/todos/pending/`。

#### /gsd-check-todos
查看和选择待办。

```
/gsd-check-todos
/gsd-check-todos api
```

---

### 配置

#### /gsd-settings
交互式配置。

```
/gsd-settings
```

配置：工作流代理、模型选择、分支策略。

#### /gsd-set-profile
快速切换模型配置。

```
/gsd-set-profile budget
```

可选：`quality`、`balanced`、`budget`、`inherit`

---

### 辅助命令

#### /gsd-do
自然语言路由。

```
/gsd-do fix the login button
/gsd-do I want to start a new milestone
```

分析输入，找到最佳匹配的 GSD 命令。

#### /gsd-help
显示命令参考。

```
/gsd-help
```

#### /gsd-update
更新 GSD 版本。

```
/gsd-update
```

#### /gsd-cleanup
归档完成的阶段目录。

```
/gsd-cleanup
```

---

## 文件结构

```
.planning/
├── PROJECT.md              # 项目愿景
├── ROADMAP.md              # 阶段路线图
├── STATE.md                # 项目状态和上下文
├── RETROSPECTIVE.md       # 回顾（每个里程碑更新）
├── config.json             # 工作流配置
├── todos/                  # 捕获的想法和任务
│   ├── pending/            # 待处理
│   └── done/               # 已完成
├── spikes/                 # 验证实验
│   ├── MANIFEST.md
│   └── NNN-name/
├── sketches/               # 设计草图
│   ├── MANIFEST.md
│   └── NNN-name/
├── debug/                  # 调试会话
│   └── resolved/            # 已解决的问题
├── milestones/             # 归档的里程碑
├── codebase/               # 代码库映射（棕地项目）
│   ├── STACK.md
│   ├── ARCHITECTURE.md
│   └── ...
└── phases/                 # 阶段目录
    ├── 01-foundation/
    │   ├── 01-01-PLAN.md
    │   ├── 01-01-SUMMARY.md
    │   └── 01-01-VERIFICATION.md
    └── 02-core-features/
```

---

## 常见工作流

### 新项目

```
/gsd-new-project        # 统一流程：问答 → 调研 → 需求 → 路线图
/clear
/gsd-plan-phase 1       # 为第一阶段创建计划
/clear
/gsd-execute-phase 1    # 执行所有计划
```

### 恢复工作

```
/gsd-progress           # 查看进度并继续
```

### 添加紧急工作

```
/gsd-insert-phase 5 "关键安全修复"
/gsd-plan-phase 5.1
/gsd-execute-phase 5.1
```

### 完成里程碑

```
/gsd-complete-milestone 1.0.0
/clear
/gsd-new-milestone "v2.0 功能"
```

### 捕获想法

```
/gsd-add-todo                    # 从对话推断
/gsd-add-todo Fix modal z-index  # 显式描述
/gsd-check-todos                 # 查看和处理待办
/gsd-check-todos api             # 按区域过滤
```

### 调试问题

```
/gsd-debug "form submission fails silently"  # 开始调试会话
# ... 调查进行中 ...
/clear
/gsd-debug                                    # 从中断处继续
```

---

## 工作流模式

### 交互模式
- 每个主要决策确认
- 在检查点暂停
- 更多指导

### YOLO 模式
- 自动批准大多数决策
- 不确认执行
- 仅在关键检查点停止

---

## 获取帮助

- 阅读 `.planning/PROJECT.md` 了解项目愿景
- 阅读 `.planning/STATE.md` 了解当前上下文
- 检查 `.planning/ROADMAP.md` 查看阶段状态
- 运行 `/gsd-progress` 查看进度

---

*最后更新：2026-04-28*
