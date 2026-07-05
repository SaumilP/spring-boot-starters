# spring-boot-starter-redis

A Spring Boot starter that provides Redis utilities, distributed locking, Spring Cache
integration, a PING-based health indicator, and Micrometer operation metrics — all wired
automatically when Redis is on the classpath.

[![Maven Central](https://img.shields.io/maven-central/v/org.sandcastle.starter-apps/spring-boot-starter-redis.svg)](https://central.sonatype.com/artifact/org.sandcastle.starter-apps/spring-boot-starter-redis)

---

## Contents

- [Installation](#installation)
- [Configuration Properties](#configuration-properties)
- [What Gets Auto-Configured](#what-gets-auto-configured)
- [RedisUtil — High-Level Operations](#redisutil--high-level-operations)
- [RedisLockUtil — Distributed Locking](#redislockutil--distributed-locking)
- [Spring Cache Integration](#spring-cache-integration)
- [Health Indicator](#health-indicator)
- [Micrometer Metrics](#micrometer-metrics)
- [Overriding Defaults](#overriding-defaults)
- [Requirements](#requirements)

---

## Installation

```groovy
// Gradle
dependencies {
    implementation 'org.sandcastle.starter-apps:spring-boot-starter-redis:1.0.0'

    // Optional — enables health indicator
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    // Optional — enables Micrometer metrics on RedisUtil operations
    implementation 'io.micrometer:micrometer-core'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
}
```

```xml
<!-- Maven -->
<dependency>
    <groupId>org.sandcastle.starter-apps</groupId>
    <artifactId>spring-boot-starter-redis</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Configuration Properties

All properties live under the `spring.redis` prefix.

| Property | Default | Description |
|---|---|---|
| `spring.redis.host` | `localhost` | Redis server hostname |
| `spring.redis.port` | `6379` | Redis server port |
| `spring.redis.metric-name` | `redis.operations` | Micrometer timer metric name |
| `spring.redis.cache-ttl-days` | `1` | Default TTL in days for Spring Cache entries |

**Example `application.yml`:**

```yaml
spring:
  redis:
    host: redis.internal.example.com
    port: 6379
    metric-name: myapp.redis.ops
    cache-ttl-days: 7
```

---

## What Gets Auto-Configured

The starter registers the following beans when `RedisConnectionFactory` is present
on the classpath. Every bean is annotated `@ConditionalOnMissingBean` so you can
replace any of them by declaring your own.

| Bean | Type | Description |
|---|---|---|
| `cachingConfigurer` | `CachingConfigurer` | Structured cache key generator (`class:method:params`) |
| `cacheManager` | `CacheManager` | `RedisCacheManager` with configurable TTL |
| `redisTemplate` | `RedisTemplate<String, Object>` | Primary template — Jackson JSON values, String keys |
| `stringRedisTemplate` | `StringRedisTemplate` | Plain string operations |
| `functionDomainRedisTemplate` | `RedisTemplate<String, Object>` | Domain object template with transaction support |
| `redisUtils` | `RedisUtil` | High-level Redis utility |
| `redisLockUtil` | `RedisLockUtil` | Distributed lock (atomic Lua script) |
| `redisCustomHealthIndicator` | `HealthIndicator` | PING-based health check (requires actuator) |
| `redisMetricsAspect` | `RedisMetricsAspect` | AOP timer for `RedisUtil` methods (requires Micrometer + AOP) |

---

## RedisUtil — High-Level Operations

`RedisUtil` wraps the most common Redis data-structure operations behind a clean, type-safe API.

```java
@Service
public class ProductCacheService {

    private final RedisUtil redisUtil;

    public ProductCacheService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void cacheProduct(String id, Product product) {
        // Store with a 10-minute TTL
        redisUtil.set("product:" + id, product, Duration.ofMinutes(10));
    }

    public Product getProduct(String id) {
        return redisUtil.get("product:" + id);
    }

    public void invalidate(String id) {
        redisUtil.del("product:" + id);
    }
}
```

Key methods on `RedisUtil`:

| Method | Description |
|---|---|
| `set(key, value)` | Store a value with no expiry |
| `set(key, value, seconds)` | Store with TTL in seconds |
| `set(key, value, Duration)` | Store with TTL as `java.time.Duration` |
| `get(key)` | Retrieve a value (generic — cast is inferred) |
| `del(key...)` | Delete one or more keys |
| `hasKey(key)` | Returns `true` if the key exists |
| `expire(key, seconds)` | Update TTL on an existing key |
| `getExpire(key)` | Remaining TTL in seconds |

---

## RedisLockUtil — Distributed Locking

`RedisLockUtil` provides a distributed mutex backed by an atomic Lua script that ensures
lock release can only be performed by the thread that acquired it.

```java
@Service
public class OrderProcessor {

    private final RedisLockUtil lockUtil;

    public OrderProcessor(RedisLockUtil lockUtil) {
        this.lockUtil = lockUtil;
    }

    public void processOrder(String orderId) {
        String lockKey = "lock:order:" + orderId;
        String requestId = UUID.randomUUID().toString();

        boolean acquired = lockUtil.tryLock(lockKey, requestId, 30L); // 30 second TTL
        if (!acquired) {
            throw new IllegalStateException("Order " + orderId + " is already being processed");
        }
        try {
            // critical section — only one node can be here at a time
            doProcess(orderId);
        } finally {
            lockUtil.releaseLock(lockKey, requestId); // Lua script ensures only the owner can release
        }
    }
}
```

The Lua script used for release:

```lua
if redis.call('get', KEYS[1]) == ARGV[1] then
    return redis.call('del', KEYS[1])
else
    return 0
end
```

This prevents a slow thread from releasing a lock it no longer holds due to TTL expiry.

---

## Spring Cache Integration

The starter wires a `RedisCacheManager` as the default Spring `CacheManager`. Use standard
Spring Cache annotations in your services:

```java
@Service
public class UserService {

    @Cacheable("users")
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

Cache TTL defaults to 1 day and is configurable via `spring.redis.cache-ttl-days`.
The `RedisKeyGenerator` builds structured keys in the format `ClassName:methodName:param1:param2:`.

---

## Health Indicator

When `spring-boot-starter-actuator` is on the classpath, the starter registers a health
indicator that issues a `PING` to Redis on every `/actuator/health` call.

```json
{
  "status": "UP",
  "components": {
    "redisCustom": {
      "status": "UP",
      "details": {
        "component": "redis",
        "response": "PONG"
      }
    }
  }
}
```

**Disable the indicator:**

```yaml
management:
  health:
    redis-custom:
      enabled: false
```

---

## Micrometer Metrics

When `micrometer-core` and `spring-aop` are on the classpath, every `RedisUtil` method call
is timed and recorded as a Micrometer `Timer`. Metrics are tagged with `operation` (method
name) and `status` (`success` or `error`).

**Default metric name:** `redis.operations`

Sample Prometheus output:

```
redis_operations_seconds_count{operation="set",status="success"} 142.0
redis_operations_seconds_sum{operation="set",status="success"}   0.341
redis_operations_seconds_count{operation="get",status="success"} 289.0
```

Override the metric name:

```yaml
spring:
  redis:
    metric-name: myapp.redis.ops
```

---

## Overriding Defaults

Every auto-configured bean is overridable. For example, to use your own `CacheManager`:

```java
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // Your custom configuration — the starter's CacheManager won't be registered
        return RedisCacheManager.builder(factory)
            .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(2)))
            .build();
    }
}
```

---

## Requirements

- Java 21 or later
- Spring Boot 4.0.4 or later
- A running Redis 6.x or later instance

For local development, use the provided Docker Compose file in the `examples/` directory:

```bash
cd examples/redis-example
docker-compose up -d
```
