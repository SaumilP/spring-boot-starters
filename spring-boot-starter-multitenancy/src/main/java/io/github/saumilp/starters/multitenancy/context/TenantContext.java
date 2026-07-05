/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.multitenancy.context;

/**
 * Holds the current tenant identifier in an {@link InheritableThreadLocal}, enabling
 * tenant context propagation to child threads (e.g., async tasks spawned by the request thread).
 *
 * <p>The tenant ID is set at the start of each request by the
 * {@link io.github.saumilp.starters.multitenancy.web.TenantResolutionFilter} and cleared
 * in the filter's {@code finally} block to prevent leakage across requests.
 *
 * <p><strong>Warning:</strong> thread-pool-based executors may reuse threads across requests.
 * Always call {@link #clear()} when the unit of work completes to avoid tenant context leakage
 * into subsequent requests handled by the same thread.
 *
 * @since 1.0.0
 */
public final class TenantContext {

    private static final InheritableThreadLocal<String> CURRENT = new InheritableThreadLocal<>();

    private TenantContext() {}

    /**
     * Sets the current tenant identifier for the executing thread.
     *
     * @param tenantId the tenant identifier to set; must not be {@code null} or blank
     */
    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    /**
     * Returns the current tenant identifier for the executing thread.
     *
     * @return the tenant identifier, or {@code null} if not set
     */
    public static String get() {
        return CURRENT.get();
    }

    /**
     * Clears the tenant identifier from the current thread's context.
     *
     * <p>This must be called in a {@code finally} block after each request to prevent the
     * tenant identifier from leaking into subsequent requests on thread-pool threads.
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * Returns whether a tenant identifier is currently set on the executing thread.
     *
     * @return {@code true} if a tenant is set; {@code false} otherwise
     */
    public static boolean isSet() {
        return CURRENT.get() != null;
    }
}
