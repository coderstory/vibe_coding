package cn.coderstory.springboot.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Map<String, Object> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return errorResponse(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Map<String, Object> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());
        String message = "数据已存在，请检查是否重复";
        if (e.getMessage() != null && e.getMessage().contains("username")) {
            message = "用户名已存在";
        } else if (e.getMessage() != null && e.getMessage().contains("phone")) {
            message = "手机号已存在";
        } else if (e.getMessage() != null && e.getMessage().contains("email")) {
            message = "邮箱已存在";
        }
        return errorResponse(409, message);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public Map<String, Object> handleBadSqlGrammarException(BadSqlGrammarException e) {
        log.error("SQL语法错误", e);
        return errorResponse(500, "数据库查询错误，请联系管理员");
    }

    @ExceptionHandler(NullPointerException.class)
    public Map<String, Object> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return errorResponse(500, "系统错误，请稍后重试");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Map<String, Object> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return errorResponse(404, "接口不存在");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return errorResponse(400, "参数错误: " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        log.error("未知异常", e);
        return errorResponse(500, "系统错误，请稍后重试");
    }

    private Map<String, Object> errorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}