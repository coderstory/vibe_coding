package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_queue")
public class SeckillQueue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String queueId;
    private Long userId;
    private Long goodsId;
    private Integer status;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
