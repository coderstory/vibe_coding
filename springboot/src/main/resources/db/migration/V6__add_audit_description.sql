-- Flyway 迁移脚本 V6 - 添加审计日志描述字段

USE admin_system;

-- 添加描述字段到审计日志表
ALTER TABLE sys_audit_log ADD COLUMN IF NOT EXISTS description VARCHAR(500) COMMENT '操作描述' AFTER ip_address;
