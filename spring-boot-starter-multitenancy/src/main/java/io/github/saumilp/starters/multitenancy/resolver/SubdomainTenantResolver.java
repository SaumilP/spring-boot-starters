/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A {@link TenantResolver} that extracts the tenant identifier from the first subdomain
 * component of the request host.
 *
 * <p>For example, a request to {@code acme.example.com} resolves to {@code "acme"}.
 * If the host has fewer than two dot-separated components (e.g., {@code localhost} or an
 * IP address), this resolver returns {@code null}.
 *
 * <p>Activate this resolver by setting:
 * <pre>{@code
 * spring:
 *   multitenancy:
 *     resolver-type: SUBDOMAIN
 * }</pre>
 *
 * @since 1.0.0
 */
public class SubdomainTenantResolver implements TenantResolver {

    /** Creates a new subdomain-based tenant resolver. */
    public SubdomainTenantResolver() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>Splits {@code request.getServerName()} on {@code '.'} and returns the first segment
     * when there are at least three segments (indicating a subdomain is present). Returns
     * {@code null} for bare hostnames, IP addresses, and {@code localhost}.
     *
     * @param request the current HTTP request; never {@code null}
     * @return the first subdomain segment, or {@code null}
     */
    @Override
    public String resolveTenant(HttpServletRequest request) {
        String host = request.getServerName();
        if (host == null || host.isBlank()) return null;

        String[] parts = host.split("\\.");
        if (parts.length < 3) return null;

        String subdomain = parts[0].trim();
        return subdomain.isEmpty() ? null : subdomain;
    }
}
