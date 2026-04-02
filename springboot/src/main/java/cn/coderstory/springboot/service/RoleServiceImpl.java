package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.entity.Role;
import cn.coderstory.springboot.entity.RoleMenuPermission;
import cn.coderstory.springboot.mapper.RoleMapper;
import cn.coderstory.springboot.mapper.MenuMapper;
import cn.coderstory.springboot.mapper.RoleMenuPermissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuPermissionMapper roleMenuPermissionMapper;
    
    @Override
    public Role getById(Long id) {
        return roleMapper.selectById(id);
    }
    
    @Override
    public IPage<Role> getRolePage(Page<Role> page, String roleName) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(Role::getRoleName, roleName);
        }
        wrapper.orderByDesc(Role::getCreateTime);
        return roleMapper.selectPage(page, wrapper);
    }
    
    @Override
    public boolean saveRole(Role role) {
        return roleMapper.insert(role) > 0;
    }
    
    @Override
    public boolean updateRole(Role role) {
        return roleMapper.updateById(role) > 0;
    }
    
    @Override
    public boolean deleteRole(Long id) {
        return roleMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<Menu> getMenusByRoleId(Long roleId) {
        return menuMapper.selectByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // Delete existing role-menu permissions
        roleMenuPermissionMapper.deleteByRoleId(roleId);
        
        // Insert new permissions
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenuPermission rmp = new RoleMenuPermission();
                rmp.setRoleId(roleId);
                rmp.setMenuId(menuId);
                roleMenuPermissionMapper.insert(rmp);
            }
        }
        
        return true;
    }
}
