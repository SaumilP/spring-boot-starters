/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.health;

import io.github.saumilp.starters.common.health.HealthDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Spring Boot Actuator {@link HealthIndicator} for Redis connectivity.
 *
 * <p>Performs a {@code PING} command against the configured Redis server on every health
 * check invocation. A successful {@code PONG} response marks the component as {@code UP};
 * any exception marks it as {@code DOWN} and exposes the error message in the details map.
 *
 * <p>The indicator is conditionally registered via
 * {@code management.health.redis-custom.enabled=true} (default: {@code true} when the
 * actuator is on the classpath). Consumers who rely on Spring Data Redis' built-in health
 * indicator can disable this one via:
 * <pre>{@code
 * management:
 *   health:
 *     redis-custom:
 *       enabled: false
 * }</pre>
 *
 * @since 1.0.0
 */
public class RedisHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(RedisHealthIndicator.class);
    private static final String COMPONENT = "redis";

    private final RedisConnectionFactory connectionFactory;

    /**
     * Constructs a {@code RedisHealthIndicator} backed by the given connection factory.
     *
     * @param connectionFactory the Redis connection factory; must not be {@code null}
     */
    public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Opens a Redis connection, issues a {@code PING}, and closes the connection. The
     * health result includes the component name and server response string in the details map.
     *
     * @return {@link Health#up()} with detail {@code response=PONG} on success, or
     *         {@link Health#down()} with a {@code error} detail on failure
     */
    @Override
    public Health health() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pong = connection.ping();
            return Health.up()
                    .withDetails(HealthDetails.builder()
                            .add("component", COMPONENT)
                            .add("response", pong)
                            .build())
                    .build();
        } catch (Exception ex) {
            log.warn("Redis health check failed: {}", ex.getMessage());
            return Health.down(ex)
                    .withDetails(HealthDetails.builder()
                            .add("component", COMPONENT)
                            .add("error", ex.getMessage())
                            .build())
                    .build();
        }
    }
}
