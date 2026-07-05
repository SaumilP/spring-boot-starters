/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the scheduler-lock starter, bound from the
 * {@code spring.scheduler-lock.*} namespace.
 *
 * <p>Example configuration:
 * <pre>{@code
 * spring:
 *   scheduler-lock:
 *     enabled: true
 *     provider: redis
 *     default-lock-at-most-for: 1m
 *     redis-key-prefix: "scheduler-lock:"
 * }</pre>
 *
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "spring.scheduler-lock")
public class SchedulerLockProperties {

    /** Whether scheduler locking is enabled. */
    private boolean enabled = true;

    /** The lock provider to use. */
    private Provider provider = Provider.IN_MEMORY;

    /** Default maximum lock hold time when a {@code @SchedulerLock} does not specify one. */
    private Duration defaultLockAtMostFor = Duration.ofMinutes(1);

    /** Key namespace prefix for the Redis provider. */
    private String redisKeyPrefix = "scheduler-lock:";

    /** Creates an instance with default values. */
    public SchedulerLockProperties() {
    }

    /** {@return whether scheduler locking is enabled} */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether scheduler locking is enabled.
     *
     * @param enabled {@code false} to disable the lock aspect
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** {@return the configured lock provider} */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Sets the lock provider.
     *
     * @param provider the provider; must not be {@code null}
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    /** {@return the default maximum lock hold time} */
    public Duration getDefaultLockAtMostFor() {
        return defaultLockAtMostFor;
    }

    /**
     * Sets the default maximum lock hold time.
     *
     * @param defaultLockAtMostFor the default duration; must not be {@code null}
     */
    public void setDefaultLockAtMostFor(Duration defaultLockAtMostFor) {
        this.defaultLockAtMostFor = defaultLockAtMostFor;
    }

    /** {@return the Redis key namespace prefix} */
    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    /**
     * Sets the Redis key namespace prefix.
     *
     * @param redisKeyPrefix the prefix; must not be {@code null}
     */
    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    /**
     * The available lock providers.
     *
     * @since 1.0.0
     */
    public enum Provider {

        /** Single-JVM in-memory locking (not cluster-safe). */
        IN_MEMORY,

        /** Redis-backed, cluster-safe locking. */
        REDIS
    }
}
