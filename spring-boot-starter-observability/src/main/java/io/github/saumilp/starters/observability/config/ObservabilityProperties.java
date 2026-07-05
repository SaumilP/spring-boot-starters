/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the observability starter, bound from the
 * {@code spring.observability.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   observability:
 *     enabled: true
 *     correlation:
 *       header-name: X-Correlation-Id
 *       mdc-key: correlationId
 *       generate-if-absent: true
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.observability")
public class ObservabilityProperties {

    /** Whether observability wiring (correlation filter, MDC propagation) is enabled. */
    private boolean enabled = true;

    /** Correlation-ID settings. */
    private final Correlation correlation = new Correlation();

    /** Creates an instance with default values. */
    public ObservabilityProperties() {
    }

    /** {@return whether the starter is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the starter is enabled.
     *
     * @param enabled {@code false} to disable all observability wiring
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the correlation-ID settings} */
    public Correlation getCorrelation() {
        return correlation;
    }

    /**
     * Correlation-ID settings controlling the request header, MDC key, and generation policy.
     *
     * @since 1.0.0
     */
    public static class Correlation {

        /** HTTP header carrying the correlation ID on inbound requests and outbound calls. */
        private String headerName = "X-Correlation-Id";

        /** SLF4J MDC key under which the correlation ID is stored for log patterns. */
        private String mdcKey = "correlationId";

        /** Whether to generate a correlation ID when the inbound request has none. */
        private boolean generateIfAbsent = true;

        /** Creates an instance with default values. */
        public Correlation() {
        }

        /** {@return the correlation HTTP header name} */
        public String getHeaderName() {
            return headerName;
        }

        /**
         * Sets the correlation HTTP header name.
         *
         * @param headerName the header name; must not be {@code null} or blank
         */
        public void setHeaderName(String headerName) {
            this.headerName = headerName;
        }

        /** {@return the MDC key used to store the correlation ID} */
        public String getMdcKey() {
            return mdcKey;
        }

        /**
         * Sets the MDC key used to store the correlation ID.
         *
         * @param mdcKey the MDC key; must not be {@code null} or blank
         */
        public void setMdcKey(String mdcKey) {
            this.mdcKey = mdcKey;
        }

        /** {@return whether a correlation ID is generated when absent from the request} */
        public boolean isGenerateIfAbsent() {
            return generateIfAbsent;
        }

        /**
         * Sets whether a correlation ID is generated when absent from the request.
         *
         * @param generateIfAbsent {@code true} to generate an ID when none is present
         */
        public void setGenerateIfAbsent(boolean generateIfAbsent) {
            this.generateIfAbsent = generateIfAbsent;
        }
    }
}
