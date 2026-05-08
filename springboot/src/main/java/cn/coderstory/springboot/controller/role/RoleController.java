package cn.coderstory.springboot.controller.role;

import cn.coderstory.springboot.entity.menu.Menu;
import cn.coderstory.springboot.entity.role.Role;
import cn.coderstory.springboot.service.role.RoleService;
import cn.coderstory.springboot.dto.ApiResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器。
 * <p>
 * 提供角色的 CRUD 操作和菜单权限分配功能。
 *
 * @since 1.7.0
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色列表。
     *
     * @param roleName 角色名称（可选筛选条件）
     * @param page     页码
     * @param size     每页条数
     * @return 角色分页结果，包含记录列表和分页信息
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRolePage(
        @RequestParam(required = false) String roleName,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size) {

        Page<Role> pageParam = new Page<>(page, size);
        IPage<Role> result = roleService.getRolePage(pageParam, roleName);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("size", result.getSize());
        data.put("current", result.getCurrent());
        data.put("pages", result.getPages());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 根据角色 ID 获取角色详情。
     *
     * @param id 角色 ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    /**
     * 创建角色。
     *
     * @param role 角色信息
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRole(@RequestBody Role role) {
        boolean success = roleService.saveRole(role);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("创建成功", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("创建失败"));
        }
    }

    /**
     * 更新角色信息。
     *
     * @param id   角色 ID
     * @param role 角色信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        boolean success = roleService.updateRole(role);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("更新成功", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("更新失败"));
        }
    }

    /**
     * 删除角色。
     *
     * @param id 角色 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    /**
     * 获取角色的菜单权限列表。
     * <p>
     * 返回该角色被授权的所有菜单。
     *
     * @param id 角色 ID
     * @return 角色的菜单列表
     */
    @GetMapping("/{id}/menus")
    public ResponseEntity<ApiResponse<List<Menu>>> getRoleMenus(@PathVariable Long id) {
        List<Menu> menus = roleService.getMenusByRoleId(id);
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 分配菜单权限。
     * <p>
     * 全量替换：该角色的所有菜单权限将被新列表覆盖。
     *
     * @param id      角色 ID
     * @param request 包含菜单 ID 列表的请求
     * @return 分配结果
     */
    @PutMapping("/{id}/menus")
    public ResponseEntity<ApiResponse<Void>> assignMenus(
        @PathVariable Long id,
        @RequestBody Map<String, List<Long>> request) {

        List<Long> menuIds = request.get("menuIds");
        boolean success = roleService.assignMenus(id, menuIds);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("分配成功", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("分配失败"));
        }
    }
}
