# Roadmap — Community-Demanded Starters

This document captures **which Spring Boot starters the developer community most often
reinvents**, based on public technical discussion (blogs, Q&A, GitHub, vendor docs), and lays
out a **detailed implementation plan** for the gaps not already covered by this repo.

It is a planning artifact — nothing here is implemented yet. Each proposed starter follows the
conventions in [ARCHITECTURE.md](ARCHITECTURE.md) and [DEVELOPER.md](DEVELOPER.md).

Existing starters (already shipped): `common`, `redis`, `minio`, `aws-s3`, `rate-limiting`,
`idempotency`, `audit-log`, `feature-flags`, `llm-client`, `multitenancy`, `outbox`.

---

## 1. How demand was assessed

Signals were gathered from public, community-facing sources and weighted by three factors:

1. **Reinvention frequency** — how often the same code is written from scratch (tutorials,
   "how I built X", Q&A threads all pointing at hand-rolled solutions).
2. **Cross-cutting reach** — how many services in a typical microservice estate need it.
3. **Gap vs. built-ins** — whether Spring Boot / Spring Cloud already solves it well. A high
   score means the framework leaves an opinionated-wiring gap that teams fill themselves.

The strongest recurring themes were: **request correlation / structured logging**,
**standardized error responses (RFC 7807)**, **resilient outbound HTTP clients**, **secrets
management**, and **PII protection (masking + field encryption)**.

---

## 2. Identified candidates (prioritized)

| # | Proposed starter | Community pain point | Reinvention | Reach | Gap vs. built-ins | Priority |
|---|---|---|---|---|---|---|
| 1 | `spring-boot-starter-observability` | Correlation ID + MDC + structured JSON logs + trace propagation reimplemented per service | High | High | Medium (Micrometer Tracing exists but wiring is manual) | **P0** |
| 2 | `spring-boot-starter-problem-details` | Consistent RFC 7807 error bodies + exception→status mapping | High | High | Medium (Spring 6 has `ProblemDetail` type, not the opinionated wiring) | **P0** |
| 3 | `spring-boot-starter-resilient-client` | Declarative HTTP client with retry/circuit-breaker/timeout + logging | High | High | Medium (Resilience4j exists; bundling is manual) | **P0** |
| 4 | `spring-boot-starter-secrets` | Unified secret source (Vault / AWS SM / env) with refresh | Medium | Medium | Medium (Spring Cloud covers per-provider, not unified) | **P1** |
| 5 | `spring-boot-starter-data-privacy` | PII masking in logs + JPA field-level encryption | High | Medium | High (no first-class Spring solution) | **P1** |
| 6 | `spring-boot-starter-scheduler-lock` | Prevent duplicate `@Scheduled` runs across instances | Medium | Medium | High (ShedLock is 3rd-party; no Boot starter here) | **P1** |
| 7 | `spring-boot-starter-security-jwt` | JWT resource server + secure headers + CORS defaults | High | High | Low–Medium (Spring Security covers most; opinionated bundle still hand-rolled) | **P2** |
| 8 | `spring-boot-starter-notifications` | Pluggable email / SMS / push channels behind one API | Medium | Medium | High | **P2** |
| 9 | `spring-boot-starter-webhooks` | Outbound webhook delivery with signing + retry (pairs with `outbox`) | Medium | Medium | High | **P2** |
| 10 | `spring-boot-starter-api-keys` | Issue / validate API keys for service-to-service auth | Medium | Medium | High | **P3** |

**Honesty note on overlaps:** Spring provides raw building blocks for several of these
(`ProblemDetail`, Micrometer Tracing, Spring Cloud Vault, Spring Security OAuth2 Resource
Server). The value these starters add is **opinionated, zero-config, consistent bundling** — the
exact wiring teams currently copy between projects — not net-new capability. Where a starter is
mostly a thin convention layer (7), it is deliberately lower priority.

---

## 3. Detailed plans — P0 tier

### 3.1 `spring-boot-starter-observability`

**Goal.** One dependency gives every request a correlation ID, propagates it through logs (MDC),
outbound HTTP calls, and async boundaries, and emits structured JSON logs — all wired to
Micrometer Tracing so it interoperates with OpenTelemetry/Zipkin exporters.

**Config prefix:** `spring.observability`

| Property | Default | Purpose |
|---|---|---|
| `enabled` | `true` | Master switch |
| `correlation.header-name` | `X-Correlation-Id` | Inbound/outbound header |
| `correlation.mdc-key` | `correlationId` | MDC key used in log patterns |
| `correlation.generate-if-absent` | `true` | Create an ID when the header is missing |
| `logging.json` | `false` | Emit JSON logs (Logstash encoder) vs. pattern layout |
| `logging.include-mdc` | `true` | Add MDC entries to structured logs |
| `tracing.propagate` | `true` | Bridge correlation ID into Micrometer tracing baggage |

**Beans / auto-configuration** (`observability.config.ObservabilityAutoConfiguration`):
- `CorrelationIdFilter` (`OncePerRequestFilter`, highest precedence) — read/generate the header,
  put it in MDC, echo it on the response, clear MDC in `finally`.
- `CorrelationIdRestClientCustomizer` / `ClientHttpRequestInterceptor` — copy the current
  correlation ID onto outbound `RestClient`/`RestTemplate` calls.
- `TaskDecorator` bean — propagate MDC across `@Async` executors.
- Optional `JsonLoggingConfigurer` — activates a Logstash JSON encoder when `logging.json=true`.
- Bridge to `io.micrometer:micrometer-tracing` baggage when present (`@ConditionalOnClass`).

**Conditionals.** `@ConditionalOnWebApplication(SERVLET)` for the filter; tracing bridge gated on
`@ConditionalOnClass(Tracer.class)`; JSON logging gated on the Logstash encoder being present.

**Dependencies.** `spring-boot-starter` (SLF4J), optional `spring-web`, `micrometer-tracing`,
`logstash-logback-encoder` (all `compileOnly`/optional).

**Testing.** Unit: filter generates vs. preserves the header, MDC cleared after the chain.
Integration (`@Tag("integration")` not required — no external infra): a `@SpringBootTest` +
`MockMvc` asserting the response header and captured log MDC.

**Effort:** ~M (2–3 days). **Risk:** low. Complements `audit-log` (shares the actor/context idea).

---

### 3.2 `spring-boot-starter-problem-details`

**Goal.** Every error leaves the application as a consistent **RFC 7807** `application/problem+json`
body, including validation failures, with a stable machine-readable `code`, the request path, and
(when present) the correlation/trace ID — no per-service `@ControllerAdvice` copy-paste.

**Config prefix:** `spring.problem-details`

| Property | Default | Purpose |
|---|---|---|
| `enabled` | `true` | Master switch |
| `base-type-uri` | `about:blank` | Base for the `type` URI per problem code |
| `include-stack-trace` | `false` | Never in prod; opt-in for dev |
| `include-correlation-id` | `true` | Add the correlation ID as an extension member |
| `field-errors-key` | `errors` | Extension member holding per-field validation errors |

**Beans / auto-configuration** (`problem.config.ProblemDetailsAutoConfiguration`):
- `GlobalExceptionHandler` (`@RestControllerAdvice`, `@ConditionalOnMissingBean`) mapping:
  - `MethodArgumentNotValidException` / `ConstraintViolationException` → `400` with field errors,
  - `ResponseStatusException` → its status,
  - a pluggable `Map<Class<? extends Throwable>, HttpStatus>` for domain exceptions,
  - fallback `500` with a generic message (never leaks internals).
- `ProblemDetailFactory` — builds `ProblemDetail` (Spring 6+ type) with extension members and the
  correlation ID (pulled from MDC if `observability` is present).
- A `@ProblemMapping` annotation + registrar so consumers can declare
  `exception → status/code/title` without editing the advice.

**Conditionals.** `@ConditionalOnWebApplication`; the correlation-ID enrichment activates only when
an MDC correlation key is available (soft dependency on `observability`).

**Dependencies.** `spring-boot-starter-web`, `spring-boot-starter-validation` (`compileOnly`).

**Testing.** Unit: each exception type maps to the right status/body. Integration: `MockMvc`
asserting `application/problem+json`, `type`/`title`/`status`/`detail`/`code`, and the field-error
extension for a `@Valid` failure.

**Effort:** ~S–M (2 days). **Risk:** low. **Overlap:** uses Spring's `ProblemDetail`; adds the
opinionated mapping + extensions teams hand-roll.

---

### 3.3 `spring-boot-starter-resilient-client`

**Goal.** A pre-wired outbound HTTP client with **Resilience4j** (retry, circuit breaker, timeout,
bulkhead), request/response logging, and correlation-ID propagation — the "HTTP client every
microservice needs" without per-project glue.

**Config prefix:** `spring.resilient-client`

| Property | Default | Purpose |
|---|---|---|
| `enabled` | `true` | Master switch |
| `connect-timeout` | `2s` | Connection timeout |
| `read-timeout` | `5s` | Response timeout |
| `retry.max-attempts` | `3` | Retry attempts on transient failures |
| `retry.backoff` | `200ms` | Base backoff (exponential) |
| `circuit-breaker.failure-rate-threshold` | `50` | Open the breaker above this % |
| `circuit-breaker.wait-duration-in-open-state` | `10s` | Half-open probe delay |
| `logging.enabled` | `true` | Log method, URI, status, latency |

**Beans / auto-configuration** (`resilientclient.config.ResilientClientAutoConfiguration`):
- A configured `RestClient.Builder` (and/or `ClientHttpRequestFactory`) with the timeouts.
- Resilience4j `Retry`, `CircuitBreaker`, `TimeLimiter` registries bound to the properties.
- A `ResilientClient` façade (or `@ResilientHttpExchange` declarative interface support) that wraps
  calls in the resilience decorators and records Micrometer timers.
- Correlation-ID interceptor reused from `observability` when present.

**Conditionals.** `@ConditionalOnClass({RestClient.class, CircuitBreaker.class})`,
`@ConditionalOnMissingBean` on the façade; metrics only when a `MeterRegistry` exists.

**Dependencies.** `spring-web`, `io.github.resilience4j:resilience4j-spring-boot3` (verify the
Boot 4 compatible line), `micrometer-core` (`compileOnly`).

**Testing.** Unit: retry/circuit-breaker decorators fire on simulated failures (Resilience4j test
utilities). Integration (`@Tag("integration")`): a WireMock/Testcontainers HTTP stub returning
`503` twice then `200` to prove retry, and staying `503` to prove the breaker opens.

**Effort:** ~M–L (3–4 days). **Risk:** medium (Resilience4j × Boot 4 version compatibility).
**Overlap:** wraps Resilience4j + `RestClient`; adds the bundling and defaults.

---

## 4. Detailed plans — P1 tier

### 4.1 `spring-boot-starter-secrets`

**Goal.** A single `SecretSource` abstraction over **HashiCorp Vault**, **AWS Secrets Manager**,
and environment/file, with optional refresh, so application code never binds to a provider SDK.

**Config prefix:** `spring.secrets` (`provider: vault|aws|env`, provider sub-blocks, `refresh.enabled`,
`refresh.interval`). **API:** `SecretSource#get(String name): Optional<String>` and a typed
`@Secret("db.password")` field/param resolver. **Auto-config** picks the provider by
`@ConditionalOnProperty` + `@ConditionalOnClass` on the relevant SDK. **Testing:** LocalStack
(Secrets Manager) and a Vault Testcontainer for integration; a `MapSecretSource` for unit tests.
**Overlap:** Spring Cloud Vault / Spring Cloud AWS exist per-provider — value is the unified,
swappable interface. **Effort:** ~M–L.

### 4.2 `spring-boot-starter-data-privacy`

**Goal.** First-class **PII protection**: annotation-driven **log masking** and **JPA field-level
encryption**.

- **Masking:** `@Masked(strategy = EMAIL|CREDIT_CARD|FULL)` on DTO fields + a Jackson module /
  Logback converter that masks values in serialized output and logs.
- **Encryption:** a reusable JPA `AttributeConverter` (`@Encrypted`) using AES-GCM with a key from
  `spring-boot-starter-secrets` (or a configured key), plus an optional blind-index column for
  equality search.

**Config prefix:** `spring.data-privacy` (`encryption.key`, `encryption.algorithm`,
`masking.enabled`). **Testing:** unit tests per masking strategy; JPA slice test (`@DataJpaTest` +
H2/Postgres Testcontainer) proving round-trip encryption and that ciphertext is at rest.
**Overlap:** none first-class in Spring — high value. **Effort:** ~M–L. **Depends on:** `common`,
optionally `secrets`.

### 4.3 `spring-boot-starter-scheduler-lock`

**Goal.** Guarantee a `@Scheduled` job runs on **one instance only**, cluster-wide.

**Config prefix:** `spring.scheduler-lock` (`provider: redis|jdbc`, `default-lock-at-most-for`).
**API:** `@SchedulerLock(name, lockAtMostFor, lockAtLeastFor)` + an AOP aspect acquiring a lock via
Redis (`SET NX PX` — can reuse `spring-boot-starter-redis`'s lock util) or a JDBC lock table.
**Testing:** two application contexts against one Redis Testcontainer, asserting only one executes.
**Overlap:** ShedLock is the de-facto library; this is a Boot-native, Redis-lock-reusing alternative
consistent with the repo. **Effort:** ~M.

---

## 5. P2 / P3 sketches

- **`security-jwt`** — `@ConditionalOnClass` OAuth2 Resource Server; opinionated `SecurityFilterChain`
  with JWT validation, secure headers (HSTS, X-Content-Type-Options, frame options), and CORS from
  `spring.security-jwt.*`. Mostly convention over Spring Security. **Effort:** ~M.
- **`notifications`** — `NotificationSender` API with pluggable `email` (JavaMailSender), `sms`
  (Twilio/SNS), `push` channels; template resolution; async send. **Effort:** ~L.
- **`webhooks`** — outbound webhook registry + signed (HMAC) delivery with retry/backoff and a
  dead-letter; pairs naturally with `outbox`. **Effort:** ~L.
- **`api-keys`** — issue/rotate/validate hashed API keys, a filter for `X-Api-Key`, and per-key rate
  limits (reuse `rate-limiting`). **Effort:** ~M.

---

## 6. Cross-cutting work & sequencing

**Shared enablers first.** Land `observability` early — `problem-details`, `resilient-client`,
`audit-log`, and `data-privacy` all benefit from a correlation ID in MDC. Consider promoting a small
`CorrelationContext` accessor into `spring-boot-starter-common` so downstream starters read it
without a hard dependency.

**Per-starter definition of done** (mirrors [DEVELOPER.md](DEVELOPER.md)):
1. Module + `settings.gradle.kts` entry; `api(project(":spring-boot-starter-common"))`.
2. `@AutoConfiguration` + `AutoConfiguration.imports` registration (never `spring.factories`).
3. `@ConfigurationProperties` with documented defaults and `@EnableConfigurationProperties`.
4. `@ConditionalOnMissingBean` on every default; health/metrics conditional on actuator/Micrometer.
5. Unit tests for every public class; `@Tag("integration")` + `@EnabledIfDockerAvailable` where real
   infra is involved; clean up in `@AfterEach`.
6. Javadoc passing `-Xdoclint:all`; `spotlessApply`.
7. Module `README.md` + a runnable app under `examples/`.
8. Row in the root [README](README.md) table + `CHANGELOG.md` entry.

**Suggested delivery order:**
`observability` → `problem-details` → `resilient-client` → `data-privacy` → `scheduler-lock` →
`secrets` → (`security-jwt`, `webhooks`, `notifications`, `api-keys`).

**CI/versioning.** Each new module starts at `1.0.0`, is added to both CI jobs' scopes where it has
integration tests, and follows the existing BOM-platform build. No changes to the release workflow
are required.

---

## 7. Sources

Community signal was drawn from the following public pages:

- [Modularizing Spring Boot — spring.io](https://spring.io/blog/2025/10/28/modularizing-spring-boot/)
- [How to Build Custom Spring Boot Starters — OneUptime](https://oneuptime.com/blog/post/2026-01-30-spring-boot-custom-starters/view)
- [Spring Boot Beyond the Basics: Custom Starters — Java Code Geeks](https://www.javacodegeeks.com/2025/10/spring-boot-beyond-the-basics-custom-starters-and-performance-tuning.html)
- [Intro to Spring Boot Starters — Baeldung](https://www.baeldung.com/spring-boot-starters)
- [Correlation ID for Logging in Microservices — DZone](https://dzone.com/articles/correlation-id-for-logging-in-microservices)
- [Logging with Request Correlation using MDC — Java By Examples](https://www.javabyexamples.com/logging-with-request-correlation-using-mdc)
- [The Exception Handling Pattern 99% of Java Developers Get Wrong (RFC 7807) — Medium](https://medium.com/@martinastaberger/the-exception-handling-pattern-99-of-java-developers-get-wrong-and-how-senior-engineers-use-rfc-3fb92680ceee)
- [Tracing — Spring Boot Reference](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [Guide to Resilience4j With Spring Boot — Baeldung](https://www.baeldung.com/spring-boot-resilience4j)
- [Microservice: Circuit Breaker, Retry, and Rate Limiter with Resilience4J — Coding Shuttle](https://www.codingshuttle.com/spring-boot-handbook/microservice-circuit-breaker-retry-and-rate-limiter-with-resilience4-j/)
- [Integrate AWS Secrets Manager in Spring Boot — Baeldung](https://www.baeldung.com/spring-boot-integrate-aws-secrets-manager)
- [Managing Secrets in Spring Boot Using Vault or AWS Secrets Manager](https://harshad-sonawane.com/blog/spring-boot-secrets-management-vault-aws/)
- [OpenTelemetry with Spring Boot — spring.io](https://spring.io/blog/2025/11/18/opentelemetry-with-spring-boot/)
- [OpenTelemetry Setup in Spring Boot — Baeldung](https://www.baeldung.com/spring-boot-opentelemetry-setup)
- [Shielding sensitive information: PII masking with Spring Boot — Opcito](https://www.opcito.com/blogs/shielding-sensitive-information-pii-masking-with-spring-boot)
- [Protecting PII with Field-Level Encryption in Spring Boot — Medium](https://medium.com/@vs98990/protecting-pii-with-field-level-encryption-in-spring-boot-dd566fb4183c)
- [Mask Sensitive Data in Logs With Logback — Baeldung](https://www.baeldung.com/logback-mask-sensitive-data)
- [GDPR — Encryption, Masking and Logging using Spring Boot AOP — Medium](https://har-d.medium.com/easy-implementation-of-gdpr-with-aspect-oriented-programming-27e96f47767d)
- [multi-tenant-springboot-starter — GitHub](https://github.com/rahul-s-bhatt/multi-tenant-springboot-starter)
- [ultimate-backend (multi-tenant SaaS, CQRS) — GitHub](https://github.com/juicycleff/ultimate-backend)
