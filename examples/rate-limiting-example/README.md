# rate-limiting-example

Demonstrates the `spring-boot-starter-rate-limiting` starter with per-IP sliding-window limits and named limits.

## Prerequisites

- Java 21
- Docker and Docker Compose

## Running locally

```bash
docker compose up -d
./gradlew :examples:rate-limiting-example:bootRun
```

## API

### Search endpoint (5 requests per minute per IP)

```bash
curl "http://localhost:8080/api/search?q=spring"
# {"query":"spring","results":["Result 1 for spring","Result 2 for spring"]}
```

**Trigger the rate limit** by calling it 6 times rapidly:
```bash
for i in {1..6}; do curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/api/search?q=test"; done
# 200 200 200 200 200 429
```

**429 response body:**
```json
{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded for key 'rl:127.0.0.1:...'",
  "retryAfterSeconds": 60,
  "timestamp": "2024-07-04T10:15:30Z"
}
```

### Admin search endpoint (named limit: 100 req/60 sec)

```bash
curl "http://localhost:8080/api/admin/search?q=spring"
```

## How it works

- `@RateLimit(requests=5, per=TimeUnit.MINUTES)` — method-level annotation enforced by AOP
- Keys are built from the remote IP + method name
- Redis sliding-window Lua script ensures atomic, distributed enforcement
- In-memory fallback activates automatically when Redis is unavailable
