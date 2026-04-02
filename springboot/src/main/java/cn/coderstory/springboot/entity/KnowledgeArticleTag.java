package cn.coderstory.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge_article_tag")
public class KnowledgeArticleTag {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long articleId;
    
    private Long tagId;
}
