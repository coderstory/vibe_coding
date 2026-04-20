CREATE TABLE IF NOT EXISTS ip_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ip_address VARCHAR(50) NOT NULL UNIQUE COMMENT 'IP地址',
    reason VARCHAR(200) COMMENT '封禁原因',
    expire_time DATETIME COMMENT '过期时间(NULL表示永久)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_expire (expire_time)
) COMMENT='IP黑名单';