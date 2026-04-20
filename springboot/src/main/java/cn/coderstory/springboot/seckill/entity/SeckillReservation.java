package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seckill_reservation")
public class SeckillReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long activityId;
    private LocalDateTime reserveTime;
    private Integer status;
    private Boolean notified;
    private LocalDateTime notifyTime;
}