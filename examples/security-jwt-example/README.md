# security-jwt-example

Demonstrates [`spring-boot-starter-security-jwt`](../../spring-boot-starter-security-jwt/README.md):
an opinionated JWT resource-server filter chain with a public path, a secured path, secure response
headers, and CORS.

## Run

```bash
./gradlew :examples:security-jwt-example:bootRun
```

Set `JWK_SET_URI` to your identity provider's JWKS endpoint to validate real tokens.

## Try it

```bash
# Public path — no token required
curl -s http://localhost:8080/public/ping
# → pong

# Secured path — no token → 401
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/secure/hello
# → 401

# Secure headers are applied to every response
curl -s -D - http://localhost:8080/public/ping | grep -i 'x-frame-options\|x-content-type-options\|strict-transport'
```

With a valid bearer token from your issuer:

```bash
curl -s http://localhost:8080/secure/hello -H "Authorization: Bearer $TOKEN"
# → hello, authenticated caller
```
