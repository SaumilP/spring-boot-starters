# Architecture

This document describes how the `spring-boot-starters` mono-repo is structured, how each
starter is wired into a consuming application, and the cross-cutting conventions that keep
the eleven starters consistent. For day-to-day build and contribution instructions, see
[DEVELOPER.md](DEVELOPER.md).

---

## 1. Goals

- **Drop-in infrastructure.** A consumer adds one dependency and gets a working, observable,
  production-ready integration with sensible defaults.
- **Zero lock-in.** Every default bean is overridable. The starter never fights the consumer.
- **Consistency across modules.** All starters share the same layout, naming, conditional
  strategy, observability approach, and test style, so learning one means learning all.
- **Real-infrastructure testing.** Behaviour is verified against real backends via
  Testcontainers, not mocks.

---

## 2. Module topology

The repository is a single Gradle multi-project build. There is exactly one base library
(`spring-boot-starter-common`); every other starter depends on it and nothing else in the repo.

```
                       ┌─────────────────────────────┐
                       │  spring-boot-starter-common  │   shared: exceptions,
                       │  (no auto-configuration)     │   HealthDetails builder,
                       └──────────────┬──────────────┘    metric constants
                                      │ api dependency
        ┌───────────────┬────────────┼────────────┬───────────────┐
        ▼               ▼            ▼            ▼               ▼
     redis           minio        aws-s3     rate-limiting   idempotency
        ▼               ▼            ▼            ▼               ▼
   audit-log      feature-flags  llm-client  multitenancy      outbox
```

- **`spring-boot-starter-common`** contains no `@AutoConfiguration`. It holds only shared
  abstractions (`StarterException`, `HealthDetails` builder, metric name constants) that every
  other module reuses. It is depended on with Gradle `api(project(":spring-boot-starter-common"))`
  so its types are re-exported transitively.
- The remaining ten starters are peers. They never depend on each other; a starter that needs
  Redis (rate-limiting, idempotency) declares a direct dependency on Spring Data Redis, not on
  `spring-boot-starter-redis`.

### Starters at a glance

| Starter | Auto-configuration entry point | Config prefix | Key backing tech |
|---|---|---|---|
| common | *(none — library only)* | — | — |
| redis | `configs.RedisAutoConfiguration` | `spring.redis` | Spring Data Redis, Lua locking |
| minio | `configs.MinioAutoConfiguration`, `configs.MinioMetricsConfiguration` | `spring.minio` | MinIO Java client |
| aws-s3 | `s3.config.S3AutoConfiguration` | `spring.aws.s3` | AWS SDK v2 (`S3Client`, `S3Presigner`) |
| rate-limiting | `ratelimit.config.RateLimitAutoConfiguration` | `spring.rate-limit` | Redis + in-memory sliding window, AOP |
| idempotency | `idempotency.config.IdempotencyAutoConfiguration` | `spring.idempotency` | Servlet filter, Redis store |
| audit-log | `auditlog.config.AuditLogAutoConfiguration` | `spring.audit-log` | AOP, pluggable sinks (log/JPA) |
| feature-flags | `featureflags.config.FeatureFlagsAutoConfiguration` | `spring.feature-flags` | OpenFeature SDK |
| llm-client | `llm.config.LlmClientAutoConfiguration` | `spring.llm` | `RestClient`, retry |
| multitenancy | `multitenancy.config.MultitenancyAutoConfiguration` | `spring.multitenancy` | Hibernate multi-tenancy |
| outbox | `outbox.config.OutboxAutoConfiguration` | `spring.outbox` | JPA + Kafka / RabbitMQ relay |

---

## 3. Anatomy of a starter

Each starter follows the same internal package layout:

```
spring-boot-starter-<feature>/
├── build.gradle.kts
└── src/main/
    ├── java/io/github/saumilp/starters/<feature>/
    │   ├── config/         ← @AutoConfiguration + @ConfigurationProperties
    │   ├── <domain>/       ← service / util / model / annotation / aspect
    │   └── ...
    └── resources/META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### Auto-configuration registration

Starters use the modern Spring Boot mechanism — **not** the legacy `spring.factories`.
Each auto-configuration class is listed, one per line, in:

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

Example (`spring-boot-starter-aws-s3`):

```
io.github.saumilp.starters.s3.config.S3AutoConfiguration
```

The class itself is annotated `@AutoConfiguration` and imports its bound properties:

```java
@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@EnableConfigurationProperties(S3ConfigurationProperties.class)
@ConditionalOnProperty(prefix = "spring.aws.s3", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class S3AutoConfiguration { ... }
```

### Conditional strategy

The consumer-first philosophy is enforced with conditions on every bean:

- **`@ConditionalOnClass`** — the starter activates only when its backing technology is on the
  classpath (e.g. `S3Client`, `RedisConnectionFactory`, `MinioClient`).
- **`@ConditionalOnMissingBean`** — a consumer-declared bean of the same type always wins; the
  starter's default silently steps aside.
- **`@ConditionalOnProperty`** — a top-level `spring.<feature>.enabled` flag (defaulting to
  `true`) allows the entire starter to be switched off without removing the dependency.
- **`@EnableConfigurationProperties`** — binds the `@ConfigurationProperties` class so
  `spring.<feature>.*` values populate a single bound bean that is injected into the beans that
  need it (rather than constructing an unbound instance).

### Configuration properties

Every starter exposes a `@ConfigurationProperties`-annotated class bound to its prefix. All
properties have documented defaults; a starter works out of the box with a minimal amount of
required configuration (typically just an endpoint/credentials for the ones that talk to a
remote service).

---

## 4. Observability

Starters that talk to a remote service ship observability that is **conditional**, so it adds
zero cost when the actuator or Micrometer is absent:

- **Health indicators** implement `org.springframework.boot.health.contributor.HealthIndicator`
  (Spring Boot 4.0's relocated health API) and are gated with `@ConditionalOnClass` on the
  health/actuator types plus a `management.health.<name>.enabled` property.
- **Micrometer metrics** are registered only when a `MeterRegistry` bean is present
  (`@ConditionalOnBean(MeterRegistry.class)`), typically via an AOP aspect that times backend
  operations and tags them by operation name and success/failure outcome.

The `HealthDetails` builder in `spring-boot-starter-common` gives every health indicator a
consistent, null-safe, insertion-ordered detail map.

---

## 5. Build architecture

The root `build.gradle.kts` configures every sub-project through a `subprojects {}` block:

- **Dependency management via a Gradle-native BOM platform.** Instead of the
  `io.spring.dependency-management` plugin, each non-example module declares
  `platform(SpringBootPlugin.BOM_COORDINATES)` on its `implementation`, `compileOnly`,
  `annotationProcessor`, `testImplementation`, and `testRuntimeOnly` configurations. This pins
  all Spring / Jackson / Testcontainers versions from the Spring Boot 4.0 BOM.
- **Java toolchain 21**, auto-provisioned by the Foojay resolver declared in
  `settings.gradle.kts`.
- **Spotless** enforces formatting (trailing whitespace, tabs→spaces, final newline, unused
  imports) for Java and misc files; `spotlessCheck` runs in CI.
- **Publishing & signing** (`maven-publish` + `signing`) are applied only to non-example
  modules, producing a signed POM, sources JAR, and Javadoc JAR for Maven Central (OSSRH). The
  publication `group` is `io.github.saumilp.starters`.
- **Javadoc** runs with `-Xdoclint:all`; the CI `javadoc` task treats documentation gaps as
  warnings surfaced during the build. Example apps are demonstration code and have their
  Javadoc task disabled.

Examples apply `org.springframework.boot` + `io.spring.dependency-management` in their own
plugins block (which auto-imports the Boot BOM) and are excluded from publishing.

---

## 6. Testing architecture

Two test scopes share one source set, separated by JUnit tag:

- **Unit tests** run under the `test` task, which **excludes** the `integration` tag. They use
  JUnit 5 + AssertJ + Mockito and mock only external collaborators.
- **Integration tests** are tagged `@Tag("integration")` and run under a dedicated
  `integrationTest` task (registered in the root build) that includes only that tag. They use
  **Testcontainers** for real backends (Redis, MinIO) and are annotated
  `@EnabledIfDockerAvailable` so they skip cleanly when no Docker daemon is present rather than
  failing. Each test cleans up its own state in `@AfterEach`.

This split means `./gradlew test` never requires Docker, while `./gradlew integrationTest`
exercises the real integrations.

---

## 7. CI/CD

`.github/workflows/ci.yml` runs on pushes to `main` / `feature/**` and PRs to `main`:

1. **Build & Unit Tests** (`ubuntu-latest`, JDK 21) — `./gradlew test`, then `./gradlew javadoc`,
   then `./gradlew spotlessCheck`, uploading test reports.
2. **Integration Tests** — `./gradlew :spring-boot-starter-redis:integrationTest
   :spring-boot-starter-minio:integrationTest`. Docker is pre-installed on `ubuntu-latest`, so
   the Testcontainers-backed tests run against real Redis and MinIO containers.

`.github/workflows/release.yml` publishes signed artifacts to Maven Central and creates a
GitHub Release when a `v<major>.<minor>.<patch>` tag is pushed.

---

## 8. Spring Boot 4.0 baseline

The repo targets Spring Boot 4.0 / Spring Framework 7 / Java 21. Notable platform changes that
shape the code:

- **Health API relocation** — `HealthIndicator` / `Health` / `Status` moved from
  `org.springframework.boot.actuate.health` to `org.springframework.boot.health.contributor`
  (new `spring-boot-health` module).
- **`spring-boot-starter-aop` removed** — AOP-based starters depend on `org.springframework:spring-aop`
  and `org.aspectj:aspectjweaver` directly.
- **Jackson 3** — Spring Boot 4.0 ships Jackson 3 (`tools.jackson.*`) alongside Jackson 2
  (`com.fasterxml.jackson.*`); starters pick the correct one per API.
- **Testcontainers 2.x** — artifact IDs gained a `testcontainers-` prefix and Docker gating
  moved to the `@EnabledIfDockerAvailable` annotation.
- **Auto-config relocation** — some Boot auto-configurations moved to feature modules (e.g.
  `RedisAutoConfiguration` → `DataRedisAutoConfiguration` in `spring-boot-data-redis`).
