/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RedisHealthIndicator}.
 */
class RedisHealthIndicatorTest {

    private RedisConnectionFactory connectionFactory;
    private RedisConnection connection;
    private RedisHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        connectionFactory = mock(RedisConnectionFactory.class);
        connection = mock(RedisConnection.class);
        when(connectionFactory.getConnection()).thenReturn(connection);
        healthIndicator = new RedisHealthIndicator(connectionFactory);
    }

    @Test
    void should_returnUp_when_pingSucceeds() {
        when(connection.ping()).thenReturn("PONG");

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("response", "PONG");
        assertThat(health.getDetails()).containsEntry("component", "redis");
    }

    @Test
    void should_returnDown_when_connectionThrowsException() {
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Connection refused"));

        Health health = healthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails().get("error")).asString().contains("Connection refused");
    }

    @Test
    void should_includeComponentTag_when_healthy() {
        when(connection.ping()).thenReturn("PONG");

        Health health = healthIndicator.health();

        assertThat(health.getDetails()).containsKey("component");
    }

    @Test
    void should_includeComponentTag_when_unhealthy() {
        when(connection.ping()).thenThrow(new RuntimeException("timeout"));

        Health health = healthIndicator.health();

        assertThat(health.getDetails()).containsKey("component");
    }
}
