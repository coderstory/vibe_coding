package cn.coderstory.springboot.service;

import cn.coderstory.springboot.entity.AuditLog;
import cn.coderstory.springboot.mapper.AuditLogMapper;
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
public class AuditService {
    
    private final AuditLogMapper auditLogMapper;
    
    @Async
    public void log(Long userId, String username, String operation, 
            String targetType, String targetId, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setOperation(operation);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setIpAddress(ipAddress);
            
            auditLogMapper.insert(auditLog);
            log.info("审计日志记录: user={}, operation={}, target={}", 
                    username, operation, targetId);
        } catch (Exception e) {
            log.error("审计日志记录失败: {}", e.getMessage());
        }
    }
    
    public IPage<AuditLog> getAuditLogPage(Page<AuditLog> page, String operator, 
            String operationType, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.selectPage(page, operator, operationType, startTime, endTime);
    }
}
