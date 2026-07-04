# outbox-example

Demonstrates the `spring-boot-starter-outbox` starter with the Transactional Outbox pattern.  
Orders are saved to PostgreSQL and an `ORDER_PLACED` event is atomically written to the outbox table. A scheduled relay then publishes events to Kafka.

## Prerequisites

- Java 21
- Docker + Docker Compose

## Running locally

```bash
# Start PostgreSQL and Kafka
docker compose up -d

# Run the example (JPA creates the tables automatically via ddl-auto=update)
./gradlew :examples:outbox-example:bootRun
```

## API

### Place an order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"details": "2x Widget A, 1x Widget B"}'
# {"orderId":"...","status":"PLACED","message":"Order placed and outbox event queued"}
```

### With a specific order ID
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId": "order-001", "details": "1x Special Item"}'
```

## How it works

1. `OrderService.placeOrder()` runs inside a `@Transactional` boundary
2. The `Order` entity is saved to the `orders` table
3. `OutboxEventPublisher.publish()` atomically inserts an `OutboxEvent` row into the `outbox_events` table
4. Both writes commit or rollback together — no dual-write inconsistency
5. The `OutboxEventRelay` scheduler (every 5 s) picks up `PENDING` events and sends them to the `example.Order` Kafka topic
6. Successfully published events are marked `PROCESSED`; events that fail are retried up to `max-retries` times then marked `FAILED`

## Consuming events from Kafka

```bash
# List topics
docker exec -it <kafka-container> kafka-topics --bootstrap-server localhost:9092 --list

# Consume ORDER_PLACED events
docker exec -it <kafka-container> kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic example.Order \
  --from-beginning
```

## Outbox table DDL (for production / Flyway)

```sql
CREATE TABLE outbox_events (
    id            UUID         PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    payload        TEXT,
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    retry_count    INT          NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL,
    processed_at   TIMESTAMP
);
CREATE INDEX idx_outbox_status ON outbox_events(status, created_at);
```
