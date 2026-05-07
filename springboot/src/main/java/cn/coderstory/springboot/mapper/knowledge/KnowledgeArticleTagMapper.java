package cn.coderstory.springboot.mapper.knowledge;

import cn.coderstory.springboot.entity.knowledge.KnowledgeArticleTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeArticleTagMapper extends BaseMapper<KnowledgeArticleTag> {

    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);

    void deleteByArticleId(@Param("articleId") Long articleId);

    void insertBatch(@Param("articleId") Long articleId, @Param("tagIds") List<Long> tagIds);
}
