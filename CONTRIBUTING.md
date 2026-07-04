# Contributing to spring-boot-starters

Thank you for your interest in contributing. The goal of this project is to maintain a curated set of production-quality Spring Boot starters, and every contribution should raise the bar for the whole repo.

---

## Getting Started

1. Fork the repository and create a branch from `main`.
2. Install JDK 21 or later. The Gradle toolchain will enforce this automatically.
3. Run `./gradlew build` to verify the project compiles and all unit tests pass.
4. Run `./gradlew spotlessApply` to auto-format your code before committing.

---

## Code Standards

### Java

- **Java 21+** — use records, sealed classes, text blocks, and pattern matching where they improve clarity.
- **No raw types.** Generic types must be fully specified.
- **Immutable by default.** Prefer `final` fields and return `Map.copyOf()` / `List.copyOf()` from any method returning a collection.
- **Spotless formatting** is enforced in CI. Run `./gradlew spotlessApply` locally.

### Spring Patterns

- Use `@AutoConfiguration` (not `@Configuration`) as the top-level entry point.
- Register your auto-configuration class in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Do **not** use `spring.factories` — it was deprecated in Spring Boot 2.7 and removed in Spring Boot 3+.
- Annotate every auto-configured bean with `@ConditionalOnMissingBean` unless there is a compelling reason not to.
- Health indicators and Micrometer metrics must be conditional on the actuator / micrometer being present (use `@ConditionalOnClass`).

### JavaDoc

Every public type and method must have a JavaDoc comment. Format:

```java
/**
 * One-sentence summary of what the method does (imperative mood).
 *
 * <p>Optional second paragraph for caveats or context.
 *
 * @param paramName description; constraints (e.g. "must not be {@code null}")
 * @return description of what is returned; never {@code null} vs. may be {@code null}
 * @throws ExceptionType when this condition applies
 * @since 1.0.0
 */
```

The CI `javadoc` task runs with `-Xdoclint:all`. Any missing or malformed tag causes a build failure.

---

## Testing

### Unit Tests

- Every public class must have a unit test.
- Use JUnit 5 and AssertJ.
- Name tests `should_<behaviour>_when_<condition>`. Example: `should_returnNull_when_keyNotFound`.
- Mock only external collaborators (Redis connection, HTTP clients). Do not mock your own classes.

### Integration Tests

- Tag integration tests with `@Tag("integration")`.
- Use Testcontainers — no in-memory fakes or mocked servers for infrastructure components.
- Integration tests must clean up after themselves (delete created keys/objects in `@AfterEach`).

Run integration tests with:

```bash
./gradlew test -Dgroups=integration
```

---

## Submitting a New Starter

1. Create a new Gradle module under the project root. Follow the naming convention `spring-boot-starter-<feature>`.
1. Add the module to `settings.gradle`.
2. Depend on `spring-boot-starter-common`:
   ```groovy
   dependencies {
       api project(':spring-boot-starter-common')
   }
   ```
3. Write `@AutoConfiguration` and register it in `AutoConfiguration.imports`.
4. Add a comprehensive README following the template of
   [spring-boot-starter-redis](spring-boot-starter-redis/README.md).
5. Add a CHANGELOG entry under `[Unreleased]`.
6. Open a pull request using the PR template.

---

## Commit Message Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <short summary>

<optional body>
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`.
Scope: the module name, e.g. `redis`, `minio`, `common`.

Example:

```
feat(redis): add sliding-window rate-limit Lua script

Implements the RateLimitUtil with a Redis Lua script that atomically increments a counter per key within a rolling time window.
```

---

## Release Process

Releases are automated. Maintainers tag a commit with `v<major>.<minor>.<patch>` and push the tag. The `release.yml` workflow publishes to Maven Central and creates a GitHub Release automatically.
