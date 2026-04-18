-- Flyway 迁移脚本 V7 - 添加菜单管理页面并授权

USE admin_system;

-- 插入菜单管理页面（添加到系统管理下）
INSERT INTO sys_menu (parent_id, name, path, icon, sort_order) VALUES
(1, '菜单管理', '/system/menu', 'Menu', 4);

-- 获取刚插入的菜单ID并授权给超级管理员
SET @menu_id = LAST_INSERT_ID();
SET @sql = CONCAT('INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, ', @menu_id, ') ON DUPLICATE KEY UPDATE role_id = role_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
