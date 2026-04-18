package cn.coderstory.springboot.exception;

import cn.coderstory.springboot.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        String message = "数据已存在，请检查是否重复";
        if (e.getMessage() != null && e.getMessage().contains("username")) {
            message = "用户名已存在";
        } else if (e.getMessage() != null && e.getMessage().contains("phone")) {
            message = "手机号已存在";
        } else if (e.getMessage() != null && e.getMessage().contains("email")) {
            message = "邮箱已存在";
        }
        return ApiResponse.conflict(message);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ApiResponse<Void> handleBadSqlGrammarException(BadSqlGrammarException e) {
        log.error("SQL语法错误", e);
        return ApiResponse.error(500, "数据库查询错误，请联系管理员");
    }

    @ExceptionHandler(NullPointerException.class)
    public ApiResponse<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return ApiResponse.error(500, "系统错误，请稍后重试");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return ApiResponse.notFound("接口不存在");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return ApiResponse.badRequest("参数错误: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("未知异常", e);
        return ApiResponse.error(500, "系统错误，请稍后重试");
    }
}