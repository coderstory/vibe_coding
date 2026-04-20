package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.Role;
import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.mapper.RoleMapper;
import cn.coderstory.springboot.service.UserService;
import cn.coderstory.springboot.vo.ApiResponse;
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

/**
 * 用户管理控制器
 * 提供用户 CRUD 及相关操作的 RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleMapper roleMapper;

    /**
     * 分页查询用户列表
     * 支持多条件组合筛选，返回 MyBatis Plus 分页结果
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<User> pageParam = new Page<>(page, size);
        IPage<User> result = userService.getUserPage(pageParam, username, name, department, enabled, phone);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("size", result.getSize());
        data.put("current", result.getCurrent());
        data.put("pages", result.getPages());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取用户详情
     * 返回包含角色名称的用户视图对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserVO>> getUserById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 创建用户
     * 注意：密码需要单独传入，由 Service 层进行加密处理
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUser(@RequestBody Map<String, Object> request) {
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

        return ResponseEntity.ok(ApiResponse.success("用户创建成功", null));
    }

    /**
     * 更新用户信息
     * 部分字段更新，ID 从路径参数获取
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
        return ResponseEntity.ok(ApiResponse.success("用户更新成功", null));
    }

    /**
     * 删除用户
     * 使用逻辑删除，由 MyBatis Plus 自动处理
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
    }

    /**
     * 重置用户密码
     * 新密码由调用方生成，服务端仅负责加密存储
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String password = request.get("password");
        userService.resetPassword(id, password);
        return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
    }

    /**
     * 获取所有角色列表
     * 用于用户编辑时的角色选择下拉框
     */
    @GetMapping("/roles/all")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleMapper.selectList(null);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    /**
     * 更新用户状态
     * enabled: 1=启用, 0=禁用
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer enabled = request.get("enabled");
        userService.updateUserStatus(id, enabled);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", null));
    }
}