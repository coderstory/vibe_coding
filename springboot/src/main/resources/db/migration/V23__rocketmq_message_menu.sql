-- Flyway 迁移脚本 V23 - RocketMQ 消息管理菜单
-- 添加消息管理菜单位于 RocketMQ 父菜单下

USE admin_system;

-- 创建消息管理子菜单（id=23）
-- RocketMQ 父菜单已存在（id=20）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(23, 20, '消息管理', '/rocketmq/messages', 'ChatDotRound', 3);

-- 为超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 23);
