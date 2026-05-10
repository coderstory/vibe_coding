# Testing Patterns

**Analysis Date:** 2026-05-10

## Test Framework

**Backend (Spring Boot):**
- JUnit 5 (Jupiter) — platform-based, via `spring-boot-starter-test`
- Mockito 5 — via `spring-boot-starter-test` transitive dependency
- ArchUnit 1.4.2 — via `com.tngtech.archunit:archunit-junit5`
- Config: `springboot/build.gradle.kts` — `useJUnitPlatform()` for test task, `exclude("**/*IT.class")` for integration test exclusion pattern

**Frontend (Vue/Vite):**
- Vitest 4.x — via `app-vue/vitest.config.js`
- `@vue/test-utils` 2.4.6 for component mounting
- jsdom 29.x for DOM environment
- Config: `app-vue/vitest.config.js`

## Test Configuration

**Backend** (`springboot/build.gradle.kts`):
```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
    exclude("**/*IT.class")  // Integration test pattern excluded from "test" task
}
```

**Frontend** (`app-vue/vitest.config.js`):
```javascript
export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: 'jsdom',
    include: ['src/**/*.{test,spec}.{js,ts}'],
    coverage: {
      reporter: ['text', 'json', 'html']
    }
  },
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})
```

## Run Commands

**Backend:**
```bash
cd springboot
./gradlew.bat test                      # All unit tests
./gradlew.bat test --tests "*ClassName"  # Single test class
./gradlew.bat build                      # Build + tests
./gradlew.bat check                      # Full quality check (tests + Checkstyle + PMD + SpotBugs + Error Prone)
```

**Frontend:**
```bash
cd app-vue
npm run test          # Vitest (all)
npm run test:ui       # Vitest with UI
npm run coverage      # Vitest with coverage report
```

## Test File Organization

**Backend:**
- Tests mirror source structure under `springboot/src/test/java/cn/coderstory/springboot/`
- Unit tests: `src/test/java/.../{TestClass}Test.java` (suffix `Test`)
- Architecture tests: `ArchitectureTest.java` at project root package
- Integration tests currently use `Test` suffix too (no `IT` suffix used yet, despite build exclusion pattern)
- No dedicated integration test directory currently

**Directory structure:**
```
springboot/src/test/java/cn/coderstory/springboot/
├── ArchitectureTest.java
├── PasswordHashTest.java
├── VirtualThreadTest.java
├── controller/
│   ├── RocketMQControllerTest.java
│   └── monitor/hardware/
│       └── HardwareMonitorControllerTest.java
├── exception/
│   └── GlobalExceptionHandlerTest.java
├── limiter/
│   └── QpsLimiterTest.java
├── lock/
│   └── DistributedLockServiceTest.java
├── order/service/
│   └── OrderServiceTest.java
├── seckill/service/
│   └── SignServiceTest.java
├── security/
│   └── IdempotentServiceTest.java
├── service/
│   └── UserServiceImplTest.java
└── stock/service/
    └── StockServiceTest.java
    └── monitor/hardware/
        ├── RingBufferTest.java
        └── HardwareMetricsServiceImplTest.java
```

**Frontend:**
- Test files co-located with source in `__tests__/` directories
- Naming: `{ModuleName}.test.ts`

```
app-vue/src/
├── api/__tests__/
│   └── user.test.ts           # API error handling tests
└── views/rocketmq/__tests__/
    └── ConsumerGroupList.test.ts  # Vue component tests
```

## Test Naming Conventions

**Backend (verified across all test files):**
- Class name: `{TargetClass}Test.java` — `HardwareMetricsServiceImplTest`, `UserServiceImplTest`, `GlobalExceptionHandlerTest`
- `@DisplayName` at class, nested class, and method level — Chinese descriptions
- `@Nested` inner classes for logical grouping (feature/module grouping)
- Test method names: `{when}_{then}` pattern in camelCase with Chinese `@DisplayName`:
  ```java
  @Nested
  @DisplayName("CPU 采集")
  class CpuCollectionTests {
      @Test
      @DisplayName("CPU 正常采集返回系统使用率和各核使用率")
      void collectCpuNormal() { ... }

      @Test
      @DisplayName("CPU 采集异常时局部降级，其他指标不受影响")
      void whenCpuFails_otherMetricsUnaffected() { ... }
  }
  ```

**Frontend (verified across test files):**
- Test file: `{ModuleName}.test.ts`
- `describe` blocks in Chinese:
  ```typescript
  describe('User API Error Handling', () => {
    describe('extractErrorMessage', () => {
      it('should extract message from backend response', () => { ... })
    })
  })
  ```
- `it()` descriptions in English (observable but inconsistent; some use Chinese)

## Test Structure Patterns

### Backend Unit Tests (Mockito)

**Standard pattern — Mockito `@ExtendWith` approach:**

```java
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HardwareMetricsService 单元测试")
class HardwareMetricsServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Initialize mocks
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("用户存在时返回用户信息")
        void whenUserExists_returnsUser() {
            // Arrange
            when(userMapper.selectUserWithRoleName(1L)).thenReturn(userVO);
            // Act
            UserVO result = userService.getUserById(1L);
            // Assert
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
            verify(userMapper).selectUserWithRoleName(1L);
        }
    }
}
```

Consistent patterns observed:
- `@ExtendWith(MockitoExtension.class)` on every unit test class
- `@Mock` for all dependencies
- `@InjectMocks` for the service under test
- `@Nested` + `@DisplayName` for grouping tests by method or feature
- Arrange/Act/Assert with blank line separation
- `verify()` used to confirm mocked interactions

### Controller Unit Tests (MockMvc standalone)

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("HardwareMonitorController 单元测试")
class HardwareMonitorControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HardwareMetricsService metricsService;

    @InjectMocks
    private HardwareMonitorController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("正常返回 HardwareMetricsDTO 完整 JSON")
    void shouldReturnFullMetricsDto() throws Exception {
        when(metricsService.getCurrentMetrics()).thenReturn(mockHardwareMetricsDTO());

        mockMvc.perform(get("/api/monitor/hardware/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cpu.systemLoad").value(45.2));
    }
}
```

Key patterns:
- `MockMvcBuilders.standaloneSetup(controller)` — no Spring context needed
- `jsonPath("$.code")` assertions for ApiResponse structure
- Test private helper methods to build mock DTOs: `mockHardwareMetricsDTO()`, `mockSystemInfo()`, `mockHistory()`
- `@Nested` grouping by endpoint: `GetCurrentEndpoint`, `GetTrendEndpoint`, `GetSystemEndpoint`

### Exception/Handler Tests (Plain JUnit, no Mockito)

```java
@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("处理 BusinessException")
    void handleBusinessException() {
        BusinessException ex = BusinessException.conflict("用户名已存在");
        ApiResponse<Void> result = handler.handleBusinessException(ex);
        assertEquals(409, result.getCode());
        assertEquals("用户名已存在", result.getMessage());
    }
}
```

Direct instantiation of handler — no mocking needed for stateless handlers.

### Integration Tests (SpringBootTest)

```java
@SpringBootTest(classes = SpringbootApplication.class)
@DisplayName("DistributedLockService 集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DistributedLockServiceTest {

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        // Clean up test data from Redis
    }

    @Test
    @Order(1)
    @DisplayName("应能获取锁")
    void shouldAcquireLock() { ... }
}
```

Key patterns:
- `@SpringBootTest(classes = SpringbootApplication.class)` — loads full Spring context
- `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` — ordered tests
- `@Order(1..N)` — explicit order
- `@Autowired` for injected beans (acceptable in integration tests)
- `@AfterEach tearDown()` — cleanup test data from Redis/DB
- Uses `Set<String>` to track test keys for cleanup

### Frontend Unit Tests (Vitest)

**API function tests:**
```typescript
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return { ...actual, ElMessage: { error: vi.fn() } }
})

describe('User API Error Handling', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('should extract message from backend response', async () => {
    const mockError = { response: { data: { message: '用户名已存在', code: 409 } } }
    const extractErrorMessage = (error: any) => { /* ... */ }
    expect(extractErrorMessage(mockError)).toBe('用户名已存在')
  })
})
```

**Component tests (Vue Test Utils):**
```typescript
import { mount } from '@vue/test-utils'
import ConsumerGroupList from '@/views/rocketmq/ConsumerGroupList.vue'

vi.mock('@/api/modules/rocketmq', () => ({
  getConsumerGroupList: vi.fn()
}))

describe('ConsumerGroupList.vue', () => {
  it('应正确渲染搜索表单', () => {
    const wrapper = mount(ConsumerGroupList, {
      global: {
        stubs: {
          'el-input': ElInput,
          'el-button': ElButton,
          'el-table': ElTable,
          'el-pagination': ElPagination
        }
      }
    })
    expect(wrapper.find('input[placeholder="输入 Consumer Group 名称搜索"]').exists()).toBe(true)
  })
})
```

Key patterns:
- `vi.mock()` at top level for API module mocking
- `mount()` with `global.stubs` for Element Plus component stubs
- `wrapper.find()` and `wrapper.findAll()` for DOM queries
- `trigger('click')`, `setValue()` for user interaction simulation
- `await new Promise(resolve => setTimeout(resolve, 100))` for async wait

## Mocking Strategy

**Backend — What to Mock:**
- External dependencies (database via MyBatis Plus Mapper, Redis, OSHI system API)
- Service layer dependencies for controller tests
- `@Mock` + `@InjectMocks` for service under test
- `MockMvcBuilders.standaloneSetup()` for controller tests (no servlet container)

**Backend — What NOT to Mock:**
- `BusinessException` and `GlobalExceptionHandler` — tested directly as POJOs
- `RingBuffer` — tested directly as POJO (no mocking needed)

**Frontend — What to Mock:**
- API modules (`vi.mock('@/api/modules/rocketmq')`)
- Element Plus components (via `stubs`)
- External libraries (`vi.mock('element-plus')`)

## Coverage

**Backend:** JaCoCo (listed in CLAUDE.md toolchain). No explicit coverage target in `build.gradle.kts`. Coverage report generation not explicitly configured.

**Frontend:** Vitest built-in coverage via `vitest coverage` command. Generates `text`, `json`, `html` reports per `vitest.config.js`. No coverage threshold configured.

**View coverage:**
```bash
cd app-vue && npm run coverage   # Frontend coverage report
cd springboot && ./gradlew.bat test jacocoTestReport  # Backend (if configured)
```

## Test Types

### Unit Tests
- **Scope:** Individual classes in isolation (services, controllers, exception handlers, utility classes)
- **Backend:** Mockito `@ExtendWith(MockitoExtension.class)`, `@Mock` all dependencies
- **Frontend:** Vitest with `vi.mock()` for API modules, `mount()` with stubs for component tests
- **Coverage:** 15 test files in backend, 2 test files in frontend

### Integration Tests
- **Scope:** Full Spring context with `@SpringBootTest`, connecting to real Redis/MySQL
- **Backend:** `DistributedLockServiceTest`, `QpsLimiterTest`, `SignServiceTest`
- **Frontend:** None detected (no Playwright or Cypress config found for E2E)
- **Note:** Integration tests currently share the `Test` suffix pattern — build exclusion `exclude("**/*IT.class")` does not currently filter them

### E2E Tests
- Not used. `@playwright/test` is listed as a devDependency in `app-vue/package.json` but no test files or config found.

## Architecture Tests

**File:** `springboot/src/test/java/cn/coderstory/springboot/ArchitectureTest.java`

```java
class ArchitectureTest {

    @Disabled("Phase 18 修复 Controller→Mapper 直接注入后启用")
    @Test
    void controllerShouldNotDependOnMapper() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..mapper..")
            .because("Controller 不应直接依赖 Mapper，应通过 Service 层访问");
        rule.check(new ClassFileImporter().importPackages("cn.coderstory.springboot"));
    }

    @Disabled("Phase 18 修复分层依赖后启用")
    @Test
    void serviceShouldNotDependOnController() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..service..")
            .should().dependOnClassesThat().resideInAnyPackage("..controller..")
            .because("Service 层不应反向依赖 Controller 层");
        rule.check(new ClassFileImporter().importPackages("cn.coderstory.springboot"));
    }
}
```

Both architecture tests are currently `@Disabled` — controller->mapper direct dependency is a known violation (e.g., `UserController` injects `RoleMapper`).

## Known Testing Issues

### 1. Windows Chinese Path Encoding
**Issue:** Gradle tests fail with `ClassNotFoundException` on Windows due to Chinese path encoding.
**Fix:** `/springboot/build.gradle.kts` must include `-Dfile.encoding=GBK`.
**Status:** Documented in CLAUDE.md pitfall #1. Check if currently set in `build.gradle.kts` or `gradle.properties`.

### 2. Test Exclusion Pattern Mismatch
**Issue:** `build.gradle.kts` has `exclude("**/*IT.class")` but no test files use the `IT` suffix — integration tests like `DistributedLockServiceTest` and `QpsLimiterTest` use `Test` suffix. This means integration tests run alongside unit tests in the default `test` task.

### 3. Legacy @author Tags in Tests
`DistributedLockServiceTest.java` and `RocketMQControllerTest.java` contain `@author system` and `@version 1.0` tags, violating the project's no-`@author` convention.

### 4. Limited Frontend Test Coverage
Only 2 frontend test files exist (`user.test.ts` for API error handling, `ConsumerGroupList.test.ts` for one component). Most views, stores, composables, and router guards are untested.

### 5. Order-Dependent Integration Tests
`DistributedLockServiceTest`, `QpsLimiterTest`, and `SignServiceTest` use `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` with explicit `@Order(N)` — tests must run in a specific sequence because state is shared (e.g., lock acquired in test 1, verified in test 2).

## Test Data Patterns

**Backend:**
- Inline factory methods in test classes: `mockHardwareMetricsDTO()`, `mockSystemInfo()`, `mockHistory()` (see `HardwareMonitorControllerTest`)
- Inline test data setup in `@BeforeEach`: `testUser` object in `UserServiceImplTest`
- No shared test fixtures or factory files detected

**Frontend:**
- Inline mock data in test functions: `mockGroups`, `mockError` objects
- API mocks via `vi.mock()` + `.mockResolvedValue()`

**Cleanup pattern (Redis/DB integration tests):**
```java
private final Set<String> testKeys = new HashSet<>();

@AfterEach
void tearDown() {
    if (!testKeys.isEmpty()) {
        redisTemplate.delete(testKeys);
        testKeys.clear();
    }
}
```

---

*Testing analysis: 2026-05-10*
