/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Redis starter, bound from the {@code spring.redis.*}
 * namespace in the application's {@code application.yml} or {@code application.properties}.
 *
 * <p>All properties have sensible defaults so that a minimal configuration requires only the
 * host and port (which themselves default to localhost:6379 via Spring Data Redis).
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   redis:
 *     host: redis.example.com
 *     port: 6379
 *     metric-name: my-app.redis.operations
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(RedisConfigurationProperties.PREFIX)
public class RedisConfigurationProperties {

    /** The configuration property prefix for all Redis starter properties. */
    public static final String PREFIX = "spring.redis";

    /**
     * Redis server hostname. Defaults to {@code localhost}.
     */
    private String host = "localhost";

    /**
     * Redis server port. Defaults to {@code 6379}.
     */
    private int port = 6379;

    /**
     * Micrometer metric name used to record Redis operation timings.
     * Defaults to {@code redis.operations}.
     */
    private String metricName = "redis.operations";

    /**
     * Default cache TTL in days applied to all entries managed by the starter's CacheManager.
     * Defaults to {@code 1}.
     */
    private long cacheTtlDays = 1L;

    /**
     * Returns the Redis server hostname.
     *
     * @return the hostname; never {@code null}
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the Redis server hostname.
     *
     * @param host the hostname; must not be {@code null} or blank
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the Redis server port.
     *
     * @return the port number; always a positive integer
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the Redis server port.
     *
     * @param port the port number; must be in the range 1–65535
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Returns the Micrometer metric name for Redis operation timing.
     *
     * @return the metric name; never {@code null}
     */
    public String getMetricName() {
        return metricName;
    }

    /**
     * Sets the Micrometer metric name for Redis operation timing.
     *
     * @param metricName the metric name; must not be {@code null} or blank
     */
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * Returns the default cache entry TTL in days.
     *
     * @return the TTL in days; always a positive long
     */
    public long getCacheTtlDays() {
        return cacheTtlDays;
    }

    /**
     * Sets the default cache entry TTL in days.
     *
     * @param cacheTtlDays the TTL in days; must be a positive value
     */
    public void setCacheTtlDays(long cacheTtlDays) {
        this.cacheTtlDays = cacheTtlDays;
    }
}
