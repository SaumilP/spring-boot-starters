# Developer Guide

Everything you need to build, test, extend, and release the `spring-boot-starters` mono-repo.
For the design rationale and module topology, see [ARCHITECTURE.md](ARCHITECTURE.md); for
coding standards and the PR process, see [CONTRIBUTING.md](CONTRIBUTING.md).

---

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 21 (LTS) | The Gradle toolchain auto-provisions it via the Foojay resolver if missing. |
| Gradle | 9.x | Use the wrapper (`./gradlew`); do not use a system Gradle. |
| Docker | any recent | Required only for `integrationTest` (Testcontainers). |
| Git | 2.x+ | — |

You do **not** need a local Spring Boot, Redis, or MinIO install — unit tests need none, and
integration tests spin up containers on demand.

---

## 2. Repository layout

```
spring-boot-starters/
├── build.gradle.kts        root build (BOM platform, toolchain, spotless, publish, signing)
├── settings.gradle.kts     module registry + Foojay toolchain resolver
├── spring-boot-starter-*/  the eleven starters (see ARCHITECTURE.md)
└── examples/*/             runnable demo apps (not published, no Javadoc)
```

A starter's own layout:

```
spring-boot-starter-<feature>/
├── build.gradle.kts
├── README.md               configuration reference + usage
└── src/
    ├── main/java/io/github/saumilp/starters/<feature>/
    │   ├── config/         @AutoConfiguration + @ConfigurationProperties
    │   └── <domain>/       service / util / model / annotation / aspect
    ├── main/resources/META-INF/spring/
    │   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
    └── test/java/...        unit tests + @Tag("integration") tests
```

---

## 3. Common Gradle commands

```bash
# Compile, run unit tests, generate Javadoc, run Spotless — the full local gate
./gradlew build

# Unit tests only (integration tests are excluded by tag; no Docker needed)
./gradlew test

# Integration tests (Testcontainers; requires a running Docker daemon)
./gradlew integrationTest
./gradlew :spring-boot-starter-redis:integrationTest      # a single module

# Documentation and formatting
./gradlew javadoc          # runs with -Xdoclint:all
./gradlew spotlessApply    # auto-format
./gradlew spotlessCheck    # verify formatting (as CI does)

# Work on a single module
./gradlew :spring-boot-starter-outbox:test
./gradlew :spring-boot-starter-outbox:compileJava

# Install all starters to the local Maven repo for testing in another project
./gradlew publishToMavenLocal
```

> **Tip:** `test` and `integrationTest` share the same test source set — they differ only by
> JUnit tag. If a test needs Docker, tag it `@Tag("integration")` so it never runs under `test`.

---

## 4. Running integration tests

Integration tests are `@Tag("integration")` + `@EnabledIfDockerAvailable`:

- Without Docker, `./gradlew integrationTest` **skips** them (they are reported as skipped, not
  failed).
- With Docker, they start real containers (`redis:7.2-alpine`, a pinned `minio/minio` image),
  wire the container endpoint into Spring via `@DynamicPropertySource`, and clean up in
  `@AfterEach`.

```bash
# Verify Docker is available first
docker info

# Then run
./gradlew integrationTest
```

If a test hangs pulling an image, confirm the pinned image tag still exists in the registry and
that your Docker daemon has network access.

---

## 5. Adding a new starter

1. **Create the module** `spring-boot-starter-<feature>/` with a `build.gradle.kts`:

   ```kotlin
   plugins {
       id("org.springframework.boot") apply false
   }

   description = "Spring Boot Starter - <one-line summary>"
   version     = "1.0.0"

   dependencies {
       api(project(":spring-boot-starter-common"))
       annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
       implementation("org.springframework.boot:spring-boot-starter")
       implementation("org.springframework.boot:spring-boot-autoconfigure")
       // backing tech as compileOnly if optional, implementation if required
       testImplementation("org.springframework.boot:spring-boot-starter-test")
       testRuntimeOnly("org.junit.platform:junit-platform-launcher")
   }

   tasks.javadoc {
       source = sourceSets["main"].allJava
       (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:all", "-quiet")
   }
   ```

2. **Register the module** in `settings.gradle.kts` (`include("spring-boot-starter-<feature>")`).

3. **Write the auto-configuration** under `config/`:

   ```java
   @AutoConfiguration
   @ConditionalOnClass(SomeBackendType.class)
   @EnableConfigurationProperties(FeatureProperties.class)
   @ConditionalOnProperty(prefix = "spring.<feature>", name = "enabled",
           havingValue = "true", matchIfMissing = true)
   public class FeatureAutoConfiguration {
       @Bean
       @ConditionalOnMissingBean
       public FeatureService featureService(FeatureProperties props) { ... }
   }
   ```

4. **Register it** in
   `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
   (one fully-qualified class name per line). Do **not** use `spring.factories`.

5. **Bind properties** with a `@ConfigurationProperties(prefix = "spring.<feature>")` class,
   documented defaults, and an explicit no-arg constructor.

6. **Add observability** (optional) — a `HealthIndicator` from
   `org.springframework.boot.health.contributor` and/or a Micrometer aspect, both conditional.

7. **Write tests** — unit tests for every public class, plus a `@Tag("integration")` test if the
   starter talks to real infrastructure.

8. **Document** — a `README.md` following the [redis README](spring-boot-starter-redis/README.md),
   a row in the root [README](README.md) table, and a `CHANGELOG.md` entry.

---

## 6. Coding standards (quick reference)

Full rules live in [CONTRIBUTING.md](CONTRIBUTING.md); the essentials:

- **Java 21** idioms (records, sealed types, text blocks, pattern matching) where they aid clarity.
- **No raw types**; `final` fields; return unmodifiable collection copies.
- **Every public type and method** has Javadoc. CI runs `javadoc` with `-Xdoclint:all`, so:
  - block tags (`@param`, `@return`, `@throws`) must start at the beginning of a comment line;
  - getters can use the compact `{@return ...}` inline form;
  - classes with no explicit constructor still need a documented one to satisfy the doclet.
- **Spring:** `@AutoConfiguration` entry points, `@ConditionalOnMissingBean` on defaults,
  `@EnableConfigurationProperties` for binding, health/metrics gated by `@ConditionalOnClass` /
  `@ConditionalOnBean`.
- **Formatting** is enforced — run `./gradlew spotlessApply` before committing.

---

## 7. Testing conventions

- Name tests `should_<behaviour>_when_<condition>` (e.g. `should_returnNull_when_keyNotFound`).
- Unit tests: JUnit 5 + AssertJ + Mockito; mock only external collaborators, never your own classes.
- Integration tests: `@Tag("integration")` + `@EnabledIfDockerAvailable` + Testcontainers; clean
  up created keys/objects in `@AfterEach`. Because containers are typically `static` (shared
  across the class), ensure teardown is unconditional so state never leaks between tests.
- Object/key naming in integration tests should match the implementation's semantics (e.g. a
  non-recursive listing returns a `foo/` prefix for a nested `foo/bar` key — prefer flat keys
  unless you are explicitly testing prefixes).

---

## 8. Spring Boot 4.0 gotchas

When porting code or writing new starters, keep these platform changes in mind:

| Area | Change |
|---|---|
| Health API | `HealthIndicator`/`Health`/`Status` are now in `org.springframework.boot.health.contributor` (module `spring-boot-health`). |
| AOP | `spring-boot-starter-aop` no longer exists — use `org.springframework:spring-aop` + `org.aspectj:aspectjweaver`. |
| Jackson | Jackson 3 (`tools.jackson.*`) ships alongside Jackson 2 (`com.fasterxml.jackson.*`); Spring Data Redis serializers still use Jackson 2. |
| Testcontainers | 2.x artifact IDs are `testcontainers-junit-jupiter`, `testcontainers-minio`, etc.; use `@EnabledIfDockerAvailable` instead of `@Testcontainers(disabledWithoutDocker = true)`. |
| Boot auto-config | Relocations, e.g. `RedisAutoConfiguration` → `DataRedisAutoConfiguration` (`org.springframework.boot.data.redis.autoconfigure`). |
| Config binding | Prefer `@EnableConfigurationProperties` + injection over `new`-ing a `@ConfigurationProperties` bean in a `@Bean` method — an unbound instance yields null values in minimal test contexts. |

---

## 9. Publishing & release

- Local install: `./gradlew publishToMavenLocal` puts all starters into `~/.m2/repository` under
  `io.github.saumilp.starters`.
- Releases are automated: a maintainer pushes a `v<major>.<minor>.<patch>` tag; `release.yml`
  builds, signs, and publishes to Maven Central (OSSRH) and creates a GitHub Release. Signing
  and OSSRH credentials are provided via CI secrets (`GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`,
  `OSSRH_USERNAME`, `OSSRH_PASSWORD`).

---

## 10. Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `integrationTest` fails with "Could not find a valid Docker environment" | Docker daemon not running. Start Docker or let the tests skip (they will, via `@EnabledIfDockerAvailable`). |
| `javadoc` warnings about missing comments | Add the missing Javadoc; block tags must begin a line. Example apps are exempt (Javadoc disabled). |
| `spotlessCheck` fails in CI | Run `./gradlew spotlessApply` and commit the result. |
| Spring Boot plugin "requires Gradle 9.x" | Use `./gradlew` (the wrapper), not a system Gradle. |
| `@ConfigurationProperties` values are null in a test | Bind via `@EnableConfigurationProperties` and inject the bean; don't `new` it in a `@Bean` method. |
