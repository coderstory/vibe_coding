# Phase 2: 用户与权限管理 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-02
**Phase:** 02-user-permissions
**Areas discussed:** 用户管理字段, 权限模型粒度, 角色权限分配, 密码重置策略, 审计日志详情, 审计查询条件

---

## 用户管理字段

| Option | Description | Selected |
|--------|-------------|----------|
| 用户名 + 密码 + 姓名 | 核心三字段 | |
| 用户名 + 密码 + 姓名 + 手机号 | 完整字段，手机号必填 | |
| 用户名、密码、姓名、性别、头像、创建日期、是否启用、邮箱、部门、岗位 | 用户提供的详细字段 | ✓ |

**User's choice:** 用户名、密码、姓名、性别、头像、创建日期、是否启用、邮箱、部门、岗位
**Notes:** 不需要手机号字段

---

## 权限模型粒度

| Option | Description | Selected |
|--------|-------------|----------|
| 页面级权限（推荐） | 用户分配角色，角色分配菜单访问权限 | ✓ |
| 按钮级权限 | 角色可以控制到每个按钮的显示/隐藏 | |

**User's choice:** 页面级权限
**Notes:** 简化版 RBAC

---

## 角色权限分配

| Option | Description | Selected |
|--------|-------------|----------|
| 树形菜单选择（推荐） | 菜单树形结构，勾选分配 | ✓ |
| 复选框列表 | 平铺所有菜单项，逐一勾选 | |

**User's choice:** 树形菜单选择
**Notes:** 清晰直观

---

## 密码重置策略

| Option | Description | Selected |
|--------|-------------|----------|
| 重置为默认密码（推荐） | 如 '123456'，管理员告知用户 | |
| 生成随机密码 | 系统生成随机密码，发送 | |
| 用户直接输入新密码 | 管理员直接设置新密码 | ✓ |

**User's choice:** 用户直接输入新的密码
**Notes:** 管理员直接输入新密码

---

## 审计日志字段

| Option | Description | Selected |
|--------|-------------|----------|
| 操作人 + 时间 + 操作类型 + 目标（推荐） | 核心4字段 | ✓ |
| 操作人 + 时间 + 操作类型 + 目标 + IP地址 | 完整字段 | |

**User's choice:** 操作人 + 时间 + 操作类型 + 目标
**Notes:** 不需要 IP 地址

---

## 审计查询条件

| Option | Description | Selected |
|--------|-------------|----------|
| 按时间范围 + 操作人 + 操作类型（推荐） | 最常用的3个过滤条件 | ✓ |
| 仅按时间范围 | 简化版 | |

**User's choice:** 按时间范围 + 操作人 + 操作类型
**Notes:** 

---

## OpenCode's Discretion

- 列表分页大小、排序规则等细节由 OpenCode 决定
- 前端表格组件具体实现由 OpenCode 决定

## Deferred Ideas

无
