package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.Menu;
import java.util.List;

public interface MenuService {
    
    List<Menu> getMenuTree();
    
    List<Menu> getMenuTreeByRoleId(Long roleId);
}
