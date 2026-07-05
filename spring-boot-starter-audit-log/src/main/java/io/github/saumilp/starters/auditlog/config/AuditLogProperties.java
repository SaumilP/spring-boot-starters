/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.auditlog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the audit-log starter, bound from the
 * {@code spring.audit-log.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   audit-log:
 *     enabled: true
 *     logging-sink:
 *       enabled: true
 *     jpa-sink:
 *       enabled: false
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.audit-log")
public class AuditLogProperties {

    /** Creates an instance with default values. */
    public AuditLogProperties() {
    }

    /** Whether audit logging is globally enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /** Settings for the SLF4J logging sink. */
    private SinkConfig loggingSink = new SinkConfig(true);

    /** Settings for the JPA persistence sink. */
    private SinkConfig jpaSink = new SinkConfig(false);

    /**
     * Returns whether audit logging is globally enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets whether audit logging is globally enabled.
     *
     * @param enabled {@code false} to disable all audit interception
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the logging sink configuration.
     *
     * @return the logging sink config; never {@code null}
     */
    public SinkConfig getLoggingSink() { return loggingSink; }

    /**
     * Sets the logging sink configuration.
     *
     * @param loggingSink the logging sink configuration; must not be {@code null}
     */
    public void setLoggingSink(SinkConfig loggingSink) { this.loggingSink = loggingSink; }

    /**
     * Returns the JPA sink configuration.
     *
     * @return the JPA sink config; never {@code null}
     */
    public SinkConfig getJpaSink() { return jpaSink; }

    /**
     * Sets the JPA sink configuration.
     *
     * @param jpaSink the JPA sink configuration; must not be {@code null}
     */
    public void setJpaSink(SinkConfig jpaSink) { this.jpaSink = jpaSink; }

    /**
     * Per-sink enable/disable toggle.
     *
     * @since 1.0.0
     */
    public static class SinkConfig {

        /** Whether this sink is active. */
        private boolean enabled;

        /**
         * Constructs a {@code SinkConfig} with the given default enabled state.
         *
         * @param enabled {@code true} to enable this sink by default
         */
        public SinkConfig(boolean enabled) { this.enabled = enabled; }

        /**
         * No-arg constructor required for Spring property binding.
         */
        public SinkConfig() {}

        /**
         * Returns whether this sink is enabled.
         *
         * @return {@code true} if this sink is active
         */
        public boolean isEnabled() { return enabled; }

        /**
         * Sets whether this sink is enabled.
         *
         * @param enabled the enabled flag
         */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
