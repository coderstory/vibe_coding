package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.KnowledgeFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeFileMapper extends BaseMapper<KnowledgeFile> {
    
    List<KnowledgeFile> selectByArticleId(@Param("articleId") Long articleId);
}
