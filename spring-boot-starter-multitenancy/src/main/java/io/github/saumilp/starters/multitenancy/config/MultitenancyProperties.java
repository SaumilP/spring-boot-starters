/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the multitenancy starter, bound from the
 * {@code spring.multitenancy.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   multitenancy:
 *     enabled: true
 *     strategy: schema
 *     tenant-header-name: X-Tenant-Id
 *     require-tenant: true
 *     default-tenant: public
 *     resolver-type: HEADER
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.multitenancy")
public class MultitenancyProperties {

    /** Creates an instance with default values. */
    public MultitenancyProperties() {
    }

    /** Whether multitenancy support is enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /**
     * The multitenancy isolation strategy. Supported values:
     * <ul>
     *   <li>{@code "schema"} — each tenant has a dedicated database schema (default)</li>
     *   <li>{@code "database"} — each tenant has a dedicated database (requires a custom
     *       {@link io.github.saumilp.starters.multitenancy.hibernate.SchemaMultiTenantConnectionProvider})</li>
     * </ul>
     */
    private String strategy = "schema";

    /**
     * The HTTP header name used by {@link io.github.saumilp.starters.multitenancy.resolver.HeaderTenantResolver}
     * to read the tenant identifier. Defaults to {@code X-Tenant-Id}.
     */
    private String tenantHeaderName = "X-Tenant-Id";

    /**
     * Whether requests without a resolvable tenant identifier should be rejected with
     * HTTP {@code 400 Bad Request}. Defaults to {@code true}.
     *
     * <p>Set to {@code false} to allow requests without a tenant (e.g., for public endpoints
     * that should fall back to the {@link #defaultTenant}).
     */
    private boolean requireTenant = true;

    /**
     * The tenant identifier used as a fallback when no tenant can be resolved and
     * {@link #requireTenant} is {@code false}. Defaults to {@code "public"}.
     */
    private String defaultTenant = "public";

    /**
     * Which built-in {@link io.github.saumilp.starters.multitenancy.resolver.TenantResolver}
     * implementation to register automatically. Defaults to {@link ResolverType#HEADER}.
     */
    private ResolverType resolverType = ResolverType.HEADER;

    /**
     * Returns whether multitenancy is globally enabled.
     * @return {@code true} if enabled
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets whether multitenancy is enabled.
     *
     * @param enabled {@code false} to disable all multitenancy support
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the isolation strategy.
     * @return {@code "schema"} or {@code "database"}
     */
    public String getStrategy() { return strategy; }

    /**
     * Sets the isolation strategy.
     *
     * @param strategy the isolation strategy
     */
    public void setStrategy(String strategy) { this.strategy = strategy; }

    /**
     * Returns the HTTP header name used to carry the tenant identifier.
     * @return the header name; never {@code null}
     */
    public String getTenantHeaderName() { return tenantHeaderName; }

    /**
     * Sets the tenant HTTP header name.
     *
     * @param tenantHeaderName the header name; must not be {@code null}
     */
    public void setTenantHeaderName(String tenantHeaderName) { this.tenantHeaderName = tenantHeaderName; }

    /**
     * Returns whether requests without a tenant are rejected.
     * @return {@code true} if a missing tenant causes HTTP 400
     */
    public boolean isRequireTenant() { return requireTenant; }

    /**
     * Sets whether requests without a tenant are rejected.
     *
     * @param requireTenant {@code true} to reject requests with no tenant
     */
    public void setRequireTenant(boolean requireTenant) { this.requireTenant = requireTenant; }

    /**
     * Returns the fallback tenant identifier used when none is resolved.
     * @return the default tenant; never {@code null}
     */
    public String getDefaultTenant() { return defaultTenant; }

    /**
     * Sets the fallback tenant identifier.
     *
     * @param defaultTenant the fallback tenant identifier
     */
    public void setDefaultTenant(String defaultTenant) { this.defaultTenant = defaultTenant; }

    /**
     * Returns the resolver type to auto-register.
     * @return the resolver type; never {@code null}
     */
    public ResolverType getResolverType() { return resolverType; }

    /**
     * Sets the resolver type to auto-register.
     *
     * @param resolverType the resolver type to register
     */
    public void setResolverType(ResolverType resolverType) { this.resolverType = resolverType; }

    /**
     * Enumerates the built-in {@link io.github.saumilp.starters.multitenancy.resolver.TenantResolver}
     * implementations that can be activated via configuration.
     *
     * @since 1.0.0
     */
    public enum ResolverType {
        /**
         * Reads the tenant from an HTTP request header (default: {@code X-Tenant-Id}).
         * @see io.github.saumilp.starters.multitenancy.resolver.HeaderTenantResolver
         */
        HEADER,

        /**
         * Extracts the tenant from the first subdomain of the request host
         * (e.g., {@code acme.example.com} → {@code "acme"}).
         * @see io.github.saumilp.starters.multitenancy.resolver.SubdomainTenantResolver
         */
        SUBDOMAIN
    }
}
