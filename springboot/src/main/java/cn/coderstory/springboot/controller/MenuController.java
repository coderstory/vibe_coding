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
}
