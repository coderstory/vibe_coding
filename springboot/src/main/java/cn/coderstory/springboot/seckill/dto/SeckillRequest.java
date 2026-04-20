package cn.coderstory.springboot.seckill.dto;

import lombok.Data;

@Data
public class SeckillRequest {
    private Long goodsId;
    private Long activityId;
    private String sign;
    private Long timestamp;
    private String idempotentKey;
}