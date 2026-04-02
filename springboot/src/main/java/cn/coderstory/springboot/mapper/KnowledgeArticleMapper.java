package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.KnowledgeArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticle> {
    
    List<KnowledgeArticle> searchArticles(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
