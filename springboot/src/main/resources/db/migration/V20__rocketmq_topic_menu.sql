-- Flyway 迁移脚本 V20 - RocketMQ Topic 管理菜单
-- 添加 Topic 管理菜单位于 RocketMQ 父菜单下

USE admin_system;

-- 创建 RocketMQ 父菜单（id=20）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(20, 0, 'RocketMQ管理', '', 'Lightning', 6);

-- 创建 Topic 管理子菜单（id=21）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(21, 20, 'Topic 管理', '/rocketmq/topics', 'Topic', 1);

-- 为超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 20), (1, 21);
