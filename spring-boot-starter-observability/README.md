# spring-boot-starter-observability

Request **correlation IDs**, **MDC log propagation**, and **async context propagation** for Spring
Boot services — one dependency, zero boilerplate.

Every inbound request gets a correlation ID (read from a header or generated), bound to the SLF4J
MDC for the duration of the request, and echoed back on the response. The ID can be propagated to
async executor threads and to outbound HTTP calls so a single request is traceable end-to-end.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-observability:1.0.0'
}
```

---

## What you get

| Bean | Condition | Purpose |
|---|---|---|
| `CorrelationIdFilter` (registered) | servlet web app | Reads/generates the correlation ID, binds it to MDC, echoes it on the response |
| `MdcTaskDecorator` | always | Propagates MDC (correlation ID) to `@Async` / executor threads |
| `CorrelationIdClientHttpRequestInterceptor` | provided utility | Opt-in: propagate the ID onto outbound `RestClient` / `RestTemplate` calls |

All beans are `@ConditionalOnMissingBean`; declare your own to override.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.observability.enabled` | `true` | Master switch |
| `spring.observability.correlation.header-name` | `X-Correlation-Id` | Inbound/outbound header |
| `spring.observability.correlation.mdc-key` | `correlationId` | MDC key for log patterns |
| `spring.observability.correlation.generate-if-absent` | `true` | Generate an ID when the header is missing |

### Show the correlation ID in logs

Add the MDC key to your Logback pattern:

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level [%X{correlationId}] %logger{36} - %msg%n</pattern>
```

---

## Usage

### Reading the current correlation ID

```java
import io.github.saumilp.starters.observability.correlation.CorrelationContext;

CorrelationContext.getCorrelationId().ifPresent(id -> ...);
```

### Propagate to async threads

Register the decorator on your executor:

```java
@Bean
ThreadPoolTaskExecutor appExecutor(MdcTaskDecorator decorator) {
    var executor = new ThreadPoolTaskExecutor();
    executor.setTaskDecorator(decorator);
    executor.initialize();
    return executor;
}
```

### Propagate to outbound HTTP calls

```java
@Bean
RestClient downstreamClient(RestClient.Builder builder, ObservabilityProperties props) {
    return builder
        .requestInterceptor(new CorrelationIdClientHttpRequestInterceptor(
            props.getCorrelation().getHeaderName()))
        .build();
}
```

---

## Notes

- The filter runs at `Ordered.HIGHEST_PRECEDENCE` so the ID is available to all downstream filters
  and handlers.
- The MDC entry is always cleared in a `finally` block, so IDs never leak across pooled threads.
- Structured JSON logging and native Micrometer Tracing baggage bridging are planned for a future
  release; the correlation ID already works with any Micrometer/OpenTelemetry setup via the MDC.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
