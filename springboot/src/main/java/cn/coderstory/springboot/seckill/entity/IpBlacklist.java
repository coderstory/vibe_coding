package cn.coderstory.springboot.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ip_blacklist")
public class IpBlacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ipAddress;
    private String reason;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}