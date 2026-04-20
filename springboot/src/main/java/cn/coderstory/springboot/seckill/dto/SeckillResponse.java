package cn.coderstory.springboot.seckill.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeckillResponse {
    private String queueId;
    private Integer status;
    private String message;

    public static SeckillResponse queued(String queueId) {
        return SeckillResponse.builder()
            .queueId(queueId)
            .status(0)
            .message("排队中")
            .build();
    }

    public static SeckillResponse success(String queueId) {
        return SeckillResponse.builder()
            .queueId(queueId)
            .status(1)
            .message("抢购成功")
            .build();
    }

    public static SeckillResponse failed(String message) {
        return SeckillResponse.builder()
            .status(2)
            .message(message)
            .build();
    }
}