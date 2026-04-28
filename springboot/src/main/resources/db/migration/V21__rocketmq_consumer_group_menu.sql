-- Flyway 迁移脚本 V21 - RocketMQ Consumer Group 管理菜单
-- 添加 Consumer Group 管理菜单位于 RocketMQ 父菜单下

USE admin_system;

-- 创建 Consumer Group 管理子菜单（id=22）
-- RocketMQ 父菜单已存在（id=20）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(22, 20, 'Consumer Group 管理', '/rocketmq/consumer-groups', 'Connection', 2);

-- 为超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 22);