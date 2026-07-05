# spring-boot-starter-outbox

A Spring Boot auto-configuration starter that implements the **Transactional Outbox pattern** — the reliable solution to the dual-write problem that arises when a service needs to atomically update its database and publish a message to a broker.

---

## The Problem

Publishing directly to a message broker inside a database transaction is unreliable:

- The broker publish may succeed but the database transaction rolls back → phantom message
- The database transaction commits but the broker publish fails → lost message

The Outbox pattern solves this by writing events **to the database inside the same transaction** as the business data, then using a separate relay process to forward committed events to the broker.

---

## How It Works

```
Business service (@Transactional)
  │
  ├─ Save business entity        ─┐
  └─ OutboxEventPublisher.publish ─┘ → one atomic DB transaction
                                         │
                              [DB commit]
                                         │
                         OutboxEventRelay (@Scheduled)
                              │  polls outbox_events WHERE status='PENDING'
                              │
                              ├─ MessageBrokerAdapter.send()
                              │    Kafka: outbox.<EVENT_TYPE>
                              │    RabbitMQ: outbox exchange + routing key
                              │
                              └─ UPDATE status='PROCESSED'
```

At-least-once delivery is guaranteed. Consumers should use the event `id` (UUID) as a deduplication key.

---

## Features

- **Atomic outbox writes** — event rows are committed within the business transaction
- **`@Scheduled` relay** — configurable polling interval, no external coordinator required
- **Pluggable broker adapters** — Kafka and RabbitMQ provided; custom adapters via `MessageBrokerAdapter` bean
- **Automatic retry with backoff** — failed events are retried up to `maxRetries` times
- **Terminal FAILED state** — exhausted events are marked and skipped, preventing infinite loops
- **`@ConditionalOnMissingBean`** on all components — full override support
- **No Flyway dependency** — schema DDL provided for copy-paste; consumer manages migrations

---

## Requirements

- Spring Boot 4.x
- Java 21
- Spring Data JPA (PostgreSQL recommended)
- Apache Kafka **or** RabbitMQ (whichever you configure)

---

## Installation

**Gradle:**
```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-outbox:1.0.0'
// Plus your broker dependency:
implementation 'org.springframework.kafka:spring-kafka'
// or:
implementation 'org.springframework.boot:spring-boot-starter-amqp'
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.saumilp.starters</groupId>
    <artifactId>spring-boot-starter-outbox</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Database Schema

Create the `outbox_events` table before starting the application. Place this in a Flyway migration file (e.g., `V1__create_outbox_events.sql`):

```sql
CREATE TABLE outbox_events (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100)  NOT NULL,
    aggregate_id   VARCHAR(255)  NOT NULL,
    event_type     VARCHAR(100)  NOT NULL,
    payload        TEXT          NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    retry_count    INTEGER       NOT NULL DEFAULT 0,
    error_message  TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    processed_at   TIMESTAMPTZ
);

CREATE INDEX idx_outbox_status_created ON outbox_events(status, created_at);
```

---

## Configuration

All properties are under `spring.outbox.*`:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.outbox.enabled` | `boolean` | `true` | Disables all beans when `false` |
| `spring.outbox.relay-interval-ms` | `long` | `5000` | Fixed delay (ms) between relay polling cycles |
| `spring.outbox.max-retries` | `int` | `3` | Max relay attempts before marking an event FAILED |
| `spring.outbox.broker` | `String` | `"kafka"` | Broker type: `kafka` or `rabbitmq` |
| `spring.outbox.kafka-topic-prefix` | `String` | `"outbox."` | Prefix prepended to `eventType` to form the Kafka topic |
| `spring.outbox.rabbit-exchange` | `String` | `"outbox"` | RabbitMQ exchange name |

---

## Configuration Examples

**Kafka:**
```yaml
spring:
  outbox:
    enabled: true
    relay-interval-ms: 3000
    max-retries: 5
    broker: kafka
    kafka-topic-prefix: "events."

  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

**RabbitMQ:**
```yaml
spring:
  outbox:
    enabled: true
    broker: rabbitmq
    rabbit-exchange: domain-events

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

---

## Usage

Inject `OutboxEventPublisher` into your service and call `publish()` **inside a `@Transactional` method**:

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventPublisher outboxPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(request));

        // Atomically record the event in the outbox
        outboxPublisher.publish(
            "Order",                        // aggregateType
            order.getId().toString(),       // aggregateId (Kafka partition key)
            "ORDER_CREATED",               // eventType (→ topic suffix or routing key)
            objectMapper.writeValueAsString(order)  // JSON payload
        );

        return order;
    }
}
```

The relay will pick up the `ORDER_CREATED` event within `relay-interval-ms` milliseconds after the transaction commits and forward it to:
- Kafka topic: `outbox.ORDER_CREATED`
- RabbitMQ exchange `outbox` with routing key `ORDER_CREATED`

---

## Custom Broker Adapter

Implement `MessageBrokerAdapter` and declare it as a Spring bean to override the default:

```java
@Bean
public MessageBrokerAdapter customBrokerAdapter() {
    return event -> {
        // Forward to SNS, SQS, Pulsar, NATS, etc.
        myBrokerClient.publish(event.getEventType(), event.getPayload());
    };
}
```

---

## Supported Versions

| Component | Version |
|-----------|---------|
| Spring Boot | 4.x |
| Java | 21+ |
| Spring Kafka | 3.x |
| Spring AMQP | 3.x |
| PostgreSQL | 13+ (for `gen_random_uuid()`) |

---

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for coding standards, Javadoc requirements, and the pull request process.

---

*Part of [spring-boot-starters](https://github.com/SaumilP/spring-boot-starters) — production-ready Spring Boot auto-configurations.*
