package cn.coderstory.springboot.mapper.menu;

import cn.coderstory.springboot.entity.menu.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> selectMenuTree();

    List<Menu> selectByRoleId(@Param("roleId") Long roleId);

    List<Menu> selectAllOrderBySortOrder();

    List<Menu> selectByParentId(@Param("parentId") Long parentId);
}
