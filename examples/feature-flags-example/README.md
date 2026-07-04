# feature-flags-example

Demonstrates the `spring-boot-starter-feature-flags` starter with a file-based feature flag provider.

## Prerequisites

- Java 21 (no external infrastructure needed)

## Running locally

```bash
./gradlew :examples:feature-flags-example:bootRun
```

## API

### Check flag status
```bash
curl http://localhost:8080/feature/status
# {"new-dashboard":true,"beta-search":false}
```

### Access a feature-gated endpoint (flag ON)
```bash
curl http://localhost:8080/feature/new-dashboard
# {"ui":"new-dashboard","status":"active"}
```

### Toggle a flag off and restart
Edit `src/main/resources/feature-flags.yml`:
```yaml
new-dashboard: false
```
Then restart the app. Now the guarded endpoint returns `FeatureDisabledException` (mapped to 500 by default — add a `@ControllerAdvice` for custom HTTP status).

## Switching to Unleash

1. Add the Unleash SDK dependency to `build.gradle`
2. Declare a custom `FeatureProvider` bean that wraps the Unleash client
3. The auto-configuration picks it up via `@ConditionalOnMissingBean`
