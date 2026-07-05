/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.resolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A {@link TenantResolver} that reads the tenant identifier from a configurable HTTP request
 * header.
 *
 * <p>This is the default resolver registered by the auto-configuration when
 * {@code spring.multitenancy.resolver-type=HEADER} (the default). The header name is
 * configurable via {@code spring.multitenancy.tenant-header-name} and defaults to
 * {@code X-Tenant-Id}.
 *
 * <p>Example request:
 * <pre>{@code
 * GET /api/orders HTTP/1.1
 * X-Tenant-Id: acme
 * }</pre>
 *
 * @since 1.0.0
 */
public class HeaderTenantResolver implements TenantResolver {

    private final String headerName;

    /**
     * Constructs a resolver that reads the tenant from the given header name.
     *
     * @param headerName the HTTP header name to inspect; must not be {@code null} or blank
     */
    public HeaderTenantResolver(String headerName) {
        this.headerName = headerName;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns the value of the configured header, or {@code null} if the header is absent
     * or blank.
     *
     * @param request the current HTTP request; never {@code null}
     * @return the tenant identifier, or {@code null}
     */
    @Override
    public String resolveTenant(HttpServletRequest request) {
        String value = request.getHeader(headerName);
        return (value != null && !value.isBlank()) ? value.trim() : null;
    }

    /**
     * Returns the header name this resolver reads.
     *
     * @return the configured header name; never {@code null}
     */
    public String getHeaderName() {
        return headerName;
    }
}
