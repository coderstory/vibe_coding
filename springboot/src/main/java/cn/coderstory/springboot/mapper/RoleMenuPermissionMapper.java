package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.RoleMenuPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface RoleMenuPermissionMapper extends BaseMapper<RoleMenuPermission> {
    
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
