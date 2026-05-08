package cn.coderstory.springboot.service.auth;

import cn.coderstory.springboot.service.audit.AuditService;
import cn.coderstory.springboot.exception.BusinessException;
import cn.coderstory.springboot.config.JwtTokenProvider;
import cn.coderstory.springboot.config.PasswordEncoder;
import cn.coderstory.springboot.entity.user.User;
import cn.coderstory.springboot.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 认证服务。
 * <p>
 * 提供用户登录、Token 刷新、登出和当前用户信息查询功能。
 * 登录成功后生成 JWT 访问令牌和刷新令牌，并记录审计日志。
 *
 * @since 1.7.0
 */
public class AuthService {

    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * 用户登录。
     * <p>
     * 根据用户名和密码验证用户身份，验证通过后生成 JWT 令牌。
     *
     * @param username  用户名
     * @param password  密码（明文）
     * @param ipAddress 登录来源 IP 地址
     * @return 登录结果，包含 token、refreshToken、expiresIn 和 user 信息
     * @throws RuntimeException 用户名或密码错误，或用户已被禁用时抛出
     */
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

    /**
     * 刷新访问令牌。
     * <p>
     * 验证刷新令牌有效性后生成新的访问令牌和刷新令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 新的 token、refreshToken 和 expiresIn 信息
     * @throws BusinessException 刷新令牌无效或已过期时抛出
     */
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

    /**
     * 用户登出。
     * <p>
     * 记录登出审计日志。
     *
     * @param userId    用户 ID
     * @param username  用户名
     * @param ipAddress 登出来源 IP 地址
     */
    public void logout(Long userId, String username, String ipAddress) {
        if (userId != null) {
            auditService.log(userId, username, "LOGOUT", "USER",
                String.valueOf(userId), ipAddress);
        }
    }

    /**
     * 获取当前登录用户信息。
     *
     * @param userId 用户 ID
     * @return 用户实体，包含用户基本信息
     */
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }
}
