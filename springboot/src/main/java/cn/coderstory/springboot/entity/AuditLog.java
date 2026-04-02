package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_audit_log")
public class AuditLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String operation;      // 登录/登出/新增/编辑/删除
    
    private String targetType;     // 目标对象类型
    
    private String targetId;       // 目标对象ID
    
    private String ipAddress;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
