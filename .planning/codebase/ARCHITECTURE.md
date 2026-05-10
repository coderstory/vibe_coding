<!-- refreshed: 2026-05-10 -->
# Architecture

**Analysis Date:** 2026-05-10

## System Overview

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                         Vue 3 Frontend (app-vue/)                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Views   │  │  Store   │  │  Router  │  │  API     │  │ Composables│  │
│  │  pages/  │  │  Pinia   │  │  guards  │  │  modules │  │  hooks/   │  │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────────┘  │
│       └─────────────┴─────────────┴─────────────┘                       │
│                             │ HTTP (Axios)                              │
└─────────────────────────────┼───────────────────────────────────────────┘
                              │
┌─────────────────────────────┼───────────────────────────────────────────┐
│                    Spring Boot Backend (springboot/)                     │
│                              ▼                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │              Controller Layer  (../controller/)                  │   │
│  │  auth/  user/  role/  menu/  audit/  knowledge/  seckill/       │   │
│  │  order/  rocketmq/  monitor/                                     │   │
│  └────────────────────────┬─────────────────────────────────────────┘   │
│                           │                                             │
│  ┌────────────────────────▼─────────────────────────────────────────┐   │
│  │              Service Layer  (../service/)                        │   │
│  │  *Service interface + *ServiceImpl implementation per domain      │   │
│  └────────────────────────┬─────────────────────────────────────────┘   │
│                           │                                             │
│  ┌────────────────────────▼─────────────────────────────────────────┐   │
│  │              Mapper Layer  (../mapper/)                          │   │
│  │  MyBatis Plus BaseMapper<T> + optional mapper.xml for complex SQL │   │
│  └────────────────────────┬─────────────────────────────────────────┘   │
│                           │                                             │
│                           ▼                                             │
│                   MySQL (admin_system)                                  │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │ Shared Cross-Cutting:                                            │   │
│  │  config/  security/  aspect/  exception/  util/  limiter/  lock/ │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  External Integrations:                                                 │
│    Redis ←→ Redisson  ←→ StringRedisTemplate (Lua scripts)             │
│    RocketMQ ←→ TransactionMQProducer / RocketMQListener                │
└─────────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| Frontend Views | Page-level Vue components, one per route | `app-vue/src/views/` |
| Frontend Store | Global state (user auth, token) via Pinia | `app-vue/src/store/user.ts` |
| Frontend Router | Route config + navigation guards | `app-vue/src/router/index.ts`, `guards.ts` |
| Frontend API | Axios-based service layer, one module per domain | `app-vue/src/api/modules/` |
| Controller | REST endpoints, request validation, response wrapping | `springboot/src/main/java/cn/coderstory/springboot/controller/` |
| Service | Business logic, orchestrating mappers and external calls | `springboot/src/main/java/cn/coderstory/springboot/service/` |
| Mapper | MyBatis Plus data access, SQL mapping | `springboot/src/main/java/cn/coderstory/springboot/mapper/` |
| Entity | JPA-style database table mapping with MyBatis Plus annotations | `springboot/src/main/java/cn/coderstory/springboot/entity/` |
| DTO/VO | Request/response data transfer objects | `springboot/src/main/java/cn/coderstory/springboot/dto/`, `vo/` |
| Config | Spring configuration classes, JWT, Redis, MQ, security | `springboot/src/main/java/cn/coderstory/springboot/config/` |
| Exception | Global exception handler + BusinessException hierarchy | `springboot/src/main/java/cn/coderstory/springboot/exception/` |
| Aspect | AOP cross-cutting concerns (audit logging) | `springboot/src/main/java/cn/coderstory/springboot/aspect/` |
| Limiter | Concurrency, QPS, and IP rate limiters | `springboot/src/main/java/cn/coderstory/springboot/limiter/` |
| Lock | Distributed lock abstraction with Redis implementation | `springboot/src/main/java/cn/coderstory/springboot/lock/` |
| MQ | RocketMQ producers and consumers | `springboot/src/main/java/cn/coderstory/springboot/mq/` |
| SSE | Server-Sent Events push service | `springboot/src/main/java/cn/coderstory/springboot/sse/` |

## Pattern Overview

**Overall:** Domain-driven package layout with strict Controller--Service--Mapper layered architecture.

**Key Characteristics:**
- Each business domain is a self-contained package under the root: `controller/{domain}/`, `service/{domain}/`, `mapper/{domain}/`, `entity/{domain}/`, `dto/{domain}/`
- The `shared/` package contains cross-cutting concerns: config, security, aspect, exception, util, limiter, lock
- Frontend mirrors the backend domain split in `api/modules/` and `views/{domain}/`
- Service layer uses interface/implementation pattern (`*Service` interface + `*ServiceImpl` class)
- Dependency injection via `@RequiredArgsConstructor` with `private final` fields (no `@Autowired`)
- Unified API response via `ApiResponse<T>` with static factory methods (`success()`, `error()`, `badRequest()`, etc.)

## Layers

**Controller Layer:**
- Purpose: HTTP endpoint definitions, request parameter extraction, response wrapping
- Location: `springboot/src/main/java/cn/coderstory/springboot/controller/{domain}/`
- Contains: REST controllers annotated with `@RestController` + `@RequestMapping("/api/{domain}")`
- Depends on: Service interfaces, DTOs, exception classes
- Used by: Frontend HTTP calls (via Axios)

**Service Layer:**
- Purpose: Business logic orchestration, transaction boundaries, external system coordination
- Location: `springboot/src/main/java/cn/coderstory/springboot/service/{domain}/` (interface) + `impl/` (implementation)
- Contains: `*Service` interfaces with method contracts, `*ServiceImpl` classes with business logic
- Depends on: Mapper interfaces, entity classes, DTOs, shared services (limiter, lock, MQ, SSE)
- Used by: Controller layer

**Mapper Layer:**
- Purpose: Data access, SQL mapping, MyBatis Plus CRUD operations
- Location: `springboot/src/main/java/cn/coderstory/springboot/mapper/{domain}/`
- Contains: Interfaces extending `BaseMapper<T>`, optional XML files in `resources/mapper/`
- Depends on: Entity classes (generic type parameter)
- Used by: Service layer

**Entity Layer:**
- Purpose: Database table mapping, ORM annotations
- Location: `springboot/src/main/java/cn/coderstory/springboot/entity/{domain}/`
- Contains: POJO classes with `@TableName`, `@TableId`, `@TableLogic` annotations
- Used by: Mapper layer, Service layer

**Config Layer (shared):**
- Purpose: Spring bean configuration, security, JWT, Redis, RocketMQ setup
- Location: `springboot/src/main/java/cn/coderstory/springboot/config/`
- Key files: `SecurityConfig.java`, `JwtAuthenticationFilter.java`, `JwtTokenProvider.java`, `RedissonConfig.java`, `RocketMQConfig.java`, `CorsConfig.java`, `WebConfig.java`

## Data Flow

### Primary Request Path (Standard CRUD)

1. Frontend calls API module function (`app-vue/src/api/modules/{domain}.ts`) via Axios instance (`app-vue/src/api/request.ts`)
2. Axios request interceptor adds `Authorization: Bearer <token>` header and `X-User-Id` header (`app-vue/src/api/request.ts:39-58`)
3. Request reaches backend Controller endpoint (`springboot/src/main/java/cn/coderstory/springboot/controller/{domain}/*Controller.java`)
4. `JwtAuthenticationFilter` parses JWT token and sets `SecurityContextHolder` before controller execution (`springboot/src/main/java/cn/coderstory/springboot/config/JwtAuthenticationFilter.java:46-68`)
5. Controller delegates to Service interface method
6. `AuditAspect` AOP intercepts Service method if it is a write operation (`springboot/src/main/java/cn/coderstory/springboot/aspect/AuditAspect.java:74-131`)
7. Service calls Mapper (MyBatis Plus `BaseMapper` or custom XML SQL)
8. Response flows back through Mapper -- Service -- Controller -- HTTP
9. Controller wraps result in `ApiResponse<T>` and returns
10. Frontend Axios response interceptor unwraps `ApiResponse`, checks `code === 200`, shows error messages via `ElMessage` on failure

### Authentication Flow

1. Frontend sends `POST /api/auth/login` with username/password
2. `AuthController` validates credentials, calls `AuthService` (`springboot/src/main/java/cn/coderstory/springboot/controller/auth/AuthController.java`)
3. Service generates JWT Access Token (24h expiry) + Refresh Token (7d expiry) via `JwtTokenProvider` (`springboot/src/main/java/cn/coderstory/springboot/config/JwtTokenProvider.java`)
4. Backend returns `{token, refreshToken, user}`; frontend stores in `localStorage`
5. On subsequent requests, `JwtAuthenticationFilter.doFilterInternal()`:
   - Extracts `Bearer <token>` from `Authorization` header
   - Validates token via `JwtTokenProvider.validateToken()`
   - Sets `UsernamePasswordAuthenticationToken` into `SecurityContextHolder`
   - Sets `userId` and `username` as request attributes
6. On 401 response, frontend Axios interceptor automatically calls `POST /api/auth/refresh` with refreshToken, retries original request (`app-vue/src/api/request.ts:180-191`)
7. Login/register paths are excluded from JWT filtering (`JwtAuthenticationFilter.shouldNotFilter()`)

### Seckill Flow (High Concurrency)

1. Frontend generates `queueId`, establishes SSE connection (`GET /api/seckill/subscribe/{queueId}`) (`springboot/src/main/java/cn/coderstory/springboot/controller/seckill/SeckillSseController.java:37-41`)
2. Frontend sends `POST /api/seckill/buy` with `{goodsId, activityId, idempotentKey, sign, queueId}`
3. `SeckillServiceImpl.seckill()` executes multi-layer protection (`springboot/src/main/java/cn/coderstory/springboot/service/seckill/impl/SeckillServiceImpl.java:155-265`):
   - Layer 1: Idempotency check (Redis SETNX with 10min TTL)
   - Layer 2: QPS rate limiting (sliding window)
   - Layer 3: Concurrency limiting (semaphore)
   - Layer 4: Activity status validation (DB query)
   - Layer 5: Signature verification (time-bound HMAC)
   - Layer 6: Distributed lock + Redis atomic stock deduction (Lua script `DECRBY` with stock check)
   - Layer 7: RocketMQ transactional message for order creation
4. `OrderTransactionProducer` sends transaction message; `executeLocalTransaction` callback inserts `Order` into DB (`springboot/src/main/java/cn/coderstory/springboot/mq/seckill/producer/OrderTransactionProducer.java:44-85`)
5. Order creation success triggers SSE push via `SeckillSseService.sendSuccess()` to the waiting frontend

### Audit Logging Flow

1. `AuditAspect` defines pointcut on `cn.coderstory.springboot.service..*` package (excluding `AuditService` itself) (`springboot/src/main/java/cn/coderstory/springboot/aspect/AuditAspect.java:60-62`)
2. Around advice infers operation type from method name prefix (save/create/add/insert/update/delete/remove/assign/grant, etc.)
3. `ThreadLocal<Boolean>` flag prevents duplicate audit logging within same thread
4. Extracts userId from JWT token in request, builds operation description
5. Calls `AuditService.log()` asynchronously (method annotated with `@Async`)
6. Excluded methods (queries like `getAuditLogPage`, `selectPage`, etc.) are skipped

### SSE Push Architecture

1. Frontend generates `queueId` client-side before initiating the seckill request
2. Frontend calls `GET /api/seckill/subscribe/{queueId}` to open SSE connection (`springboot/src/main/java/cn/coderstory/springboot/controller/seckill/SeckillSseController.java`)
3. `SeckillSseService.subscribe()` (`springboot/src/main/java/cn/coderstory/springboot/sse/seckill/SeckillSseService.java:79-124`):
   - Creates `SseEmitter` with configurable timeout
   - Stores in `ConcurrentHashMap<String, SseEmitter>` (keyed by queueId)
   - Registers `onCompletion`, `onTimeout`, `onError` callbacks that remove from map
   - Sends initial `connected` event
4. Backend pushes results via `sendSuccess()`, `sendFailed()`, `sendWaiting()`, `sendHeartbeat()` methods
5. Events named `seckill_result` (status 1=success, 2=fail) and `seckill_status` (status 0=queued)
6. Client calls `GET /api/seckill/unsubscribe/{queueId}` to close connection

## Key Abstractions

**BusinessException:**
- Purpose: Standardized business exception with HTTP status codes
- File: `springboot/src/main/java/cn/coderstory/springboot/exception/BusinessException.java`
- Pattern: Static factory methods (`badRequest()`, `unauthorized()`, `forbidden()`, `notFound()`, `conflict()`, `serverError()`)
- Usage: Thrown in Service or Controller, caught by `GlobalExceptionHandler`

**ApiResponse:**
- Purpose: Unified API response wrapper `{code, message, data}`
- File: `springboot/src/main/java/cn/coderstory/springboot/dto/ApiResponse.java`
- Pattern: Static factory methods mirroring HTTP status codes (200, 400, 401, 403, 404, 409, 500)
- Usage: Return type of all Controller methods

**DistributedLockService:**
- Purpose: Distributed lock abstraction
- Files: `springboot/src/main/java/cn/coderstory/springboot/lock/DistributedLockService.java` (interface), `lock/impl/DistributedLockServiceImpl.java` (Redis-based implementation)
- Pattern: `executeWithLock(lockKey, runnable)` with Redis `SETNX` + TTL
- Usage: Seckill activity-level lock, stock operations

**Limiter hierarchy:**
- Files: `springboot/src/main/java/cn/coderstory/springboot/limiter/QpsLimiter.java`, `ConcurrencyLimiter.java`, `IpRateLimiter.java`
- Pattern: Token bucket and semaphore-based rate limiting
- Usage: Seckill multi-layer protection

**RingBuffer:**
- Purpose: Lock-free ring buffer for hardware metrics time-series data
- Files: `springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/RingBuffer.java`
- Pattern: Fixed-size circular buffer with atomic index updates
- Usage: OSHI-based CPU/memory/disk metrics collection in `HardwareMetricsServiceImpl`

## Entry Points

**Frontend:**
- Entry: `app-vue/src/main.ts` (Vue app bootstrap)
- Router: `app-vue/src/router/index.ts` creates `createWebHistory()` router, sets up guards
- Auth guard: `app-vue/src/router/guards.ts` checks `useUserStore().isLoggedIn`, redirects to `/login`

**Backend:**
- Entry: `springboot/src/main/java/cn/coderstory/springboot/SpringbootApplication.java`
- Security chain: `SecurityConfig.securityFilterChain()` (`springboot/src/main/java/cn/coderstory/springboot/config/SecurityConfig.java`)
- JWT filter: `JwtAuthenticationFilter.doFilterInternal()` (`springboot/src/main/java/cn/coderstory/springboot/config/JwtAuthenticationFilter.java`)

## Architectural Constraints

- **Layer violation rules (enforced by ArchUnit):** `ArchitectureTest.java` (`springboot/src/test/java/cn/coderstory/springboot/ArchitectureTest.java`) defines:
  - `controllerShouldNotDependOnMapper` -- Controller must not directly inject Mapper (currently known violations exist, test is `@Disabled` pending Phase 18)
  - `serviceShouldNotDependOnController` -- Service must not reverse-depend on Controller (also `@Disabled`)
- **Threading:** Single-threaded per-request (Tomcat worker threads). Seckill uses `ConcurrencyLimiter` (semaphore) for concurrency control. Audit logging uses `@Async` for asynchronous DB writes.
- **Global state:** `SeckillSseService.emitters` is a `ConcurrentHashMap<String, SseEmitter>` shared across threads. `AuditAspect.AUDIT_FLAG` is `ThreadLocal<Boolean>` for per-thread dedup.
- **Circular imports:** Not detected in current scan. Shared package (`config/`, `exception/`, `dto/`) is intentionally flat to avoid cycles.
- **No `@SuppressWarnings`:** Enforced by project convention (CLAUDE.md).
- **Flyway migration immutability:** Existing migration scripts must never be modified; new changes require a new versioned script.

## Anti-Patterns

### Controller directly injecting Mapper

**What happens:** Several controllers (e.g., `SeckillController` at `springboot/src/main/java/cn/coderstory/springboot/controller/seckill/SeckillController.java:44-45`) directly inject `SeckillGoodsMapper` and `SeckillActivityMapper` instead of going through a Service layer.
**Why it's wrong:** Bypasses the Service layer, skipping transaction boundaries, audit logging, and business validation. Violates the layered architecture.
**Do this instead:** Inject the corresponding Service interface and call its method. Checked by `ArchitectureTest.controllerShouldNotDependOnMapper()`.

### Service method naming coupling with AOP inference

**What happens:** `AuditAspect` infers CRUD operation type by matching method name prefixes (`save*`=CREATE, `update*`=UPDATE, `delete*`=DELETE). This is fragile -- a method named `deleteXxx` that only reads data would be incorrectly classified.
**Why it's wrong:** Relies on naming convention rather than explicit annotation. Typos or unconventional names silently skip auditing.
**Do this instead:** Consider adding a custom `@AuditAction("CREATE")` annotation on Service methods for explicit opt-in auditing.

## Error Handling

**Strategy:** Centralized via `@RestControllerAdvice` (`GlobalExceptionHandler` at `springboot/src/main/java/cn/coderstory/springboot/exception/GlobalExceptionHandler.java`).

**Patterns:**
- `BusinessException` (checked via handler) -> returns `ApiResponse` with corresponding HTTP error code
- `DuplicateKeyException` -> mapped to 409 Conflict with human-readable duplicate field message
- `BadSqlGrammarException` -> 500 Internal Server Error (logged with stack trace)
- `NullPointerException` -> 500 (logged with stack trace)
- `NoHandlerFoundException` -> 404 Not Found
- `IllegalArgumentException` -> 400 Bad Request
- Catch-all `Exception` -> 500 (logged with stack trace)
- Frontend Axios interceptor catches non-200 codes, shows `ElMessage.error()` with extracted message

## Cross-Cutting Concerns

**Logging:** SLF4J with Lombok `@Slf4j` annotation on all service classes and controllers. Log levels: `log.debug()` for auth details, `log.warn()` for business failures/rate limiting, `log.error()` for exceptions.

**Validation:** Primarily manual validation in Controller methods (null checks, status checks). No Bean Validation (`@Valid`, `@NotBlank`) annotations detected.

**Authentication:** JWT-based, implemented via `JwtAuthenticationFilter` (extends `OncePerRequestFilter`). Bearer token in `Authorization` header. Stateless session management. Excludes login/register paths.

**Idempotency:** Redis-backed `IdempotentService` at `springboot/src/main/java/cn/coderstory/springboot/config/IdempotentService.java`. Uses `SETNX` with TTL. Applied in Seckill flow.

---

*Architecture analysis: 2026-05-10*
