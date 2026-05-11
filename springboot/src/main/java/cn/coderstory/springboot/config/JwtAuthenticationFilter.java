package cn.coderstory.springboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器。
 * <p>
 * 从请求头中提取 Bearer Token 并解析用户身份，
 * 将认证信息设置到 Spring SecurityContext 中。
 * 登录和注册路径跳过过滤。
 *
 * @since 1.7.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 执行请求过滤。
     * <p>
     * 从请求中提取并验证 JWT Token，解析用户信息后设置到 SecurityContext。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);

            String role = jwtTokenProvider.getRoleFromToken(token);
            String authority = "ADMIN".equalsIgnoreCase(role) ? "ROLE_ADMIN" : "ROLE_USER";

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority(authority))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Authenticated user: {} (id: {}, role: {})", username, userId, role);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断当前请求是否跳过过滤。
     *
     * @param request HTTP 请求
     * @return true 跳过过滤（登录/注册路径），false 执行过滤
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register");
    }

    // 从请求头或 SSE query param 中提取 Bearer Token
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // EventSource 不支持自定义 header，token 通过 query 参数传递
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isEmpty()) {
            return queryToken;
        }
        return null;
    }
}
