# 流程表单开发 Skill

## 触发条件

当用户需要创建流程表单页面时使用此 Skill。支持从归档表自动生成表单配置。

## 概述

流程表单系统支持两种模式：

| 模式 | 适用场景 | 开发方式 |
|------|----------|----------|
| **JSON 配置** | 简单表单、快速开发 | 编写 JSON 配置文件（支持节点级配置） |
| **Vue 组件** | 复杂表单、自定义逻辑 | 编写 Vue 组件（支持内嵌 JSON 配置） |

**核心特性**：
- 从归档表自动生成：读取数据库表字段和注释，自动推断控件类型
- 节点级表单配置：每个流程节点可以有独立的表单配置
- 内置审批意见：WorkflowPanel 自动处理审批意见，无需配置
- 自动标题生成：流程标题自动生成为「流程名称-发起人」格式，无需用户输入
- 混合渲染：Vue 组件模式支持内嵌 JSON，实现灵活组合
- **归档表绑定**：流程完成后自动保存表单数据到指定归档表

---

## 完整开发流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    流程表单开发完整流程                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Step 1: 准备归档表                                              │
│    └─ 确认数据库表已创建，字段有注释                               │
│                                                                 │
│  Step 2: 调用本 Skill 生成表单                                   │
│    ├─ 读取归档表结构和注释                                        │
│    ├─ 推断控件类型                                               │
│    ├─ 询问用户选择模式（JSON / Vue）                              │
│    └─ 生成 JSON 配置或 Vue 组件                                   │
│                                                                 │
│  Step 3: 流程设计器配置                                          │
│    ├─ 创建流程定义                                               │
│    ├─ 设计流程节点（开始 → 审批 → 结束）                          │
│    ├─ 配置表单模式和路径                                          │
│    ├─ 【重要】绑定归档表                                          │
│    └─ 发布流程                                                   │
│                                                                 │
│  Step 4: 手动调整表单配置                                        │
│    ├─ 调整字段属性（必填、禁用等）                                 │
│    ├─ 配置节点表单（哪些字段在哪个节点显示）                        │
│    └─ 添加联动逻辑（可选）                                        │
│                                                                 │
│  Step 5: 测试运行流程                                            │
│    ├─ 发起流程测试                                               │
│    ├─ 审批流程测试                                               │
│    └─ 验证数据保存到归档表                                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Step 2 详细流程：从归档表生成表单

### 第一步：获取归档表信息

使用数据库工具读取表结构：

```sql
-- 查询表字段信息和注释
SELECT 
  COLUMN_NAME as name,
  COLUMN_TYPE as type,
  IS_NULLABLE as nullable,
  COLUMN_COMMENT as comment,
  COLUMN_KEY as `key`
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = '数据库名' 
  AND TABLE_NAME = '表名'
ORDER BY ORDINAL_POSITION;
```

### 第二步：字段类型推断控件

根据数据库字段类型自动推断控件类型：

| MySQL 类型 | JSON 控件类型 | 说明 |
|-----------|--------------|------|
| `VARCHAR`, `CHAR` | `text` | 文本输入 |
| `TEXT`, `LONGTEXT` | `textarea` | 多行文本 |
| `INT`, `BIGINT`, `SMALLINT`, `TINYINT` | `number` | 数字输入 |
| `DECIMAL`, `FLOAT`, `DOUBLE` | `number` | 数字输入（带精度） |
| `DATE` | `date` | 日期选择 |
| `DATETIME`, `TIMESTAMP` | `datetime` | 日期时间选择 |
| `ENUM` | `select` | 下拉选择 |
| `BOOLEAN`, `TINYINT(1)` | `switch` | 开关 |

### 第三步：排除系统字段

以下字段默认不生成到表单配置中：

| 排除字段 | 说明 |
|----------|------|
| `id` | 主键 |
| `create_time`, `created_at` | 创建时间（自动填充） |
| `update_time`, `updated_at` | 更新时间（自动填充） |
| `create_by`, `creator` | 创建人（自动填充） |
| `update_by`, `updater` | 更新人（自动填充） |
| `deleted`, `is_deleted` | 逻辑删除标识 |
| `tenant_id` | 租户ID |
| `version` | 乐观锁版本号 |
| `workflow_instance_id` | 流程实例ID（自动填充） |

### 第四步：询问用户

生成前询问用户：

1. **选择模式**：使用 JSON 配置模式还是 Vue 组件模式？
   - 简单表单 → 推荐 JSON 配置
   - 需要复杂联动 → 推荐 Vue 组件

2. **字段配置**：
   - 哪些字段需要必填？
   - 是否有下拉选项字段需要配置选项？
   - 是否有字段需要特殊处理（如禁用、隐藏）？

3. **流程信息**：
   - 流程名称（如：请假申请）
   - 流程 Key（如：LEAVE_REQUEST）

### 第五步：生成配置

**默认规则**：
- 所有字段默认归属于"开始"节点
- 字段标签使用数据库注释，如无注释则使用字段名
- 默认布局为每行 2 个字段（colSpan: 12）

---

## 生成的 JSON 配置模板

```json
{
  "formKey": "{FORM_KEY}",
  "version": "2.0.0",
  "labelWidth": "120px",
  "nodes": {
    "开始": {
      "sections": [
        {
          "key": "basicInfo",
          "title": "基本信息",
          "fields": [
            { "name": "field1", "label": "字段1", "type": "text", "required": true, "colSpan": 12 },
            { "name": "field2", "label": "字段2", "type": "number", "colSpan": 12 }
          ]
        }
      ]
    }
  }
}
```

---

## 生成的 Vue 组件模板

```vue
<template>
  <WorkflowPanel
    ref="workflowRef"
    :mode="pageMode"
    :workflow-definition-id="workflowDefinitionId"
    :current-node-name="currentNodeName"
    :node-operations="nodeOperations"
    @submit="handleSubmit"
    @success="onSuccess"
    @cancel="onCancel"
  >
    <!-- DynamicForm 渲染内嵌 JSON 配置 -->
    <DynamicForm
      v-if="nodeFormConfig"
      ref="dynamicFormRef"
      :config="nodeFormConfig"
      v-model="formData"
      :readonly="!isFormEditable"
    />
  </WorkflowPanel>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkflowForm } from '@/composables/useWorkflowForm'
import WorkflowPanel from '../components/WorkflowPanel.vue'
import DynamicForm from '@/components/DynamicForm/index.vue'
import type { FormConfig, NodeFormConfig } from '@/components/DynamicForm/types'

const router = useRouter()

const {
  pageMode,
  workflowDefinitionId,
  currentNodeName,
  formData,
  isFormEditable,
  nodeOperations,
  mergeNodeFormConfig  // 【重要】合并节点配置的方法
} = useWorkflowForm({
  startNodeName: '开始'  // 指定发起节点名称
})

const workflowRef = ref()
const dynamicFormRef = ref()

// 内嵌 JSON 配置（从归档表自动生成）
const formConfig: FormConfig = {
  formKey: '{FORM_KEY}',
  version: '2.0.0',
  labelWidth: '120px',
  nodes: {
    "开始": {
      sections: [
        {
          key: 'basicInfo',
          title: '基本信息',
          fields: [
            // 字段配置...
          ]
        }
      ]
    },
    // 审批节点：无需重复定义字段，mergeNodeFormConfig 会自动合并发起节点的配置
    // 如果审批节点需要额外字段，可以在这里添加 sections
    "部门审批": {
      sections: []  // 空数组，发起节点的字段会自动以只读方式显示
    }
  }
}

// 获取当前节点的表单配置（合并发起节点和当前节点）
// 审批/查看模式下会自动合并发起节点的表单配置，使审批人能看到发起人填写的数据
const nodeFormConfig = computed<NodeFormConfig | null>(() => {
  return mergeNodeFormConfig(formConfig)
})

const handleSubmit = async () => {
  const valid = await dynamicFormRef.value?.validate()
  if (!valid) {
    workflowRef.value?.resetSubmitting()
    return
  }
  await workflowRef.value?.doStartWorkflow(formData.value)
}

const onSuccess = () => router.push('/dashboard/TodoWorkflow')
const onCancel = () => router.back()
</script>
```

---

## 流程标题自动生成

流程实例的标题由后端自动生成，格式为：**流程名称-发起人姓名**

例如：
- 请假申请-张三
- 周报申请-李四

后端自动从流程定义获取名称，并拼接当前用户姓名。

---

## Step 3 详细流程：流程设计器配置

### 创建流程定义

1. 进入流程管理 → 流程设计
2. 点击"新建流程"
3. 填写基本信息：
   - 流程名称：如"请假申请"
   - 流程标识：如"LEAVE_REQUEST"（与 formKey 一致）
   - 页面路径：如 `/dashboard/workflow/pages/LeaveRequest`
   - 页面名称：如"请假申请"
4. 选择表单模式：
   - JSON 配置：填写 `LEAVE_REQUEST`（对应 forms/LEAVE_REQUEST.json）
   - Vue 组件：填写 `./pages/LeaveRequest.vue`

### 设计流程节点

1. 添加开始节点
2. 添加审批节点（如：部门审批、总经理审批）
3. 配置节点属性：
   - 节点名称：与 JSON 配置中的 key 对应
   - 审批人：选择角色或具体人员
   - 操作权限：同意、拒绝、退回、转交
4. 添加结束节点
5. 连接节点，配置流转条件

### 绑定归档表【重要】

在编辑流程属性弹窗中：

1. 找到"归档表"下拉框
2. 选择需要保存数据的归档表（如 `biz_leave_request`）
3. 归档表字段会自动与表单字段匹配（驼峰转下划线）
4. 流程完成后，表单数据会自动插入到归档表

**归档表要求**：
- 表单字段名（驼峰）会自动转为数据库字段名（下划线）
- 归档表应包含 `workflow_instance_id` 字段，用于关联流程实例
- 归档表可以包含 `status`、`create_by`、`create_time` 等系统字段

### 发布流程

1. 校验流程设计
2. 点击"发布"
3. 确认发布

---

## 归档表字段匹配规则

流程完成后，表单数据保存到归档表时会自动进行字段名转换：

| 表单字段名 (驼峰) | 数据库字段名 (下划线) |
|------------------|---------------------|
| `leaveType` | `leave_type` |
| `startDate` | `start_date` |
| `leaveDays` | `leave_days` |
| `reason` | `reason` |

**自动填充的系统字段**：

| 字段名 | 值 |
|--------|-----|
| `workflow_instance_id` | 流程实例 ID |
| `create_by` | 发起人 ID |
| `create_time` | 当前时间 |
| `status` | 默认 1 |

---

## Step 4 详细流程：手动调整配置

### 调整字段属性

在生成的 JSON 配置中调整：

```json
{
  "name": "leaveDays",
  "label": "请假天数",
  "type": "number",
  "required": true,
  "colSpan": 12,
  "props": {
    "min": 0.5,
    "max": 30,
    "step": 0.5
  }
}
```

### 配置节点表单

**重要**：审批节点无需重复定义发起节点的字段，`mergeNodeFormConfig` 会自动合并发起节点的配置并以只读方式显示。

```json
{
  "nodes": {
    "开始": {
      "sections": [
        {
          "key": "leaveInfo",
          "title": "请假信息",
          "fields": [
            { "name": "leaveType", ... },
            { "name": "startDate", ... },
            { "name": "endDate", ... },
            { "name": "leaveDays", ... },
            { "name": "reason", ... }
          ]
        }
      ]
    },
    "部门审批": {
      "sections": []
    }
  }
}
```

**合并逻辑**：
- 发起模式：直接返回发起节点配置
- 审批/查看模式：自动合并发起节点配置（字段设为只读）+ 当前节点额外配置
- 发起节点字段的 title 会自动追加 "（发起信息）" 后缀

如果审批节点需要额外字段（如审批意见以外的补充信息）：

```json
{
  "nodes": {
    "开始": { ... },
    "部门审批": {
      "sections": [
        {
          "key": "approvalExtra",
          "title": "审批补充信息",
          "fields": [
            { "name": "approvalNote", "label": "审批备注", "type": "textarea", "colSpan": 24 }
          ]
        }
      ]
    }
  }
}
```

### 添加联动逻辑

```json
{
  "initScript": {
    "onLoad": "form.startDate = getMonday();",
    "onFieldChange": {
      "startDate": "form.endDate = getSunday(form.startDate);"
    }
  }
}
```

---

## Step 5 详细流程：测试运行

### 发起流程测试

1. 进入"发起流程"页面
2. 选择流程类型
3. 填写表单数据
4. 提交流程
5. 验证数据保存到归档表

### 审批流程测试

1. 使用审批人账号登录
2. 进入"待办任务"
3. 查看流程详情
4. 执行审批操作
5. 验证审批记录保存

### 验证归档数据

流程完成后，检查归档表：

```sql
SELECT * FROM biz_leave_request 
WHERE workflow_instance_id = {流程实例ID};
```

---

## 常用字段配置参考

### 文本输入

```json
{ "name": "title", "label": "标题", "type": "text", "required": true, "colSpan": 24 }
```

### 数字输入

```json
{ "name": "amount", "label": "金额", "type": "number", "required": true, "colSpan": 12,
  "props": { "min": 0, "precision": 2 }
}
```

### 日期选择

```json
{ "name": "date", "label": "日期", "type": "date", "required": true, "colSpan": 12,
  "props": { "disabledDate": "future" }
}
```

### 下拉选择

```json
{ "name": "type", "label": "类型", "type": "select", "required": true, "colSpan": 12,
  "options": [
    { "label": "选项1", "value": "1" },
    { "label": "选项2", "value": "2" }
  ]
}
```

### 多行文本

```json
{ "name": "content", "label": "内容", "type": "textarea", "colSpan": 24,
  "props": { "rows": 4, "maxlength": 500 }
}
```

### 只读字段

```json
{ "name": "serialNo", "label": "流水号", "type": "text", "disabled": true, "colSpan": 12 }
```

---

## 审批意见处理

审批意见是 `WorkflowPanel` 的内置功能：

| 节点类型 | 是否显示审批意见 |
|----------|------------------|
| 发起节点 | 不显示 |
| 审批节点 | 自动显示 |

**无需在 JSON 配置或 Vue 组件中定义审批意见字段**。

---

## 注意事项

1. **节点名称必须唯一**：JSON 配置中的 key 与流程节点名称对应
2. **字段 name 不能重复**：同一流程内所有字段名称唯一
3. **归档表需有字段注释**：注释会作为字段标签显示
4. **colSpan 总和应为 24 的倍数**：保证布局整齐
5. **审批意见无需配置**：WorkflowPanel 自动处理
6. **流程标题自动生成**：格式为「流程名称-发起人」，由后端自动生成
7. **流程 Key 与 formKey 一致**：便于管理和查找
8. **归档表字段名使用下划线**：表单字段（驼峰）会自动转为数据库字段（下划线）
9. **绑定归档表后流程完成自动保存**：无需手动处理数据持久化

---

## 相关文档

- [流程表单开发指南 v2](../../docs/workflow-form-development-guide-v2.md)
- [流程表单节点级配置设计](../../docs/plans/2026-03-27-workflow-form-node-config-design.md)

## 示例代码

完整的 Vue 组件示例请参考 [examples/example.vue](examples/example.vue)，展示了周报流程表单的完整实现。
