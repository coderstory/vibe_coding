CREATE TABLE IF NOT EXISTS seckill_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '用户ID',
    goods_id BIGINT COMMENT '商品ID',
    activity_id BIGINT COMMENT '活动ID',
    request_time DATETIME NOT NULL COMMENT '请求时间',
    response_time DATETIME COMMENT '响应时间',
    elapsed_ms INT COMMENT '耗时(毫秒)',
    result TINYINT COMMENT '结果: 0-排队中 1-成功 2-库存不足 3-签名无效 4-已限购',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    captcha_verified BOOLEAN DEFAULT FALSE COMMENT '验证码是否通过',
    ip_blacklisted BOOLEAN DEFAULT FALSE COMMENT '是否IP黑名单',
    sign VARCHAR(128) COMMENT '请求签名',
    idempotent_key VARCHAR(64) COMMENT '幂等键',
    INDEX idx_time (request_time),
    INDEX idx_user (user_id),
    INDEX idx_goods (goods_id),
    INDEX idx_result (result)
) COMMENT='秒杀请求日志表';