/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.web;

import io.github.saumilp.starters.multitenancy.config.MultitenancyProperties;
import io.github.saumilp.starters.multitenancy.context.TenantContext;
import io.github.saumilp.starters.multitenancy.resolver.TenantResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that resolves and propagates the tenant identifier for each incoming request.
 *
 * <h2>Processing sequence</h2>
 * <ol>
 *   <li>Delegates to the injected {@link TenantResolver} to extract the tenant identifier
 *       from the request.</li>
 *   <li>If a tenant is resolved, stores it in {@link TenantContext} and continues the
 *       filter chain.</li>
 *   <li>If no tenant is resolved and {@link MultitenancyProperties#isRequireTenant()} is
 *       {@code true}, responds immediately with HTTP {@code 400 Bad Request} and a JSON
 *       error body.</li>
 *   <li>If no tenant is resolved and {@code requireTenant} is {@code false}, continues
 *       the filter chain without setting a tenant context.</li>
 *   <li>The {@code finally} block unconditionally calls {@link TenantContext#clear()} to
 *       prevent context leakage across requests on thread-pool threads.</li>
 * </ol>
 *
 * <p>The filter is registered at {@code HIGHEST_PRECEDENCE + 5} by the auto-configuration,
 * running after only the most critical infrastructure filters (e.g., security).
 *
 * @since 1.0.0
 */
public class TenantResolutionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantResolutionFilter.class);

    private final TenantResolver resolver;
    private final MultitenancyProperties props;

    /**
     * Constructs the filter with the given resolver and configuration.
     *
     * @param resolver the strategy for extracting the tenant from a request; must not be {@code null}
     * @param props    the multitenancy configuration properties; must not be {@code null}
     */
    public TenantResolutionFilter(TenantResolver resolver, MultitenancyProperties props) {
        this.resolver = resolver;
        this.props    = props;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Resolves the tenant, sets it in {@link TenantContext}, processes the request, and
     * clears the context in the {@code finally} block.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        String tenantId = null;
        try {
            tenantId = resolver.resolveTenant(request);

            if (tenantId != null) {
                log.debug("Resolved tenant '{}' for request '{}'", tenantId, request.getRequestURI());
                TenantContext.set(tenantId);
                filterChain.doFilter(request, response);
            } else if (props.isRequireTenant()) {
                log.warn("No tenant resolved for request '{}'; returning 400", request.getRequestURI());
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Missing tenant identifier\"}");
            } else {
                filterChain.doFilter(request, response);
            }
        } finally {
            TenantContext.clear();
        }
    }
}
