package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Menu;
import cn.coderstory.springboot.entity.Role;
import cn.coderstory.springboot.service.RoleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRolePage(
            @RequestParam(required = false) String roleName,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Page<Role> pageParam = new Page<>(page, size);
        IPage<Role> result = roleService.getRolePage(pageParam, roleName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("size", result.getSize());
        data.put("current", result.getCurrent());
        data.put("pages", result.getPages());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", role);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        boolean success = roleService.saveRole(role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", success ? 200 : 500);
        response.put("message", success ? "success" : "创建失败");
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        boolean success = roleService.updateRole(role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", success ? 200 : 500);
        response.put("message", success ? "success" : "更新失败");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", success ? 200 : 500);
        response.put("message", success ? "success" : "删除失败");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/menus")
    public ResponseEntity<Map<String, Object>> getRoleMenus(@PathVariable Long id) {
        List<Menu> menus = roleService.getMenusByRoleId(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", menus);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/menus")
    public ResponseEntity<Map<String, Object>> assignMenus(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> request) {
        
        List<Long> menuIds = request.get("menuIds");
        boolean success = roleService.assignMenus(id, menuIds);
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", success ? 200 : 500);
        response.put("message", success ? "success" : "分配失败");
        
        return ResponseEntity.ok(response);
    }
}
