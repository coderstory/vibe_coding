package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Role;
import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.mapper.RoleMapper;
import cn.coderstory.springboot.service.UserService;
import cn.coderstory.springboot.vo.UserVO;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleMapper roleMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<User> pageParam = new Page<>(page, size);
        IPage<User> result = userService.getUserPage(pageParam, username, name, department, enabled, phone);

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
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", user);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> request) {
        User user = new User();
        user.setUsername((String) request.get("username"));
        user.setName((String) request.get("name"));
        user.setGender((Integer) request.get("gender"));
        user.setEmail((String) request.get("email"));
        user.setDepartment((String) request.get("department"));
        user.setPosition((String) request.get("position"));
        Object roleIdObj = request.get("roleId");
        if (roleIdObj != null) {
            user.setRoleId(((Number) roleIdObj).longValue());
        }
        user.setEnabled((Integer) request.get("enabled"));
        user.setAvatar((String) request.get("avatar"));
        user.setPhone((String) request.get("phone"));

        String password = (String) request.get("password");
        userService.saveUser(user, password);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "用户创建成功");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "用户更新成功");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "用户删除成功");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        userService.resetPassword(id, password);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "密码重置成功");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles/all")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        List<Role> roles = roleMapper.selectList(null);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "success");
        response.put("data", roles);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer enabled = request.get("enabled");
        userService.updateUserStatus(id, enabled);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "状态更新成功");

        return ResponseEntity.ok(response);
    }
}
