# Spring Boot Starters

A collection of production-ready Spring Boot auto-configuration libraries that help you
integrate common infrastructure quickly, consistently, and with zero boilerplate.

[![CI](https://github.com/saumilpatel/spring-boot-starters/actions/workflows/ci.yml/badge.svg)](https://github.com/saumilpatel/spring-boot-starters/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.sandcastle.starter-apps/spring-boot-starter-redis.svg)](https://central.sonatype.com/search?q=org.sandcastle.starter-apps)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)](https://spring.io/projects/spring-boot)

---

## Why custom starters?

As Spring-based systems grow, teams commonly face duplicated configuration across services,
inconsistent observability, and boilerplate setup that slows down every new project.
Custom Spring Boot starters solve this by packaging reusable infrastructure concerns into
drop-in dependencies. Add a dependency. Get a working, production-ready setup — by default.

---

## Available Starters

| Starter | Description | Version |
|---|---|---|
| [spring-boot-starter-common](spring-boot-starter-common/README.md) | Shared exceptions, metrics constants, health builder | 1.0.0 |
| [spring-boot-starter-redis](spring-boot-starter-redis/README.md) | Redis utilities, distributed locking, health + Micrometer metrics | 1.0.0 |
| [spring-boot-starter-minio](spring-boot-starter-minio/README.md) | MinIO / S3-compatible object storage with health + Micrometer metrics | 2.0.0 |

---

## Quick Start

```groovy
// Gradle
dependencies {
    implementation 'org.sandcastle.starter-apps:spring-boot-starter-redis:1.0.0'
    implementation 'org.sandcastle.starter-apps:spring-boot-starter-minio:2.0.0'
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

## Requirements

| Requirement | Version |
|---|---|
| Java | 21 LTS or later |
| Spring Boot | 4.0.4 or later |
| Spring Framework | 7.x (transitive via Boot) |

---

## Design Philosophy

**Consumer-first.** Every auto-configured bean is annotated `@ConditionalOnMissingBean`.
Declare your own bean of the same type and the starter's default disappears completely.

**No surprises.** Starters never embed schema management (Flyway/Liquibase). If a starter
requires a schema, it documents the DDL in its README and lets you run it.

**Observable by default.** Health indicators and Micrometer metrics are included in every
starter that touches a remote service — conditional on the actuator / micrometer being
present, so they add zero overhead if you don't use them.

**Tested against real infrastructure.** All integration tests use Testcontainers. If a
test passes locally it will pass in CI.

---

## Project Structure

```
spring-boot-starters/
├── build.gradle                    ← root build: BOM, plugins, publishing, signing
├── settings.gradle                 ← multi-module root
├── spring-boot-starter-common/     ← shared abstractions (no auto-config)
├── spring-boot-starter-minio/      ← MinIO / S3 object storage
├── spring-boot-starter-redis/      ← Redis utilities and distributed locking
└── examples/                       ← runnable example applications (not published)
```

---

## Building Locally

```bash
./gradlew build                      # compile, test (unit only), javadoc
./gradlew test -Dgroups=integration  # integration tests (requires Docker)
./gradlew spotlessApply              # auto-format code
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for coding standards, commit conventions,
JavaDoc requirements, and how to submit a new starter.

---

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
