package cn.coderstory.springboot.vo;

import org.springframework.http.ResponseEntity;

/**
 * Controller 返回结果工具类
 * 提供便捷方法构建标准响应
 */
public class ResultResponse {

    /**
     * 返回成功响应（无数据）
     */
    public static ResponseEntity<ApiResponse<Void>> ok() {
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 返回成功响应（带数据）
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 返回成功响应（带消息和数据）
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    /**
     * 返回成功响应（仅消息）
     */
    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    /**
     * 返回 400 错误
     */
    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(message));
    }

    /**
     * 返回 401 未授权
     */
    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return ResponseEntity.status(401).body(ApiResponse.unauthorized(message));
    }

    /**
     * 返回 403 禁止访问
     */
    public static ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        return ResponseEntity.status(403).body(ApiResponse.forbidden(message));
    }

    /**
     * 返回 404 未找到
     */
    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(404).body(ApiResponse.notFound(message));
    }

    /**
     * 返回 409 冲突
     */
    public static ResponseEntity<ApiResponse<Void>> conflict(String message) {
        return ResponseEntity.status(409).body(ApiResponse.conflict(message));
    }

    /**
     * 返回 500 内部错误
     */
    public static ResponseEntity<ApiResponse<Void>> error(String message) {
        return ResponseEntity.status(500).body(ApiResponse.error(message));
    }
}
