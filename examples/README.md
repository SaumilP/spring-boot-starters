# Examples

Runnable Spring Boot 4 applications demonstrating each starter. Every example ships with a `docker-compose.yml` for its required infrastructure.

## Quick start

```bash
# 1. Start the example's infrastructure
cd examples/<example-name>
docker compose up -d

# 2. Run the example application
cd ../..
./gradlew :examples:<example-name>:bootRun
```

## Examples

| Example | Starter demonstrated | Infrastructure | Description |
|---|---|---|---|
| [redis-example](redis-example/README.md) | `spring-boot-starter-redis` | Redis 7 | String cache via `RedisUtil` (GET / PUT / DELETE) |
| [minio-example](minio-example/README.md) | `spring-boot-starter-minio` | MinIO | File upload, download, delete, and presigned URLs |
| [rate-limiting-example](rate-limiting-example/README.md) | `spring-boot-starter-rate-limiting` | Redis 7 | `@RateLimit` on endpoints with sliding-window enforcement |
| [idempotency-example](idempotency-example/README.md) | `spring-boot-starter-idempotency` | Redis 7 | `Idempotency-Key` header — duplicate POST requests are deduplicated |
| [audit-log-example](audit-log-example/README.md) | `spring-boot-starter-audit-log` | None | `@Audited` writes structured audit events to the application log |
| [feature-flags-example](feature-flags-example/README.md) | `spring-boot-starter-feature-flags` | None | `@FeatureEnabled` gates endpoints behind file-based feature flags |
| [llm-client-example](llm-client-example/README.md) | `spring-boot-starter-llm-client` | Ollama | `POST /chat` calls a local LLM via `LlmClient` |
| [multitenancy-example](multitenancy-example/README.md) | `spring-boot-starter-multitenancy` | None | Header-based tenant resolution via `TenantContext` |
| [aws-s3-example](aws-s3-example/README.md) | `spring-boot-starter-aws-s3` | LocalStack 3.4 | Upload, download, presign, and delete objects against a local S3 emulator |
| [outbox-example](outbox-example/README.md) | `spring-boot-starter-outbox` | PostgreSQL 16, Kafka | Transactional Outbox: order placement + atomic `ORDER_PLACED` event relay to Kafka |

## Infrastructure summary

| Example | Docker services |
|---|---|
| redis-example | `redis:7-alpine` on port 6379 |
| minio-example | `minio/minio:latest` on ports 9000 (API) + 9001 (console) |
| rate-limiting-example | `redis:7-alpine` on port 6379 |
| idempotency-example | `redis:7-alpine` on port 6379 |
| audit-log-example | None |
| feature-flags-example | None |
| llm-client-example | `ollama/ollama:latest` on port 11434 |
| multitenancy-example | None |
| aws-s3-example | `localstack/localstack:3.4` on port 4566 |
| outbox-example | `postgres:16-alpine` on port 5432, Confluent Kafka on port 9092 |
