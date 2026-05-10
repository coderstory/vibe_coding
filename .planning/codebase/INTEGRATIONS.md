# External Integrations

**Analysis Date:** 2026-05-10

## APIs & External Services

**No external SaaS APIs integrated.** The backend has no calls to third-party cloud services (Stripe, Supabase, AWS, etc.). All integrations are self-hosted infrastructure:

| Service | Usage | Config File | Status |
|---------|-------|-------------|--------|
| MySQL 9.x | Primary data store | `config/datasource.yaml` | On-prem, port 3306 |
| Redis 7.x | Cache + distributed lock + rate limiting | `config/cache.yaml` | On-prem, port 6379 |
| RocketMQ 5.x | Async messaging (seckill, admin) | `config/mq.yaml` | On-prem, NameServer :9876 |
| OSHI 7.x | Hardware metrics collection (FFM) | `config/business.yaml` | In-process library |

## Data Storage

**Databases:**
- **MySQL 9.7.0** via MySQL Connector/J
  - Connection: `jdbc:mysql://${DB_HOST:127.0.0.1}:3306/admin_system`
  - Config: `springboot/src/main/resources/config/datasource.yaml`
  - Pool: HikariCP (min-idle: 5, max-pool-size: 20, 30s timeout, 30min max-lifetime)
  - Client: `com.mysql.cj.jdbc.Driver`
  - Env overrides: `DB_HOST`, `DB_USER`, `DB_PASSWORD`

- **Flyway 12.5.0** — Schema migration
  - Config: `config/datasource.yaml` (`spring.flyway.*`) and `build.gradle.kts` (`flyway { ... }`)
  - Migration location: `classpath:db/migration/` maps to `springboot/src/main/resources/db/migration/`
  - Naming convention: `V{version}__{description}.sql`
    - Examples: `V1__init.sql`, `V10__seckill_queue.sql`, `V21__rocketmq_consumer_group_menu.sql` (24 total scripts)
  - Baseline: version `"0"`, description `"init"`, `baselineOnMigrate: true`
  - Rule: Each schema change creates a new script; existing scripts are never modified (immutable).
  - Flyway is enabled both at runtime (`spring.flyway.enabled: true`) and as a Gradle task (`flyway` plugin in `build.gradle.kts`). Gradle config inline with hardcoded fallback credentials.

**File Storage:**
- **Local filesystem only** — Knowledge files stored via `KnowledgeServiceImpl`. No external object storage (S3, MinIO) integrated.

**Caching:**
- **Redis** via `spring-boot-starter-data-redis`
  - Host: `127.0.0.1:6379` (config `config/cache.yaml`)
  - Database: `0`, password: none (dev)
  - Lettuce connection pool: max-active 20, max-idle 10, min-idle 5
  - Usage patterns:
    - Seckill stock pre-deduction: `seckill:stock:{goodsId}` (String)
    - Concurrent processing count: `seckill:processing:count` (String)
    - QPS rate limiting: `seckill:qps:*` (keys pattern)
    - IP rate limiting: dynamic keys

- **Redisson 4.3.1** — Distributed lock
  - Config: `springboot/src/main/java/cn/coderstory/springboot/config/RedissonConfig.java`
  - Single server mode: `redis://127.0.0.1:6379`
  - Pool: min-idle 5, pool-size 20
  - Retry: 3 attempts, 1500ms interval
  - Usage: `Rlock` for seckill stock locking and distributed mutual exclusion
  - Also used as caching layer for RocketMQ admin data (stored in `MapCache`)

## Message Queue

**RocketMQ 5.5.0:**
- NameServer: `rocketmq.name-server: 127.0.0.1:9876`
- Producer groups:
  - `seckill_producer_group` — Default producer (config: `config/mq.yaml`)
  - `MSG_SENDER_PRODUCER_GROUP` — Message sending producer (`RocketMQConfig.java`)
- Config: `springboot/src/main/resources/config/mq.yaml` + `RocketMQConfig.java`

**Seckill Topic Structure:**
- Producer: `OrderTransactionProducer` at `springboot/src/main/java/cn/coderstory/springboot/mq/seckill/producer/` — transactional messages for order creation
- Consumer: `StockConsumer` at `springboot/src/main/java/cn/coderstory/springboot/mq/seckill/consumer/` — stock deduction consumer

**RocketMQ Admin Operations:**
- `DefaultMQAdminExt` Bean in `RocketMQConfig.java` — cluster management
- Controller APIs at `springboot/src/main/java/cn/coderstory/springboot/controller/rocketmq/`:
  - `RocketMQController.java` — Topic CRUD, Consumer Group management, Message query
  - `RocketMQDashboardController.java` — Cluster overview, broker status, topic backlog
- Frontend API module: `app-vue/src/api/modules/rocketmq.ts`
  - Endpoints: `/api/rocketmq/topics`, `/api/rocketmq/consumer-groups`, `/api/rocketmq/messages`, `/api/rocketmq/dashboard`

## Authentication & Identity

**JWT-based (jjwt 0.13.0):**
- Token types:
  - **Access Token**: 24h expiry (`jwt.expiration=86400000`)
  - **Refresh Token**: 7d expiry (`jwt.refresh-expiration=604800000`), identified by `"type": "refresh"` claim
- Signing: HMAC-SHA (`Keys.hmacShaKeyFor`), secret from `jwt.secret` property
- Security whitelist (no auth required): `/api/auth/**`, `/api/knowledge/files/**`, `/api/seckill/subscribe/**`, `/api/seckill/unsubscribe/**`

**Implementation:**
- `JwtTokenProvider.java` (`config/`): Token generation, validation, parsing
- `JwtAuthenticationFilter.java` (`config/`): `OncePerRequestFilter` extracting `Bearer` token from `Authorization` header
- `SecurityConfig.java` (`config/`): Stateless session, CSRF disabled, JWT filter before `UsernamePasswordAuthenticationFilter`
- Frontend: `app-vue/src/api/request.ts` — Axios interceptor adds `Authorization: Bearer {token}`, automatic refresh on 401 with retry queue

**Auth Flow:**
1. `POST /api/auth/login` → returns `{token, refreshToken, user}`
2. Token stored in `localStorage` as `token` and `refreshToken`
3. All requests carry `Authorization: Bearer <token>` (Axios request interceptor)
4. On 401 (non-auth endpoint): automatically calls `POST /api/auth/refresh` with refreshToken, retries original request
5. On refresh failure: clears storage, redirects to `/login`

**Current Issue:**
- JWT secret hardcoded in `config/security.yaml` (`mySecretKeyForJwtTokenGeneration123456789`) — soft-coded fallback only. CLAUDE.md lists this as a "滞留在项" (stalled item) for environment variable hardening.

## Real-Time Push (SSE)

**Server-Sent Events (Spring MVC `SseEmitter`):**
- Service: `springboot/src/main/java/cn/coderstory/springboot/sse/seckill/SeckillSseService.java`
- Connection pool: `ConcurrentHashMap<String, SseEmitter>` keyed by `queueId`
- Timeout: 300000ms (5 min), configurable via `seckill.sse.timeout`
- Lifecycle callbacks: `onCompletion()`, `onTimeout()`, `onError()` — all clean up connection from map
- Events:
  - `connected` — Initial connection confirmation
  - `seckill_result` — Status 1 (success with orderId), 2 (failure with reason)
  - `seckill_status` — Status 0 (waiting/queued)
  - `heartbeat` — Keep-alive ping
  - `completed` — Connection close notification
- Controller: `SeckillSseController.java` at `controller/seckill/`
- White-listed: `/api/seckill/subscribe/**`, `/api/seckill/unsubscribe/**`

**Timing concern documented in CLAUDE.md:**
- Frontend must generate `queueId` before establishing SSE connection to avoid race condition between SSE connect and request send.

## Hardware Monitoring (OSHI 7.x)

**OSHI 7.1.0** (`oshi-core-ffm` module):
- Entry: `SystemInfo` Bean via `MonitoringSchedulerConfig.java`
- Service: `springboot/src/main/java/cn/coderstory/springboot/service/monitor/hardware/impl/HardwareMetricsServiceImpl.java`
- Collection: Single-threaded `ScheduledExecutorService` named `monitor-hardware-collector`
- Sampling:
  - CPU + Memory: every 2s (`sampling-interval: 2000`)
  - Disk + Network: every 3rd sample cycle (~6s, `disk-net-interval: 3`)
- Trend: RingBuffer<HardwareMetricsDTO> of 360 points (~1h at 10s effective rate after decimation of 5)
- Lock: `ReentrantReadWriteLock` — single writer thread, multiple reader API threads
- Fallback: Individual metric failure logs error but does not crash collection loop
- Controller: `HardwareMonitorController.java` at `controller/monitor/hardware/`
  - `GET /api/monitor/hardware/current` — Snapshot
  - `GET /api/monitor/hardware/trend?metric=cpu&range=360` — Time series
  - `GET /api/monitor/hardware/system` — OS info
- Supported trend metrics: `cpu`, `memory`, `disk_read`, `disk_write`, `net_sent`, `net_recv`
- Config: `config/business.yaml` (`monitor.hardware.*`)

## Frontend-Backend API Contract

**Transport:**
- HTTP/JSON over localhost
- Frontend: Axios, base URL `/api`, 10s timeout
- Vite dev proxy: `/api` -> `http://localhost:8080`
- Response wrapper: `ApiResponse<T>` { code: number, message: string, data: T }

**Response Convention:**
- Success: code `200`, data payload
- Error: code `4xx`/`5xx`, message string, data null
- Frontend unified message display via `ElMessage.error()` in response interceptor

**API Endpoint Map:**

| Prefix | Controller | Purpose |
|--------|-----------|---------|
| `POST /api/auth/**` | `AuthController` | Login, register, refresh, logout |
| `GET /api/user/**` | `UserController` | User CRUD |
| `GET /api/role/**` | `RoleController` | Role CRUD |
| `GET /api/menu/**` | `MenuController` | Menu tree |
| `GET /api/knowledge/**` | `KnowledgeController` | Knowledge base articles |
| `GET /api/audit/**` | `AuditController` | Audit logs |
| `GET /api/seckill/**` | `SeckillController` + `SeckillSseController` | Seckill activities, goods, SSE |
| `GET /api/order/**` | `OrderController` | Seckill orders |
| `GET /api/cart/**` | `CartController` | Shopping cart |
| `POST /api/rocketmq/**` | `RocketMQController` | Topic/ConsumerGroup/Message admin |
| `GET /api/rocketmq/dashboard/**` | `RocketMQDashboardController` | Cluster dashboard |
| `GET /api/monitor/metrics` | `MonitorController` | System metrics (Redis-based) |
| `GET /api/monitor/hardware/**` | `HardwareMonitorController` | OSHI hardware monitoring |

## Monitoring & Observability

**Error Tracking:**
- No external error tracking (Sentry, Datadog, etc.) integrated.
- Errors logged to console via `@Slf4j` / `log.error()`.

**Logging:**
- Framework: SLF4J + Logback (Spring Boot default)
- Config: `config/business.yaml` (`logging.level.cn.coderstory: DEBUG`)
- SQL logging via MyBatis Plus: `org.apache.ibatis.logging.stdout.StdOutImpl`
- Pattern: `@Slf4j` annotation on all Service/Controller classes

**Audit Logging:**
- AOP-based: `AuditAspect` at `aspect/AuditAspect.java`
- Pointcut: `cn.coderstory.springboot.service..*` package
- Method prefix analysis: `save/create/add/insert/update/edit/delete/remove` etc. determine CRUD type
- Deduplication: `ThreadLocal<Boolean>` prevents duplicate logging per thread
- Async: `@Async` annotation for non-blocking write
- Excludes: `getAuditLogPage`, `getMenuTree`, `getCurrentUser` etc.

**Health Check:**
- `GET /api/monitor/health` — Returns `{"status": "UP"}` — Basic liveness only, no dependency checks.

## CI/CD & Deployment

**Hosting:**
- Not explicitly configured. No Dockerfile found. No CI pipeline config (GitHub Actions, Jenkins, etc.) found.
- Development only: local `npm run dev` + `./gradlew.bat bootRun`

**CI Pipeline:**
- Not detected. No `.github/workflows/`, `.gitlab-ci.yml`, or `Jenkinsfile` found.

## Environment Configuration

**Required env vars:**
- `DB_HOST` — Default: `127.0.0.1`
- `DB_USER` — Default: `root`
- `DB_PASSWORD` — Default: `123456`
- `JWT_SECRET` — Fallback: hardcoded in `config/security.yaml`

**Secrets location:**
- JWT secret currently hardcoded in `springboot/src/main/resources/config/security.yaml`
- DB password in `springboot/src/main/resources/config/datasource.yaml` has env var fallback pattern
- `.env` file present in project root — may contain dev overrides (do not read contents)

## Ports

| Service | Port | Config Location |
|---------|------|----------------|
| Frontend (Vite dev) | 5173 | `app-vue/vite.config.js` |
| Backend (Spring Boot) | 8080 | `springboot/src/main/resources/application.yaml` |
| MySQL | 3306 | `config/datasource.yaml` |
| Redis | 6379 | `config/cache.yaml` |
| RocketMQ NameServer | 9876 | `config/mq.yaml` |
| RocketMQ Broker | 10911 | RocketMQ default |

## Webhooks & Callbacks

**Incoming:**
- None.

**Outgoing:**
- None. No webhook callbacks to external services.

---

*Integration audit: 2026-05-10*
