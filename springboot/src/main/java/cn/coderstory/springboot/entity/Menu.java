package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class Menu {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;
    
    private String name;
    
    private String path;
    
    private String icon;
    
    private Integer sortOrder;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
