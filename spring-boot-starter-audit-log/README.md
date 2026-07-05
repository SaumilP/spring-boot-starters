# spring-boot-starter-audit-log

Annotation-driven audit logging for Spring Boot applications. Add `@Audited` to any Spring-managed method and every invocation — successful or failed — is captured as a structured event and routed to one or more configurable sinks: a structured SLF4J log, a relational database via JPA, or your own custom sink.

Audit logs are a compliance requirement in most regulated industries (PCI DSS, HIPAA, SOC 2) and an invaluable debugging tool when tracing who did what and when. This starter takes care of the cross-cutting plumbing so your application code stays clean.

---

## Features

- `@Audited` annotation — place on any Spring bean method; no interface or base class required
- **Pluggable sinks** — SLF4J logging (default) and JPA persistence (opt-in); combine them via the built-in `CompositeAuditEventSink`
- **Spring Security integration** — automatically resolves the authenticated principal as the actor; falls back to `"anonymous"` when Security is absent
- **Custom actor resolution** — replace `ActorResolver` with your own bean (JWT claim, tenant ID, API key, etc.)
- **Failure events captured** — exceptions do not suppress audit records; outcome is `FAILURE` and the exception message is included
- **Duration tracking** — every event includes the method execution time in milliseconds
- **Resource ID extraction** — use `resourceIdExpression` to record the affected entity ID directly from method parameters
- **No Flyway dependency** — the starter never manages schema; DDL is provided for you to run
- All beans use `@ConditionalOnMissingBean` — every component is replaceable

---

## Requirements

| Dependency | Version |
|---|---|
| Spring Boot | 4.x |
| Java | 21+ |
| Spring Data JPA | Optional (for JPA sink) |
| Spring Security | Optional (for principal resolution) |

Redis is **not** required.

---

## Installation

**Gradle:**

```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-audit-log:1.0.0'
```

**Maven:**

```xml
<dependency>
    <groupId>io.github.saumilp.starters</groupId>
    <artifactId>spring-boot-starter-audit-log</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Quick Start

Annotate any Spring-managed method:

```java
import io.github.saumilp.starters.auditlog.annotation.Audited;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Audited(action = "CREATE_ORDER", resource = "Order", resourceIdExpression = "#customerId")
    public Order createOrder(Long customerId, OrderRequest request) {
        // ... business logic
    }

    @Audited(action = "CANCEL_ORDER", resource = "Order", resourceIdExpression = "#orderId")
    public void cancelOrder(Long orderId) {
        // ...
    }
}
```

With default configuration the `AUDIT` logger emits a structured line after each call:

```
INFO  AUDIT - AUDIT action=CREATE_ORDER resource=Order resourceId=42 actor=alice outcome=SUCCESS durationMs=87
WARN  AUDIT - AUDIT action=CANCEL_ORDER resource=Order resourceId=99 actor=bob outcome=FAILURE durationMs=12 error=Order not found
```

---

## Configuration

All properties are bound from the `spring.audit-log.*` namespace.

| Property | Type | Default | Description |
|---|---|---|---|
| `spring.audit-log.enabled` | `boolean` | `true` | Globally enable or disable audit interception |
| `spring.audit-log.logging-sink.enabled` | `boolean` | `true` | Enable the SLF4J structured log sink |
| `spring.audit-log.jpa-sink.enabled` | `boolean` | `false` | Enable the JPA database persistence sink |

### Full application.yml example

```yaml
spring:
  audit-log:
    enabled: true
    logging-sink:
      enabled: true      # emit structured log lines via SLF4J (AUDIT logger)
    jpa-sink:
      enabled: true      # persist to audit_events table; requires schema below
```

---

## Database Schema (JPA Sink)

The JPA sink requires the `audit_events` table. Create it with a Flyway migration named `V1__create_audit_events.sql` in your application's `db/migration` directory:

```sql
CREATE TABLE audit_events (
    id            BIGSERIAL PRIMARY KEY,
    action        VARCHAR(100)  NOT NULL,
    resource      VARCHAR(100),
    resource_id   VARCHAR(255),
    actor         VARCHAR(255)  NOT NULL,
    outcome       VARCHAR(10)   NOT NULL,
    error_message TEXT,
    occurred_at   TIMESTAMPTZ   NOT NULL,
    duration_ms   BIGINT        NOT NULL
);

CREATE INDEX idx_audit_events_actor       ON audit_events(actor);
CREATE INDEX idx_audit_events_action      ON audit_events(action);
CREATE INDEX idx_audit_events_occurred_at ON audit_events(occurred_at);
```

> The starter itself has **no Flyway dependency**. You are responsible for running
> this migration using your existing database migration toolchain.

---

## Custom Actor Resolution

By default the starter reads the current principal from Spring Security's `SecurityContextHolder`. To use a different source — a JWT claim, a custom thread-local, or an API key header — declare your own `ActorResolver` bean:

```java
@Component
public class JwtActorResolver implements ActorResolver {

    @Override
    public String resolve() {
        // Extract subject claim from your JWT filter's ThreadLocal, for example:
        return JwtContext.getCurrentSubject().orElse("anonymous");
    }
}
```

Because `ActorResolver` is registered with `@ConditionalOnMissingBean`, your bean automatically takes precedence.

---

## Custom Sinks

Implement `AuditEventSink` and declare it as a Spring bean to add a custom destination (Kafka, webhook, cloud audit trail, etc.):

```java
@Component
public class KafkaAuditEventSink implements AuditEventSink {

    private final KafkaTemplate<String, AuditEvent> kafka;

    public KafkaAuditEventSink(KafkaTemplate<String, AuditEvent> kafka) {
        this.kafka = kafka;
    }

    @Override
    public void publish(AuditEvent event) {
        kafka.send("audit-events", event.action(), event);
    }
}
```

All `AuditEventSink` beans in the application context are automatically collected by the `CompositeAuditEventSink` and invoked in registration order. A failure in any single sink is caught, logged, and swallowed so the remaining sinks still receive the event.

---

## Overriding Beans

Every bean registered by this starter uses `@ConditionalOnMissingBean`. Declare your own bean of the same type in your `@Configuration` class to replace any starter-provided component:

| Bean type | What it does |
|---|---|
| `ActorResolver` | Resolves the current actor identity |
| `LoggingAuditEventSink` | SLF4J sink |
| `JpaAuditEventSink` | JPA persistence sink |
| `CompositeAuditEventSink` | Fan-out to all sinks |
| `AuditLogAspect` | AOP interceptor |

---

## Supported Versions

| Starter version | Spring Boot | Java |
|---|---|---|
| 1.0.0 | 4.x | 21+ |

---

[GitHub — SaumilP/spring-boot-starters](https://github.com/SaumilP/spring-boot-starters)
