package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
}
