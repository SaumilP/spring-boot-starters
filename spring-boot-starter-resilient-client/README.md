# spring-boot-starter-resilient-client

A resilient outbound HTTP client for Spring Boot — **retry**, **circuit breaker**, and **timeouts**
pre-wired with [Resilience4j](https://resilience4j.readme.io/), so you stop copying the same client
glue into every service.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-resilient-client:1.0.0'
}
```

---

## What you get

| Bean | Condition | Purpose |
|---|---|---|
| `Retry` (`resilientClientRetry`) | always | Shared retry policy (max attempts + fixed backoff) |
| `CircuitBreaker` (`resilientClientCircuitBreaker`) | always | Shared count-based circuit breaker |
| `ResilientClient` | always | Façade that runs any call through the breaker + retry |
| `RestClient` (`resilientRestClient`) | `RestClient` on classpath | `RestClient` pre-configured with connect/read timeouts |

All beans are `@ConditionalOnMissingBean`; declare your own to override.

---

## Usage

```java
@Service
class CatalogClient {

    private final ResilientClient resilient;
    private final RestClient restClient;

    CatalogClient(ResilientClient resilient,
                  @Qualifier("resilientRestClient") RestClient restClient) {
        this.resilient = resilient;
        this.restClient = restClient;
    }

    Product fetch(String id) {
        return resilient.execute(() ->
            restClient.get().uri("https://catalog/api/products/{id}", id)
                .retrieve().body(Product.class));
    }
}
```

The circuit breaker records each attempt (it is the inner decorator); the retry policy re-invokes on
failure (the outer decorator). When the breaker is open, `execute` throws
`CallNotPermittedException`.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.resilient-client.enabled` | `true` | Master switch |
| `spring.resilient-client.connect-timeout` | `2s` | Connection establishment timeout |
| `spring.resilient-client.read-timeout` | `5s` | Response read timeout |
| `spring.resilient-client.retry.max-attempts` | `3` | Attempts including the initial call |
| `spring.resilient-client.retry.backoff` | `200ms` | Fixed wait between attempts |
| `spring.resilient-client.circuit-breaker.failure-rate-threshold` | `50` | Failure % that opens the breaker |
| `spring.resilient-client.circuit-breaker.wait-duration-in-open-state` | `10s` | Time the breaker stays open |
| `spring.resilient-client.circuit-breaker.sliding-window-size` | `10` | Count-based window size |
| `spring.resilient-client.circuit-breaker.minimum-number-of-calls` | `10` | Calls before the rate is evaluated |

---

## Notes

- Uses Resilience4j **core** modules directly (no Spring-Boot-version-coupled integration module),
  so it tracks the Spring Boot 4 baseline cleanly.
- Micrometer metrics and exponential (jittered) backoff are planned enhancements; v1 ships fixed
  backoff and the core resilience decorators.
- Pairs naturally with [`spring-boot-starter-observability`](../spring-boot-starter-observability/README.md):
  add the correlation interceptor to `resilientRestClient` to propagate the correlation ID downstream.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
