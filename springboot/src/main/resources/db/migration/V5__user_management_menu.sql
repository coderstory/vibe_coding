-- Flyway 迁移脚本 V5 - 用户管理菜单数据
-- 幂等插入：如果菜单已存在则忽略

USE admin_system;

-- 确保用户管理菜单存在（id=2, parent_id=1）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(2, 1, '用户管理', '/system/user', 'User', 1);

-- 确保系统管理父菜单存在（id=1）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(1, 0, '系统管理', '', 'Setting', 1);

-- 确保超级管理员角色拥有用户管理菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 1), (1, 2);