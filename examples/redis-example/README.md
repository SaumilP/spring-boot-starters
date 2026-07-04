# redis-example

Demonstrates the `spring-boot-starter-redis` starter with a simple key/value cache REST API.

## Prerequisites

- Java 21
- Docker and Docker Compose

## Running locally

**1. Start Redis:**
```bash
docker compose up -d
```

**2. Run the application:**
```bash
./gradlew :examples:redis-example:bootRun
```

## API

### Store a value
```bash
curl -X PUT http://localhost:8080/cache/greeting \
  -H "Content-Type: application/json" \
  -d '"Hello, Redis!"'
# {"key":"greeting","status":"stored"}
```

### Read a value
```bash
curl http://localhost:8080/cache/greeting
# {"key":"greeting","value":"Hello, Redis!"}
```

### Delete a value
```bash
curl -X DELETE http://localhost:8080/cache/greeting
# {"key":"greeting","status":"deleted"}
```

### Health check
```bash
curl http://localhost:8080/actuator/health
```

## What this demonstrates

- Auto-configured `RedisUtil` injected directly into the controller
- `RedisTemplate` with Jackson JSON serialization (registered by the starter)
- Redis health indicator at `/actuator/health`
