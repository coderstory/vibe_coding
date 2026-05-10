# Coding Conventions

**Analysis Date:** 2026-05-10

## Naming Patterns

**Files (Backend):**
- PascalCase per class/file: `UserController.java`, `UserServiceImpl.java`, `UserMapper.java`, `User.java`, `ApiResponse.java`, `BusinessException.java`
- Interface files match class names: `UserService.java` (interface), `UserServiceImpl.java` (implementation)
- Mapper XML files: `UserMapper.xml` (co-located with entity in `mapper/` directory)

**Files (Frontend):**
- PascalCase for Vue components: `UserManagement.vue`, `MonitorDashboard.vue`, `ConsumerGroupList.vue`
- camelCase for `.ts` utility/API files: `request.ts`, `types.ts`, `user.ts`
- Test files: `{ModuleName}.test.ts` co-located in `__tests__/` dir

**Classes (Backend):**
- Controllers: `*Controller` pattern — `UserController`, `AuthController`, `MonitorController`, `HardwareMonitorController`, `RocketMQController`
- Services: `*Service` interface + `*ServiceImpl` implementation — `UserService` / `UserServiceImpl`, `HardwareMetricsService` / `HardwareMetricsServiceImpl`
- Mappers: `*Mapper` — `UserMapper`, `RoleMapper`
- Entities: Domain-named — `User`, `Menu`, `Role`, `SeckillActivity`, `SeckillGoods`
- DTOs: `*DTO`, `*VO`, `*Params` — `HardwareMetricsDTO`, `UserVO`, `DiskMetricsDTO`
- Exception: `BusinessException` (single class with static factories)
- Response: `ApiResponse<T>`

**Functions/Methods (Backend):**
- camelCase: `getUserPage()`, `saveUser()`, `resetPassword()`, `getCurrentMetrics()`
- Controller methods named after HTTP semantics: `getUserPage`, `createUser`, `updateUser`, `deleteUser`
- Private helpers named descriptively: `extractTrend()`, `sumDiskReadBytes()`, `getClientIp()`

**Functions/Variables (Frontend):**
- camelCase for all JS/TS: `searchForm`, `userList`, `loading`, `pagination`, `loadMetrics()`
- Composables prefixed with `use`: `useRoute()`, `useRouter()` (Vue built-ins; project custom composables also follow `use` prefix per CLAUDE.md)
- Event handlers: `handleSearch`, `handleReset` (observed in `UserManagement.vue`)
- API functions: camelCase, descriptive — `getUserList()`, `createUser()`, `deleteUser()`

**Types (Frontend):**
- PascalCase interfaces: `User`, `UserVO`, `ApiResponse<T>`, `PageResult<T>`, `LoginParams`, `LoginResult`
- All types centralized in `app-vue/src/api/types.ts`

**CSS Classes (Frontend):**
- kebab-case: `monitor-dashboard`, `group-name-link`, `el-*` (Element Plus built-in)

**Packages (Backend):**
- Lowercase, structured by domain: `controller.user`, `service.user.impl`, `entity.user`, `mapper.user`, `dto.user`
- Cross-cutting: `shared.config`, `shared.exception`, `shared.security`, `shared.aspect`, `shared.util`, `shared.limiter`

## Code Style

**Formatting:**
- Java: Checkstyle (`config/checkstyle/checkstyle.xml` v10.21.4) — errors allowed (`isIgnoreFailures = true`)
- TypeScript/Vue: ESLint 10.x flat config (`app-vue/eslint.config.js`) + Prettier (`app-vue/.prettierrc`)
- CSS: Stylelint 17.x (`npm run lint` runs both ESLint and Stylelint)

**ESLint Rules (Frontend):**
- Indent: 2 spaces
- Quotes: single
- Semicolons: none (`never`)
- `no-unused-vars`: delegated to `@typescript-eslint/no-unused-vars` (warn)
- `no-console`: off (allowed)
- Comma-dangle: warn
- Member delimiter style: warn
- File-ending newline: warn

**Checkstyle Rules (Backend):**
- `UnusedImports`, `RedundantImport`
- `ConstantName`, `LocalFinalVariableName`, `LocalVariableName`, `MemberName`, `PackageName`, `ParameterName`, `StaticVariableName`, `TypeName`
- `AvoidStarImport` with explicit exceptions: `com.baomidou.mybatisplus.annotation`, `org.springframework.web.bind.annotation`, `io.jsonwebtoken`, `java.util`
- `IllegalImport` prevention
- `EmptyBlock`, `NeedBraces` (allows single-line statements for guard clauses)

**PMD Rules (Backend):**
- Enables: `bestpractices.xml`, `codestyle.xml` (with excludes), `design.xml` (with excludes), `errorprone.xml`
- Explicitly excluded: `AtLeastOneConstructor`, `MethodArgumentCouldBeFinal`, `LocalVariableCouldBeFinal`, `LongVariable`, `ShortVariable`, `OnlyOneReturn`, `LawOfDemeter`, `DataClass`

Java code uses `--enable-preview` compiler flag for Java 26 preview features.

## Import Organization

**Backend (observed in `UserServiceImpl.java`, `HardwareMonitorController.java`, etc.):**
1. Project classes (`cn.coderstory.springboot.*`)
2. Third-party libraries (`com.baomidou.mybatisplus.*`, `org.springframework.*`, `lombok.*`)
3. Standard library (`java.util.*`, `java.time.*`)
4. Static imports last (`import static org.mockito.Mockito.*`)

**Frontend (observed in `UserManagement.vue`, `request.ts`):**
1. Vue core (`from 'vue'`)
2. Vue Router
3. Element Plus components
4. Project modules (prefixed with `@/`): `@/api/modules/user`, `@/api/types`
5. Third-party libraries (`axios`, `vitest`)
6. Type imports using `import type`

## Path Aliases

**Frontend:**
- `@` maps to `src/`: `import UserList from '@/views/system/UserManagement.vue'`
- Configured in `vite.config.js` and `vitest.config.js` via `resolve.alias`

## Dependency Injection (Backend)

**Mandatory pattern:** `@RequiredArgsConstructor` + `private final` fields:
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleMapper roleMapper;
}
```

`@Autowired` field injection is forbidden per CLAUDE.md rule. This is strictly followed across all observed controllers and services:
- `UserController` — `private final UserService userService`
- `AuthController` — `private final AuthService authService; private final JwtTokenProvider jwtTokenProvider`
- `HardwareMetricsServiceImpl` — `private final SystemInfo systemInfo; private final ...`
- `MonitorController` — `private final MonitorService monitorService`

Exception: integration tests use `@Autowired` on test fields (e.g., `DistributedLockServiceTest`, `SignServiceTest`), which is standard Spring test practice.

## Error Handling

**Pattern:** `BusinessException` static factories + `GlobalExceptionHandler` `@RestControllerAdvice`:

```java
// Service layer throws exceptions
throw BusinessException.notFound("用户不存在");
throw BusinessException.badRequest("密码不能为空");
throw BusinessException.conflict("用户名已存在");

// GlobalExceptionHandler catches and returns ApiResponse
@ExceptionHandler(BusinessException.class)
public ApiResponse<Void> handleBusinessException(BusinessException e) {
    log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
    return ApiResponse.error(e.getCode(), e.getMessage());
}
```

`GlobalExceptionHandler` (`springboot/src/main/java/cn/coderstory/springboot/exception/GlobalExceptionHandler.java`) handles:
- `BusinessException` — returns `ApiResponse` with custom code and message
- `DuplicateKeyException` — returns 409 with context-aware message (username/phone/email-specific)
- `BadSqlGrammarException` — returns 500
- `NullPointerException` — returns 500
- `NoHandlerFoundException` — returns 404
- `IllegalArgumentException` — returns 400
- `Exception` (fallback) — returns 500

## API Response

**Pattern:** `ResponseEntity<ApiResponse<T>>` or direct `ApiResponse<T>`:

```java
// Controller using ResponseEntity wrapper
return ResponseEntity.ok(ApiResponse.success(data));

// Alternative direct return (no ResponseEntity)
return ApiResponse.success(metricsService.getCurrentMetrics());
```

`ApiResponse<T>` (`springboot/src/main/java/cn/coderstory/springboot/dto/ApiResponse.java`) structure:
```json
{ "code": 200, "message": "success", "data": { ... } }
```

Factory methods: `success()`, `success(T data)`, `success(message, data)`, `error(code, message)`, `badRequest(message)`, `unauthorized(message)`, `forbidden(message)`, `notFound(message)`, `conflict(message)`

Both patterns are observed in the codebase: `MonitorController` returns `ApiResponse<T>` directly (simpler), while `UserController` and `AuthController` wrap in `ResponseEntity`. New code should prefer the simpler `ApiResponse<T>` direct return.

## Lombok Usage

**Consistent patterns across all backend classes:**

- `@Data` — all entities, DTOs (generates getters, setters, toString, equals, hashCode)
- `@Slf4j` — all controllers, services, exception handlers for logging
- `@RequiredArgsConstructor` — all controllers and services for constructor injection
- `@NoArgsConstructor` + `@AllArgsConstructor` — `ApiResponse` only

## Entity Conventions

**MyBatis Plus annotations are consistent across all entities:**

```java
@Data
@TableName("sys_user")  // Maps to database table
public class User {
    @TableId(type = IdType.AUTO)  // Auto-increment primary key
    private Long id;

    private String username;       // Column matches field name (default)

    @TableLogic                    // Logical delete flag
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)        // Auto-fill on insert
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // Auto-fill on insert and update
    private LocalDateTime updateTime;

    @TableField(exist = false)     // Not a database column
    private List<Menu> children = new ArrayList<>();
}
```

Key conventions verified in entity classes (`User.java`, `Menu.java`, `SeckillActivity.java`, `Role.java`, `SeckillGoods.java`):
- `@TableName` with explicit table name
- `@TableId(type = IdType.AUTO)` for primary key
- `@TableLogic` on `Integer deleted` field for soft delete
- `@TableField(exist = false)` for non-column fields
- `@TableField(fill = FieldFill.INSERT/INSERT_UPDATE)` for timestamps
- No field-level `@Column` annotations when column name matches field name
- No Javadoc on fields (L0 tier — field names are self-documenting)

## Comment Standards

Detailed spec in `docs/comment-standards.md`. Key rules:

**L0-L3 Tier System:**
| Tier | Constraint | Scope | Verified |
|------|-----------|-------|----------|
| L0 | Not required | Entity/Model classes | — No Javadoc on `User.java`, `Menu.java`, `SeckillActivity.java` |
| L1 | Suggested | Mapper interfaces, private utility methods | — `UserMapper.java` has no class Javadoc (L1 tier) |
| L2 | Encouraged | Service interfaces, Vue components | — `UserService.java` has class Javadoc; `MonitorDashboard.vue` has `script lang="ts"` doc block |
| L3 | Mandatory | Config, Controller, Exception, JWT, AOP, YAML | — `UserController.java`: full Javadoc; `AuthController.java`: full Javadoc; `HardwareMonitorController.java`: full Javadoc; `BusinessException.java`: full Javadoc; `GlobalExceptionHandler.java`: **missing class Javadoc (violation)** |

**Language:** All comments in Chinese. Technical terms (token, cache, JWT, RocketMQ, Redis, DTO, VO, API) retain English.

**Forbidden elements:**
- `@author` — use git blame/log instead (not observed in new files; some test files like `RocketMQControllerTest.java`, `DistributedLockServiceTest.java` still use `@author` — legacy non-compliance)
- `@since` is required (but version reference should match CLAUDE.md evolution history)
- Empty skeleton Javadoc (tags without descriptions)
- Commented-out code blocks
- getter/setter Javadoc

**Vue component documentation pattern:**
```vue
<script lang="ts">
/**
 * 监控大盘页面组件。展示系统运行状态、性能指标和资源使用情况的实时监控面板。
 */
</script>
<script lang="ts" setup>
// ... component logic
</script>
```

**Frontend API function documentation:**
```typescript
/**
 * 获取用户分页列表。
 *
 * @param params 查询参数（支持多条件筛选）
 * @return 用户分页列表
 */
export function getUserList(params: UserQueryParams) { ... }
```

## Flyway Migrations

**Location:** `springboot/src/main/resources/db/migration/`

**Naming convention:** `V{version}__{description}.sql`
- Examples: `V1__init.sql`, `V4__remove_theme_settings.sql`, `V22__fix_topic_menu_icon.sql`

**Rules:**
- Each change is a new script — never modify executed scripts
- Use idempotent SQL statements
- Flyway tracks execution history in `flyway_schema_history` table

## Git Conventions

- Commit descriptions in Chinese (per CLAUDE.md and verified in commit history)
- Clean up extra/cache files before committing
- `.claude/projects/` memory files not committed

## Quality Rules Enforced by Toolchain

**ArchUnit (`springboot/src/test/java/cn/coderstory/springboot/ArchitectureTest.java`):**
- **Disabled (pending phase 18 fix):** Controller should not directly depend on Mapper
- **Disabled (pending phase 18 fix):** Service should not depend on Controller
- Uses `ClassFileImporter` to scan `cn.coderstory.springboot` package

**Checkstyle** (`config/checkstyle/checkstyle.xml`, `isIgnoreFailures = true`):
- Reports violations but does not fail the build
- Covers naming, imports (avoid star imports except explicit whitelist), braces, empty blocks

**PMD** (`config/pmd/pmd-rules.xml`):
- Covers best practices, code style, design, error-prone categories
- `isIgnoreFailures` behavior (warnings-only, not blocking)

**SpotBugs + Error Prone:**
- Listed in CLAUDE.md toolchain but no dedicated config files found in project root — likely using Gradle plugin defaults

**JaCoCo:**
- Listed in CLAUDE.md toolchain. Coverage target not explicitly defined in `build.gradle.kts`.
- Frontend coverage: `vitest coverage` command generates text/json/html reports per `vitest.config.js`

**Build commands for quality checks:**
```powershell
cd springboot
./gradlew.bat check    # Full quality check suite
./gradlew.bat test     # Tests only
./gradlew.bat build    # Build + tests
```

## Anti-Patterns Observed

### Controller directly injecting Mapper
`UserController` injects `RoleMapper` directly (line 34), bypassing Service layer. ArchUnit test `controllerShouldNotDependOnMapper()` is `@Disabled` pending phase 18 fix.

### Integration tests using `@author` tag
`DistributedLockServiceTest.java` and `RocketMQControllerTest.java` contain `@author system` and `@version 1.0` tags, violating the project's no-`@author` rule. These are legacy tests not yet updated.

### Controller return type inconsistency
Some controllers return `ResponseEntity<ApiResponse<T>>` (`UserController`, `AuthController`) while others return `ApiResponse<T>` directly (`MonitorController`, `HardwareMonitorController`). New code should prefer direct `ApiResponse<T>` return.

---

*Convention analysis: 2026-05-10*
