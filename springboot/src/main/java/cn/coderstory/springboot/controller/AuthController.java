package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.security.JwtTokenProvider;
import cn.coderstory.springboot.service.AuthService;
import cn.coderstory.springboot.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出、Token刷新等认证相关操作
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     * 验证用户名密码后返回 JWT Token，支持 Token 自动刷新机制
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        String username = request.get("username");
        String password = request.get("password");
        String ipAddress = getClientIp(httpRequest);

        Map<String, Object> data = authService.login(username, password, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 刷新 Token
     * 使用 RefreshToken 获取新的 AccessToken，延长会话有效期
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, Object> data = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 用户登出
     * 记录登出日志，客户端应清除本地存储的 Token
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String ipAddress = getClientIp(request);

            authService.logout(userId, username, ipAddress);
        }

        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取当前用户信息
     * 从 Token 中解析用户 ID，查询完整用户信息
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).body(ApiResponse.unauthorized("未登录"));
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = authService.getCurrentUser(userId);

        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.unauthorized("用户不存在"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("roleId", user.getRoleId());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 从请求头提取 Bearer Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端真实 IP
     * 优先从 X-Forwarded-For 头获取，兼容代理和负载均衡场景
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}