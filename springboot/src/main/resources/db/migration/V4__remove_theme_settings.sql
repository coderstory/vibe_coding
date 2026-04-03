-- V4: 删除主题设置相关表和代码
-- sys_user_settings 表仅用于存储主题设置，现已移除主题切换功能
DROP TABLE IF EXISTS sys_user_settings;
