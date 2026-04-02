package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.KnowledgeTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeTagMapper extends BaseMapper<KnowledgeTag> {
    
    KnowledgeTag selectByName(@Param("name") String name);
    
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);
}
