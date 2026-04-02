---
name: extract-button-permissions
description: 提取按钮权限并生成数据库脚本
---

# 执行计划

## 概述

当开发者需要为前端页面生成按钮权限数据库脚本时使用此技能。扫描指定 Vue 页面的 `el-button` 元素，提取按钮信息，生成 16 位唯一 authId，输出 INSERT 脚本并追加到 init.sql。

## 触发时机

开发者主动调用：`/extract-button-permissions`

## 开发者需要提供的信息

| 信息 | 说明 | 示例 |
|------|------|------|
| 菜单ID | 父菜单的数据库ID | `42` |
| 页面路径 | Vue 文件的相对路径 | `src/views/UserManagement.vue` |

## 工作流程

```
1. 接收开发者输入
   ├── 菜单ID (parent_id)
   └── 页面文件路径

2. 扫描页面
   ├── 读取 Vue 文件内容
   ├── 匹配所有 <el-button> 元素
   ├── 提取按钮文字作为名称
   └── 检查 authId 属性

3. 处理 authId
   ├── 有 authId → 使用现有值
   └── 无 authId → 生成 16 位唯一数字ID
                    └── 自动添加到按钮元素,同时带上v-auth元素

4. 生成 SQL 并写入
   ├── 生成 INSERT 语句
   ├── 检查 init.sql 是否已存在
   └── 追加到 init.sql 文件

5. 输出结果报告
```

## 扫描规则

### 扫描目标
- `<el-button>` 元素
- 提取按钮文字内容作为菜单名称（name 字段）
- 检查/生成 `authId` 属性作为权限标识（permission 字段）

### 过滤规则（不扫描以下按钮）

1. **弹窗内按钮** - 在 `<el-dialog>` 内的"取消"、"确定"、"关闭"按钮
2. **分页组件按钮** - 分页器内的按钮
3. **无文字按钮** - 只有图标没有文字的按钮
4. **特殊用途按钮** - 如"搜索"、"重置"等工具栏查询按钮

### 过滤关键词

```
取消、确定、关闭、搜索、重置、查询、上一页、下一页
```

## authId 生成规则

### 格式
- 16 位纯数字
- 格式：`13位时间戳(毫秒级后13位) + 3位随机数`

### 唯一性保障

1. **页面内检查** - 确保同一页面内 authId 不重复
2. **数据库校验** - 查询 `sys_menus.permission` 确保全局唯一
3. **冲突重试** - 遇冲突时重新生成

### 生成示例

```
时间戳后13位: 8461234567891
随机3位数字: 234
最终 authId: 8461234567891234
```

## SQL 生成格式

```sql
-- 按钮权限：{菜单名称}菜单（菜单ID: {parent_id}）
INSERT INTO sys_menus (name, description, parent_id, menu_type, sort, permission, status, create_time, update_time, tenant_id) VALUES
('{按钮名称}', '{按钮名称}按钮', {parent_id}, 'BUTTON', {sort}, '{authId}', 1, NOW(), NOW(), 1),
('{按钮名称2}', '{按钮名称2}按钮', {parent_id}, 'BUTTON', {sort2}, '{authId2}', 1, NOW(), NOW(), 1);
```

## 输出示例

### 控制台输出

```
=== 按钮权限扫描结果 ===

页面: src/views/UserManagement.vue
菜单ID: 42

扫描到的按钮:
┌──────────────┬─────────────────┬────────────┐
│ 按钮名称      │ authId          │ 状态       │
├──────────────┼─────────────────┼────────────┤
│ 添加成员      │ 8461234567891234│ 新生成     │
│ 导出         │ 8461234567895678│ 新生成     │
│ 编辑         │ 1234567890123456│ 已存在     │
│ 删除         │ 8461234567899012│ 新生成     │
└──────────────┴─────────────────┴────────────┘

生成的 SQL:
-- 按钮权限：用户管理菜单（菜单ID: 42）
INSERT INTO sys_menus (name, description, parent_id, menu_type, sort, permission, status, create_time, update_time, tenant_id) VALUES
('添加成员', '添加成员按钮', 42, 'BUTTON', 1, '8461234567891234', 1, NOW(), NOW(), 1),
('导出', '导出按钮', 42, 'BUTTON', 2, '8461234567895678', 1, NOW(), NOW(), 1),
('删除', '删除按钮', 42, 'BUTTON', 3, '8461234567899012', 1, NOW(), NOW(), 1);

已追加到: service/src/main/resources/db/init.sql
```

## 文件修改

### Vue 文件修改

为没有 authId 的按钮自动添加属性：

```vue
<!-- 修改前 -->
<el-button type="primary" @click="handleAdd">添加成员</el-button>

<!-- 修改后 -->
<el-button type="primary" v-auth authId="8461234567891234" @click="handleAdd">添加成员</el-button>
```

### init.sql 追加

在 init.sql 中追加按钮权限 INSERT 语句。（UTF-8 编码）

## 注意事项

1. **增量更新** - 已存在的 authId 不会重复生成
2. **文件备份** - 修改前建议开发者确认 git 已提交
3. **手动验证** - 生成后建议开发者检查 SQL 正确性
4. **重复检查** - 按 permission 检查避免重复插入数据库

## 相关文件

| 文件 | 说明 |
|------|------|
| `app/src/views/*.vue` | 前端页面文件 |
| `service/src/main/resources/db/init.sql` | 数据库初始化脚本 |
| `app/src/types/menu.ts` | 菜单类型定义 |
