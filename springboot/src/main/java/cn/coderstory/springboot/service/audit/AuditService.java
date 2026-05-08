package cn.coderstory.springboot.service.audit;

import cn.coderstory.springboot.entity.audit.AuditLog;
import cn.coderstory.springboot.mapper.audit.AuditLogMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * 审计日志服务。
 * <p>
 * 提供异步审计日志记录和日志分页查询功能。
 * 日志记录通过 @Async 异步执行，不影响主业务流程。
 *
 * @since 1.7.0
 */
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    @Async
    /**
     * 记录审计日志（无详细描述）。
     *
     * @param userId     操作用户 ID
     * @param username   操作用户名
     * @param operation  操作类型
     * @param targetType 操作目标类型
     * @param targetId   操作目标 ID
     * @param ipAddress  操作来源 IP 地址
     */
    public void log(Long userId, String username, String operation,
                    String targetType, String targetId, String ipAddress) {
        log(userId, username, operation, targetType, targetId, ipAddress, null);
    }

    @Async
    /**
     * 记录审计日志（含详细描述）。
     *
     * @param userId      操作用户 ID
     * @param username    操作用户名
     * @param operation   操作类型
     * @param targetType  操作目标类型
     * @param targetId    操作目标 ID
     * @param ipAddress   操作来源 IP 地址
     * @param description 操作详细描述
     */
    public void log(Long userId, String username, String operation,
                    String targetType, String targetId, String ipAddress, String description) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setOperation(operation);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setIpAddress(ipAddress);
            auditLog.setDescription(description);

            auditLogMapper.insert(auditLog);
            log.info("审计日志记录: user={}, operation={}, target={}, description={}",
                username, operation, targetId, description);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }

    /**
     * 分页查询审计日志。
     *
     * @param page          分页参数
     * @param operator      操作人用户名（可选，模糊匹配）
     * @param operationType 操作类型（可选）
     * @param startTime     开始时间（可选）
     * @param endTime       结束时间（可选）
     * @return 审计日志分页结果
     */
    public IPage<AuditLog> getAuditLogPage(Page<AuditLog> page, String operator,
                                           String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectPage(page, operator, operationType, startTime, endTime);
    }
}
