package cn.coderstory.springboot.security;

import cn.coderstory.springboot.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider 单元测试")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", "testSecretKeyForJwtTokenGeneration1234567890");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 86400000L);
        ReflectionTestUtils.setField(tokenProvider, "refreshExpiration", 604800000L);
    }

    @Test
    @DisplayName("generateToken 包含 role 声明")
    void generateTokenShouldIncludeRoleClaim() {
        String token = tokenProvider.generateToken(1L, "testuser", "admin");
        assertNotNull(token);

        String role = tokenProvider.getRoleFromToken(token);
        assertEquals("admin", role);
    }

    @Test
    @DisplayName("getRoleFromToken 返回正确角色")
    void getRoleFromTokenShouldReturnCorrectRole() {
        String adminToken = tokenProvider.generateToken(1L, "admin", "admin");
        String userToken = tokenProvider.generateToken(2L, "user", "user");

        assertEquals("admin", tokenProvider.getRoleFromToken(adminToken));
        assertEquals("user", tokenProvider.getRoleFromToken(userToken));
    }

    @Test
    @DisplayName("generateToken 无 role 参数时默认角色为 user")
    void generateTokenShouldDefaultToUserRole() {
        String token = tokenProvider.generateToken(3L, "defaultuser");
        assertNotNull(token);

        String role = tokenProvider.getRoleFromToken(token);
        assertEquals("user", role);
    }

    @Test
    @DisplayName("validateToken 对有效 token 返回 true")
    void validateTokenShouldReturnTrueForValidToken() {
        String token = tokenProvider.generateToken(1L, "testuser", "user");
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("validateToken 对无效 token 返回 false")
    void validateTokenShouldReturnFalseForInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid.token.here"));
    }
}
