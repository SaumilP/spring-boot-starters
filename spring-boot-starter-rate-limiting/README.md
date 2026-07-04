# spring-boot-starter-rate-limiting

Plug-and-play annotation-driven rate limiting for Spring Boot applications. Protect any method or
controller endpoint with a single `@RateLimit` annotation — the starter handles sliding-window
counting, Redis-backed distributed enforcement, and HTTP 429 response formatting automatically.

---

## Features

- **`@RateLimit` annotation** — place on any Spring-managed method or controller class
- **Sliding window algorithm** — accurate per-key request counting with automatic expiry
- **Redis backend** — atomic Lua script ensures correctness across multiple application instances
- **In-memory fallback** — automatically used when Redis is not on the classpath, or for testing
- **Named limits** — centralise limit values in `application.yml` and reference by name
- **HTTP 429 responses** — structured JSON body with a `Retry-After` header
- **Fail-open on Redis error** — requests are allowed through during Redis outages to avoid cascading failures
- **Fully replaceable** — override any auto-configured bean by declaring your own

---

## Requirements

| Requirement        | Version            |
|--------------------|--------------------|
| Java               | 21 or later        |
| Spring Boot        | 4.0.4 or later     |
| Redis (optional)   | 6.x or later       |
| Spring AOP         | included via Boot  |

---

## Installation

**Gradle:**

```groovy
dependencies {
    implementation 'org.sandcastle:spring-boot-starter-rate-limiting:1.0.0'
}
```

**Maven:**

```xml
<dependency>
    <groupId>org.sandcastle</groupId>
    <artifactId>spring-boot-starter-rate-limiting</artifactId>
    <version>1.0.0</version>
</dependency>
```

No additional configuration is required for basic use. If Redis is configured in your application,
the starter automatically uses it for distributed rate limiting.

---

## Configuration

All properties are under the `spring.rate-limit` namespace.

| Property                                        | Type      | Default | Description                                                                 |
|-------------------------------------------------|-----------|---------|-----------------------------------------------------------------------------|
| `spring.rate-limit.enabled`                     | `boolean` | `true`  | Master switch — set to `false` to disable rate limiting entirely.           |
| `spring.rate-limit.default-requests`            | `int`     | `60`    | Default maximum requests per window when no annotation override is present. |
| `spring.rate-limit.default-window-seconds`      | `long`    | `60`    | Default window duration in seconds.                                         |
| `spring.rate-limit.key-prefix`                  | `String`  | `"rl:"` | Prefix prepended to all Redis rate-limit keys.                              |
| `spring.rate-limit.named-limits.<name>.requests`       | `int`  | `60`   | Maximum requests for the named limit.                                       |
| `spring.rate-limit.named-limits.<name>.window-seconds` | `long` | `60`   | Window duration in seconds for the named limit.                             |

### Example `application.yml`

```yaml
spring:
  redis:
    host: localhost
    port: 6379

  rate-limit:
    enabled: true
    default-requests: 100
    default-window-seconds: 60
    key-prefix: "rl:"
    named-limits:
      # Strict limit for login attempts — 5 per 15 minutes
      login:
        requests: 5
        window-seconds: 900
      # Conservative limit for data exports — 10 per hour
      export:
        requests: 10
        window-seconds: 3600
      # Standard API limit — 200 per minute
      api-standard:
        requests: 200
        window-seconds: 60
```

---

## Usage

### Method-level annotation

Apply `@RateLimit` directly on a controller method. The default key is composed of the caller's
remote IP address and the fully-qualified method name.

```java
@RestController
@RequestMapping("/api")
public class SearchController {

    // 60 requests per minute per caller IP + method (default)
    @RateLimit
    @GetMapping("/search")
    public List<Result> search(@RequestParam String q) {
        return searchService.find(q);
    }

    // Custom: 10 requests per hour
    @RateLimit(requests = 10, per = TimeUnit.HOURS)
    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody ExportRequest request) {
        return exportService.generate(request);
    }
}
```

### Class-level annotation (applies to all methods)

```java
@RateLimit(requests = 30, per = TimeUnit.MINUTES)
@RestController
@RequestMapping("/internal")
public class InternalController {

    @GetMapping("/status")
    public Map<String, Object> status() { ... }

    @GetMapping("/metrics")
    public Map<String, Object> metrics() { ... }
}
```

### Named limits (centralised configuration)

Reference a named limit defined in `application.yml` to avoid scattering numeric values across
annotations. Named limits override the annotation's `requests` and `per` values.

```java
@RateLimit(name = "login")
@PostMapping("/auth/login")
public TokenResponse login(@RequestBody LoginRequest request) {
    return authService.authenticate(request);
}

@RateLimit(name = "export")
@PostMapping("/reports/export")
public ResponseEntity<byte[]> exportReport(@RequestBody ReportRequest request) {
    return reportService.export(request);
}
```

### Custom key expression

Provide a literal key string to override automatic key resolution. The key is appended to the
configured `spring.rate-limit.key-prefix`.

```java
// Rate-limit per user ID extracted from the request header
@RateLimit(requests = 50, per = TimeUnit.MINUTES, key = "user-api-calls")
@GetMapping("/data")
public DataResponse fetchData(@RequestHeader("X-User-Id") String userId) {
    return dataService.fetch(userId);
}
```

---

## Observability

### HTTP 429 Response

When the rate limit is exceeded, the bundled `RateLimitExceptionHandler` returns:

**Status:** `429 Too Many Requests`

**Headers:**
```
Retry-After: 60
Content-Type: application/json
```

**Body:**
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded for key 'rl:192.168.1.1:com.example.SearchController.search': max 60 requests per 60 seconds.",
  "retryAfterSeconds": 60,
  "timestamp": "2024-07-04T10:15:30.123456Z"
}
```

### Custom exception handling

To override the default 429 response format, declare your own `@ControllerAdvice` that handles
`RateLimitExceededException`:

```java
@RestControllerAdvice
public class MyExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimit(RateLimitExceededException ex) {
        ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        detail.setDetail(ex.getMessage());
        detail.setProperty("retryAfter", ex.getWindowSeconds());
        return detail;
    }
}
```

### Replacing the rate limiter

Declare your own `RateLimiter` bean to use a different backend (e.g., Bucket4j, Resilience4j):

```java
@Bean
public RateLimiter myCustomRateLimiter() {
    return (key, maxRequests, windowSeconds) -> myBackend.tryAcquire(key);
}
```

---

## Backends

| Backend              | When active                                        | Distributed? |
|----------------------|----------------------------------------------------|--------------|
| `RedisTokenBucketRateLimiter` | `spring-boot-starter-data-redis` on classpath | Yes          |
| `InMemorySlidingWindowRateLimiter` | Fallback when Redis is absent          | No (JVM-local) |

The Redis implementation uses an atomic Lua script (sorted-set sliding window) to prevent
race conditions under concurrent requests from multiple application instances. On Redis failure,
the limiter fails open — requests are allowed through — to prevent outages from cascading into
application downtime.

---

## Supported Versions

| Starter Version | Spring Boot | Java |
|-----------------|-------------|------|
| 1.0.0           | 4.0.x       | 21+  |

---

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines on code style, JavaDoc requirements,
test conventions, and how to add a new starter to this mono-repo.
