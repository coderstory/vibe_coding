package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.service.MenuService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTree() {
        List<Menu> menus = menuService.getMenuTree();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/tree/{roleId}")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenuTreeByRoleId(@PathVariable Long roleId) {
        List<Menu> menus = menuService.getMenuTreeByRoleId(roleId);
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Menu>>> getUserMenus(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Menu>> createMenu(@RequestBody Menu menu) {
        Menu created = menuService.createMenu(menu);
        return ResponseEntity.ok(ApiResponse.success("菜单创建成功", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Menu>> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        Menu updated = menuService.updateMenu(id, menu);
        return ResponseEntity.ok(ApiResponse.success("菜单更新成功", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success("菜单删除成功", null));
    }
}
