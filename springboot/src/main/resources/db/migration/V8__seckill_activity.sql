CREATE TABLE IF NOT EXISTS seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '活动名称',
    description VARCHAR(500) COMMENT '活动描述',
    start_time DATETIME NOT NULL COMMENT '活动开始时间',
    end_time DATETIME NOT NULL COMMENT '活动结束时间',
    status TINYINT DEFAULT 0 COMMENT '活动状态: 0-未开始 1-进行中 2-已结束',
    per_limit INT DEFAULT 1 COMMENT '每人限购数量',
    enable_captcha BOOLEAN DEFAULT TRUE COMMENT '是否启用验证码',
    enable_ip_limit BOOLEAN DEFAULT TRUE COMMENT '是否启用IP限制',
    sign_key VARCHAR(64) COMMENT '活动签名密钥',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) COMMENT='秒杀活动表';