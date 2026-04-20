CREATE TABLE IF NOT EXISTS stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL UNIQUE COMMENT '商品ID',
    total_stock INT NOT NULL COMMENT '总库存',
    available_stock INT NOT NULL COMMENT '可用库存',
    locked_stock INT DEFAULT 0 COMMENT '已锁定库存',
    version INT DEFAULT 0 COMMENT '乐观锁版本',
    mq_deduct_count INT DEFAULT 0 COMMENT 'MQ已扣减次数',
    mq_rollback_count INT DEFAULT 0 COMMENT 'MQ已回滚次数',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT='库存表';