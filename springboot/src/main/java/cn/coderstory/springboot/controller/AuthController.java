package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.security.JwtTokenProvider;
import cn.coderstory.springboot.service.AuthService;
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
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String ipAddress = getClientIp(httpRequest);
            
            Map<String, Object> data = authService.login(username, password, ipAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 401);
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = new HashMap<>();
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String ipAddress = getClientIp(request);
            
            authService.logout(userId, username, ipAddress);
        }
        
        response.put("code", 200);
        response.put("message", "success");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        Map<String, Object> response = new HashMap<>();
        
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.put("code", 401);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = authService.getCurrentUser(userId);
        
        if (user == null) {
            response.put("code", 401);
            response.put("message", "用户不存在");
            return ResponseEntity.status(401).body(response);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("roleId", user.getRoleId());
        
        response.put("code", 200);
        response.put("data", data);
        return ResponseEntity.ok(response);
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
