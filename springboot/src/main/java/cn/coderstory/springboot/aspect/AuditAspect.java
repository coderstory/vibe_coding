package cn.coderstory.springboot.aspect;

import cn.coderstory.springboot.security.JwtTokenProvider;
import cn.coderstory.springboot.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    
    private final AuditService auditService;
    private final JwtTokenProvider jwtTokenProvider;
    
    // 拦截service层所有public方法，但排除AuditService自身方法
    @Pointcut("execution(* cn.coderstory.springboot.service..*(..)) && !execution(* cn.coderstory.springboot.service.AuditService.*(..))")
    public void servicePointcut() {}
    
    @Around("servicePointcut()")
    public Object auditAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // 推断操作类型
        String operation = inferOperationType(methodName);
        
        // 如果不是数据变更操作，不记录
        if (operation == null) {
            return joinPoint.proceed();
        }
        
        // 获取请求和用户信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long userId = null;
        String username = null;
        String ipAddress = null;
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = getClientIp(request);
            
            String token = getTokenFromRequest(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                userId = jwtTokenProvider.getUserIdFromToken(token);
                username = jwtTokenProvider.getUsernameFromToken(token);
            }
        }
        
        // 执行原方法
        Object result = joinPoint.proceed();
        
        // 记录审计日志
        try {
            String targetType = className.replace("ServiceImpl", "").replace("Service", "");
            String targetId = extractTargetId(result);
            
            if (userId != null && username != null) {
                auditService.log(userId, username, operation, targetType, targetId, ipAddress);
            }
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
        
        return result;
    }
    
    private String inferOperationType(String methodName) {
        String lowerName = methodName.toLowerCase();
        if (lowerName.startsWith("save") || lowerName.startsWith("create") || lowerName.startsWith("add")) {
            return "新增";
        } else if (lowerName.startsWith("update") || lowerName.startsWith("edit")) {
            return "编辑";
        } else if (lowerName.startsWith("delete") || lowerName.startsWith("remove")) {
            return "删除";
        }
        return null; // 不记录的操作类型
    }
    
    private String extractTargetId(Object result) {
        if (result == null) {
            return null;
        }
        // 尝试从返回结果中提取ID
        try {
            if (result instanceof java.util.Map) {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) result;
                if (map.get("id") != null) {
                    return String.valueOf(map.get("id"));
                }
            }
            // 尝试调用getId方法
            var method = result.getClass().getMethod("getId");
            Object id = method.invoke(result);
            return id != null ? String.valueOf(id) : null;
        } catch (Exception e) {
            return null;
        }
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
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
