package cn.coderstory.springboot.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token 提供者。
 * <p>
 * 提供 Token 生成、验证和解析功能。
 * 使用 HMAC-SHA 算法签名，支持 Access Token 和 Refresh Token 两种类型。
 *
 * @since 1.7.0
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:mySecretKeyForJwtTokenGeneration123456}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration; // 默认 24 小时

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration; // 默认 7 天

    // 根据密钥字符串生成 HMAC-SHA 签名密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Access Token。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Access Token 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 生成 Refresh Token。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("username", username)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 验证 Token 是否有效。
     *
     * @param token JWT Token 字符串
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否为 Refresh Token。
     *
     * @param token JWT Token 字符串
     * @return true 是 Refresh Token，false 否
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中解析用户 ID。
     *
     * @param token JWT Token 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中解析用户名。
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.get("username", String.class);
    }

    /**
     * 获取 Access Token 过期时间（毫秒）。
     *
     * @return 过期时间
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * 从 Token 中解析过期时间。
     *
     * @param token JWT Token 字符串
     * @return 过期日期
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getExpiration();
    }
}
