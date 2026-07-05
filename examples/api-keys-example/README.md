# api-keys-example

Demonstrates [`spring-boot-starter-api-keys`](../../spring-boot-starter-api-keys/README.md): issuing
an API key and enforcing it on a protected path (`/internal/**`) via the auto-configured filter.
Uses the in-memory store, so it runs with no external setup.

## Run

```bash
./gradlew :examples:api-keys-example:bootRun
```

## Try it

```bash
# Issue a key (public path) — the plaintext is shown ONCE
curl -s -X POST http://localhost:8080/keys \
  -H 'Content-Type: application/json' \
  -d '{"principal":"service-a"}'
# → {"apiKey":"sk_...","id":"..."}

# Call the protected endpoint without a key → 401
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/internal/data
# → 401

# Call it with the issued key → 200
curl -s http://localhost:8080/internal/data -H "X-Api-Key: sk_..."
# → {"data":"secret","principal":"service-a"}
```

Only the key's hash is stored — the plaintext is unrecoverable after issue. Revoke a key
programmatically via `ApiKeyService.revoke(id)`.
