# spring-boot-starter-security-jwt

An **opinionated JWT resource-server security setup** — one dependency gives every endpoint
bearer-token authentication, secure response headers, and CORS defaults, with no hand-rolled
`SecurityFilterChain`.

The chain is stateless (no session), CSRF-disabled (appropriate for token APIs), authenticates
every request except the configured public paths, and validates JWTs against your JWKS or issuer.

---

## Installation

```groovy
dependencies {
    implementation 'io.github.saumilp.starters:spring-boot-starter-security-jwt:1.0.0'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
}
```

---

## What you get

| Bean | Condition | Purpose |
|---|---|---|
| `SecurityFilterChain` | servlet web app | Stateless JWT resource server with secure headers |
| `JwtDecoder` | `jwk-set-uri` or `issuer-uri` set | Validates JWT signatures |
| `CorsConfigurationSource` | an allowed origin configured | Applies CORS to the chain |

All beans are `@ConditionalOnMissingBean`; declare your own `SecurityFilterChain` to take full
control.

---

## Configuration

| Property | Default | Description |
|---|---|---|
| `spring.security-jwt.enabled` | `true` | Master switch |
| `spring.security-jwt.jwk-set-uri` | — | JWKS endpoint (takes precedence over issuer) |
| `spring.security-jwt.issuer-uri` | — | OIDC issuer location (JWKS discovered from it) |
| `spring.security-jwt.public-paths` | `[]` | Ant-style paths that bypass authentication |
| `spring.security-jwt.cors.allowed-origins` | `[]` | Exact CORS origins |
| `spring.security-jwt.cors.allowed-origin-patterns` | `[]` | CORS origin patterns |
| `spring.security-jwt.cors.allowed-methods` | `GET,POST,PUT,PATCH,DELETE,OPTIONS` | Allowed methods |
| `spring.security-jwt.cors.allowed-headers` | `*` | Allowed headers |
| `spring.security-jwt.cors.allow-credentials` | `false` | Allow cookies / auth headers |
| `spring.security-jwt.cors.max-age-seconds` | `3600` | Pre-flight cache duration |
| `spring.security-jwt.headers.hsts` | `true` | Emit HSTS |
| `spring.security-jwt.headers.hsts-max-age-seconds` | `31536000` | HSTS max-age |
| `spring.security-jwt.headers.hsts-include-subdomains` | `true` | Include subdomains in HSTS |
| `spring.security-jwt.headers.frame-options` | `DENY` | `DENY`, `SAMEORIGIN`, or `DISABLE` |
| `spring.security-jwt.headers.content-type-options` | `true` | Emit `X-Content-Type-Options: nosniff` |

### Example

```yaml
spring:
  security-jwt:
    jwk-set-uri: https://issuer.example.com/.well-known/jwks.json
    public-paths:
      - /actuator/health
      - /public/**
    cors:
      allowed-origins: [ "https://app.example.com" ]
      allow-credentials: true
```

---

## Overriding

Declare your own `SecurityFilterChain` bean and the starter's default disappears completely — you
keep the `JwtDecoder` and property binding, but own the routing rules.

---

## License

Apache License 2.0 — see [LICENSE](../LICENSE).
