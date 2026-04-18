package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.User;
import cn.coderstory.springboot.exception.BusinessException;
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
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // 记录登录审计日志
        auditService.log(user.getId(), user.getUsername(), "LOGIN", "USER",
                String.valueOf(user.getId()), ipAddress);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("refreshToken", refreshToken);
        data.put("expiresIn", jwtTokenProvider.getExpirationTime());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("name", user.getName());
        userInfo.put("roleId", user.getRoleId());
        data.put("user", userInfo);

        return data;
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw BusinessException.badRequest("刷新令牌不能为空");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("刷新令牌无效或已过期");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw BusinessException.unauthorized("无效的刷新令牌类型");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw BusinessException.unauthorized("用户不存在或已禁用");
        }

        // 生成新的访问令牌和刷新令牌
        String newToken = jwtTokenProvider.generateToken(userId, username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        data.put("refreshToken", newRefreshToken);
        data.put("expiresIn", jwtTokenProvider.getExpirationTime());

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
