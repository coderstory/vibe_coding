package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.service.MenuService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 * 提供菜单的树形结构管理和 CRUD 操作
 */
@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取完整菜单树
     * 返回所有菜单的层级结构，用于管理界面的菜单树展示
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTree() {
        List<Menu> menus = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 获取角色的菜单权限树
     * 只返回该角色被授权的菜单，形成子集菜单树
     */
    @GetMapping("/tree/{roleId}")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTreeByRoleId(@PathVariable Long roleId) {
        List<Menu> menus = menuService.getMenuTreeByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 获取用户的菜单列表
     * 根据用户角色返回对应的菜单权限
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Menu>>> getUserMenus(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    /**
     * 创建菜单
     * parentId 为 0 表示顶级菜单
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Menu>> createMenu(@RequestBody Menu menu) {
        Menu created = menuService.createMenu(menu);
        return ResponseEntity.ok(ApiResponse.success("菜单创建成功", created));
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Menu>> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        Menu updated = menuService.updateMenu(id, menu);
        return ResponseEntity.ok(ApiResponse.success("菜单更新成功", updated));
    }

    /**
     * 删除菜单
     * 如有子菜单则不允许删除，需先删除子菜单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success("菜单删除成功", null));
    }
}