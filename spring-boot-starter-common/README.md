# spring-boot-starter-common

Shared abstractions and utilities used by all `spring-boot-starters` in this project.
This module contains no auto-configuration — it is a pure library dependency that other
starters pull in via `api project(':spring-boot-starter-common')`.

Consumer applications that import a starter indirectly get `spring-boot-starter-common`
types on their compile classpath without declaring the dependency directly.

---

## What's Inside

### Exception Hierarchy

```
RuntimeException
  └── StarterException                   — base unchecked exception for all starters
        └── StarterConfigurationException — thrown at startup when configuration is invalid
```

`StarterException` accepts a message, a cause, or both. Use it as the base for any
domain-specific exceptions you add in downstream starters.

### Annotations

| Annotation | Purpose |
|---|---|
| `@StarterBean` | Marks a bean or method as auto-configured by a starter (informational only; no runtime behaviour) |

### Metrics Constants (`MeterRegistryUtils`)

Pre-defined Micrometer tag keys and status values shared across all starters:

| Constant | Value |
|---|---|
| `TAG_OPERATION` | `"operation"` |
| `TAG_STATUS` | `"status"` |
| `TAG_COMPONENT` | `"component"` |
| `STATUS_SUCCESS` | `"success"` |
| `STATUS_ERROR` | `"error"` |

Using these constants keeps metric tag names consistent across all starters so dashboards
and alerts work without modification when you adopt additional starters.

### Health Details Builder (`HealthDetails`)

A fluent, null-safe builder for the `details` map passed to Spring Actuator's `Health` object:

```java
import org.sandcastle.starters.common.health.HealthDetails;
import org.springframework.boot.actuate.health.Health;

Health health = Health.up()
    .withDetails(HealthDetails.builder()
        .add("component", "my-service")
        .add("version", buildVersion)        // null-safe: null values are silently skipped
        .add("latencyMs", latency)
        .build())                            // returns Map.copyOf() — immutable and ordered
    .build();
```

`HealthDetails` preserves insertion order and returns an unmodifiable map.

---

## Usage

This module is not published as a standalone dependency for end consumers. It is
transitively included when you depend on any starter in this project:

```groovy
// In your application build.gradle
dependencies {
    // spring-boot-starter-common is transitively included via `api`
    implementation 'org.sandcastle.starter-apps:spring-boot-starter-redis:1.0.0'
}
```

If you are building a new starter in this mono-repo, declare it as:

```groovy
dependencies {
    api project(':spring-boot-starter-common')
}
```
