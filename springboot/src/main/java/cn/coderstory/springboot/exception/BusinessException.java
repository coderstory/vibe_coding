package cn.coderstory.springboot.exception;

import lombok.Getter;

/**
 * 业务异常类。
 * <p>
 * 提供常见 HTTP 状态码的静态工厂方法
 * （badRequest/unauthorized/forbidden/notFound/conflict/serverError）。
 * 用于 Controller 层向调用方返回标准化的错误响应。
 *
 * @since 1.7.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    /**
     * 创建 400 错误请求异常。
     *
     * @param message 错误描述
     * @return BusinessException 实例
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 创建 401 未授权异常。
     *
     * @param message 错误描述
     * @return BusinessException 实例
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /**
     * 创建 403 禁止访问异常。
     *
     * @param message 错误描述
     * @return BusinessException 实例
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 创建 404 资源未找到异常。
     *
     * @param message 错误描述
     * @return BusinessException 实例
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 创建 409 资源冲突异常。
     *
     * @param message 错误描述
     * @return BusinessException 实例
     */
    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }

    /**
     * 创建 500 服务器内部错误异常。
     *
     * @param message 错误描述
     * @return BusinessException 实例
     */
    public static BusinessException serverError(String message) {
        return new BusinessException(500, message);
    }
}