-- Flyway 迁移脚本 V25 - 硬件监控页面菜单
-- 添加硬件监控菜单位于监控大盘父菜单下

USE admin_system;

-- 创建硬件监控子菜单（id=25）
-- 监控大盘父菜单已存在（id=14）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(25, 14, '硬件监控', '/monitor/hardware', 'Monitor', 1);

-- 为超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 25);
