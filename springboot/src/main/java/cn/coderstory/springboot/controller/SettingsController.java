package cn.coderstory.springboot.controller;

import cn.coderstory.springboot.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {
    
    private final UserSettingsService userSettingsService;
    
    @GetMapping("/theme")
    public ResponseEntity<Map<String, Object>> getTheme(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        String theme = userSettingsService.getTheme(userId);
        response.put("code", 200);
        response.put("data", Map.of("theme", theme));
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/theme")
    public ResponseEntity<Map<String, Object>> saveTheme(
            HttpServletRequest request,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();
        
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "未登录");
            return ResponseEntity.status(401).body(response);
        }
        
        String theme = body.getOrDefault("theme", "light");
        userSettingsService.saveTheme(userId, theme);
        
        response.put("code", 200);
        response.put("message", "success");
        return ResponseEntity.ok(response);
    }
}
