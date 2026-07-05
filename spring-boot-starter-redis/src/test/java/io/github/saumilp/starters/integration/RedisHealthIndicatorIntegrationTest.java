/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.github.saumilp.starters.health.RedisHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RedisHealthIndicator} backed by a live Redis container.
 *
 * <p>Verifies that the PING health check returns {@code UP} with the expected detail map
 * when Redis is reachable.
 *
 * @since 1.0.0
 */
@Tag("integration")
@Testcontainers
@EnabledIfDockerAvailable
@SpringBootTest(classes = {DataRedisAutoConfiguration.class,
        io.github.saumilp.starters.configs.RedisAutoConfiguration.class})
class RedisHealthIndicatorIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", REDIS::getFirstMappedPort);
    }

    @Autowired
    private RedisHealthIndicator redisHealthIndicator;

    @Test
    void should_returnUp_when_redisContainerIsRunning() {
        Health health = redisHealthIndicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void should_includePongResponse_when_pingSucceeds() {
        Health health = redisHealthIndicator.health();

        assertThat(health.getDetails()).containsEntry("response", "PONG");
    }

    @Test
    void should_includeComponentDetail_when_healthy() {
        Health health = redisHealthIndicator.health();

        assertThat(health.getDetails()).containsEntry("component", "redis");
    }
}
