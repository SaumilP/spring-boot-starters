# spring-boot-starter-api-keys

**Issue, hash, validate, and revoke API keys** for service-to-service authentication — one
dependency gives you key management plus a servlet filter that enforces a valid `X-Api-Key` on the
paths you choose.

Keys are high-entropy random tokens; only their **hash** is stored, so a leaked store cannot be used
to authenticate. The plaintext is shown exactly once at issue time.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-api-keys:1.0.0'
}
```

---

## What you get

| Bean | Condition | Purpose |
|---|---|---|
| `ApiKeyService` | always | Issue / validate / revoke keys |
| `ApiKeyStore` (`InMemoryApiKeyStore`) | always | Stores hashed key metadata |
| `ApiKeyHasher` | always | SHA-256 hashing with constant-time compare |
| `ApiKeyAuthFilter` (registered) | servlet web app | Enforces a valid key on protected paths |

All beans are `@ConditionalOnMissingBean`; supply a JPA/Redis-backed `ApiKeyStore` to persist keys.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.api-keys.enabled` | `true` | Master switch |
| `spring.api-keys.header-name` | `X-Api-Key` | Header carrying the key |
| `spring.api-keys.hash-algorithm` | `SHA-256` | Digest used at rest |
| `spring.api-keys.key-bytes` | `32` | Entropy of generated keys |
| `spring.api-keys.prefix` | `sk` | Prefix on generated keys |
| `spring.api-keys.protected-paths` | `[]` | Ant-style paths the filter enforces (empty = enforce nothing) |

---

## Usage

### Issue a key

```java
IssuedApiKey issued = apiKeyService.issue("service-a", Set.of("orders:read"));
// Return issued.plaintext() to the caller ONCE — it is never recoverable.
```

### Protect endpoints

```yaml
spring:
  api-keys:
    protected-paths:
      - /internal/**
```

Requests to `/internal/**` now require a valid `X-Api-Key` header or receive `401`. On success the
resolved `ApiKey` is exposed as the request attribute
`io.github.saumilp.apikeys.principal`:

```java
@GetMapping("/internal/data")
String data(HttpServletRequest request) {
    ApiKey key = (ApiKey) request.getAttribute(ApiKeyAuthFilter.PRINCIPAL_ATTRIBUTE);
    return "hello " + key.principal();
}
```

### Revoke

```java
apiKeyService.revoke(issued.apiKey().id());
```

---

## Notes

- Pair with [`spring-boot-starter-rate-limiting`](../spring-boot-starter-rate-limiting/README.md) to
  apply per-key request limits.
- The default store is in-memory and resets on restart — provide a persistent `ApiKeyStore` bean for
  production.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
