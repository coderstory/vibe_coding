package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.entity.Role;
import cn.coderstory.springboot.service.RoleService;
import cn.coderstory.springboot.vo.ApiResponse;
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
 * 角色管理控制器
 * 提供角色 CRUD 及权限分配操作
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 分页查询角色列表
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
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    /**
     * 创建角色
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
     * 更新角色
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
     * 删除角色
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
     * 获取角色的菜单权限
     * 返回该角色被授权的所有菜单 ID 列表
     */
    @GetMapping("/{id}/menus")
    public ResponseEntity<ApiResponse<List<Menu>>> getRoleMenus(@PathVariable Long id) {
        List<Menu> menus = roleService.getMenusByRoleId(id);
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 分配菜单权限
     * 全量替换：该角色的所有菜单权限将被新列表覆盖
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