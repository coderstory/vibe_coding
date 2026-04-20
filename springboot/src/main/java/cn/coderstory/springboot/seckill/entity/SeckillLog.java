package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seckill_log")
public class SeckillLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long goodsId;
    private Long activityId;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private Integer elapsedMs;
    private Integer result;
    private String failReason;
    private Boolean captchaVerified;
    private Boolean ipBlacklisted;
    private String sign;
    private String idempotentKey;
}