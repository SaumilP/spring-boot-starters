/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.ratelimit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for the rate-limiting starter, bound from the
 * {@code spring.rate-limit.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   rate-limit:
 *     enabled: true
 *     default-requests: 60
 *     default-window-seconds: 60
 *     key-prefix: "rl:"
 *     named-limits:
 *       login:
 *         requests: 5
 *         window-seconds: 900
 *       export:
 *         requests: 10
 *         window-seconds: 3600
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.rate-limit")
public class RateLimitProperties {

    /** Whether rate limiting is enabled. Defaults to {@code true}. */
    private boolean enabled = true;

    /** Default maximum requests per window when no named limit or annotation override applies. */
    private int defaultRequests = 60;

    /** Default window duration in seconds. Defaults to {@code 60} (one minute). */
    private long defaultWindowSeconds = 60L;

    /** Redis key namespace prefix prepended to all rate-limit keys. Defaults to {@code "rl:"}. */
    private String keyPrefix = "rl:";

    /**
     * Named rate-limit configurations. Each entry may be referenced by name via
     * {@link org.sandcastle.starters.ratelimit.annotation.RateLimit#name()}.
     */
    private Map<String, NamedLimit> namedLimits = new HashMap<>();

    /**
     * Returns whether rate limiting is globally enabled.
     *
     * @return {@code true} if rate limiting is active; {@code false} to disable entirely
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Sets whether rate limiting is globally enabled.
     *
     * @param enabled {@code true} to enable rate limiting; {@code false} to disable
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the default maximum requests per window.
     *
     * @return the default request limit; always a positive integer
     */
    public int getDefaultRequests() { return defaultRequests; }

    /**
     * Sets the default maximum requests per window.
     *
     * @param defaultRequests the default request count; must be positive
     */
    public void setDefaultRequests(int defaultRequests) { this.defaultRequests = defaultRequests; }

    /**
     * Returns the default window duration in seconds.
     *
     * @return the window length in seconds; always positive
     */
    public long getDefaultWindowSeconds() { return defaultWindowSeconds; }

    /**
     * Sets the default window duration in seconds.
     *
     * @param defaultWindowSeconds the default window in seconds; must be positive
     */
    public void setDefaultWindowSeconds(long defaultWindowSeconds) {
        this.defaultWindowSeconds = defaultWindowSeconds;
    }

    /**
     * Returns the Redis key prefix applied to all rate-limit bucket keys.
     *
     * @return the key prefix; never {@code null}
     */
    public String getKeyPrefix() { return keyPrefix; }

    /**
     * Sets the Redis key prefix applied to all rate-limit bucket keys.
     *
     * @param keyPrefix the prefix to prepend; must not be {@code null}
     */
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    /**
     * Returns the map of named rate-limit configurations.
     *
     * @return a mutable map of name to {@link NamedLimit}; never {@code null}
     */
    public Map<String, NamedLimit> getNamedLimits() { return namedLimits; }

    /**
     * Sets the map of named rate-limit configurations.
     *
     * @param namedLimits the named limit map; must not be {@code null}
     */
    public void setNamedLimits(Map<String, NamedLimit> namedLimits) { this.namedLimits = namedLimits; }

    /**
     * A named rate-limit configuration that can be referenced by
     * {@link org.sandcastle.starters.ratelimit.annotation.RateLimit#name()}.
     *
     * @since 1.0.0
     */
    public static class NamedLimit {

        /** Maximum number of requests allowed per window. */
        private int requests = 60;

        /** Window duration in seconds. */
        private long windowSeconds = 60L;

        /**
         * Returns the maximum requests for this named limit.
         *
         * @return the request count; always a positive integer
         */
        public int getRequests() { return requests; }

        /**
         * Sets the maximum requests for this named limit.
         *
         * @param requests the maximum request count; must be positive
         */
        public void setRequests(int requests) { this.requests = requests; }

        /**
         * Returns the window duration for this named limit in seconds.
         *
         * @return the window in seconds; always positive
         */
        public long getWindowSeconds() { return windowSeconds; }

        /**
         * Sets the window duration for this named limit in seconds.
         *
         * @param windowSeconds the window in seconds; must be positive
         */
        public void setWindowSeconds(long windowSeconds) { this.windowSeconds = windowSeconds; }
    }
}
