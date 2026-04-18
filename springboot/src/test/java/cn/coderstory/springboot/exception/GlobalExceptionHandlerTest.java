package cn.coderstory.springboot.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("BusinessException")
    class BusinessExceptionTests {

        @Test
        @DisplayName("默认错误码为400")
        void defaultCodeIs400() {
            BusinessException ex = new BusinessException("测试错误");
            assertEquals(400, ex.getCode());
            assertEquals("测试错误", ex.getMessage());
        }

        @Test
        @DisplayName("badRequest 工厂方法")
        void badRequest() {
            BusinessException ex = BusinessException.badRequest("参数错误");
            assertEquals(400, ex.getCode());
            assertEquals("参数错误", ex.getMessage());
        }

        @Test
        @DisplayName("unauthorized 工厂方法")
        void unauthorized() {
            BusinessException ex = BusinessException.unauthorized("未授权");
            assertEquals(401, ex.getCode());
            assertEquals("未授权", ex.getMessage());
        }

        @Test
        @DisplayName("forbidden 工厂方法")
        void forbidden() {
            BusinessException ex = BusinessException.forbidden("禁止访问");
            assertEquals(403, ex.getCode());
            assertEquals("禁止访问", ex.getMessage());
        }

        @Test
        @DisplayName("notFound 工厂方法")
        void notFound() {
            BusinessException ex = BusinessException.notFound("资源不存在");
            assertEquals(404, ex.getCode());
            assertEquals("资源不存在", ex.getMessage());
        }

        @Test
        @DisplayName("conflict 工厂方法")
        void conflict() {
            BusinessException ex = BusinessException.conflict("数据冲突");
            assertEquals(409, ex.getCode());
            assertEquals("数据冲突", ex.getMessage());
        }

        @Test
        @DisplayName("serverError 工厂方法")
        void serverError() {
            BusinessException ex = BusinessException.serverError("服务器错误");
            assertEquals(500, ex.getCode());
            assertEquals("服务器错误", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("GlobalExceptionHandler")
    class HandlerTests {

        @Test
        @DisplayName("处理 BusinessException")
        void handleBusinessException() {
            BusinessException ex = BusinessException.conflict("用户名已存在");
            Map<String, Object> result = handler.handleBusinessException(ex);

            assertEquals(409, result.get("code"));
            assertEquals("用户名已存在", result.get("message"));
        }

        @Test
        @DisplayName("处理 DuplicateKeyException - username")
        void handleDuplicateKeyUsername() {
            DuplicateKeyException ex = new DuplicateKeyException("Duplicate entry 'admin' for key 'username'");
            Map<String, Object> result = handler.handleDuplicateKeyException(ex);

            assertEquals(409, result.get("code"));
            assertEquals("用户名已存在", result.get("message"));
        }

        @Test
        @DisplayName("处理 DuplicateKeyException - phone")
        void handleDuplicateKeyPhone() {
            DuplicateKeyException ex = new DuplicateKeyException("Duplicate entry '13800138000' for key 'phone'");
            Map<String, Object> result = handler.handleDuplicateKeyException(ex);

            assertEquals(409, result.get("code"));
            assertEquals("手机号已存在", result.get("message"));
        }

        @Test
        @DisplayName("处理 DuplicateKeyException - email")
        void handleDuplicateKeyEmail() {
            DuplicateKeyException ex = new DuplicateKeyException("Duplicate entry 'test@example.com' for key 'email'");
            Map<String, Object> result = handler.handleDuplicateKeyException(ex);

            assertEquals(409, result.get("code"));
            assertEquals("邮箱已存在", result.get("message"));
        }

        @Test
        @DisplayName("处理 DuplicateKeyException - 默认消息")
        void handleDuplicateKeyDefault() {
            DuplicateKeyException ex = new DuplicateKeyException("Duplicate entry 'something' for key 'idx_xxx'");
            Map<String, Object> result = handler.handleDuplicateKeyException(ex);

            assertEquals(409, result.get("code"));
            assertEquals("数据已存在，请检查是否重复", result.get("message"));
        }

        @Test
        @DisplayName("处理 NullPointerException")
        void handleNullPointerException() {
            NullPointerException ex = new NullPointerException("something is null");
            Map<String, Object> result = handler.handleNullPointerException(ex);

            assertEquals(500, result.get("code"));
            assertEquals("系统错误，请稍后重试", result.get("message"));
        }

        @Test
        @DisplayName("处理 IllegalArgumentException")
        void handleIllegalArgumentException() {
            IllegalArgumentException ex = new IllegalArgumentException("年龄不能为负数");
            Map<String, Object> result = handler.handleIllegalArgumentException(ex);

            assertEquals(400, result.get("code"));
            assertTrue(((String) result.get("message")).contains("年龄不能为负数"));
        }

        @Test
        @DisplayName("处理通用 Exception")
        void handleGenericException() {
            Exception ex = new RuntimeException("未知错误");
            Map<String, Object> result = handler.handleException(ex);

            assertEquals(500, result.get("code"));
            assertEquals("系统错误，请稍后重试", result.get("message"));
        }
    }
}