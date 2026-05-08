package cn.coderstory.springboot.service.role;

import cn.coderstory.springboot.entity.menu.Menu;
import cn.coderstory.springboot.entity.role.Role;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 角色管理服务接口。
 * <p>
 * 提供角色的 CRUD 操作和角色-菜单分配功能。
 *
 * @since 1.7.0
 */
public interface RoleService {

    /**
     * 根据 ID 获取角色。
     *
     * @param id 角色 ID
     * @return 角色实体
     */
    Role getById(Long id);

    /**
     * 分页查询角色列表。
     *
     * @param page     分页参数
     * @param roleName 角色名称（可选，模糊匹配）
     * @return 角色分页结果
     */
    IPage<Role> getRolePage(Page<Role> page, String roleName);

    /**
     * 新增角色。
     *
     * @param role 角色实体
     * @return 新增是否成功
     */
    boolean saveRole(Role role);

    /**
     * 更新角色。
     *
     * @param role 角色更新数据
     * @return 更新是否成功
     */
    boolean updateRole(Role role);

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     * @return 删除是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 根据角色 ID 获取已分配的菜单列表。
     *
     * @param roleId 角色 ID
     * @return 菜单列表
     */
    List<Menu> getMenusByRoleId(Long roleId);

    /**
     * 为角色分配菜单权限。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 列表
     * @return 分配是否成功
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);
}
