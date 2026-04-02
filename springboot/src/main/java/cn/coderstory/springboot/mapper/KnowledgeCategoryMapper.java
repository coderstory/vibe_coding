package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.KnowledgeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
    
    List<KnowledgeCategory> selectTree();
}
