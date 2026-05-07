package cn.coderstory.springboot.mapper.knowledge;

import cn.coderstory.springboot.entity.knowledge.KnowledgeFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeFileMapper extends BaseMapper<KnowledgeFile> {

    List<KnowledgeFile> selectByArticleId(@Param("articleId") Long articleId);
}
