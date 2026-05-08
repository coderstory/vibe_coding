package cn.coderstory.springboot.controller.auth;

import cn.coderstory.springboot.service.auth.AuthService;
import cn.coderstory.springboot.config.JwtTokenProvider;
import cn.coderstory.springboot.dto.ApiResponse;
import cn.coderstory.springboot.entity.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器。
 * <p>
 * 处理用户登录、登出、Token 刷新等认证相关操作，管理用户会话和 JWT 令牌生命周期。
 *
 * @since 1.7.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录。
     * <p>
     * 验证用户名和密码，登录成功后返回 JWT Token 和刷新令牌。
     *
     * @param request     登录请求，包含用户名和密码
     * @param httpRequest HTTP 请求，用于获取客户端 IP
     * @return 登录结果，包含 Token、刷新令牌和用户信息
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
     * 刷新 Token。
     * <p>
     * 使用刷新令牌获取新的访问令牌，延长用户会话有效期。
     *
     * @param request 包含刷新令牌的请求
     * @return 新的 Token 和刷新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, Object> data = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 用户登出。
     * <p>
     * 记录登出日志并清除服务端会话状态，客户端应同时清除本地存储的 Token。
     *
     * @param request HTTP 请求，用于提取 Token 和客户端 IP
     * @return 登出结果
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
     * 获取当前用户信息。
     * <p>
     * 从请求中解析 JWT Token，查询当前登录用户的详细信息。
     *
     * @param request HTTP 请求，用于提取 Token
     * @return 当前用户的基本信息
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

    // 从请求头提取 Bearer Token
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 获取客户端真实 IP，优先从 X-Forwarded-For 头获取，兼容代理和负载均衡场景
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
