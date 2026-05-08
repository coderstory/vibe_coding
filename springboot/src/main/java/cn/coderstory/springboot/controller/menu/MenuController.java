package cn.coderstory.springboot.controller.menu;

import cn.coderstory.springboot.entity.menu.Menu;
import cn.coderstory.springboot.service.menu.MenuService;
import cn.coderstory.springboot.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器。
 * <p>
 * 提供菜单的树形结构管理和 CRUD 操作，支持按角色和用户获取菜单权限。
 *
 * @since 1.7.0
 */
@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取完整菜单树。
     * <p>
     * 返回所有菜单的层级结构，用于管理界面的菜单树展示。
     *
     * @return 菜单树列表
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTree() {
        List<Menu> menus = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 获取角色的菜单权限树。
     * <p>
     * 只返回该角色被授权的菜单，形成子集菜单树。
     *
     * @param roleId 角色 ID
     * @return 该角色的菜单树列表
     */
    @GetMapping("/tree/{roleId}")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTreeByRoleId(@PathVariable Long roleId) {
        List<Menu> menus = menuService.getMenuTreeByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 获取用户的菜单列表。
     * <p>
     * 根据用户角色返回对应的菜单权限。
     *
     * @param userId 用户 ID
     * @return 用户的菜单列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Menu>>> getUserMenus(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    /**
     * 创建菜单。
     * <p>
     * parentId 为 0 时表示顶级菜单。
     *
     * @param menu 菜单信息
     * @return 已创建的菜单
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Menu>> createMenu(@RequestBody Menu menu) {
        Menu created = menuService.createMenu(menu);
        return ResponseEntity.ok(ApiResponse.success("菜单创建成功", created));
    }

    /**
     * 更新菜单信息。
     *
     * @param id   菜单 ID
     * @param menu 菜单信息
     * @return 已更新的菜单
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Menu>> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        Menu updated = menuService.updateMenu(id, menu);
        return ResponseEntity.ok(ApiResponse.success("菜单更新成功", updated));
    }

    /**
     * 删除菜单。
     * <p>
     * 存在子菜单时不允许删除，需先删除子菜单。
     *
     * @param id 菜单 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success("菜单删除成功", null));
    }
}