-- Flyway 迁移脚本 V7 - 添加菜单管理页面到菜单表

USE admin_system;

-- 插入菜单管理页面（添加到系统管理下）
INSERT INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(6, 1, '菜单管理', '/system/menu', 'Menu', 4)
ON DUPLICATE KEY UPDATE name = name;
