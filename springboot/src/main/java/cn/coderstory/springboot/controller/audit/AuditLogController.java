package cn.coderstory.springboot.controller.audit;

import cn.coderstory.springboot.entity.audit.AuditLog;
import cn.coderstory.springboot.service.audit.AuditService;
import cn.coderstory.springboot.dto.ApiResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志查询控制器。
 * <p>
 * 提供审计日志的分页查询和条件过滤功能，支持按操作人、操作类型和时间范围进行筛选。
 *
 * @since 1.7.0
 */
@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    /**
     * 分页查询审计日志。
     * <p>
     * 支持按操作人、操作类型和操作时间范围进行筛选，返回分页结果。
     *
     * @param operator      操作人用户名
     * @param operationType 操作类型
     * @param startTime     操作开始时间
     * @param endTime       操作结束时间
     * @param page          页码
     * @param size          每页条数
     * @return 审计日志分页结果，包含记录列表和分页信息
     */
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLogs(
        @RequestParam(required = false) String operator,
        @RequestParam(required = false) String operationType,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "20") Integer size) {

        Page<AuditLog> pageParam = new Page<>(page, size);
        var result = auditService.getAuditLogPage(pageParam, operator, operationType, startTime, endTime);

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("size", result.getSize());
        data.put("current", result.getCurrent());
        data.put("pages", result.getPages());

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
