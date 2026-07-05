/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.github.saumilp.starters.utils.RedisLockUtil;
import io.github.saumilp.starters.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RedisUtil} backed by a live Redis container.
 *
 * <p>These tests verify that the key/value operations and TTL management work against a real
 * Redis server. They require Docker to be available in the CI environment.
 *
 * @since 1.0.0
 */
@Tag("integration")
@Testcontainers
@EnabledIfDockerAvailable
@SpringBootTest(classes = {DataRedisAutoConfiguration.class,
        io.github.saumilp.starters.configs.RedisAutoConfiguration.class})
class RedisUtilIntegrationTest {

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
    private RedisUtil redisUtil;

    @AfterEach
    void cleanUp() {
        redisUtil.del("test:key", "test:ttl", "test:duration");
    }

    @Test
    void should_storeAndRetrieveValue_when_keySet() {
        redisUtil.set("test:key", "hello-redis");

        String value = redisUtil.get("test:key");

        assertThat(value).isEqualTo("hello-redis");
    }

    @Test
    void should_returnTrue_when_keyExists() {
        redisUtil.set("test:key", "value");

        assertThat(redisUtil.hasKey("test:key")).isTrue();
    }

    @Test
    void should_returnNull_when_keyDeleted() {
        redisUtil.set("test:key", "to-delete");
        redisUtil.del("test:key");

        assertThat(redisUtil.<String>get("test:key")).isNull();
    }

    @Test
    void should_expireKey_when_ttlSetInSeconds() throws InterruptedException {
        redisUtil.set("test:ttl", "expiring-value", 1L);

        TimeUnit.SECONDS.sleep(2);

        assertThat(redisUtil.<String>get("test:ttl")).isNull();
    }

    @Test
    void should_storeWithDurationTtl_when_durationProvided() {
        redisUtil.set("test:duration", "duration-value", Duration.ofMinutes(5));

        Long remaining = redisUtil.getExpire("test:duration");

        assertThat(remaining).isGreaterThan(0);
    }
}
