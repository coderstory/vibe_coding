-- Flyway 迁移脚本 V6 - 添加审计日志描述字段

USE admin_system;

-- 添加描述字段到审计日志表（MySQL 版本，需要先检查列是否存在）
SET @column_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'admin_system'
    AND TABLE_NAME = 'sys_audit_log'
    AND COLUMN_NAME = 'description'
);

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE sys_audit_log ADD COLUMN description VARCHAR(500) COMMENT ''操作描述'' AFTER ip_address',
    'SELECT ''Column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
