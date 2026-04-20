CREATE TABLE IF NOT EXISTS seckill_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    reserve_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '状态: 0-预约 1-已提醒 2-已过期',
    notified BOOLEAN DEFAULT FALSE COMMENT '是否已发送提醒',
    notify_time DATETIME COMMENT '提醒时间',
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    INDEX idx_activity (activity_id),
    INDEX idx_notify (status, notified)
) COMMENT='秒杀预约表';