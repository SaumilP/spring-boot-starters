# idempotency-example

Demonstrates the `spring-boot-starter-idempotency` starter. Sending `POST /orders` twice with the same `Idempotency-Key` header returns the cached response on the second call.

## Prerequisites

- Java 21
- Docker and Docker Compose

## Running locally

```bash
docker compose up -d
./gradlew :examples:idempotency-example:bootRun
```

## Demo

### First request — executes the handler, returns 201

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-key-001" \
  -d '{"item":"laptop","quantity":"1"}'
```
```
HTTP/1.1 201 Created
{"orderId":"3fa85f64-...","item":"laptop","quantity":"1","createdAt":"..."}
```

### Second request — replayed from cache, returns 200 with replay header

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-key-001" \
  -d '{"item":"laptop","quantity":"1"}'
```
```
HTTP/1.1 200 OK
X-Idempotency-Replayed: true
{"orderId":"3fa85f64-...","item":"laptop","quantity":"1","createdAt":"..."}
```

Note the **same `orderId`** — the handler was not re-executed.

### Concurrent duplicate — returns 409

If two requests with the same key arrive simultaneously while the first is still processing, the second receives:
```
HTTP/1.1 409 Conflict
{"status":409,"error":"Conflict","message":"A request with this Idempotency-Key is already in progress."}
```
