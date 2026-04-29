-- Flyway 迁移脚本 V24 - RocketMQ 监控面板菜单
-- 添加监控面板菜单位于 RocketMQ 父菜单下

USE admin_system;

-- 创建监控面板子菜单（id=24）
-- RocketMQ 父菜单已存在（id=20）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(24, 20, '监控面板', '/rocketmq/dashboard', 'DataLine', 4);

-- 为超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 24);
