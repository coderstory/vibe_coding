package cn.coderstory.springboot.mapper;

import cn.coderstory.springboot.entity.AuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
    
    @Select("SELECT * FROM sys_audit_log " +
            "WHERE (#{operator} IS NULL OR username LIKE CONCAT('%', #{operator}, '%')) " +
            "AND (#{operationType} IS NULL OR operation = #{operationType}) " +
            "AND (#{startTime} IS NULL OR create_time >= #{startTime}) " +
            "AND (#{endTime} IS NULL OR create_time <= #{endTime}) " +
            "ORDER BY create_time DESC")
    IPage<AuditLog> selectPage(Page<AuditLog> page,
            @Param("operator") String operator,
            @Param("operationType") String operationType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
