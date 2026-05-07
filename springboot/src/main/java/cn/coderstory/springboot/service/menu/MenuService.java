package cn.coderstory.springboot.service.menu;

import cn.coderstory.springboot.entity.menu.Menu;

import java.util.List;

public interface MenuService {

    List<Menu> getMenuTree();

    List<Menu> getMenuTreeByRoleId(Long roleId);

    Menu createMenu(Menu menu);

    Menu updateMenu(Long id, Menu menu);

    boolean deleteMenu(Long id);
}
