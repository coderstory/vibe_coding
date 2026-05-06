package cn.coderstory.springboot.menu.service;

import cn.coderstory.springboot.menu.entity.Menu;
import java.util.List;

public interface MenuService {

    List<Menu> getMenuTree();

    List<Menu> getMenuTreeByRoleId(Long roleId);

    Menu createMenu(Menu menu);

    Menu updateMenu(Long id, Menu menu);

    boolean deleteMenu(Long id);
}
