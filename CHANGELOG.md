# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added

- `spring-boot-starter-notifications` — new module providing the core notification SPI:
  a `NotificationSender` interface, `NotificationMessage` / `NotificationResult` model, a
  `Channel` enum (`EMAIL`/`SMS`/`PUSH`/`IN_APP`/`WHATSAPP`), a `CompositeNotificationSender`
  that routes each message to the delegate supporting its channel, a `LoggingNotificationSender`
  fallback, and a `NotificationService` façade with sync/async dispatch; bound from
  `spring.notifications.*`.
- `spring-boot-starter-security-jwt` — new module providing an opinionated, stateless JWT
  resource-server `SecurityFilterChain` with bearer-token authentication, secure response headers
  (HSTS, `X-Content-Type-Options`, `X-Frame-Options`), CORS, a `JwtDecoder` built from the
  configured JWKS/issuer URI, and configurable public paths; bound from `spring.security-jwt.*`.
- `spring-boot-starter-webhooks` — new module providing outbound webhook delivery via a
  `WebhookDeliveryService` with HMAC (`WebhookSigner`) payload signing, exponential-backoff retry,
  and dead-lettering (`DeadLetterStore`), plus an `InMemoryWebhookRegistry`; bound from
  `spring.webhooks.*`.
- `spring-boot-starter-api-keys` — new module providing API key issue/validate/revoke via
  `ApiKeyService` (only key hashes stored through `ApiKeyHasher`), an `ApiKeyStore`
  (`InMemoryApiKeyStore` default), and an `ApiKeyAuthFilter` that enforces `X-Api-Key` on
  configured protected paths; bound from `spring.api-keys.*`.
- `spring-boot-starter-secrets` — new module providing a unified `SecretSource` SPI with
  `EnvironmentSecretSource` (default) and `AwsSecretsManagerSecretSource` providers selected via
  `spring.secrets.provider`; bound from `spring.secrets.*`.
- `spring-boot-starter-scheduler-lock` — new module providing a `@SchedulerLock` annotation and
  aspect so a `@Scheduled` task runs on one instance only, with pluggable `LockProvider`
  implementations (`InMemoryLockProvider`, Redis-backed `RedisLockProvider`); bound from
  `spring.scheduler-lock.*`.
- `spring-boot-starter-data-privacy` — new module providing a `MaskingService` (email/card/full
  masking) and JPA field-level AES-256-GCM encryption via `EncryptedStringConverter` /
  `AesGcmEncryptor`; bound from `spring.data-privacy.*`.
- `spring-boot-starter-resilient-client` — new module providing a `ResilientClient` façade
  and auto-configured Resilience4j `Retry` and `CircuitBreaker`, plus a timeout-configured
  `resilientRestClient`; bound from `spring.resilient-client.*`.
- `spring-boot-starter-problem-details` — new module providing a `GlobalExceptionHandler`
  that renders unhandled exceptions as RFC 7807 `application/problem+json` responses with a
  machine-readable `code`, per-field validation errors, request path, and optional correlation
  ID; bound from `spring.problem-details.*`.
- `spring-boot-starter-observability` — new module providing request correlation IDs
  (`CorrelationIdFilter`), MDC log propagation, async context propagation
  (`MdcTaskDecorator`), a `CorrelationContext` accessor, and an opt-in outbound
  `CorrelationIdClientHttpRequestInterceptor`, bound from `spring.observability.*`.
- `spring-boot-starter-common` — new module providing shared exception hierarchy
  (`StarterException`, `StarterConfigurationException`), `MeterRegistryUtils` Micrometer
  constants, `HealthDetails` fluent builder, and `@StarterBean` informational annotation.
- `RedisHealthIndicator` — actuator PING-based health check for Redis connectivity.
- `RedisMetricsAspect` — AOP aspect that records Micrometer `Timer` metrics for every
  `RedisUtil` method call, tagged with `operation` and `status`.
- `RedisOperationException` extending `StarterException` for clean exception propagation.
- `RedisConfigurationProperties` extended with `host`, `port`, `metricName`, and
  `cacheTtlDays` properties.
- Testcontainers-based integration tests for Redis (`RedisUtilIntegrationTest`,
  `RedisLockUtilIntegrationTest`, `RedisHealthIndicatorIntegrationTest`) and MinIO
  (`MinioStorageServiceIntegrationTest`).
- GitHub Actions CI workflow (`ci.yml`) with unit test, integration test, Javadoc gate,
  and Spotless lint check jobs.
- GitHub Actions release workflow (`release.yml`) for automated Maven Central publishing
  on version tags.

### Changed

- **Mono-repo restructure** — root `build.gradle` and `settings.gradle` now manage all submodules as a Gradle multi-module project. Per-module `settings.gradle` files removed.
- Spring Boot baseline raised to **4.0.4** (Spring Framework 7.x, Jakarta EE 11, Java 21+).
- `RedisAutoConfiguration` migrated from `@Configuration` to `@AutoConfiguration` with `@ConditionalOnClass(RedisConnectionFactory.class)`.
- Registration moved from deprecated `META-INF/spring.factories` to `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- `RedisKeyGenerator` migrated from deprecated `CachingConfigurerSupport` (removed in Spring Framework 7.x) to `CachingConfigurer` interface.
- All auto-configured beans annotated with `@ConditionalOnMissingBean` for consumer override support.
- `buildObjectMapper()` extracted as a private helper in `RedisAutoConfiguration` to eliminate three duplicate `ObjectMapper` constructions.
- `spring-boot-starter-minio` and `spring-boot-starter-redis` now declare `api project(':spring-boot-starter-common')` — common types are transitively available to consumers.

### Removed

- `MinioException` and `MinioFetchException` from `spring-boot-starter-redis` (wrong module; redis starter now uses `RedisOperationException`).
- Deprecated `META-INF/spring.factories` from `spring-boot-starter-redis`.
- Per-module `settings.gradle` files (conflicted with root multi-module build).

---

## [2.0.0] — spring-boot-starter-minio — 2024-01-01

### Added
- `MinioStorageServiceImpl` — full `StorageService` implementation covering upload, download, copy, pre-signed URLs, legal holds, and encryption config.
- `MinioHealthIndicator` — actuator health check for MinIO bucket availability.
- `MinioMetricsConfiguration` — Micrometer metrics for storage operations.
- `MinioConfigurationProperties` — `spring.minio.*` configuration binding with timeouts, proxy support, and bucket auto-creation.

---

## [1.0.0] — spring-boot-starter-redis — 2024-01-01

### Added
- `RedisUtil` — high-level Redis utility for strings, sets, lists, hashes, and sorted sets.
- `RedisLockUtil` — distributed lock with atomic Lua release script.
- `RedisAutoConfiguration` — auto-configures `RedisTemplate`, `StringRedisTemplate`, `CacheManager`, and utility beans.
- `RedisKeyGenerator` — structured Spring Cache key generator.
