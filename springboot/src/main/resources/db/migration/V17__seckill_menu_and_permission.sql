-- Flyway 迁移脚本 V17 - 添加秒杀系统菜单和授权
-- 添加秒杀系统、订单管理、监控大盘等菜单

USE admin_system;

-- 插入秒杀系统父菜单（id=15）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(15, 0, '秒杀系统', '', 'Lightning', 3);

-- 插入秒杀系统子菜单
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(9, 15, '秒杀首页', '/seckill', 'Shop', 1),
(10, 15, '抢购记录', '/seckill/record', 'Document', 2),
(11, 15, '秒杀购物车', '/seckill/cart', 'ShoppingCart', 3);

-- 插入订单管理菜单（id=12）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(12, 0, '订单管理', '', 'Ticket', 4);

-- 插入订单管理子菜单
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(13, 12, '订单列表', '/order/list', 'List', 1);

-- 插入监控大盘菜单（id=14）
INSERT IGNORE INTO sys_menu (id, parent_id, name, path, icon, sort_order) VALUES
(14, 0, '监控大盘', '/monitor', 'DataLine', 5);

-- 为超级管理员角色分配所有秒杀系统菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(1, 15), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14);
