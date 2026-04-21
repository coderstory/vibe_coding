-- Flyway 迁移脚本 V19 - 添加商品管理和活动管理菜单
-- 将商品管理、活动管理添加到秒杀系统菜单下

USE admin_system;

-- 更新秒杀系统父菜单名称（如果有）
UPDATE sys_menu SET name = '秒杀系统' WHERE id = 15;

-- 插入商品管理子菜单（id=16）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(16, 15, '商品管理', '/seckill/goods', 'Goods', 1);

-- 插入活动管理子菜单（id=17）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(17, 15, '活动管理', '/seckill/activity', 'Calendar', 2);

-- 为超级管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 16), (1, 17);
