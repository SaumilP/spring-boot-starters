/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy interface for resolving the current tenant identifier from an HTTP request.
 *
 * <p>The starter ships three built-in implementations:
 * <ul>
 *   <li>{@link HeaderTenantResolver} — reads the tenant from a configurable request header
 *       (default: {@code X-Tenant-Id}). Suitable for API-first applications where clients
 *       pass the tenant explicitly.</li>
 *   <li>{@link SubdomainTenantResolver} — extracts the first subdomain from the request host
 *       (e.g., {@code acme.example.com} → {@code "acme"}). Suitable for SaaS applications
 *       where each tenant has its own subdomain.</li>
 * </ul>
 *
 * <p>Applications can provide a custom implementation by declaring a bean of type
 * {@code TenantResolver} in the application context, which will take precedence over the
 * auto-configured one due to {@code @ConditionalOnMissingBean}.
 *
 * @since 1.0.0
 */
public interface TenantResolver {

    /**
     * Resolves the tenant identifier from the given HTTP request.
     *
     * <p>Implementations must be thread-safe and must not modify the request.
     *
     * @param request the current HTTP request; never {@code null}
     * @return the resolved tenant identifier, or {@code null} if no tenant can be determined
     *         from this request
     */
    String resolveTenant(HttpServletRequest request);
}
