package cn.coderstory.springboot.mapper.knowledge;

import cn.coderstory.springboot.entity.knowledge.KnowledgeArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeArticleMapper extends BaseMapper<KnowledgeArticle> {

    List<KnowledgeArticle> searchArticles(@Param("keyword") String keyword, @Param("categoryId") Long categoryId);
}
