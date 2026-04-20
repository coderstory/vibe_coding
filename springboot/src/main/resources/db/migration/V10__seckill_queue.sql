CREATE TABLE IF NOT EXISTS seckill_queue (
    queue_id VARCHAR(64) PRIMARY KEY COMMENT '队列ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    goods_id BIGINT NOT NULL COMMENT '商品ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-排队中 1-成功 2-失败',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='秒杀排队表';