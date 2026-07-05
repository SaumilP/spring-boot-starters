# data-privacy-example

Demonstrates [`spring-boot-starter-data-privacy`](../../spring-boot-starter-data-privacy/README.md):
JPA field-level encryption of a `nationalIdNumber` and masked display via the masking service.

Backed by an in-memory H2 database, so it runs with no external setup.

## Run

```bash
./gradlew :examples:data-privacy-example:bootRun
```

## Try it

```bash
# Store a customer — the national ID is encrypted at rest
curl -s -X POST http://localhost:8080/customers \
  -H 'Content-Type: application/json' \
  -d '{"name":"Ada Lovelace","nationalIdNumber":"AB1234567"}'
# → {"id":1,"status":"stored (national ID encrypted)"}

# Read it back masked for display (keeps the last 4 characters)
curl -s http://localhost:8080/customers/1
# → {"name":"Ada Lovelace","nationalIdNumber":"*****4567"}

# Read the decrypted value — encryption is transparent on read
curl -s http://localhost:8080/customers/1/raw
# → {"name":"Ada Lovelace","nationalIdNumber":"AB1234567"}
```

The `national_id_number` column stores ciphertext (AES-256-GCM); only the application, holding the
configured key, can decrypt it.

## Configuration

The encryption key is read from `spring.data-privacy.encryption.key`. This example uses a demo
default; supply a strong, random secret via the `FIELD_ENCRYPTION_KEY` environment variable in any
real deployment.
