/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.correlation;

import org.slf4j.MDC;

import java.util.Optional;

/**
 * Static accessor for the correlation ID of the current thread.
 *
 * <p>The value is stored in the SLF4J {@link MDC} by {@link CorrelationIdFilter}. The MDC key is
 * configurable via {@code spring.observability.correlation.mdc-key}; the filter records the
 * resolved key here so that application code can read the ID without knowing the configuration.
 *
 * @since 1.0.0
 */
public final class CorrelationContext {

    /** The default MDC key, used until {@link CorrelationIdFilter} overrides it at startup. */
    public static final String DEFAULT_MDC_KEY = "correlationId";

    private static volatile String mdcKey = DEFAULT_MDC_KEY;

    private CorrelationContext() {
    }

    /**
     * Records the MDC key in use. Called by {@link CorrelationIdFilter} during construction.
     *
     * @param key the resolved MDC key; must not be {@code null} or blank
     */
    static void setMdcKey(String key) {
        mdcKey = key;
    }

    /** {@return the MDC key under which the correlation ID is stored} */
    public static String mdcKey() {
        return mdcKey;
    }

    /**
     * Returns the correlation ID bound to the current thread, if any.
     *
     * @return the current correlation ID, or {@link Optional#empty()} if none is set
     */
    public static Optional<String> getCorrelationId() {
        return Optional.ofNullable(MDC.get(mdcKey));
    }
}
