-- Flyway 迁移脚本 V18 - 修改菜单名称
-- 将"业务数据-数据列表"改为"学习工具-知识库"

USE admin_system;

-- 修改父菜单名称：业务数据 -> 学习工具
UPDATE sys_menu SET name = '学习工具' WHERE id = 5 AND name = '业务数据';

-- 修改子菜单名称：数据列表 -> 知识库
UPDATE sys_menu SET name = '知识库' WHERE id = 6 AND name = '数据列表';
