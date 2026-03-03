# ShopJoy E-Commerce System

A production-grade Spring Boot e-commerce backend featuring async order processing, JWT/OAuth2 security, GraphQL API, Caffeine caching, and a thread-safe concurrency layer — stress-tested with Apache JMeter at 500 concurrent users.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Spring Boot 3.x |
| API | REST (Spring MVC) + GraphQL (Spring for GraphQL) |
| Security | Spring Security, JWT (HS512), OAuth2 / Google |
| Persistence | Spring Data JPA, PostgreSQL, HikariCP |
| Caching | Caffeine (three-tier TTL strategy) |
| Async | `@Async`, `CompletableFuture`, `ThreadPoolTaskExecutor` |
| Mapping | MapStruct |
| Email | Spring Boot Mail (JavaMailSender, async) |
| Monitoring | Spring AOP aspects (logging, performance, transaction, audit) |
| Build | Maven |
| Testing | Apache JMeter (load/stress), Postman |

---

## Project Structure

```
src/main/java/com/shopjoy/
├── aspect/          # AOP: logging, performance, transaction, rate-limit, audit
├── config/          # Security, cache, async, CORS, JWT filter, OpenAPI
├── controller/      # REST controllers (auth, product, order, inventory, ...)
├── dto/             # Request/response DTOs + MapStruct mappers
├── entity/          # JPA entities
├── exception/       # Global exception handler + custom exceptions
├── graphql/         # GraphQL mutation/query resolvers
├── repository/      # Spring Data JPA repositories
├── security/        # CustomUserDetails, OAuth2 success handler
├── service/         # Service interfaces + implementations
│   └── impl/
└── util/            # JwtUtil, AspectUtils, CacheMetricsCollector, ...
```

---

## Running the Application

### Prerequisites
- Java 21+
- PostgreSQL 14+
- Maven 3.9+

### Configuration

Set the following in `application.properties` or as environment variables:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shopjoy
spring.datasource.username=<user>
spring.datasource.password=<password>

jwt.secret=<256-bit-hex-secret>
jwt.expiration=86400000
jwt.refresh.expiration=604800000

spring.mail.host=smtp.gmail.com
spring.mail.username=<email>
spring.mail.password=<app-password>

app.admin.username=admin
app.admin.password=<strong-password>
app.admin.email=admin@shopjoy.com
```

### Start

```bash
./mvnw spring-boot:run
```

| Endpoint | URL |
|---|---|
| REST API | http://localhost:8080/api/v1 |
| GraphQL | http://localhost:8080/graphql |
| GraphiQL | http://localhost:8080/graphiql |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

## Authentication

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "kwaku_larbi",
  "email": "kwaku@example.com",
  "password": "Test@123",
  "firstName": "Kwaku",
  "lastName": "Larbi",
  "phone": "0244000000"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "kwaku_larbi",
  "password": "Test@123"
}
```

Returns `{ "token": "Bearer eyJ...", "expiresIn": 86400000 }`.

Use the token as `Authorization: Bearer <token>` on all protected endpoints.

---

## Key Features

### Async Order Processing
Order creation runs via `@Async("appTaskExecutor")` returning `CompletableFuture<OrderResponse>`. The HTTP thread is released immediately; inventory reservation, pricing, and persistence happen on the async pool. An order-confirmation email fires via a separate async method after completion.

### Three-Tier Caffeine Cache

| Manager | TTL | Max Size | Caches |
|---|---|---|---|
| `cacheManager` (primary) | 30 min | 1 000 | products, categories, users, userDetails |
| `mediumCacheManager` | 10 min | 500 | orders, reviews, addresses |
| `shortCacheManager` | 2 min | 500 | inventory, cart, stock levels |

All write operations carry `@CacheEvict` to keep caches consistent.

### JWT + Refresh Token
- Access token: HS512, 24 h TTL
- Refresh token: stored in `refresh_tokens` table, 7-day TTL
- Token blacklist: `ConcurrentHashMap<String, LocalDateTime>` with hourly eviction of expired entries
- `JwtAuthenticationFilter` skips public paths (`/api/v1/auth/**`, `/oauth2/**`) via `shouldNotFilter()`

### Rate Limiting
Login attempts tracked per-username and per-IP in `ConcurrentHashMap`. Default: 5 attempts / 15-minute window. Exceeded requests receive `HTTP 429` with a `Retry-After` duration. Temporarily disabled during JMeter stress tests.

### Security Audit Logging
All authentication events (login success/failure, token expiry, access denied, rate-limit exceeded) are persisted via `SecurityAuditServiceImpl.logEvent()`, which is annotated `@Async("taskExecutor")` to avoid blocking the request thread.

---

## Performance Optimizations Applied

### 1. BCrypt Rounds Reduced (CRITICAL)
`BCryptPasswordEncoder` strength reduced from 10 → 8 rounds. Cuts hash time from 100–150 ms to 25–40 ms, directly reducing login latency by 40–50 %.

### 2. Async Security Audit (MEDIUM)
`SecurityAuditService.logEvent()` annotated `@Async("taskExecutor")`. Synchronous DB writes no longer block the HTTP thread — 20–30 ms removed per authenticated request.

### 3. JWT Filter Short-Circuit (HIGH)
`shouldNotFilter()` returns `true` for all public paths. The full JWT validation chain is skipped for login, registration, and OAuth2 redirect endpoints.

### 4. User Details Caching (MEDIUM)
`@Cacheable(value="userDetails", key="#username")` on `CustomUserDetailsService.loadUserByUsername()`. Repeat authenticated requests skip the `SELECT` on the `users` table entirely.

### 5. Product Listing Cache (HIGH)
`getProductsWithFilters`, `searchProductsByName`, and `getProductsByPriceRange` all cached with 30-minute TTL. Under load, 500 concurrent product-browsing requests return from memory with 0–2 ms response time.

### 6. Thread-Safe Collections (Phase 3)

| Class | Before | After |
|---|---|---|
| `PerformanceMetricsCollector` | `Collections.synchronizedList(ArrayList)` | `CopyOnWriteArrayList` |
| `PerformanceMetricsCollector` | `Map<String, Long>` + `merge()` | `Map<String, AtomicLong>` + `incrementAndGet()` |
| `JwtAuthenticationFilter` | `Arrays.asList(...)` (mutable) | `List.of(...)` (immutable) |
| `RateLimitServiceImpl` | — | `ConcurrentHashMap` (already correct) |
| `TokenBlacklistServiceImpl` | — | `ConcurrentHashMap` (already correct) |

### 7. Executor Tuning (Phase 3)

| Executor | Before | After | Formula |
|---|---|---|---|
| `taskExecutor` (audit) | core=10, max=50 (hardcoded) | core=N×2, max=N×2 | IO-bound: cores × 2 |
| `appTaskExecutor` (orders/email) | core=N, max=N×2 | core=N×2, max=N×4 | IO-bound: cores × 2 / × 4 |

`N` = `Runtime.getRuntime().availableProcessors()` — evaluated once at startup via a `static final` constant.

### 8. Lazy Initialization (Development)
`spring.main.lazy-initialization=true` defers non-critical bean creation; startup time reduced by ~50 % in development.

---

## Performance Comparison

Measured via Apache JMeter — 500 concurrent users, 30-second ramp-up.

| Endpoint | Before (ms) | After (ms) | Improvement |
|---|---|---|---|
| `POST /api/v1/auth/login` | 315–522 | 60–90 | ~80 % |
| `POST /api/v1/auth/register` | 280–450 | 70–110 | ~75 % |
| `GET /api/v1/products` (cached) | 180–350 | 0–2 | ~99 % |
| `GET /api/v1/products` (cold) | 180–350 | 40–80 | ~77 % |
| `POST /api/v1/orders` | 800–1 200 | 19–30 | ~97 % |
| `GET /api/v1/inventory/{id}` | 120–200 | 1–5 | ~97 % |

**Notes:**
- "Before" numbers sourced from `application-2026-02-24.15.log` (JMeter stress run — login failures at 315–522 ms under concurrent load with synchronous audit writes contributing 20–30 ms per request).
- "After" numbers sourced from `application-2026-02-25.7.log` (order creation 19–30 ms, product listing 0–2 ms from Caffeine).
- Login improvement reflects BCrypt 10→8 rounds + async audit + `@Transactional` removed from login.
- Order improvement reflects `@Async` execution via `appTaskExecutor` + `SERIALIZABLE` isolation only within the service boundary.

---

## Bottlenecks Identified and Resolved

### Bottleneck 1 — Synchronous BCrypt in Login (CRITICAL)
**Problem:** BCrypt at 10 rounds takes 100–150 ms per hash. With `@Transactional` wrapping the login method, a database connection was held open during the entire BCrypt computation, blocking the HikariCP pool under concurrent load.

**Fix:** Reduced to 8 rounds; removed `@Transactional` from login; BCrypt is now performed outside any transaction boundary.

### Bottleneck 2 — Blocking Security Audit Writes
**Problem:** Every login attempt (success or failure) synchronously wrote to the `security_audit_logs` table before returning a response.

**Fix:** `@Async("taskExecutor")` on `SecurityAuditServiceImpl.logEvent()`. Audit persistence is fire-and-forget from the HTTP thread's perspective.

### Bottleneck 3 — Full JWT Chain on Public Endpoints
**Problem:** Every request — including `/api/v1/auth/login` — passed through `JwtAuthenticationFilter.doFilterInternal()`, performing header parsing and blacklist lookup even though public endpoints require no token.

**Fix:** `shouldNotFilter()` override returns `true` for all public path prefixes.

### Bottleneck 4 — Repeated `loadUserByUsername` DB Queries
**Problem:** Every authenticated request triggered a `SELECT * FROM users WHERE username = ?`. Under 500 concurrent users this caused heavy read pressure on the users table.

**Fix:** `@Cacheable("userDetails")` with 30-minute TTL on `CustomUserDetailsService`.

### Bottleneck 5 — Race Condition in PerformanceMetricsCollector
**Problem:** Inner `Collections.synchronizedList(ArrayList)` is thread-safe for individual operations but not for compound actions (iterate + add). `Long` counter via `merge()` is non-atomic under contention.

**Fix:** `CopyOnWriteArrayList` (safe for concurrent reads, rare writes) + `AtomicLong.incrementAndGet()`.

### Bottleneck 6 — Mutable Static Collection in JWT Filter
**Problem:** `PUBLIC_PATHS = Arrays.asList(...)` creates a fixed-size but structurally mutable list — any code calling `set()` would silently corrupt it across all threads.

**Fix:** `List.of(...)` — fully immutable.

---

## Algorithm Complexity

| Operation | Before | After | Notes |
|---|---|---|---|
| Product search by name | O(N) per request | O(1) cached | N = total products; cache key = keyword |
| Product filter query | O(N) per request | O(1) cached | Spec-based JPA + page result cached 30 min |
| Price range lookup | O(N) per request | O(1) cached | Composite key = minPrice:maxPrice |
| User lookup per request | O(log N) DB | O(1) in-memory | HikariCP + Caffeine cache |
| Token blacklist check | O(1) | O(1) | ConcurrentHashMap lookup |
| Rate limit check | O(1) | O(1) | ConcurrentHashMap.compute() — atomic |
| Metrics counter increment | O(1) contended | O(1) wait-free | AtomicLong vs Long + merge() |

---

## Async Architecture

```
HTTP Thread
    │
    ├── login()  ──────────────────────────────── returns JWT (60–90 ms)
    │               │
    │               └── [async-audit-N] logEvent() ── DB write (fire & forget)
    │
    ├── createOrder() ─────────────────────────── returns CompletableFuture<OrderResponse>
    │               │
    │               └── [app-async-N] processOrder()
    │                       ├── validateStock()     (SERIALIZABLE tx)
    │                       ├── reserveInventory()
    │                       ├── persist Order
    │                       └── [app-async-M] sendOrderConfirmationEmail()
    │
    └── GET /products ─────────────────────────── returns from Caffeine (0–2 ms)
```

---

## Thread Pool Configuration

```
taskExecutor (async-audit-*)
    corePoolSize  = availableProcessors × 2
    maxPoolSize   = availableProcessors × 2
    queueCapacity = 500
    purpose       = security audit log writes (IO-bound)

appTaskExecutor (app-async-*)
    corePoolSize  = availableProcessors × 2
    maxPoolSize   = availableProcessors × 4
    queueCapacity = 200
    purpose       = order processing, email dispatch (IO-bound)
```

Both executors call `setWaitForTasksToCompleteOnShutdown(true)` to drain in-flight work on graceful shutdown.

---

## Risk Controls

| Risk | Mitigation |
|---|---|
| Async breaking transactions | Transaction boundaries kept inside service methods; resolvers/controllers never call `.get()` or `.join()` |
| Thread pool exhaustion | Fixed-size executor with rejection handler that logs and drops; queue capacity limits back-pressure |
| Stale cache data | Short TTL on volatile data (inventory: 2 min); `@CacheEvict` on all write paths |
| Race condition in rate limiter | `ConcurrentHashMap.compute()` is atomic; `LoginAttempt` mutation happens inside the compute lambda |
| Token reuse after logout | `ConcurrentHashMap`-based blacklist checked on every authenticated request; hourly cleanup of expired entries |

---

## GraphQL Mutations (Order Flow)

```graphql
mutation PlaceOrder($input: CreateOrderInput!) {
  createOrder(input: $input) {
    id
    status
    totalAmount
    items {
      productName
      quantity
      unitPrice
    }
  }
}

mutation UpdateStatus($orderId: Int!, $status: OrderStatus!) {
  updateOrderStatus(orderId: $orderId, status: $status)
}
```

Accessible at `/graphql`. The GraphiQL IDE is available at `/graphiql`.

---

## JMeter Stress Test Setup

Test files located in `jmeter-tests/`:

| File | Purpose |
|---|---|
| `Registration Load Test.jmx` | Full registration + login scenario |
| `registration-data.csv` | Parameterised user data (name, email, username) |
| `login.csv` | Matching usernames + `Test@123` password |
| `order.csv` | Product IDs for order placement |
| `search-terms.csv` | Keywords for product search |

**Run configuration:**
- Threads: 500
- Ramp-up: 30 s
- Duration: 120 s
- Rate limiting: disabled during stress test (re-enable for production)

---

## License

Developed for academic and demonstration purposes.
