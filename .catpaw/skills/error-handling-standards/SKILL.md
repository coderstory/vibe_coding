---
name: error-handling-standards
description: >-
  统一错误处理规范。当处理前后端错误处理、异常捕获、错误响应格式、日志记录时使用。
---

# 错误处理规范

本技能定义 RobitCode 项目的统一错误处理标准，确保前后端错误处理一致、可追踪、用户友好。

---

## 一、后端错误处理（Java）

### 1.1 异常层次结构

```
RuntimeException
└── BusinessException          # 业务异常基类
    ├── ValidationException    # 参数校验异常
    ├── AuthException          # 认证授权异常
    ├── NotFoundException      # 资源不存在异常
    └── ConflictException      # 资源冲突异常
```

### 1.2 统一异常类

```java
@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final Object[] args;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }

    public BusinessException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }
}
```

### 1.3 全局异常处理器

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return Result.fail("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

### 1.4 错误码规范

| 前缀 | 类别 | 示例 |
|------|------|------|
| `AUTH_` | 认证授权 | `AUTH_TOKEN_EXPIRED`, `AUTH_PERMISSION_DENIED` |
| `USER_` | 用户相关 | `USER_NOT_FOUND`, `USER_DISABLED` |
| `VALIDATION_` | 参数校验 | `VALIDATION_ERROR`, `VALIDATION_REQUIRED` |
| `BUSINESS_` | 业务规则 | `BUSINESS_STATE_ERROR`, `BUSINESS_LIMIT_EXCEEDED` |
| `SYSTEM_` | 系统错误 | `SYSTEM_ERROR`, `SYSTEM_TIMEOUT` |

### 1.5 日志记录规范

```java
// 业务异常 - WARN 级别
log.warn("业务异常: userId={}, action={}, message={}", userId, action, e.getMessage());

// 系统异常 - ERROR 级别，包含堆栈
log.error("系统异常: userId={}, action={}", userId, action, e);

// 关键操作 - INFO 级别
log.info("操作成功: userId={}, action={}, result={}", userId, action, result);
```

---

## 二、前端错误处理（Vue）

### 2.1 API 错误拦截

```typescript
// api/request.ts
axiosInstance.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const { response, config } = error;

    if (response) {
      const { status, data } = response;

      switch (status) {
        case 401:
          // Token 过期，跳转登录
          router.push('/login');
          return Promise.reject(new AuthError('登录已过期，请重新登录'));

        case 403:
          ElMessage.error('没有操作权限');
          return Promise.reject(new PermissionError('权限不足'));

        case 404:
          ElMessage.error('请求的资源不存在');
          return Promise.reject(new NotFoundError('资源不存在'));

        case 422:
          // 参数校验错误，显示具体字段错误
          handleValidationErrors(data.errors);
          return Promise.reject(new ValidationError(data.message, data.errors));

        case 500:
          ElMessage.error('服务器错误，请稍后重试');
          return Promise.reject(new SystemError('服务器错误'));

        default:
          ElMessage.error(data?.message || '请求失败');
          return Promise.reject(error);
      }
    }

    // 网络错误
    ElMessage.error('网络连接失败，请检查网络');
    return Promise.reject(new NetworkError('网络错误'));
  }
);
```

### 2.2 错误类型定义

```typescript
// types/errors.ts
export class AppError extends Error {
  constructor(
    public code: string,
    message: string,
    public details?: Record<string, string[]>
  ) {
    super(message);
    this.name = 'AppError';
  }
}

export class ValidationError extends AppError {
  constructor(message: string, errors: Record<string, string[]>) {
    super('VALIDATION_ERROR', message, errors);
    this.name = 'ValidationError';
  }
}

export class AuthError extends AppError {
  constructor(message: string) {
    super('AUTH_ERROR', message);
    this.name = 'AuthError';
  }
}
```

### 2.3 组件错误处理

```vue
<script setup lang="ts">
import { useAsync } from '@/composables/useAsync';

const { data, error, loading, execute } = useAsync(async () => {
  return await api.getData();
});

// 统一错误处理
watch(error, (err) => {
  if (err instanceof ValidationError) {
    // 表单校验错误 - 在表单中显示
    formErrors.value = err.details;
  } else if (err instanceof AuthError) {
    // 认证错误 - 已在拦截器处理
  } else {
    // 其他错误 - 显示提示
    ElMessage.error(err.message);
  }
});
</script>
```

### 2.4 全局错误处理

```typescript
// main.ts
app.config.errorHandler = (err, vm, info) => {
  console.error('Vue Error:', err, info);
  // 发送错误到监控系统
  trackError(err as Error, { component: vm?.$options.name, info });
};

window.onerror = (message, source, lineno, colno, error) => {
  console.error('Global Error:', message, source, lineno, colno, error);
  trackError(error || new Error(String(message)), { source, lineno, colno });
  return false;
};
```

---

## 三、错误响应格式

### 3.1 标准响应结构

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

### 3.2 错误响应结构

```json
{
  "code": 40001,
  "message": "用户名或密码错误",
  "data": null,
  "traceId": "abc123def456"
}
```

### 3.3 参数校验错误响应

```json
{
  "code": 42200,
  "message": "参数校验失败",
  "data": null,
  "errors": {
    "username": ["用户名不能为空", "用户名长度必须在4-20之间"],
    "email": ["邮箱格式不正确"]
  }
}
```

---

## 四、最佳实践

### 4.1 不要做的事情

❌ **直接暴露异常堆栈给前端**
```java
return Result.fail(e.getMessage()); // 可能暴露敏感信息
```

❌ **忽略错误处理**
```typescript
try {
  await api.save(data);
} catch (e) {
  // 什么都不做
}
```

❌ **使用通用错误消息**
```java
return Result.fail("操作失败"); // 用户无法理解问题
```

### 4.2 应该做的事情

✅ **提供明确的错误信息和解决建议**
```java
return Result.fail("USER_PASSWORD_WRONG", "用户名或密码错误，请检查后重试");
```

✅ **记录足够的上下文信息**
```java
log.error("订单创建失败: userId={}, productId={}, error={}",
    userId, productId, e.getMessage(), e);
```

✅ **区分用户可见和不可见的错误**
```typescript
if (isUserVisibleError(error)) {
  ElMessage.error(error.message);
} else {
  ElMessage.error('操作失败，请稍后重试');
  trackError(error); // 记录到监控系统
}
```

---

## 五、错误监控

### 5.1 关键指标

| 指标 | 说明 | 阈值 |
|------|------|------|
| 错误率 | 错误请求数/总请求数 | < 0.1% |
| 5xx 错误率 | 服务器错误占比 | < 0.01% |
| 平均响应时间 | 包含错误响应 | < 500ms |
| 错误恢复时间 | 从发生到修复 | < 30min |

### 5.2 错误追踪

每个请求都应该有唯一的 `traceId`，方便追踪问题：

```java
// 后端
MDC.put("traceId", UUID.randomUUID().toString().replace("-", "").substring(0, 16));

// 前端
axiosInstance.interceptors.request.use((config) => {
  config.headers['X-Trace-Id'] = generateTraceId();
  return config;
});
```
