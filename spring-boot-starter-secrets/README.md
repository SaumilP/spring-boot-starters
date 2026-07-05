# spring-boot-starter-secrets

A unified `SecretSource` abstraction so application code reads secrets the same way regardless of
where they live — the environment, or AWS Secrets Manager — swappable by configuration alone.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-secrets:1.0.0'
    // for the AWS provider:
    implementation 'software.amazon.awssdk:secretsmanager'
}
```

---

## Usage

```java
@Service
class PaymentService {
    private final SecretSource secrets;

    PaymentService(SecretSource secrets) {
        this.secrets = secrets;
    }

    void charge() {
        String apiKey = secrets.get("payments/api-key")
            .orElseThrow(() -> new IllegalStateException("payments/api-key not configured"));
        // ...
    }
}
```

`SecretSource#get` returns `Optional<String>` — a missing secret is `Optional.empty()`, never an
exception.

---

## Providers

| `spring.secrets.provider` | Bean | Backing store |
|---|---|---|
| `env` (default) | `EnvironmentSecretSource` | Spring `Environment` (env vars, system props, config files) |
| `aws` | `AwsSecretsManagerSecretSource` | AWS Secrets Manager (AWS SDK v2) |

Declare your own `SecretSource` bean to plug in another backend; the auto-configured one backs off.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.secrets.enabled` | `true` | Master switch |
| `spring.secrets.provider` | `env` | `env` or `aws` |
| `spring.secrets.aws.region` | `us-east-1` | Region for the Secrets Manager client |
| `spring.secrets.aws.endpoint-override` | *(unset)* | Custom endpoint (e.g. LocalStack) |

The AWS client uses the standard AWS SDK credential provider chain (environment, profile, instance
role, ...).

---

## Notes

- HashiCorp Vault is a planned provider; the `SecretSource` SPI makes adding it a drop-in.
- Dynamic refresh (`@RefreshScope`-style reloading) is a planned enhancement.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
