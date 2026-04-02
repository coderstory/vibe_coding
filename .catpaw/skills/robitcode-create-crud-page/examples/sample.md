# 示例输入

## 示例 1：自然语言描述

```
创建一个产品管理页面，包含产品名称、编码、价格、库存、所属分类、状态、排序
```

**解析结果：**
- 实体：Product
- 中文名：产品
- 字段：
  - name (String) - 产品名称
  - code (String) - 编码
  - price (BigDecimal) - 价格
  - stock (Integer) - 库存
  - categoryId (Long → Category) - 所属分类
  - status (Integer) - 状态
  - sort (Integer) - 排序

---

## 示例 2：SQL DDL

```sql
CREATE TABLE sys_product (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '编码',
  `price` DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` BIGINT DEFAULT NULL,
  `update_by` BIGINT DEFAULT NULL,
  `tenant_id` BIGINT DEFAULT NULL,
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';
```

---

## 示例 3：结构化 JSON

```json
{
  "entity": "Product",
  "label": "产品",
  "table": "sys_product",
  "fields": [
    {
      "name": "name",
      "label": "产品名称",
      "type": "string",
      "required": true,
      "tableShow": true,
      "formType": "input",
      "sortable": true
    },
    {
      "name": "code",
      "label": "编码",
      "type": "string",
      "required": false,
      "tableShow": true,
      "formType": "input"
    },
    {
      "name": "price",
      "label": "价格",
      "type": "decimal",
      "required": true,
      "tableShow": true,
      "formType": "number"
    },
    {
      "name": "stock",
      "label": "库存",
      "type": "integer",
      "required": false,
      "tableShow": true,
      "formType": "number"
    },
    {
      "name": "categoryId",
      "label": "所属分类",
      "type": "foreign",
      "refEntity": "Category",
      "refLabelField": "name",
      "refValueField": "id",
      "required": false,
      "tableShow": true,
      "formType": "select",
      "filterable": true
    },
    {
      "name": "sort",
      "label": "排序",
      "type": "integer",
      "required": false,
      "tableShow": true,
      "formType": "number"
    },
    {
      "name": "status",
      "label": "状态",
      "type": "integer",
      "required": false,
      "tableShow": true,
      "formType": "switch"
    },
    {
      "name": "remark",
      "label": "备注",
      "type": "string",
      "required": false,
      "tableShow": false,
      "formType": "textarea"
    }
  ]
}
```

---

## 字段类型对照表

| type 值 | Java 类型 | 数据库类型 | 表单组件 |
|---------|-----------|------------|----------|
| string | String | VARCHAR | input |
| text | String | TEXT | textarea |
| integer | Integer | INT | number |
| long | Long | BIGINT | number |
| decimal | BigDecimal | DECIMAL | number |
| boolean | Boolean | TINYINT | switch |
| datetime | LocalDateTime | DATETIME | date-picker |
| date | LocalDate | DATE | date-picker |
| foreign | Long | BIGINT | select |

## formType 可选值

| formType | 说明 |
|----------|------|
| input | 文本输入框 |
| textarea | 多行文本 |
| number | 数字输入框 |
| select | 下拉选择 |
| switch | 开关 |
| date-picker | 日期选择 |
| radio | 单选框 |
| checkbox | 复选框 |
