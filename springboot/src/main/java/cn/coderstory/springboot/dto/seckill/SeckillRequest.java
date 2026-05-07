package cn.coderstory.springboot.dto.seckill;

import lombok.Data;

@Data
public class SeckillRequest {
    private Long goodsId;
    private Long activityId;
    private String sign;
    private Long timestamp;
    private String idempotentKey;
    private String queueId; // 前端传入，用于SSE通知
}