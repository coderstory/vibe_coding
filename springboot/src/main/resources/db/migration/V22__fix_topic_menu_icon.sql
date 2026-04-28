-- Flyway 迁移脚本 V22 - 修复 Topic 管理菜单图标
-- 将无效的 'Topic' 图标改为有效的 Element Plus 图标 'Tickets'

USE admin_system;

-- 更新 Topic 管理菜单的图标 (id=21)
-- 'Topic' 不是有效的 Element Plus 图标，'Tickets' 更合适
UPDATE sys_menu SET icon = 'Tickets' WHERE id = 21 AND icon = 'Topic';