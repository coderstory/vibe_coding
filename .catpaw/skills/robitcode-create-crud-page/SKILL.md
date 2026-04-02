---
name: robitcode-create-crud-page
description: "根据用户需求快速生成标准 CRUD 管理页面，支持多表关联场景。"
---

# 快速创建管理页


## 概述

根据用户需求快速生成标准 CRUD 管理页面，支持多表关联场景。

## 核心能力

- **多格式输入**：自然语言、SQL DDL、结构化 JSON
- **多表关联**：外键引用、关联查询、下拉选择
- **项目感知**：扫描现有实体，智能推断关联
- **自动表名**：统一添加 `sys_` 前缀，符合项目规范

## 表名命名规范

所有数据库表名**自动**添加 `sys_` 前缀，无需用户手动输入。

| 场景 | 输入示例 | 生成表名 |
|------|---------|---------|
| 自然语言 | "产品管理" | `sys_product` |
| 自然语言 | "客户信息表" | `sys_customer_information` |
| SQL DDL | `CREATE TABLE product` | `sys_product` |
| SQL DDL | `CREATE TABLE sys_product` | `sys_product`（已有前缀不重复）|
| JSON | `"table": "order"` | `sys_order` |
| JSON | `"table": "sys_order"` | `sys_order`（已有前缀不重复）|

**规则**：
1. 统一小写下划线命名（snake_case）
2. 自动检测并避免重复添加 `sys_` 前缀
3. 设计确认时展示最终表名，用户无需关心前缀

---

## 执行流程

```
输入解析 → 关联分析 → 设计确认 → 前置检查 → 代码生成 → 路由注册 → 多语言配置
                               ↑
                         【检查OptionVO等通用类】
```

---

## 第一步：输入解析

支持三种输入格式，统一转换为结构化定义。

### 1.1 自然语言

**识别规则：**
- 实体名称：核心名词 → 英文驼峰（"产品管理" → Product）
- 数据库表名：自动从实体名称转换 → 小写下划线，并添加 `sys_` 前缀（Product → `sys_product`，CustomerInfo → `sys_customer_info`）
- 字段类型：名称→String、价格→BigDecimal、数量→Integer、状态→Integer、时间→LocalDateTime
- 关联字段："所属xxx"、"xxxId" → 外键引用

**表名转换规则**：
```
用户输入："产品管理"
实体名称：Product
数据库表：sys_product

用户输入："客户信息表"  
实体名称：CustomerInformation
数据库表：sys_customer_information
```

### 1.2 SQL DDL

- 从 `CREATE TABLE` 提取表名和字段
- 从 `FOREIGN KEY` 识别关联关系
- 从 `COMMENT` 提取字段中文名
- **表名处理**：提取的表名自动添加 `sys_` 前缀（如原表名已有则不重复添加）

**表名转换示例**：
```sql
-- 用户输入
CREATE TABLE product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) COMMENT '产品名称'
);

-- 实际生成
CREATE TABLE sys_product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) COMMENT '产品名称'
);
```

**处理逻辑**：
1. 提取 `CREATE TABLE` 后的表名（去除可能存在的反引号 `` ` ``）
2. 检查是否以 `sys_` 开头
3. 无则添加，有则保持
4. 转换为小写下划线格式

### 1.3 结构化 JSON

```json
{
  "entity": "Product",
  "label": "产品",
  "table": "product",
  "fields": [
    {"name": "name", "label": "产品名称", "type": "string", "required": true},
    {"name": "categoryId", "label": "所属分类", "type": "foreign", "refEntity": "Category"}
  ]
}
```

**说明**：
- `entity`: 实体类名（英文驼峰）
- `label`: 中文名称
- `table`: 数据库表名（**不需要**加 `sys_` 前缀，会自动添加）
- `fields`: 字段列表

**表名处理**：
- 输入 `"table": "product"` → 实际使用 `sys_product`
- 输入 `"table": "sys_product"` → 实际使用 `sys_product`（不重复添加）

---

## 第二步：关联分析

### 自动检测

- 字段名以 `Id` 结尾或包含 `_id`
- 扫描 `entity/` 目录匹配现有实体

### 确认交互

检测到关联字段时询问：

```
关联字段 [categoryId] 配置：
- 关联实体：Category（可从现有实体选择）
- 显示字段：name
- 是否必填：是/否
- 关联类型：A)下拉单选 B)下拉多选
```

---

## 第三步：设计确认（强制确认环节）

展示完整设计，**必须等待用户明确确认后才能继续**：

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
【设计确认】请检查以下设计是否正确：
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

【实体信息】
- 实体名称：Product
- 中文名称：产品
- 数据库表：sys_product （自动生成，符合 sys_ 前缀规范）

【字段列表】
| 字段名 | 中文名 | 类型 | 必填 | 表格 | 表单项 | 关联 |
|--------|--------|------|------|------|--------|------|
| name | 产品名称 | String | 是 | 是 | 输入框 | - |
| categoryId | 所属分类 | Long | 是 | 是 | 下拉选择 | Category.name |

【标准字段（自动添加）】
id, deleted, createTime, updateTime, createBy, updateBy, tenantId

【将生成的文件】
后端：Entity, Mapper, MapperXML, Service, Controller, DTO, VO
前端：types, api, vue组件
配置：路由注册, 数据库脚本, 多语言配置

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
请确认：
1. ✅ 确认正确，开始生成代码
2. 📝 需要修改（请描述修改内容，如：字段名xx改为xx，添加字段xx等）
3. ❌ 取消创建
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### 确认处理流程

**用户选择 1（确认正确）**
- 开始执行第四步：代码生成

**用户选择 2（需要修改）**
- 记录用户修改意见
- 根据意见调整设计（如修改字段名、添加/删除字段、调整类型等）
- **重新展示修改后的完整设计，再次等待确认**
- 循环直到用户确认或取消

**用户选择 3（取消创建）**
- 终止流程，不生成任何代码
- 输出：`❌ 已取消创建，未生成任何文件`

### 修改意见处理规则

| 修改类型 | 处理方式 |
|---------|---------|
| 修改字段中文名 | 更新字段 label |
| 修改字段类型 | 更新字段 type（需校验合法性） |
| 添加字段 | 添加到字段列表，推断类型 |
| 删除字段 | 从字段列表移除 |
| 修改关联实体 | 更新 refEntity 和显示字段 |
| 调整必填/显示 | 更新 required/showInTable/showInForm |

**注意**：每次修改后必须重新展示完整设计表，确保用户看到最终效果。

---

## 第四步：代码生成

### 前置检查（必须首先执行）

**生成任何代码之前，必须先检查并创建以下通用类：**

#### 1. OptionVO（通用下拉选项）- 【必须】
- **路径**：`service/src/main/java/com/robitcode/vo/OptionVO.java`
- **检查命令**：使用 glob 工具检查文件是否存在
- **如果不存在，必须立即创建**：

```java
package com.robitcode.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionVO {
    private Long value;
    private String label;
}
```

⚠️ **重要**：所有 Service 的 `getOptions()` 方法都返回 `List<OptionVO>`，如果此类不存在会导致编译错误。**必须在生成 Service 之前确保此类存在**。

---

### 执行顺序

1. **数据库** - 更新 `init.sql`（DDL 建表语句）
2. **后端** - Entity → Mapper → MapperXML → Service → Controller → DTO → VO
3. **前端** - types → api → vue组件
4. **路由** - 更新 `router/index.ts`

### 模板文件

生成代码时按需读取模板：

| 阶段 | 模板文件 |
|------|----------|
| 数据库 | [templates/database.md](templates/database.md) |
| 后端代码 | [templates/backend.md](templates/backend.md) |
| 前端代码 | [templates/frontend.md](templates/frontend.md) |

**重要：生成前必须读取对应模板文件，确保代码规范一致。**

### 示例参考

完整示例请参考 [examples/sample.md](examples/sample.md)。

### 参考现有代码

生成代码时，优先参考项目现有代码模式：
- 读取 `entity/` 目录下的实体类
- 读取 `controller/` 目录下的控制器
- 读取 `service/` 目录下的服务类
- 模仿其命名风格、注解使用、异常处理方式

### Windows 编码修复（重要）

在 Windows 环境下追加中文内容到 SQL 文件时，必须使用 UTF-8 编码，否则会出现乱码。

**错误方式（PowerShell 默认 GBK 编码）：**
```powershell
Add-Content -Path "init.sql" -Value "-- 中文内容"
```

**正确方式（.NET UTF-8 编码）：**
```powershell
$content = @"
-- =============================================
-- 客户信息表 (sys_customer_information)
-- =============================================
CREATE TABLE IF NOT EXISTS sys_customer_information (
  -- 中文注释
) ENGINE=InnoDB COMMENT='客户信息表';
"@
[System.IO.File]::AppendAllText("service/src/main/resources/db/init.sql", $content, [System.Text.Encoding]::UTF8)
```

**或者使用 `Out-File` 指定编码：**
```powershell
"-- 中文内容" | Out-File -FilePath "init.sql" -Append -Encoding UTF8
```

---

## 第五步：路由注册

在 `app/src/router/index.ts` 的 dashboard children 末尾添加：

```typescript
{
  path: '{EntityName}Manage',
  name: '{EntityName}Manage',
  component: () => import('@/views/{EntityName}Manage.vue'),
  meta: { title: 'menu.{entityName}Manage', icon: '{推断图标}', requiresAuth: true }
}
```

**图标推断：** 产品→Goods、订单→List、用户→User、文档→Document、分类→FolderOpened、默认→Document

---

## 第六步：多语言配置

### 更新中文语言包

在 `app/src/locales/zh-CN.ts` 的 `menu` 对象末尾添加：

```typescript
{entityName}Manage: '{中文名称}管理',
```

**位置示例：**
```typescript
menu: {
  // ... 其他菜单项
  biReport: 'BI报表',
  // 在此添加新菜单
  {entityName}Manage: '{中文名称}管理',
},
```

### 更新英文语言包

在 `app/src/locales/en-US.ts` 的 `menu` 对象末尾添加：

```typescript
{entityName}Manage: '{EntityName} Management',
```

**英文命名规则：**
- 实体名称转 PascalCase（如 officeSupply → OfficeSupply）
- 添加 " Management" 后缀
- 示例：`officeSupplyManage: 'Office Supply Management'`

### 添加模块翻译（可选）

如果页面有特殊的字段或提示需要翻译，在语言包中添加对应的模块对象：

```typescript
// zh-CN.ts
{entityName}: {
  title: '{中文名称}管理',
  name: '{字段中文名}',
  // ... 其他字段翻译
},

// en-US.ts
{entityName}: {
  title: '{EntityName} Management',
  name: '{Field Name}',
  // ... 其他字段翻译
},
```

---

## 第七步：输出汇总

### 关联实体API差异

生成前端代码时，如果涉及关联实体（如客户经理、所属部门等），**必须检查关联实体的实际API**，不同实体提供的查询接口可能不同：

**处理原则**：
- 生成代码前，先用 glob 检查关联实体的 API 文件 (`app/src/api/{entity}.ts`)
- 根据实际存在的函数名生成代码
- User 实体使用 `getUserPage`，需要 `.records` 获取数组

---

## 第六步：输出汇总

完成后展示：

```
✅ 管理页面创建完成！

【生成的文件】
后端：entity, mapper, mapper.xml, service, controller, dto, vo
前端：types/{entity}.ts, api/{entity}.ts, views/{EntityName}Manage.vue
配置：router/index.ts, db/init.sql, locales/zh-CN.ts, locales/en-US.ts

【API 端点】
GET  /{path}/page        GET  /{path}/options
GET  /{path}/{id}        POST /{path}
PUT  /{path}/{id}        DELETE /{path}/{id}
DELETE /{path}/batch

【后续步骤】
1. 执行数据库脚本（建表）
2. 在系统管理后台配置菜单
3. 重启后端服务
4. 刷新前端页面
```

---

## 关键约束

1. **必须强制确认后再生成** - 展示完整设计后，必须等待用户明确选择"✅ 确认正确"后才能开始生成代码。如果用户选择修改，需根据意见调整设计后重新展示确认，循环直到用户确认或取消。严禁未经确认直接生成代码。
2. **前置检查必须先执行** - **生成任何代码文件之前**，必须先检查并创建 `OptionVO` 等通用类。使用 glob 工具检查文件是否存在，不存在则立即创建，然后再继续生成其他代码。
3. **按需读取模板** - 生成代码前读取对应模板文件
4. **参考现有代码** - 优先模仿项目已有代码风格
5. **表名规范 sys_ 前缀** - 所有数据库表名必须统一添加 `sys_` 前缀。自然语言/SQL DDL/JSON 输入的表名自动转换，设计确认时展示最终表名，无需用户手动处理。
6. **标准字段必加** - 所有实体包含 id/deleted/createTime/updateTime/createBy/updateBy/tenantId
7. **关联字段处理** - VO 包含关联显示字段、Mapper XML 包含关联查询
8. **移动端适配必做** - 生成的页面必须支持移动端响应式适配，详见下方规范

---

## 移动端响应式适配规范

### 设计原则

生成的页面必须遵循 **PC 优先、移动端降级** 的响应式设计原则，确保在不同设备上都有良好的用户体验。

### 响应式断点定义

| 断点 | 宽度范围 | 设备类型 |
|------|---------|---------|
| xs | < 768px | 手机 |
| sm | ≥ 768px | 平板竖屏 |
| md | ≥ 992px | 小屏桌面/平板横屏 |
| lg | ≥ 1200px | 桌面 |

---

### 核心适配策略

#### 1. 搜索栏适配（重要优化）

**PC 端**：水平排列，所有筛选项一行展示，标签右对齐
**移动端**：
- **标签和输入框在同一行**，标签左对齐，输入框占据剩余空间
- **标签文本过长时自动缩略**（max-width: 80px，text-overflow: ellipsis）
- 默认折叠次要筛选项，只显示关键搜索项（如名称、状态）
- 提供「展开筛选/收起筛选」按钮
- 搜索按钮居中排列，支持换行

```vue
<el-form :model="searchForm" class="search-form" :inline="!isMobile">
  <el-row :gutter="16">
    <!-- 移动端默认显示主要搜索项 -->
    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <el-form-item label="名称">
        <el-input v-model="searchForm.name" placeholder="请输入" clearable />
      </el-form-item>
    </el-col>
    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
    </el-col>
    <!-- 次要搜索项在移动端默认隐藏 -->
    <el-col v-show="!isMobile || searchExpanded" :xs="24" :sm="12" :md="8" :lg="6">
      <el-form-item label="创建时间">
        <el-date-picker v-model="searchForm.dateRange" type="daterange" />
      </el-form-item>
    </el-col>
  </el-row>
  <div class="search-buttons">
    <el-button v-if="isMobile && !searchExpanded" type="primary" link @click="searchExpanded = true">
      <el-icon><ArrowDown /></el-icon>展开筛选
    </el-button>
    <el-button v-if="isMobile && searchExpanded" link @click="searchExpanded = false">
      <el-icon><ArrowUp /></el-icon>收起筛选
    </el-button>
    <el-button type="primary" @click="handleSearch">
      <el-icon><Search /></el-icon><span v-if="!isMobile">搜索</span>
    </el-button>
    <el-button @click="handleReset">
      <el-icon><RefreshRight /></el-icon><span v-if="!isMobile">重置</span>
    </el-button>
  </div>
</el-form>
```

#### 2. 数据列表适配

**PC 端**：`el-table` 表格，支持多列排序、选择
**移动端**：卡片列表替代表格

```vue
<!-- PC 表格视图 -->
<el-table v-if="!isMobile" :data="tableData" stripe>
  <el-table-column prop="name" label="名称" min-width="120" />
  <el-table-column prop="status" label="状态" width="100">
    <template #default="{ row }">
      <el-tag :type="row.status === 1 ? 'success' : 'info'">
        {{ row.status === 1 ? '启用' : '禁用' }}
      </el-tag>
    </template>
  </el-table-column>
  <el-table-column prop="createTime" label="创建时间" width="180" />
  <el-table-column label="操作" fixed="right" width="150">
    <template #default="{ row }">
      <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
      <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
    </template>
  </el-table-column>
</el-table>

<!-- 移动端卡片视图 -->
<div v-else class="mobile-card-list">
  <el-card v-for="item in tableData" :key="item.id" class="mobile-card" shadow="hover">
    <div class="card-header">
      <span class="card-title">{{ item.name }}</span>
      <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
        {{ item.status === 1 ? '启用' : '禁用' }}
      </el-tag>
    </div>
    <div class="card-body">
      <div class="card-row">
        <span class="label">创建时间：</span>
        <span class="value">{{ item.createTime }}</span>
      </div>
    </div>
    <div class="card-footer">
      <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
      <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
    </div>
  </el-card>

  <!-- 移动端分页（固定底部） -->
  <div class="mobile-pagination">
    <el-pagination
      v-model:current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      :small="true"
      @current-change="handlePageChange"
    />
    <div class="total-info">共 {{ total }} 条</div>
  </div>

  <!-- 空状态 -->
  <el-empty v-if="tableData.length === 0" description="暂无数据" />
</div>
```

#### 3. 新增/编辑表单适配

**PC 端**：居中对话框，宽度 500-800px
**移动端**：全屏对话框，表单单列布局，标签顶部对齐

```vue
<el-dialog
  v-model="dialogVisible"
  :title="dialogTitle"
  :width="isMobile ? '100%' : '600px'"
  :fullscreen="isMobile"
  :close-on-click-modal="!isMobile"
>
  <el-form
    :model="formData"
    :label-width="isMobile ? '100%' : '100px'"
    :label-position="isMobile ? 'top' : 'right'"
  >
    <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称' }]">
      <el-input v-model="formData.name" placeholder="请输入名称" />
    </el-form-item>
    <!-- 更多表单项 -->
  </el-form>
  <template #footer>
    <div class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </div>
  </template>
</el-dialog>
```

#### 4. 分页适配（重要优化）

**PC 端**：完整分页器（总数、每页条数、页码、跳转）
**移动端**：
- **固定在屏幕底部**（position: fixed）
- 左侧分页器，右侧总数
- 简化分页器（只显示上一页、下一页、当前页）
- **容器需要 padding-bottom 留出底部空间**

```vue
<!-- PC端分页 -->
<div v-if="!isMobile" class="pagination-wrapper">
  <el-pagination
    v-model:current-page="pageNum"
    v-model:page-size="pageSize"
    :total="total"
    :page-sizes="[10, 20, 50, 100]"
    layout="total, sizes, prev, pager, next, jumper"
    @size-change="handleSizeChange"
    @current-change="handlePageChange"
  />
</div>

<!-- 移动端分页（放在卡片列表内） -->
<div v-else class="mobile-pagination">
  <el-pagination
    v-model:current-page="pageNum"
    :page-size="pageSize"
    :total="total"
    layout="prev, pager, next"
    :small="true"
    @current-change="handlePageChange"
  />
  <div class="total-info">共 {{ total }} 条</div>
</div>
```

#### 5. 工具栏按钮适配

**PC 端**：图标 + 文字
**移动端**：仅图标或文字

```vue
<div class="toolbar">
  <el-button type="primary" @click="handleAdd">
    <el-icon><Plus /></el-icon>
    <span v-if="!isMobile">新增</span>
  </el-button>
  <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
    <el-icon><Delete /></el-icon>
    <span v-if="!isMobile">批量删除</span>
  </el-button>
</div>
```

---

### 响应式工具函数

生成页面时，需要检查 `composables/useResponsive.ts` 是否存在，不存在则创建：

```typescript
import { ref, computed, onMounted, onUnmounted } from 'vue'

export function useResponsive() {
  const windowWidth = ref(window.innerWidth)
  
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }
  
  onMounted(() => {
    window.addEventListener('resize', updateWidth)
  })
  
  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })
  
  const isMobile = computed(() => windowWidth.value < 768)
  const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 992)
  const isDesktop = computed(() => windowWidth.value >= 992)
  
  return {
    windowWidth,
    isMobile,
    isTablet,
    isDesktop
  }
}
```

**使用方式**：每个生成的页面组件必须导入并使用：

```typescript
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()
const searchExpanded = ref(false)
```

---

### CSS 响应式样式模板（完整版）

生成的 Vue 组件必须包含以下响应式样式：

```scss
<style lang="scss" scoped>
// 容器样式
.crud-container {
  // 移动端底部留出分页空间
  @media screen and (max-width: 768px) {
    padding: 12px;
    padding-bottom: 80px;
  }
}

// 搜索表单样式
.search-card {
  margin-bottom: 12px;
  
  :deep(.el-card__body) {
    padding-bottom: 0;
  }
}

.search-form {
  .full-width {
    width: 100%;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

.search-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
  padding-bottom: 16px;
}

// 工具栏样式
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

// 分页样式
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 15px 20px;
}

// ============================================
// 移动端卡片列表样式
// ============================================
.mobile-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0;
  padding-bottom: 16px;
}

.mobile-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .card-title {
      font-weight: 600;
      font-size: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
      margin-right: 8px;
    }
  }
  
  .card-body {
    .card-row {
      display: flex;
      margin-bottom: 8px;
      font-size: 14px;
      line-height: 1.5;
      
      .label {
        color: #909399;
        min-width: 80px;
        flex-shrink: 0;
      }
      
      .value {
        color: #303133;
        flex: 1;
        word-break: break-all;
      }
    }
  }
  
  .card-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
  }
}

// 移动端分页样式
.mobile-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 0;
  
  .total-info {
    font-size: 12px;
    color: #909399;
  }
}

// 对话框底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

// ============================================
// 响应式断点样式（核心）
// ============================================
@media screen and (max-width: 768px) {
  // 搜索表单适配
  .search-card {
    :deep(.el-card__body) {
      padding: 12px;
    }
  }
  
  .search-form {
    // 标签和输入框在同一行
    :deep(.el-form-item) {
      margin-bottom: 12px;
      width: 100%;
      display: flex;
      flex-direction: row;
      align-items: center;
      flex-wrap: nowrap;
      
      .el-form-item__label {
        flex-shrink: 0;
        width: auto !important;
        max-width: 80px;
        min-width: 60px;
        text-align: left;
        padding-right: 8px;
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .el-form-item__content {
        flex: 1;
        margin-left: 0 !important;
        width: 0;
      }
    }
    
    // 移除 el-row/el-col 在移动端的间距
    :deep(.el-row) {
      margin-left: 0 !important;
      margin-right: 0 !important;
    }
    
    :deep(.el-col) {
      padding-left: 0 !important;
      padding-right: 0 !important;
    }
  }
  
  // 搜索按钮居中
  .search-buttons {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
    padding: 12px 0 0;
    margin-top: 8px;
    border-top: 1px solid #ebeef5;
    
    .el-button {
      flex: 1 1 auto;
      max-width: 120px;
    }
  }
  
  // 工具栏适配
  .toolbar {
    flex-wrap: wrap;
    gap: 8px;
    
    .el-button {
      flex: 1;
      min-width: 80px;
    }
  }
  
  // 隐藏PC端表格
  .table-card {
    display: none;
  }
  
  // 移动端分页固定底部
  .mobile-pagination {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: #fff;
    padding: 12px 16px;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
    z-index: 100;
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    
    .total-info {
      font-size: 12px;
      color: #909399;
    }
  }
  
  // 对话框按钮垂直排列
  .dialog-footer {
    flex-direction: column;
    
    .el-button {
      width: 100%;
    }
  }
}

@media screen and (min-width: 769px) {
  // PC端隐藏移动端卡片列表
  .mobile-card-list {
    display: none;
  }
}
</style>
```

---

### 页面结构模板（完整版）

```vue
<template>
  <div class="crud-container">
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" class="search-form" :inline="!isMobile">
        <el-row :gutter="16">
          <!-- 移动端默认显示主要搜索项 -->
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="名称">
              <el-input v-model="searchForm.name" placeholder="请输入" clearable class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择" clearable class="full-width">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <!-- 次要搜索项在移动端默认隐藏 -->
          <el-col v-show="!isMobile || searchExpanded" :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="创建时间">
              <el-date-picker v-model="searchForm.dateRange" type="daterange" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>
        <div class="search-buttons">
          <el-button v-if="isMobile && !searchExpanded" type="primary" link @click="searchExpanded = true">
            <el-icon><ArrowDown /></el-icon>展开筛选
          </el-button>
          <el-button v-if="isMobile && searchExpanded" link @click="searchExpanded = false">
            <el-icon><ArrowUp /></el-icon>收起筛选
          </el-button>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon><span v-if="!isMobile">搜索</span>
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon><span v-if="!isMobile">重置</span>
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        <span v-if="!isMobile">新增</span>
      </el-button>
    </div>

    <!-- PC端表格 -->
    <el-card v-if="!isMobile" class="table-card" shadow="never">
      <el-table :data="tableData" stripe>
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 移动端卡片列表 -->
    <div v-else class="mobile-card-list">
      <el-card v-for="item in tableData" :key="item.id" class="mobile-card" shadow="hover">
        <div class="card-header">
          <span class="card-title">{{ item.name }}</span>
          <el-tag :type="item.status === 1 ? 'success' : 'info'" size="small">
            {{ item.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="card-row">
            <span class="label">创建时间：</span>
            <span class="value">{{ item.createTime }}</span>
          </div>
        </div>
        <div class="card-footer">
          <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </el-card>

      <!-- 移动端分页 -->
      <div class="mobile-pagination">
        <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          :small="true"
          @current-change="handlePageChange"
        />
        <div class="total-info">共 {{ total }} 条</div>
      </div>

      <el-empty v-if="tableData.length === 0" description="暂无数据" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="isMobile ? '100%' : '600px'"
      :fullscreen="isMobile"
      destroy-on-close
    >
      <el-form :model="formData" :label-position="isMobile ? 'top' : 'right'" :label-width="isMobile ? '100%' : '100px'">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshRight, Plus, ArrowDown, ArrowUp } from '@element-plus/icons-vue'
import { useResponsive } from '@/composables/useResponsive'

const { isMobile } = useResponsive()
const searchExpanded = ref(false)
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const searchForm = reactive({ name: '', status: '', dateRange: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formData = reactive({ name: '' })

const handleSearch = () => { pageNum.value = 1; loadData() }
const handleReset = () => { /* 重置逻辑 */ }
const handleSizeChange = (size: number) => { pageSize.value = size; pageNum.value = 1; loadData() }
const handlePageChange = (page: number) => { pageNum.value = page; loadData() }
const handleAdd = () => { dialogTitle.value = '新增'; dialogVisible.value = true }
const handleEdit = (row: any) => { dialogTitle.value = '编辑'; dialogVisible.value = true }
const handleDelete = (row: any) => { /* 删除逻辑 */ }
const handleSubmit = () => { /* 提交逻辑 */ }
const loadData = () => { /* 加载数据 */ }

onMounted(() => { loadData() })
</script>
```

---

### 生成检查清单

生成页面时必须确认以下适配项：

- [ ] 已导入 `useResponsive` 并使用 `isMobile`
- [ ] 已定义 `searchExpanded` 状态变量
- [ ] 搜索栏标签和输入框在同一行（移动端）
- [ ] 搜索栏标签文本过长时自动缩略
- [ ] 搜索栏支持移动端折叠/展开
- [ ] 数据列表有移动端卡片视图
- [ ] 对话框支持移动端全屏
- [ ] 表单标签移动端顶部对齐
- [ ] 分页组件移动端固定在底部
- [ ] 容器有 `padding-bottom: 80px` 留出底部空间
- [ ] CSS 包含完整的响应式断点样式
