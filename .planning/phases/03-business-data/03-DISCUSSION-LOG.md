# Phase 3: 业务数据管理 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-02
**Phase:** 03-business-data
**Areas discussed:** 业务数据类型, 列表展示, 表单设计, 响应式布局

---

## 业务数据类型

| Option | Description | Selected |
|--------|-------------|----------|
| 通用数据表 | 名称、描述、状态、创建时间等通用字段 | |
| 客户/供应商 | 名称、联系人、电话、地址、类型 | |
| 商品/产品 | 名称、编码、分类、价格、库存 | |
| 知识库表单 | 树形分类 + 知识表单 + tag + 富文本 + 文件 + 检索 | ✓ |

**User's choice:** 知识库表单（一主多子方案）
**Notes:** 用户详细描述了需求：
- 树形分类结构
- 每个分类下可新建知识表单
- 表单支持多个 tag
- 富文本编辑器（开源、Markdown、图片/文件上传）
- 文件 zstd 压缩存数据库
- 通用文件管理模块
- 检索功能快速查找知识点

---

## 列表展示

| Option | Description | Selected |
|--------|-------------|----------|
| 左侧树 + 右侧列表 | 经典后台布局，左侧分类树右侧知识列表 | ✓ |
| 下拉选择 + 表格 | 顶部下拉切换分类，表格展示知识 | |
| 可编辑树表格 | 表格内嵌树形，支持行内编辑分类 | |

**User's choice:** 左侧树（可折叠）+ 右侧列表
**Notes:** 点击知识项在当前页签打开，全屏展示编辑

---

## 表单设计 - 富文本编辑器

| Option | Description | Selected |
|--------|-------------|----------|
| Markdown 编辑器 | Typora 风格，支持实时预览 Markdown | |
| WangEditor | 国产轻量级，支持 Markdown、图片上传 | |
| Tiptap | 基于 ProseMirror，Vue 3 官方推荐 | ✓ |

**User's choice:** Tiptap

---

## 表单设计 - 文件存储

| Option | Description | Selected |
|--------|-------------|----------|
| 数据库存储（zstd压缩） | 直接存数据库，使用 zstd 压缩内容 | ✓ |
| 本地文件系统 | 存服务器本地目录，通过接口访问 | |
| OSS/COS | 对象存储服务（需额外配置云服务） | |

**User's choice:** 数据库存储（zstd压缩）

---

## 检索功能

| Option | Description | Selected |
|--------|-------------|----------|
| 数据库 LIKE 查询 | 简单 SQL LIKE 查询 | |
| Elasticsearch | 专业搜索引擎，功能强大（需要额外部署） | |
| MySQL 全文索引 | 利用 MySQL 全文索引，平衡方案 | ✓ |

**User's choice:** MySQL 全文索引

---

## 响应式布局

| Option | Description | Selected |
|--------|-------------|----------|
| 桌面优先 | 主要适配桌面端，平板/手机简单适配 | |
| 移动端适配 | 移动端也能正常使用 | ✓ |

**User's choice:** 移动端适配

---

## OpenCode's Discretion

- 树形组件具体实现（可折叠细节）
- 分页大小、排序规则
- Tiptap 具体插件配置
- tag 选择组件实现

## Deferred Ideas

None — expanded Phase 3 scope covers all mentioned features
