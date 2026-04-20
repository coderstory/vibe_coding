package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> allMenus = menuMapper.selectAllOrderBySortOrder();
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<Menu> getMenuTreeByRoleId(Long roleId) {
        List<Menu> roleMenus = menuMapper.selectByRoleId(roleId);
        return buildMenuTree(roleMenus, 0L);
    }

    @Override
    @Transactional
    public Menu createMenu(Menu menu) {
        if (menu.getName() == null || menu.getName().isEmpty()) {
            throw BusinessException.badRequest("菜单名称不能为空");
        }
        if (menu.getPath() == null || menu.getPath().isEmpty()) {
            throw BusinessException.badRequest("菜单路径不能为空");
        }
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSortOrder() == null) {
            menu.setSortOrder(0);
        }
        menuMapper.insert(menu);
        return menu;
    }

    @Override
    @Transactional
    public Menu updateMenu(Long id, Menu menu) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null) {
            throw BusinessException.notFound("菜单不存在");
        }
        if (menu.getName() != null) {
            existing.setName(menu.getName());
        }
        if (menu.getPath() != null) {
            existing.setPath(menu.getPath());
        }
        if (menu.getIcon() != null) {
            existing.setIcon(menu.getIcon());
        }
        if (menu.getParentId() != null) {
            existing.setParentId(menu.getParentId());
        }
        if (menu.getSortOrder() != null) {
            existing.setSortOrder(menu.getSortOrder());
        }
        menuMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public boolean deleteMenu(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw BusinessException.notFound("菜单不存在");
        }
        // 检查是否有子菜单
        List<Menu> children = menuMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw BusinessException.badRequest("请先删除子菜单");
        }
        return menuMapper.deleteById(id) > 0;
    }

    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
            .filter(menu -> parentId.equals(menu.getParentId()))
            .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
            .collect(Collectors.toList());
    }
}
