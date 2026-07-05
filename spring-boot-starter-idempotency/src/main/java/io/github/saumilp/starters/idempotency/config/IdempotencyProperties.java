/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for the idempotency starter, bound from the
 * {@code spring.idempotency.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   idempotency:
 *     enabled: true
 *     header-name: Idempotency-Key
 *     ttl-seconds: 86400
 *     lock-timeout-seconds: 30
 *     key-prefix: "idm:"
 *     applicable-methods:
 *       - POST
 *       - PATCH
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.idempotency")
public class IdempotencyProperties {

    /** Creates an instance with default values. */
    public IdempotencyProperties() {
    }

    /** Whether the idempotency filter is enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /** The HTTP header name carrying the idempotency key. Defaults to {@code Idempotency-Key}. */
    private String headerName = "Idempotency-Key";

    /** How long (in seconds) to retain a cached response. Defaults to {@code 86400} (24 hours). */
    private long ttlSeconds = 86_400L;

    /**
     * How long (in seconds) to hold the in-progress lock before automatically releasing it.
     * Prevents deadlocks when a request fails before caching its response. Defaults to {@code 30}.
     */
    private long lockTimeoutSeconds = 30L;

    /** Redis key namespace prefix prepended to all idempotency entries. Defaults to {@code "idm:"}. */
    private String keyPrefix = "idm:";

    /**
     * HTTP methods for which idempotency is enforced. Requests using other methods pass through
     * without idempotency checks. Defaults to {@code [POST, PATCH]}.
     */
    private List<String> applicableMethods = List.of("POST", "PATCH");

    /**
     * Returns whether the idempotency filter is globally enabled.
     *
     * @return {@code true} if enabled
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets whether the idempotency filter is globally enabled.
     *
     * @param enabled {@code true} to enable; {@code false} to disable entirely
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the HTTP header name used to carry the idempotency key.
     *
     * @return the header name; never {@code null}
     */
    public String getHeaderName() { return headerName; }

    /**
     * Sets the HTTP header name used to carry the idempotency key.
     *
     * @param headerName the header name; must not be {@code null}
     */
    public void setHeaderName(String headerName) { this.headerName = headerName; }

    /**
     * Returns the response cache TTL in seconds.
     *
     * @return the TTL; always a positive value
     */
    public long getTtlSeconds() { return ttlSeconds; }

    /**
     * Sets the response cache TTL in seconds.
     *
     * @param ttlSeconds the TTL in seconds; must be positive
     */
    public void setTtlSeconds(long ttlSeconds) { this.ttlSeconds = ttlSeconds; }

    /**
     * Returns the in-progress lock timeout in seconds.
     *
     * @return the timeout; always positive
     */
    public long getLockTimeoutSeconds() { return lockTimeoutSeconds; }

    /**
     * Sets the in-progress lock timeout in seconds.
     *
     * @param lockTimeoutSeconds the lock timeout in seconds; must be positive
     */
    public void setLockTimeoutSeconds(long lockTimeoutSeconds) {
        this.lockTimeoutSeconds = lockTimeoutSeconds;
    }

    /**
     * Returns the Redis key prefix applied to all idempotency entries.
     *
     * @return the prefix; never {@code null}
     */
    public String getKeyPrefix() { return keyPrefix; }

    /**
     * Sets the Redis key prefix applied to all idempotency entries.
     *
     * @param keyPrefix the key prefix; must not be {@code null}
     */
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    /**
     * Returns the list of HTTP methods subject to idempotency enforcement.
     *
     * @return the method list; never {@code null} or empty
     */
    public List<String> getApplicableMethods() { return applicableMethods; }

    /**
     * Sets the list of HTTP methods subject to idempotency enforcement.
     *
     * @param applicableMethods the HTTP methods to enforce; must not be {@code null}
     */
    public void setApplicableMethods(List<String> applicableMethods) {
        this.applicableMethods = applicableMethods;
    }
}
