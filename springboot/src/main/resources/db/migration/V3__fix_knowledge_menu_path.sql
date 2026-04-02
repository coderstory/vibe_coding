-- V3: 修复知识库菜单路径

USE admin_system;

-- 修复菜单路径：/business/data -> business
UPDATE sys_menu SET path = 'business' WHERE id = 6;

-- 确保管理员角色拥有知识管理菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 6);
