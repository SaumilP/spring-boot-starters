# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added

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
