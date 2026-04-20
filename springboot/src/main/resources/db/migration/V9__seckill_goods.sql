CREATE TABLE IF NOT EXISTS seckill_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    seckill_price DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
    stock INT NOT NULL COMMENT '总库存',
    sold INT DEFAULT 0 COMMENT '已售',
    image_url VARCHAR(500) COMMENT '商品图片',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_activity (activity_id),
    FOREIGN KEY (activity_id) REFERENCES seckill_activity(id)
) COMMENT='秒杀商品表';