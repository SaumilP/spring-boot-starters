# Spring Boot Starters

A collection of production-ready Spring Boot auto-configuration libraries that help you integrate common infrastructure quickly, consistently, and with zero boilerplate.

[![CI](https://github.com/SaumilP/spring-boot-starters/actions/workflows/ci.yml/badge.svg)](https://github.com/SaumilP/spring-boot-starters/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.saumilp.starters/spring-boot-starter-redis.svg)](https://central.sonatype.com/search?q=io.github.saumilp.starters)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)](https://spring.io/projects/spring-boot)

---

## Why custom starters?

As Spring-based systems grow, teams commonly face duplicated configuration across services, inconsistent observability, and boilerplate setup that slows down every new project. Custom Spring Boot starters solve this by packaging reusable infrastructure concerns into drop-in dependencies. Add a dependency. Get a working, production-ready setup — by default.

---

## Available Starters

All artifacts are published under the `io.github.saumilp.starters` group. Each starter has its own README with configuration reference, DDL (where applicable), and usage examples.

| Starter | Description | Config prefix | Version |
|---|---|---|---|
| [spring-boot-starter-common](spring-boot-starter-common/README.md) | Shared exceptions, health-detail builder, and metric constants (no auto-configuration) | — | 1.0.0 |
| [spring-boot-starter-redis](spring-boot-starter-redis/README.md) | Redis templates, distributed locking, cache manager, health indicator, and Micrometer metrics | `spring.redis` | 1.0.0 |
| [spring-boot-starter-minio](spring-boot-starter-minio/README.md) | MinIO / S3-compatible object storage with health indicator and Micrometer metrics | `spring.minio` | 2.0.0 |
| [spring-boot-starter-aws-s3](spring-boot-starter-aws-s3/README.md) | AWS S3 object storage via AWS SDK v2, pre-signed URLs, and a health indicator | `spring.aws.s3` | 1.0.0 |
| [spring-boot-starter-rate-limiting](spring-boot-starter-rate-limiting/README.md) | Annotation-driven distributed rate limiting backed by Redis, with an in-memory fallback | `spring.rate-limit` | 1.0.0 |
| [spring-boot-starter-idempotency](spring-boot-starter-idempotency/README.md) | HTTP idempotency enforcement via the `Idempotency-Key` header with Redis-backed response caching | `spring.idempotency` | 1.0.0 |
| [spring-boot-starter-audit-log](spring-boot-starter-audit-log/README.md) | Annotation-driven audit logging with pluggable sinks (log, JPA, composite) | `spring.audit-log` | 1.0.0 |
| [spring-boot-starter-feature-flags](spring-boot-starter-feature-flags/README.md) | OpenFeature-based feature flag evaluation with file-based and Unleash providers | `spring.feature-flags` | 1.0.0 |
| [spring-boot-starter-llm-client](spring-boot-starter-llm-client/README.md) | OpenAI-compatible LLM client with retry, Micrometer metrics, and optional response caching | `spring.llm` | 1.0.0 |
| [spring-boot-starter-multitenancy](spring-boot-starter-multitenancy/README.md) | Schema-per-tenant and DB-per-tenant multitenancy using Hibernate and tenant context propagation | `spring.multitenancy` | 1.0.0 |
| [spring-boot-starter-outbox](spring-boot-starter-outbox/README.md) | Transactional Outbox pattern with JPA persistence and a pluggable Kafka / RabbitMQ relay | `spring.outbox` | 1.0.0 |

---

## Quick Start

```groovy
// Gradle (Kotlin or Groovy DSL)
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-redis:1.0.0'
    implementation 'io.github.saumilp.starters:spring-boot-starter-minio:2.0.0'
}
```

```xml
<!-- Maven -->
<dependency>
    <groupId>io.github.saumilp.starters</groupId>
    <artifactId>spring-boot-starter-redis</artifactId>
    <version>1.0.0</version>
</dependency>
```

Each starter auto-configures itself on the classpath. Enable it, point it at your infrastructure via `application.yml`, and start using the injected beans — no `@Configuration` required.

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 21 LTS or later |
| Spring Boot | 4.0.4 or later |
| Spring Framework | 7.x (transitive via Boot) |
| Gradle (build) | 9.x (wrapper provided) |

---

## Design Philosophy

**Consumer-first.** Every auto-configured bean is annotated `@ConditionalOnMissingBean`.
Declare your own bean of the same type and the starter's default disappears completely.

**No surprises.** Starters never embed schema management (Flyway/Liquibase). If a starter
requires a schema, it documents the DDL in its README and lets you run it.

**Observable by default.** Health indicators and Micrometer metrics are included in every
starter that touches a remote service — conditional on the actuator / micrometer being present, so they add zero overhead if you don't use them.

**Tested against real infrastructure.** All integration tests use Testcontainers. If a
test passes locally it will pass in CI.

For the internal design, module dependency graph, and auto-configuration mechanics, see
[ARCHITECTURE.md](ARCHITECTURE.md).

---

## Project Structure

```
spring-boot-starters/
├── build.gradle.kts                       ← root build: BOM platform, toolchain, spotless, publishing, signing
├── settings.gradle.kts                    ← module registry + Foojay JDK toolchain resolver
├── gradle/wrapper/                        ← Gradle wrapper (9.x)
├── ARCHITECTURE.md                        ← design, module graph, and internals
├── DEVELOPER.md                           ← build, test, and contribution guide
├── CONTRIBUTING.md                        ← coding standards and PR process
├── CHANGELOG.md                           ← release history
│
├── spring-boot-starter-common/            ← shared abstractions (no auto-config)
├── spring-boot-starter-redis/             ← Redis utilities & distributed locking
├── spring-boot-starter-minio/             ← MinIO / S3-compatible object storage
├── spring-boot-starter-aws-s3/            ← AWS S3 (AWS SDK v2)
├── spring-boot-starter-rate-limiting/     ← distributed rate limiting
├── spring-boot-starter-idempotency/       ← HTTP idempotency
├── spring-boot-starter-audit-log/         ← annotation-driven audit logging
├── spring-boot-starter-feature-flags/     ← OpenFeature feature flags
├── spring-boot-starter-llm-client/        ← OpenAI-compatible LLM client
├── spring-boot-starter-multitenancy/      ← Hibernate multitenancy
├── spring-boot-starter-outbox/            ← transactional outbox
│
└── examples/                              ← runnable example applications (not published)
    ├── redis-example/
    ├── minio-example/
    ├── aws-s3-example/
    ├── rate-limiting-example/
    ├── idempotency-example/
    ├── audit-log-example/
    ├── feature-flags-example/
    ├── llm-client-example/
    ├── multitenancy-example/
    └── outbox-example/
```

Every starter depends on `spring-boot-starter-common` and follows the same internal layout
(`config/`, properties, service/util, and a `META-INF/spring/...AutoConfiguration.imports`
registration). See [ARCHITECTURE.md](ARCHITECTURE.md) for the full breakdown.

---

## Building Locally

```bash
./gradlew build                 # compile, unit tests, javadoc, spotless
./gradlew test                  # unit tests only (integration tests excluded)
./gradlew integrationTest       # integration tests (requires a running Docker daemon)
./gradlew javadoc               # generate and validate Javadoc (-Xdoclint:all)
./gradlew spotlessApply         # auto-format code
./gradlew publishToMavenLocal   # install all starters to ~/.m2 for local testing
```

Integration tests are tagged `@Tag("integration")` and are excluded from the `test` task.
They are annotated `@EnabledIfDockerAvailable`, so they skip gracefully when no Docker daemon
is present. See [DEVELOPER.md](DEVELOPER.md) for the full workflow.

---

## Documentation

| Document | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Module graph, auto-configuration mechanics, build & test architecture |
| [DEVELOPER.md](DEVELOPER.md) | Local setup, Gradle commands, adding a starter, Spring Boot 4.0 notes |
| [ROADMAP.md](ROADMAP.md) | Community-demanded starters (analysis) + detailed implementation plans |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Coding standards, commit conventions, PR process |
| [CHANGELOG.md](CHANGELOG.md) | Release history |
| Per-starter `README.md` | Configuration reference, DDL, and usage examples |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for coding standards, commit conventions, JavaDoc requirements, and how to submit a new starter. See [DEVELOPER.md](DEVELOPER.md) for the day-to-day build and test workflow.

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
