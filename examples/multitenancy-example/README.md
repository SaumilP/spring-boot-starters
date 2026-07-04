# multitenancy-example

Demonstrates the `spring-boot-starter-multitenancy` starter with header-based tenant resolution.

## Prerequisites

- Java 21 (no external infrastructure needed for header resolution)

## Running locally

```bash
./gradlew :examples:multitenancy-example:bootRun
```

## API

### Request with tenant header
```bash
curl -H "X-Tenant-Id: acme" http://localhost:8080/tenant/info
# {"tenant":"acme","status":"resolved"}
```

### Request without tenant header (require-tenant=false, so it passes through)
```bash
curl http://localhost:8080/tenant/info
# {"tenant":"none","status":"not-set"}
```

### Require tenant (return 400 when missing)
Update `application.yml`:
```yaml
spring:
  multitenancy:
    require-tenant: true
```
Now a request without `X-Tenant-Id` returns:
```
HTTP/1.1 400 Bad Request
{"error":"Missing tenant identifier"}
```

## Adding Hibernate multi-tenancy

To route JPA queries to per-tenant schemas, add to `application.yml`:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        multiTenancy: SCHEMA
        multi_tenant_connection_provider: io.github.saumilp.starters.multitenancy.hibernate.SchemaMultiTenantConnectionProvider
        tenant_identifier_resolver: io.github.saumilp.starters.multitenancy.hibernate.TenantIdentifierResolver
```

See the starter README for the full Flyway DDL to create per-tenant schemas.
