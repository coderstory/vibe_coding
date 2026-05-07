package cn.coderstory.springboot.entity.seckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
