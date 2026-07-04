# spring-boot-starter-feature-flags

A Spring Boot auto-configuration starter that integrates the [OpenFeature](https://openfeature.dev/) SDK
for vendor-neutral feature flag evaluation. Control feature rollout, run A/B experiments, and gate
new functionality at runtime — without coupling your code to any specific flag-management backend.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.saumilp.starters/spring-boot-starter-feature-flags.svg)](https://central.sonatype.com/search?q=io.github.saumilp.starters)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)](https://spring.io/projects/spring-boot)

---

## Why OpenFeature?

Most feature-flag libraries lock you into a proprietary SDK. If you later switch from LaunchDarkly
to Unleash, or from Unleash to a homegrown system, you have to rewrite all flag-evaluation call
sites. OpenFeature solves this with a vendor-neutral client API backed by swappable **providers**.
This starter wires up the client, registers a provider, and exposes an `@FeatureEnabled` annotation
so you never call the OpenFeature SDK directly.

---

## Features

- **`@FeatureEnabled`** annotation for method-level flag gating with zero boilerplate
- **File-based provider** — evaluate flags from a plain YAML file; great for local development and CI
- **Hard-gate mode** (`disableWhenOff = true`) — throws `FeatureDisabledException` when a flag is off
- **Advisory mode** (default) — lets the method proceed even when the flag is off; useful for
  gradual rollout tracking without blocking traffic
- **OpenFeature vendor-neutral** — swap the provider (Unleash, LaunchDarkly, Flagsmith, etc.)
  by declaring a single bean
- **`@ConditionalOnMissingBean`** throughout — every auto-configured bean is overrideable

---

## Requirements

| Component     | Version      |
|---------------|--------------|
| Java          | 21+          |
| Spring Boot   | 4.0.4+       |
| OpenFeature   | 1.11.0       |

No database or external service is required when using the default file-based provider.

---

## Installation

**Gradle:**
```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-feature-flags:1.0.0'
```

**Maven:**
```xml
<dependency>
  <groupId>io.github.saumilp.starters</groupId>
  <artifactId>spring-boot-starter-feature-flags</artifactId>
  <version>1.0.0</version>
</dependency>
```

---

## Quick Start

**Step 1** — Create `src/main/resources/feature-flags.yml`:
```yaml
new-checkout: true
beta-dashboard: false
dark-mode: true
```

**Step 2** — Annotate your method:
```java
@FeatureEnabled(flag = "new-checkout", disableWhenOff = true)
@PostMapping("/checkout/v2")
public ResponseEntity<OrderConfirmation> checkoutV2(@RequestBody CheckoutRequest req) {
    // Only reachable when new-checkout = true
    return orderService.processV2(req);
}
```

**Step 3** — Handle the disabled case (optional — only needed when `disableWhenOff = true`):
```java
@ControllerAdvice
public class FeatureFlagExceptionHandler {

    @ExceptionHandler(FeatureDisabledException.class)
    public ResponseEntity<Void> handleDisabled(FeatureDisabledException ex) {
        // e.g. redirect to v1, or return 404
        return ResponseEntity.notFound().build();
    }
}
```

---

## Configuration

All properties are under the `spring.feature-flags` namespace.

| Property                         | Type    | Default                        | Description                                                     |
|----------------------------------|---------|--------------------------------|-----------------------------------------------------------------|
| `spring.feature-flags.enabled`   | boolean | `true`                         | Globally enable/disable flag evaluation and AOP registration    |
| `spring.feature-flags.file-path` | String  | `classpath:feature-flags.yml`  | Spring resource path to the YAML flag-definition file           |
| `spring.feature-flags.provider`  | String  | `file`                         | Provider hint: `file` or `unleash`. See custom provider section |

**Full example:**
```yaml
spring:
  feature-flags:
    enabled: true
    file-path: classpath:flags/feature-flags.yml
    provider: file
```

---

## @FeatureEnabled Reference

| Attribute       | Type    | Default | Description                                                                   |
|-----------------|---------|---------|-------------------------------------------------------------------------------|
| `flag`          | String  | —       | The feature flag key to evaluate (required)                                   |
| `disableWhenOff`| boolean | `false` | Throw `FeatureDisabledException` when flag is `false`                         |
| `fallbackUrl`   | String  | `""`    | Redirect URL hint for web handlers (not enforced by the aspect automatically) |

---

## Switching to Unleash

1. Add the Unleash OpenFeature provider dependency:
   ```groovy
   implementation 'io.getunleash:unleash-client-java:10.x.x'
   // Unleash OpenFeature provider (community):
   implementation 'dev.openfeature.contrib.providers:unleash:x.y.z'
   ```
2. Register a custom `FeatureProvider` bean — the auto-configuration backs off:
   ```java
   @Bean
   public FeatureProvider unleashFeatureProvider() {
       UnleashConfig config = UnleashConfig.builder()
           .appName("my-app")
           .instanceId("my-instance")
           .unleashAPI("https://unleash.example.com/api")
           .apiKey("my-server-key")
           .build();
       return new UnleashProvider(new DefaultUnleash(config));
   }
   ```
3. Update the provider hint (optional, informational):
   ```yaml
   spring.feature-flags.provider: unleash
   ```

---

## Overriding Beans

Every auto-configured bean uses `@ConditionalOnMissingBean`. Declare your own to take full control:

| Bean type            | Override to…                                              |
|----------------------|----------------------------------------------------------|
| `FeatureProvider`    | Use a different backend (Unleash, LaunchDarkly, etc.)    |
| `OpenFeatureAPI`     | Customise the SDK singleton (e.g., add hooks, domain)    |
| `FeatureEnabledAspect` | Customise flag-evaluation or interception behaviour    |

---

## Supported Versions

| Starter version | Spring Boot | Java | OpenFeature SDK |
|-----------------|-------------|------|-----------------|
| 1.0.0           | 4.0.4+      | 21   | 1.11.0          |

---

## Contributing

See the root [CONTRIBUTING.md](../CONTRIBUTING.md) for code standards, Javadoc requirements,
and the new-starter checklist.

---

[GitHub](https://github.com/SaumilP/spring-boot-starters) · Apache License 2.0
