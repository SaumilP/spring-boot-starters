# spring-boot-starter-idempotency

A production-ready Spring Boot starter that transparently enforces HTTP idempotency for mutating endpoints using the `Idempotency-Key` request header and a Redis-backed response cache — preventing duplicate charges, duplicate records, and duplicate side-effects without any changes to your controllers.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.saumilp.starters/spring-boot-starter-idempotency.svg)](https://central.sonatype.com/search?q=io.github.saumilp.starters)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)](https://spring.io/projects/spring-boot)

---

## The Problem

In distributed systems, network failures cause clients to retry requests — but without server-side de-duplication, a retried `POST /orders` creates two orders, and a retried `POST /payments` charges a customer twice. RFC 8725 describes `Idempotency-Key` as the standard solution: the client generates a unique key per logical operation and sends it with every retry. The server uses the key to detect and replay the original response.

This starter implements the full server-side mechanism — locking, caching, concurrent-request handling — as a Servlet filter, so your controllers remain pure and unchanged.

---

## Features

- **Zero-boilerplate idempotency** — add the dependency; existing controllers are protected automatically
- **Redis-backed response cache** — responses survive restarts and are shared across instances
- **Atomic concurrent-request handling** — `SET NX PX` locks prevent duplicate handler executions; concurrent duplicates receive HTTP `409 Conflict`
- **First-writer-wins put semantics** — no overwrite race conditions
- **Configurable scope** — apply only to `POST` and `PATCH` by default; extend to any method
- **Replay header** — cached responses include `X-Idempotency-Replayed: true` for observability
- **Override-friendly** — replace `IdempotencyStore` with your own implementation (e.g., database-backed)
- **Spring Boot auto-configuration** — no XML, no `@EnableSomething`

---

## Requirements

| Component      | Version  |
|----------------|----------|
| Java           | 21+      |
| Spring Boot    | 4.0.4+   |
| Redis          | 7.0+     |

---

## Installation

### Gradle

```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-idempotency:1.0.0'
```

### Maven

```xml
<dependency>
  <groupId>io.github.saumilp.starters</groupId>
  <artifactId>spring-boot-starter-idempotency</artifactId>
  <version>1.0.0</version>
</dependency>
```

No additional configuration is required. The filter registers automatically on startup.

---

## Configuration

All properties are bound from the `spring.idempotency.*` namespace.

| Property                              | Type           | Default           | Description                                                                 |
|---------------------------------------|----------------|-------------------|-----------------------------------------------------------------------------|
| `spring.idempotency.enabled`          | `boolean`      | `true`            | Master switch. Set to `false` to disable the filter entirely.               |
| `spring.idempotency.header-name`      | `String`       | `Idempotency-Key` | HTTP request header that carries the idempotency key.                       |
| `spring.idempotency.ttl-seconds`      | `long`         | `86400`           | How long (in seconds) a cached response is retained. Default: 24 hours.     |
| `spring.idempotency.lock-timeout-seconds` | `long`     | `30`              | How long (in seconds) to hold the in-progress lock before auto-expiry.      |
| `spring.idempotency.key-prefix`       | `String`       | `idm:`            | Redis key namespace prefix applied to all idempotency entries.              |
| `spring.idempotency.applicable-methods` | `List<String>` | `[POST, PATCH]` | HTTP methods subject to idempotency enforcement.                            |

### application.yml

```yaml
spring:
  idempotency:
    enabled: true
    header-name: Idempotency-Key      # standard header per Stripe/Shopify convention
    ttl-seconds: 86400                 # 24 h — matches typical client retry windows
    lock-timeout-seconds: 30           # auto-release lock if handler hangs
    key-prefix: "idm:"                 # namespaces keys in a shared Redis instance
    applicable-methods:
      - POST
      - PATCH
      # Add DELETE if your API uses it for non-idempotent deletions
```

---

## Usage

### Sending an idempotent request

Include the `Idempotency-Key` header with a UUID v4 generated client-side. On retry, send the **same key** — the server will replay the original response without re-executing the handler.

**curl:**
```bash
KEY=$(uuidgen)

# First request — handler executes, response cached
curl -X POST https://api.example.com/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $KEY" \
  -d '{"productId": "sku-123", "quantity": 1}'
# → HTTP 201 Created  {"orderId": "ord-789", ...}

# Retry with same key — cached response replayed instantly
curl -X POST https://api.example.com/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: $KEY" \
  -d '{"productId": "sku-123", "quantity": 1}'
# → HTTP 201 Created  {"orderId": "ord-789", ...}
#   X-Idempotency-Replayed: true
```

**Java RestClient:**
```java
String idempotencyKey = UUID.randomUUID().toString();

ResponseEntity<OrderResponse> response = restClient.post()
    .uri("/orders")
    .header("Idempotency-Key", idempotencyKey)
    .body(orderRequest)
    .retrieve()
    .toEntity(OrderResponse.class);
```

### Replayed response

Cached responses include an additional header to distinguish them from fresh executions:

```http
HTTP/1.1 201 Created
Content-Type: application/json
X-Idempotency-Replayed: true

{"orderId": "ord-789", "status": "PENDING"}
```

### Concurrent duplicate — 409 Conflict

If two requests arrive simultaneously with the same key and neither is yet cached, the second request receives:

```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "status": 409,
  "error": "Conflict",
  "message": "A request with this Idempotency-Key is already in progress."
}
```

The client should retry after a short delay. Once the first request completes, the next attempt with the same key will receive the cached response.

---

## How It Works

```
Client                  Filter                      Redis         Handler
  │                       │                            │               │
  │──POST /orders ─────-─►│                            │               │
  │  Idempotency-Key: K   │                            │               │
  │                       │──GET idm:K ───────────────►│               │
  │                       │◄── (nil) ─────────────-────│               │
  │                       │──SET NX idm:K:lock ───────►│               │
  │                       │◄── OK (lock acquired) ─────│               │
  │                       │───────────────────────-───────────────────►│
  │                       │◄───────────────────────-──── 201 Created ──│
  │                       │──SET NX idm:K (ttl=24h) ──►│               │
  │                       │──DEL idm:K:lock ──────────►│               │
  │◄──201 Created ────────│                            │               │
  │──POST /orders ────-──►│  (retry with same key)     │               │
  │  Idempotency-Key: K   │──GET idm:K ───────────────►│               │
  │                       │◄── {"orderId":"ord-789"} ──│               │
  │◄──201 + Replayed ─────│                            │               │
```

1. **Cache check** — if the key is cached, replay immediately
2. **Lock acquisition** — atomic `SET NX PX` in Redis; 409 if already held
3. **Execute** — request passes to the handler; response is captured
4. **Cache + unlock** — response stored with TTL; lock released in `finally`

---

## Overriding Beans

### Custom IdempotencyStore

To use a database-backed store or add custom logic, declare your own `IdempotencyStore` bean:

```java
@Configuration
public class MyIdempotencyConfig {

    @Bean
    public IdempotencyStore databaseIdempotencyStore(JdbcTemplate jdbc) {
        return new DatabaseIdempotencyStore(jdbc);
    }
}
```

The starter's `@ConditionalOnMissingBean(IdempotencyStore.class)` ensures the auto-configured Redis store is skipped when your bean is present.

### Disabling for specific paths

Register the filter with a custom URL pattern instead of `/*`:

```java
@Bean
public FilterRegistrationBean<IdempotencyFilter> idempotencyFilter(
        IdempotencyStore store, IdempotencyProperties props) {
    FilterRegistrationBean<IdempotencyFilter> reg = new FilterRegistrationBean<>();
    reg.setFilter(new IdempotencyFilter(store, props));
    reg.addUrlPatterns("/api/payments/*", "/api/orders/*");
    reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    return reg;
}
```

---

## Supported Versions

| Starter Version | Spring Boot | Java | Redis |
|-----------------|-------------|------|-------|
| 1.0.0           | 4.0.4+      | 21+  | 7.0+  |

---

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for coding standards, Javadoc requirements, and the new-starter checklist.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).

---

[GitHub](https://github.com/SaumilP/spring-boot-starters) · [Issues](https://github.com/SaumilP/spring-boot-starters/issues)
