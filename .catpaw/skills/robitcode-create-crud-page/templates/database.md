# 数据库模板

## DDL 规范

### 标准字段（所有表必须包含）

```sql
`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
`deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`create_by` BIGINT DEFAULT NULL COMMENT '创建人',
`update_by` BIGINT DEFAULT NULL COMMENT '更新人',
`tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID'
```

### 完整 DDL 示例

```sql
CREATE TABLE sys_{table_name} (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  
  -- 业务字段
  `name` VARCHAR(100) NOT NULL COMMENT '{字段中文名}',
  `code` VARCHAR(50) DEFAULT NULL COMMENT '编码',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  
  -- 关联字段（外键）
  `{ref_entity}_id` BIGINT DEFAULT NULL COMMENT '{关联实体中文名}ID',
  
  -- 标准字段
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` BIGINT DEFAULT NULL COMMENT '创建人',
  `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
  `tenant_id` BIGINT DEFAULT NULL COMMENT '租户ID',
  
  -- 索引
  KEY `idx_{ref_entity}` (`{ref_entity}_id`),
  KEY `idx_status` (`status`),
  
  -- 外键约束（可选）
  CONSTRAINT `fk_{table}_{ref_entity}` FOREIGN KEY (`{ref_entity}_id`) 
    REFERENCES `sys_{ref_table}` (`id`) ON DELETE SET NULL
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{表中文名}';
```

### 字段类型映射

| 业务类型 | 数据库类型 | 备注 |
|----------|------------|------|
| 名称/标题 | VARCHAR(100) | NOT NULL |
| 编码 | VARCHAR(50) | UNIQUE |
| 描述/备注 | VARCHAR(500) | |
| 长文本 | TEXT | |
| 价格/金额 | DECIMAL(10,2) | DEFAULT 0 |
| 数量/库存 | INT | DEFAULT 0 |
| 排序 | INT | DEFAULT 0 |
| 状态 | TINYINT | DEFAULT 1 |
| 外键 | BIGINT | DEFAULT NULL |
| 时间 | DATETIME | |

---

## 更新 init.sql

生成完成后，将 DDL 和菜单 SQL 追加到：

```
service/src/main/resources/db/init.sql
```
