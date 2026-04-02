package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.entity.Role;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

public interface RoleService {
    
    Role getById(Long id);
    
    IPage<Role> getRolePage(Page<Role> page, String roleName);
    
    boolean saveRole(Role role);
    
    boolean updateRole(Role role);
    
    boolean deleteRole(Long id);
    
    List<Menu> getMenusByRoleId(Long roleId);
    
    boolean assignMenus(Long roleId, List<Long> menuIds);
}
