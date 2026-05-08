package cn.coderstory.springboot.service.menu;

import cn.coderstory.springboot.entity.menu.Menu;

import java.util.List;

/**
 * 菜单管理服务接口。
 * <p>
 * 提供菜单的树形结构查询、CRUD 操作和按角色分配菜单功能。
 *
 * @since 1.7.0
 */
public interface MenuService {

    /**
     * 获取菜单树。
     *
     * @return 菜单树结构列表
     */
    List<Menu> getMenuTree();

    /**
     * 根据角色 ID 获取已分配的菜单树。
     *
     * @param roleId 角色 ID
     * @return 菜单树结构列表
     */
    List<Menu> getMenuTreeByRoleId(Long roleId);

    /**
     * 创建菜单。
     *
     * @param menu 菜单实体
     * @return 已创建的菜单
     */
    Menu createMenu(Menu menu);

    /**
     * 更新菜单。
     *
     * @param id   菜单 ID
     * @param menu 菜单更新数据
     * @return 更新后的菜单
     */
    Menu updateMenu(Long id, Menu menu);

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     * @return 删除是否成功
     */
    boolean deleteMenu(Long id);
}
