---
name: api-documentation-guide
description: >-
  API 文档编写规范。当编写 Swagger 文档、API 注释、接口描述时使用。
---

# API 文档编写规范

本技能定义 RobitCode 项目的 API 文档标准，基于 SpringDoc OpenAPI (Swagger)，确保 API 文档清晰、完整、易于理解。

---

## 一、基础配置

### 1.1 依赖配置

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.16</version>
</dependency>
```

### 1.2 全局配置

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("RobitCode API")
                .version("1.0.0")
                .description("RobitCode 企业级应用 API 文档")
                .contact(new Contact()
                    .name("开发团队")
                    .email("dev@robitcode.com")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

---

## 二、Controller 注解

### 2.1 类级注解

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户的增删改查接口")
public class UserController {
    // ...
}
```

### 2.2 方法级注解

```java
@GetMapping("/{id}")
@Operation(
    summary = "获取用户详情",
    description = "根据用户ID获取用户详细信息，包括部门和角色信息"
)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "成功获取用户信息"),
    @ApiResponse(responseCode = "404", description = "用户不存在"),
    @ApiResponse(responseCode = "401", description = "未授权访问")
})
public Result<UserVO> getUser(
    @Parameter(description = "用户ID", required = true, example = "1")
    @PathVariable Long id
) {
    // ...
}
```

### 2.3 完整示例

```java
@PostMapping
@Operation(
    summary = "创建用户",
    description = "创建新用户，用户名和邮箱必须唯一"
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "用户创建成功"),
    @ApiResponse(responseCode = "400", description = "参数校验失败"),
    @ApiResponse(responseCode = "409", description = "用户名或邮箱已存在")
})
public Result<UserVO> createUser(
    @Parameter(description = "用户创建请求", required = true)
    @Valid @RequestBody UserCreateDTO dto
) {
    return Result.success(userService.create(dto));
}
```

---

## 三、DTO 注解

### 3.1 字段描述

```java
@Data
@Schema(description = "用户创建请求")
public class UserCreateDTO {

    @Schema(
        description = "用户名",
        example = "zhangsan",
        minLength = 4,
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20之间")
    private String username;

    @Schema(
        description = "邮箱地址",
        example = "zhangsan@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(
        description = "部门ID",
        example = "1"
    )
    private Long departmentId;

    @Schema(
        description = "角色ID列表",
        example = "[1, 2, 3]"
    )
    private List<Long> roleIds;
}
```

### 3.2 嵌套对象

```java
@Data
@Schema(description = "分页查询响应")
public class PageVO<T> {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Integer current;

    @Schema(description = "每页大小", example = "10")
    private Integer size;
}
```

---

## 四、参数类型

### 4.1 路径参数

```java
@Parameter(
    name = "id",
    description = "用户ID",
    required = true,
    example = "1",
    in = ParameterIn.PATH
)
@PathVariable Long id
```

### 4.2 查询参数

```java
@Parameter(
    name = "keyword",
    description = "搜索关键词",
    example = "张"
)
@RequestParam(required = false) String keyword

@Parameter(
    name = "status",
    description = "用户状态",
    schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE", "DISABLED"})
)
@RequestParam(required = false) String status
```

### 4.3 请求体

```java
@Parameter(
    description = "用户更新请求",
    required = true
)
@Valid @RequestBody UserUpdateDTO dto
```

### 4.4 请求头

```java
@Parameter(
    name = "X-Tenant-Id",
    description = "租户ID",
    required = true,
    in = ParameterIn.HEADER
)
@RequestHeader("X-Tenant-Id") String tenantId
```

---

## 五、文档最佳实践

### 5.1 描述规范

| 类型 | 格式 | 示例 |
|------|------|------|
| summary | 简短动词短语 | "获取用户列表" |
| description | 完整说明 + 注意事项 | "获取所有用户列表，支持分页和筛选。注意：非管理员只能看到自己部门的用户" |
| example | 真实有效值 | "zhangsan@example.com" |

### 5.2 必须包含的内容

✅ **每个接口必须包含：**
- summary（接口用途）
- description（详细说明，可选）
- 参数说明（@Parameter 或 @Schema）
- 响应说明（@ApiResponse）

✅ **每个 DTO 必须包含：**
- 类级 @Schema description
- 字段级 @Schema description
- 字段级 example
- 必填字段标记

### 5.3 不要做的事情

❌ **模糊的描述**
```java
@Operation(summary = "查询") // 太模糊
@Schema(description = "名字") // 应该说明用途和格式
```

❌ **缺少示例**
```java
@Schema(description = "创建时间") // 没有 example
```

❌ **缺少必填标记**
```java
@Schema(description = "用户名") // 没有标记 required
```

---

## 六、常见场景

### 6.1 分页查询

```java
@GetMapping
@Operation(summary = "分页查询用户")
public Result<PageVO<UserVO>> listUsers(
    @Parameter(description = "页码，从1开始", example = "1")
    @RequestParam(defaultValue = "1") Integer current,

    @Parameter(description = "每页大小", example = "10")
    @RequestParam(defaultValue = "10") Integer size,

    @Parameter(description = "搜索关键词")
    @RequestParam(required = false) String keyword
) {
    // ...
}
```

### 6.2 文件上传

```java
@PostMapping("/import")
@Operation(summary = "导入用户数据")
public Result<Void> importUsers(
    @Parameter(
        description = "Excel文件",
        required = true,
        content = @Content(mediaType = "multipart/form-data")
    )
    @RequestParam("file") MultipartFile file
) {
    // ...
}
```

### 6.3 枚举参数

```java
@Schema(description = "用户状态")
public enum UserStatus {
    ACTIVE("正常"),
    INACTIVE("未激活"),
    DISABLED("已禁用");

    private final String description;
}

@Parameter(
    description = "用户状态",
    schema = @Schema(
        implementation = UserStatus.class,
        description = "ACTIVE=正常, INACTIVE=未激活, DISABLED=已禁用"
    )
)
```

---

## 七、文档生成和访问

### 7.1 访问地址

- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/api/v3/api-docs.yaml`

### 7.2 分组配置

```java
@Bean
public GroupedOpenApi userApi() {
    return GroupedOpenApi.builder()
        .group("用户管理")
        .pathsToMatch("/api/users/**")
        .build();
}

@Bean
public GroupedOpenApi workflowApi() {
    return GroupedOpenApi.builder()
        .group("工作流")
        .pathsToMatch("/api/workflow/**")
        .build();
}
```

---

## 八、检查清单

在提交代码前，检查 API 文档：

- [ ] 所有 Controller 类有 @Tag 注解
- [ ] 所有接口方法有 @Operation 注解
- [ ] 所有参数有 @Parameter 或 @Schema 注解
- [ ] 所有 DTO 类有 @Schema 注解
- [ ] 所有字段有 description 和 example
- [ ] 必填字段有 requiredMode 标记
- [ ] 响应有 @ApiResponse 说明
- [ ] 枚举值有 allowableValues 说明
- [ ] example 值真实有效
