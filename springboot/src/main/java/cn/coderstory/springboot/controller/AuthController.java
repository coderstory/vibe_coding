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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

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

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

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
