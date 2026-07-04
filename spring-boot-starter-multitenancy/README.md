# spring-boot-starter-multitenancy

Annotation-free, configuration-driven multitenancy for Spring Boot applications. Automatically
resolves the current tenant from each HTTP request, propagates it via a thread-local context,
and wires Hibernate to scope all JPA operations to the correct PostgreSQL schema.

[![Maven Central](https://img.shields.io/maven-central/v/io.github.saumilp.starters/spring-boot-starter-multitenancy.svg)](https://central.sonatype.com/search?q=io.github.saumilp.starters)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Spring Boot 4.x](https://img.shields.io/badge/Spring%20Boot-4.x-green.svg)](https://spring.io/projects/spring-boot)

---

## Overview

Multi-tenant applications serve multiple customers (tenants) from a single deployment while
keeping their data isolated. This starter implements the **schema-per-tenant** strategy for
PostgreSQL: each tenant's data lives in a dedicated database schema (e.g., `acme`, `beta`),
and the correct schema is selected automatically on every request.

Two isolation strategies are common:

| Strategy | Description | When to use |
|----------|-------------|-------------|
| **Schema-per-tenant** | All tenants share one database; each has their own schema | Most SaaS apps — simpler ops, good isolation |
| **Database-per-tenant** | Each tenant has their own database | Strict compliance requirements, very large tenants |

This starter ships full support for schema-per-tenant. Database-per-tenant requires a custom
`SchemaMultiTenantConnectionProvider` (see below).

---

## Features

- **Automatic tenant resolution** from HTTP headers or subdomain, with a pluggable strategy interface
- **`TenantContext`** — `InheritableThreadLocal` propagation to child threads
- **`TenantResolutionFilter`** — resolves, sets, and clears the tenant context per request
- **HTTP 400** on requests without a tenant (configurable)
- **Hibernate integration** — `SchemaMultiTenantConnectionProvider` (PostgreSQL `SET search_path`) and `TenantIdentifierResolver`
- All beans use `@ConditionalOnMissingBean` for full override support

---

## Requirements

- Java 21
- Spring Boot 4.x
- PostgreSQL (for the schema strategy; other databases require a custom connection provider)
- Hibernate 6.x (included with `spring-boot-starter-data-jpa`)

---

## Installation

**Gradle:**
```groovy
implementation 'io.github.saumilp.starters:spring-boot-starter-multitenancy:1.0.0'
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.saumilp.starters</groupId>
    <artifactId>spring-boot-starter-multitenancy</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Quick Start

### 1. Add dependency and configure

```yaml
spring:
  multitenancy:
    enabled: true
    resolver-type: HEADER        # read tenant from X-Tenant-Id header
    require-tenant: true         # reject requests with no tenant (HTTP 400)
  jpa:
    properties:
      hibernate:
        multiTenancy: SCHEMA
        multi_tenant_connection_provider: io.github.saumilp.starters.multitenancy.hibernate.SchemaMultiTenantConnectionProvider
        tenant_identifier_resolver: io.github.saumilp.starters.multitenancy.hibernate.TenantIdentifierResolver
```

### 2. Send requests with the tenant header

```bash
curl -H "X-Tenant-Id: acme" https://api.example.com/orders
```

Every JPA query in that request will automatically execute against the `acme` schema.

### 3. Access tenant in code

```java
@Service
public class OrderService {
    public void processOrder() {
        String tenant = TenantContext.get(); // "acme"
        // ...
    }
}
```

---

## Configuration Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.multitenancy.enabled` | `boolean` | `true` | Enable or disable multitenancy support entirely |
| `spring.multitenancy.strategy` | `String` | `"schema"` | Isolation strategy: `"schema"` or `"database"` |
| `spring.multitenancy.tenant-header-name` | `String` | `"X-Tenant-Id"` | HTTP header used by the header resolver |
| `spring.multitenancy.require-tenant` | `boolean` | `true` | Return HTTP 400 when no tenant is resolved |
| `spring.multitenancy.default-tenant` | `String` | `"public"` | Fallback tenant when `require-tenant` is `false` |
| `spring.multitenancy.resolver-type` | `ResolverType` | `HEADER` | Built-in resolver: `HEADER` or `SUBDOMAIN` |

---

## Full application.yml Example

```yaml
spring:
  multitenancy:
    enabled: true
    strategy: schema
    tenant-header-name: X-Tenant-Id
    require-tenant: true
    default-tenant: public
    resolver-type: HEADER

  datasource:
    url: jdbc:postgresql://localhost:5432/saas_db
    username: app_user
    password: secret

  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    properties:
      hibernate:
        multiTenancy: SCHEMA
        multi_tenant_connection_provider: io.github.saumilp.starters.multitenancy.hibernate.SchemaMultiTenantConnectionProvider
        tenant_identifier_resolver: io.github.saumilp.starters.multitenancy.hibernate.TenantIdentifierResolver
```

---

## Subdomain-Based Resolution

To resolve tenants from subdomains (e.g., `acme.yourapp.com`):

```yaml
spring:
  multitenancy:
    resolver-type: SUBDOMAIN
```

The resolver extracts the first subdomain component: `acme.yourapp.com` → `"acme"`.

---

## Database Schema

This starter does **not** manage schema creation. Provision each tenant schema before your
application starts accepting requests for that tenant. The recommended approach is a Flyway
migration run per tenant.

**`V1__create_tenant_schema.sql`** — run once per tenant (replace `acme` with the tenant ID):

```sql
-- Run once per tenant: replace 'acme' with the tenant identifier
CREATE SCHEMA IF NOT EXISTS acme;
-- Then run your normal migrations scoped to that schema
```

To automate tenant provisioning, implement a `TenantProvisioningService` that creates the
schema and runs Flyway migrations when a new tenant is onboarded.

---

## Custom Tenant Resolver

Implement `TenantResolver` and declare it as a bean:

```java
@Component
public class JwtTenantResolver implements TenantResolver {

    @Override
    public String resolveTenant(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) return null;
        // decode JWT and extract tenant claim
        return JwtUtil.extractClaim(token, "tenant_id");
    }
}
```

The auto-configured `HeaderTenantResolver` or `SubdomainTenantResolver` is skipped because
your bean satisfies `@ConditionalOnMissingBean(TenantResolver.class)`.

---

## Overriding Auto-Configured Beans

All beans registered by this starter use `@ConditionalOnMissingBean`. Declare your own bean
of the same type to replace the default:

| Bean type | Purpose | How to replace |
|-----------|---------|---------------|
| `TenantResolver` | Extract tenant from request | Declare `@Component TenantResolver` |
| `TenantResolutionFilter` | Set / clear `TenantContext` | Declare `@Component TenantResolutionFilter` |
| `SchemaMultiTenantConnectionProvider` | Scope JDBC connections to schema | Declare a `@Bean` of this type |
| `TenantIdentifierResolver` | Tell Hibernate the current tenant | Declare a `@Bean` of this type |

---

## Thread Pool Executors

`TenantContext` uses `InheritableThreadLocal`, so child threads spawned by the request thread
inherit the tenant. However, thread-pool executors (e.g., `@Async`) **reuse** threads — always
clear the context when an async task completes:

```java
@Async
public CompletableFuture<Void> doAsyncWork(String tenantId) {
    TenantContext.set(tenantId);  // explicitly set for pooled thread
    try {
        // ... work ...
    } finally {
        TenantContext.clear();    // always clear in finally
    }
    return CompletableFuture.completedFuture(null);
}
```

---

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for guidelines.

---

[GitHub](https://github.com/SaumilP/spring-boot-starters) · [Issues](https://github.com/SaumilP/spring-boot-starters/issues) · [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0)
