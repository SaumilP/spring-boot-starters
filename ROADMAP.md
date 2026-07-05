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
| 8 | `spring-boot-starter-notifications` | Core SPI — pluggable email / SMS / push channels behind one API | Medium | Medium | High | **P2** |
| 9 | `spring-boot-starter-webhooks` | Outbound webhook delivery with signing + retry (pairs with `outbox`) | Medium | Medium | High | **P2** |
| 10 | `spring-boot-starter-api-keys` | Issue / validate API keys for service-to-service auth | Medium | Medium | High | **P3** |
| 11 | `spring-boot-starter-twilio` | SMS / WhatsApp / voice via Twilio | High | Medium | Medium | **Requested** |
| 12 | `spring-boot-starter-resend` | Transactional email via Resend | Medium | Medium | High | **Requested** |
| 13 | `spring-boot-starter-onesignal` | Push / email / SMS / in-app via OneSignal | Medium | Medium | High | **Requested** |
| 14 | `spring-boot-starter-novu` | Multi-channel notification workflow orchestration via Novu | Medium | Medium | High | **Requested** |
| 15 | `spring-boot-starter-supabase` | Supabase BaaS — Auth, Storage, and PostgREST data access | Medium | Medium | High | **Requested** |

> Items 11–15 were explicitly requested. The four messaging providers (Twilio, Resend,
> OneSignal, Novu) are designed as **provider modules behind the `notifications` core SPI**
> (item 8) — the same "common interface, swappable provider" pattern the repo already uses for
> `StorageService` across `minio`/`aws-s3`. They also work standalone. Supabase is a separate
> BaaS integration. Detailed plans are in [§6](#6-third-party-integration-starters-requested).

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
- **`notifications`** — the **core SPI**: a `NotificationSender` interface, a `NotificationMessage`
  model (recipient, channel, subject/body, template ref, metadata), a `Channel` enum
  (`EMAIL`/`SMS`/`PUSH`/`IN_APP`/`WHATSAPP`), template resolution, and async dispatch. Ships a
  no-op/logging default; concrete transports come from the provider starters in [§6](#6-third-party-integration-starters-requested)
  (Twilio, Resend, OneSignal, Novu), each of which registers a `NotificationSender` for its
  channels. A `CompositeNotificationSender` routes a message to the provider that supports its
  channel (mirrors `CompositeAuditEventSink` in `audit-log`). **Effort:** ~M for the core.
- **`webhooks`** — outbound webhook registry + signed (HMAC) delivery with retry/backoff and a
  dead-letter; pairs naturally with `outbox`. **Effort:** ~L.
- **`api-keys`** — issue/rotate/validate hashed API keys, a filter for `X-Api-Key`, and per-key rate
  limits (reuse `rate-limiting`). **Effort:** ~M.

---

## 6. Third-party integration starters (requested)

These wrap external SaaS APIs with the repo's standard auto-configuration, typed configuration
properties, and a thin client, so consumers get a ready-to-inject bean instead of hand-wiring an
SDK. All four messaging providers implement the `notifications` core `NotificationSender` SPI (§2 item 8,
§5) for the channels they support, and also expose their native client for provider-specific features.

**Shared conventions for all five:**
- Config under `spring.<provider>.*` with an `enabled` flag; credentials never hard-coded (resolve
  via `spring-boot-starter-secrets` when present).
- `@ConditionalOnClass` on the SDK type (or, where no stable Java SDK exists, a `RestClient`-based
  client so there is no extra runtime dependency) + `@ConditionalOnProperty` on `enabled` +
  `@ConditionalOnMissingBean` on the client bean.
- A health indicator (where the provider exposes a ping/account endpoint) and Micrometer timers on
  send operations, both conditional.
- Unit tests with a stubbed HTTP server (WireMock) — these SaaS APIs have no local container — and,
  where the provider is self-hostable (Novu, Supabase), an **optional** `@Tag("integration")` +
  `@EnabledIfDockerAvailable` Testcontainers test.

### 6.1 `spring-boot-starter-twilio` — SMS / WhatsApp / voice

| Property (`spring.twilio.*`) | Purpose |
|---|---|
| `enabled` | Master switch |
| `account-sid`, `auth-token` | Twilio credentials |
| `from-number` | Default sender (E.164) |
| `messaging-service-sid` | Optional Messaging Service for sender pools |
| `channel` | `sms` (default) or `whatsapp` |

**Beans** (`twilio.config.TwilioAutoConfiguration`): initialize the SDK
(`Twilio.init(sid, token)`), expose a `TwilioClient`, and register a `TwilioNotificationSender`
implementing the `SMS`/`WHATSAPP` channels of the notifications SPI. **Deps:**
`com.twilio.sdk:twilio`. **Reference:** a community `twilio-spring-boot` already exists, confirming
demand. **Effort:** ~S–M.

### 6.2 `spring-boot-starter-resend` — transactional email

| Property (`spring.resend.*`) | Purpose |
|---|---|
| `enabled` | Master switch |
| `api-key` | Resend API key |
| `from` | Default `From` address |
| `reply-to` | Optional default reply-to |

**Beans** (`resend.config.ResendAutoConfiguration`): a `ResendEmailSender` (using the official
`com.resend:resend-java` SDK, or `RestClient` against `https://api.resend.com/emails`) that
implements the `EMAIL` channel and supports HTML/text bodies, attachments, and tags. **Effort:** ~S.

### 6.3 `spring-boot-starter-onesignal` — push / email / SMS / in-app

> **Naming note:** interpreted as **OneSignal** (customer-engagement platform with a REST API for
> push/email/SMS/in-app). "OpenSignal" is a different company (mobile-network analytics) with no
> notification-sending API — confirm if that was actually intended.

| Property (`spring.onesignal.*`) | Purpose |
|---|---|
| `enabled` | Master switch |
| `app-id` | OneSignal application ID |
| `rest-api-key` | Server REST API key |
| `default-channel` | `push` (default), `email`, or `sms` |

**Beans** (`onesignal.config.OneSignalAutoConfiguration`): a `RestClient`-based `OneSignalClient`
(no mature Java SDK — call `https://api.onesignal.com/notifications`) and a
`OneSignalNotificationSender` supporting the `PUSH`/`EMAIL`/`SMS`/`IN_APP` channels, addressing by
external user ID, segment, or player ID. **Effort:** ~M.

### 6.4 `spring-boot-starter-novu` — notification workflow orchestration

Novu is a notification *orchestration* layer: you trigger a named **workflow** for a **subscriber**
with a payload, and Novu fans out across the channels/providers configured in Novu.

| Property (`spring.novu.*`) | Purpose |
|---|---|
| `enabled` | Master switch |
| `api-key` | Novu API key |
| `base-url` | `https://api.novu.co` (override for self-hosted) |
| `default-workflow` | Optional default workflow/template ID |

**Beans** (`novu.config.NovuAutoConfiguration`): a `NovuClient` wrapping
`trigger(workflowId, subscriber, payload)` plus subscriber upsert (using `co.novu:novu-java` if
suitably maintained for Boot 4, else `RestClient`), and a `NovuNotificationSender` that maps a
generic message to a workflow trigger. Because Novu is **self-hostable via Docker**, an optional
Testcontainers integration test can exercise a real instance. **Effort:** ~M.

### 6.5 `spring-boot-starter-supabase` — Auth, Storage, PostgREST

The largest of the five — Supabase is a BaaS spanning several REST subsystems. Scope v1 to three
thin, independently-conditional clients over its REST APIs (no official Java SDK to depend on):

| Property (`spring.supabase.*`) | Purpose |
|---|---|
| `enabled` | Master switch |
| `url` | Project URL (e.g. `https://xyz.supabase.co`) |
| `anon-key` | Public anon key (client-side scope) |
| `service-role-key` | Server key for privileged operations |

**Beans** (`supabase.config.SupabaseAutoConfiguration`):
- `SupabaseAuthClient` — sign-up / sign-in / verify / admin user ops against **GoTrue**
  (`/auth/v1`).
- `SupabaseStorageClient` — bucket/object upload/download/sign against **Storage** (`/storage/v1`);
  optionally adapts to the existing `StorageService` interface for parity with `minio`/`aws-s3`.
- `SupabaseRestClient` — a thin **PostgREST** (`/rest/v1`) query/insert/update/delete helper.

Each sub-client is `@ConditionalOnProperty`-gated so consumers enable only what they use. Supabase
provides a local Docker stack (`supabase start`), enabling optional Testcontainers integration
tests; WireMock covers unit tests in CI. **Effort:** ~L (candidate for later splitting into
`supabase-auth` / `supabase-storage` / `supabase-data`).

---

## 7. Cross-cutting work & sequencing

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
`secrets` → (`security-jwt`, `webhooks`, `api-keys`).

**Requested integration track (can proceed in parallel with the P0/P1 track):** ship the
`notifications` core SPI first, then the provider starters that plug into it —
`resend` → `twilio` → `onesignal` → `novu` — followed by the standalone `supabase` BaaS starter.
Each provider is independently shippable and low-risk (thin SaaS wrapper), so they make good
"good first module" contributions.

**CI/versioning.** Each new module starts at `1.0.0`, is added to both CI jobs' scopes where it has
integration tests, and follows the existing BOM-platform build. No changes to the release workflow
are required.

---

## 8. Sources

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
- [twilio-spring-boot (Spring Boot + Twilio Java SDK) — GitHub](https://github.com/ciscoo/twilio-spring-boot)
- [Spring Boot Twilio integration — TutorialsPoint](https://www.tutorialspoint.com/spring_boot/spring_boot_twilio.htm)
- [REST API overview — OneSignal](https://documentation.onesignal.com/reference/rest-api-overview)
- [SMS, Push & Messaging API — OneSignal](https://onesignal.com/message-api)
- [Get Started with Resend and Supabase — Resend](https://resend.com/docs/knowledge-base/getting-started-with-resend-and-supabase)
- [Resend | Works With Supabase — Supabase](https://supabase.com/partners/integrations/resend)
- [Send emails with Supabase — Resend](https://resend.com/supabase)
