package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.mapper.UserMapper;
import cn.coderstory.springboot.security.JwtTokenProvider;
import cn.coderstory.springboot.security.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    
    public Map<String, Object> login(String username, String password, String ipAddress) {
        User user = userMapper.findByUsername(username);
        
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (user.getDeleted() == 1) {
            throw new RuntimeException("用户已被禁用");
        }
        
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        // 记录登录审计日志
        auditService.log(user.getId(), user.getUsername(), "LOGIN", "USER", 
                String.valueOf(user.getId()), ipAddress);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("roleId", user.getRoleId());
        data.put("user", userInfo);
        
        return data;
    }
    
    public void logout(Long userId, String username, String ipAddress) {
        if (userId != null) {
            auditService.log(userId, username, "LOGOUT", "USER", 
                    String.valueOf(userId), ipAddress);
        }
    }
    
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }
}
