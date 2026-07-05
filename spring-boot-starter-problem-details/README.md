# spring-boot-starter-problem-details

Consistent **RFC 7807** (`application/problem+json`) error responses for Spring MVC applications —
one dependency, no per-service `@ControllerAdvice` copy-paste.

Every unhandled exception leaves the application as a structured problem document with a stable
machine-readable `code`, a `timestamp`, the request path, per-field validation errors, and (when
present) the correlation ID.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-problem-details:1.0.0'
}
```

---

## What you get

A `GlobalExceptionHandler` (`@RestControllerAdvice`) is auto-registered in servlet web apps. It maps:

| Exception | Status | `code` |
|---|---|---|
| `MethodArgumentNotValidException` (bean validation on `@RequestBody`) | `400` | `validation-error` (with a `errors` list) |
| `ResponseStatusException` | its status | `request-error` |
| anything else | `500` | `internal-error` |

The bean is `@ConditionalOnMissingBean` — declare your own `GlobalExceptionHandler` (or advice) to
override it.

### Example response

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/create",
  "code": "validation-error",
  "timestamp": "2026-07-05T12:00:00Z",
  "correlationId": "8f1c...",
  "errors": [ { "field": "name", "message": "must not be blank" } ]
}
```

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.problem-details.enabled` | `true` | Master switch |
| `spring.problem-details.base-type-uri` | `about:blank` | Base for the `type` URI; the problem code is appended when set |
| `spring.problem-details.include-stack-trace` | `false` | Add a `trace` member on `500` (development only) |
| `spring.problem-details.include-correlation-id` | `true` | Add the correlation ID (read from the MDC) |
| `spring.problem-details.correlation-mdc-key` | `correlationId` | MDC key read for the correlation ID |
| `spring.problem-details.field-errors-key` | `errors` | Extension member holding validation errors |

---

## Works with observability

When [`spring-boot-starter-observability`](../spring-boot-starter-observability/README.md) is also on
the classpath, the correlation ID it binds to the MDC is automatically included in every problem
response (the default MDC keys align), giving clients and logs a shared trace identifier.

---

## Notes

- Built on Spring's native `ProblemDetail` type; this starter adds the opinionated exception→status
  mapping, extension members, and validation-error formatting.
- The catch-all handler logs the exception at `ERROR` and never exposes internals in the response
  body unless `include-stack-trace` is explicitly enabled.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
