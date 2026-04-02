package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    
    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        return menus.stream()
            .filter(menu -> parentId.equals(menu.getParentId()))
            .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
            .collect(Collectors.toList());
    }
}
