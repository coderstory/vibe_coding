# Phase 8: 用户增删改表单 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-18
**Phase:** 08-user-crud
**Areas discussed:** 密码处理逻辑, 编辑入口

---

## 密码处理逻辑

| Option | Description | Selected |
|--------|-------------|----------|
| 留空不修改（推荐） | 编辑时密码字段留空，后端不更新密码字段 | |
| 显式勾选修改 | 需要勾选「修改密码」才显示密码字段 | ✓ |

**User's choice:** 显式勾选修改
**Notes:** 编辑用户时默认不显示密码字段，需要用户主动勾选「修改密码」后才显示密码输入框，勾选后密码字段为必填。

---

## 编辑入口

| Option | Description | Selected |
|--------|-------------|----------|
| 需要编辑按钮（推荐） | 详情页添加编辑按钮，点击跳转到编辑表单 | ✓ |
| 不需要 | 只在列表页编辑即可 | |

**User's choice:** 需要编辑按钮
**Notes:** UserDetail.vue 详情页添加「编辑」按钮，点击后跳转到 UserManagement 的编辑对话框

---

## OpenCode's Discretion

- 手机号正则表达式：`/^1[3-9]\d{9}$/`
- 邮箱正则表达式：`/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/`

