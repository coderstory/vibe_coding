package cn.coderstory.springboot.mapper.knowledge;

import cn.coderstory.springboot.entity.knowledge.KnowledgeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {

    List<KnowledgeCategory> selectTree();
}
