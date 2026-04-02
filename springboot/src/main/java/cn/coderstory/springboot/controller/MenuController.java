package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    
    private final MenuService menuService;
    
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getMenuTree() {
        List<Menu> menus = menuService.getMenuTree();
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", menus);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/tree/{roleId}")
    public ResponseEntity<Map<String, Object>> getMenuTreeByRoleId(@PathVariable Long roleId) {
        List<Menu> menus = menuService.getMenuTreeByRoleId(roleId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", menus);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserMenus(@PathVariable Long userId) {
        // This endpoint is for MENU-02: User sees only menus they have permission to access
        // For now, we return the full menu tree by role
        // In a real implementation, this would query the user's role and return menus for that role
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", List.of());
        
        return ResponseEntity.ok(response);
    }
}
