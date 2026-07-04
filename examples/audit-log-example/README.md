# audit-log-example

Demonstrates the `spring-boot-starter-audit-log` starter. Every call to `POST /users` and `DELETE /users/{id}` is automatically captured in the audit trail via `@Audited`.

## Prerequisites

- Java 21 (no external infrastructure needed for the logging sink)

## Running locally

```bash
./gradlew :examples:audit-log-example:bootRun
```

## API

### Create a user
```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"alice"}'
# {"id":"3fa85f64-...","name":"alice","status":"created"}
```

**Log output:**
```
INFO  AUDIT action=CREATE_USER resource=User resourceId=alice actor=anonymous outcome=SUCCESS durationMs=3
```

### Delete a user
```bash
curl -X DELETE http://localhost:8080/users/3fa85f64-...
```

**Log output:**
```
INFO  AUDIT action=DELETE_USER resource=User resourceId=3fa85f64-... actor=anonymous outcome=SUCCESS durationMs=1
```

## Enabling the JPA sink

To persist audit events to PostgreSQL, update `application.yml`:
```yaml
spring:
  audit-log:
    jpa-sink:
      enabled: true
  datasource:
    url: jdbc:postgresql://localhost:5432/audit_demo
```

And run the Flyway migration from the starter's README (`V1__create_audit_events.sql`).
