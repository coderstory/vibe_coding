package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_file")
public class KnowledgeFile {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long articleId;
    
    private String fileName;
    
    private Long fileSize;
    
    private byte[] compressedData;
    
    private Long compressedSize;
    
    private String contentType;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
