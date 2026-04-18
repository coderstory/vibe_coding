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
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final JwtTokenProvider jwtTokenProvider;

    // 使用 ThreadLocal 避免同一线程内重复记录
    private static final ThreadLocal<Boolean> AUDIT_FLAG = ThreadLocal.withInitial(() -> false);

    // 写操作方法前缀
    private static final Set<String> WRITE_METHOD_PREFIXES = Set.of(
            "save", "create", "add", "insert",
            "update", "edit", "modify", "change",
            "delete", "remove", "drop", "truncate",
            "assign", "grant", "revoke",
            "reset", "enable", "disable",
            "upload", "download", "import", "export"
    );

    // 不记录的方法（系统内部操作）
    private static final Set<String> EXCLUDED_METHODS = Set.of(
            "getAuditLogPage", "getMenuTree", "getMenuTreeByRoleId",
            "getUserPage", "getUserById", "getCurrentUser",
            "getRolePage", "getRoleById", "getMenusByRoleId",
            "findByUsername", "selectPage", "selectById", "selectList",
            "getCategoryTree", "getArticlePage", "getArticleById",
            "getAllTags", "getTagsByArticleId", "getFilesByArticleId",
            "getFileMetadata", "downloadFile", "searchArticles"
    );

    @Pointcut("execution(* cn.coderstory.springboot.service..*(..)) && !execution(* cn.coderstory.springboot.service.AuditService.*(..))")
    public void servicePointcut() {}

    @Around("servicePointcut()")
    public Object auditAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 如果已经在审计中（避免重复），直接执行
        if (AUDIT_FLAG.get()) {
            return joinPoint.proceed();
        }

        // 推断操作类型
        OperationType operationType = inferOperationType(methodName);

        // 如果不是数据变更操作，不记录
        if (operationType == null) {
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

        // 设置审计标记
        AUDIT_FLAG.set(true);

        // 执行原方法
        Object result = joinPoint.proceed();

        // 记录审计日志
        try {
            String targetType = className.replace("ServiceImpl", "").replace("Service", "");
            String targetId = extractTargetId(result, joinPoint);
            String description = buildOperationDescription(className, methodName, joinPoint.getArgs());

            if (userId != null && username != null) {
                auditService.log(userId, username, operationType.action, targetType, targetId, ipAddress, description);
            }
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        } finally {
            AUDIT_FLAG.remove();
        }

        return result;
    }

    private OperationType inferOperationType(String methodName) {
        String lowerName = methodName.toLowerCase();

        // 检查是否在排除列表中
        if (EXCLUDED_METHODS.contains(methodName)) {
            return null;
        }

        for (String prefix : WRITE_METHOD_PREFIXES) {
            if (lowerName.startsWith(prefix)) {
                if (prefix.equals("save") || prefix.equals("create") || prefix.equals("add") ||
                        prefix.equals("insert")) {
                    return OperationType.CREATE;
                } else if (prefix.equals("update") || prefix.equals("edit") ||
                        prefix.equals("modify") || prefix.equals("change")) {
                    return OperationType.UPDATE;
                } else if (prefix.equals("delete") || prefix.equals("remove")) {
                    return OperationType.DELETE;
                } else if (prefix.equals("assign") || prefix.equals("grant")) {
                    return OperationType.ASSIGN;
                } else if (prefix.equals("reset") || prefix.equals("enable") || prefix.equals("disable")) {
                    return OperationType.STATUS_CHANGE;
                } else if (prefix.equals("upload") || prefix.equals("import")) {
                    return OperationType.UPLOAD;
                } else if (prefix.equals("download") || prefix.equals("export")) {
                    return OperationType.DOWNLOAD;
                }
            }
        }
        return null;
    }

    private String extractTargetId(Object result, ProceedingJoinPoint joinPoint) {
        if (result == null) {
            // 尝试从方法参数中提取 ID
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Long) {
                    return String.valueOf(arg);
                }
            }
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
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            return id != null ? String.valueOf(id) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildOperationDescription(String className, String methodName, Object[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(className.replace("ServiceImpl", "").replace("Service", ""))
                .append(".")
                .append(methodName);

        // 添加关键参数信息
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null && !isSimpleType(arg.getClass())) {
                    sb.append(" [param").append(i + 1).append(": ").append(arg.getClass().getSimpleName()).append("]");
                }
            }
        }

        return sb.toString();
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(String.class) ||
                type.equals(Integer.class) ||
                type.equals(Long.class) ||
                type.equals(Boolean.class) ||
                type.equals(Double.class) ||
                type.equals(Float.class);
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
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private enum OperationType {
        CREATE("新增"),
        UPDATE("编辑"),
        DELETE("删除"),
        ASSIGN("分配"),
        STATUS_CHANGE("状态变更"),
        UPLOAD("上传"),
        DOWNLOAD("下载");

        private final String action;

        OperationType(String action) {
            this.action = action;
        }
    }
}
