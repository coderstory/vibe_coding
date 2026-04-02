package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("knowledge_category")
public class KnowledgeCategory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId; 
    
    private String name;
    
    private Integer sortOrder;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(exist = false)
    private List<KnowledgeCategory> children = new ArrayList<>();
}
