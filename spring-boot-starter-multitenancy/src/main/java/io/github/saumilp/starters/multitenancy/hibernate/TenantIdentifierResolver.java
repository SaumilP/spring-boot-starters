/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.hibernate;

import io.github.saumilp.starters.multitenancy.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Hibernate {@link CurrentTenantIdentifierResolver} that delegates to {@link TenantContext}
 * to obtain the current tenant identifier for each database session.
 *
 * <p>When no tenant is set (e.g., during application startup, schema validation, or
 * background tasks that run outside a request context), this resolver returns {@code "public"}
 * so that Hibernate falls back to the default PostgreSQL schema rather than failing.
 *
 * <p>Register this resolver in your {@code application.yml}:
 * <pre>{@code
 * spring:
 *   jpa:
 *     properties:
 *       hibernate:
 *         tenant_identifier_resolver: io.github.saumilp.starters.multitenancy.hibernate.TenantIdentifierResolver
 * }</pre>
 *
 * @since 1.0.0
 */
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    /** Creates a new tenant identifier resolver. */
    public TenantIdentifierResolver() {
    }

    /**
     * Returns the current tenant identifier from {@link TenantContext}, or {@code "public"}
     * when no tenant has been set on the executing thread.
     *
     * @return the tenant schema name; never {@code null}
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.get();
        return tenantId != null ? tenantId : "public";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code false} — existing sessions are not validated against the current tenant
     *         because tenant context may change between calls in pooled environments
     */
    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
