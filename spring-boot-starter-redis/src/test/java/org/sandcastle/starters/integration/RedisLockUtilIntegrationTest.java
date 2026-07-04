/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.sandcastle.starters.utils.RedisLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RedisLockUtil} backed by a live Redis container.
 *
 * <p>Verifies distributed lock acquisition and atomic Lua-based release semantics
 * against a real Redis server.
 *
 * @since 1.0.0
 */
@Tag("integration")
@Testcontainers
@SpringBootTest(classes = {RedisAutoConfiguration.class,
        org.sandcastle.starters.configs.RedisAutoConfiguration.class})
class RedisLockUtilIntegrationTest {

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
    private RedisLockUtil redisLockUtil;

    private static final String LOCK_KEY = "integration:lock";

    @AfterEach
    void cleanUp() {
        redisLockUtil.releaseLock(LOCK_KEY, "cleanup-id");
    }

    @Test
    void should_acquireLock_when_keyIsAvailable() {
        Boolean acquired = redisLockUtil.tryLock(LOCK_KEY, "request-1", 10L);

        assertThat(acquired).isTrue();
    }

    @Test
    void should_rejectSecondAcquire_when_lockAlreadyHeld() {
        redisLockUtil.tryLock(LOCK_KEY, "request-1", 30L);

        Boolean secondAcquire = redisLockUtil.tryLock(LOCK_KEY, "request-2", 30L);

        assertThat(secondAcquire).isFalse();
    }

    @Test
    void should_allowReacquire_when_lockReleased() {
        redisLockUtil.tryLock(LOCK_KEY, "request-1", 30L);
        redisLockUtil.releaseLock(LOCK_KEY, "request-1");

        Boolean reacquired = redisLockUtil.tryLock(LOCK_KEY, "request-2", 30L);

        assertThat(reacquired).isTrue();
    }

    @Test
    void should_notReleaseLock_when_requestIdMismatches() {
        redisLockUtil.tryLock(LOCK_KEY, "request-1", 30L);
        redisLockUtil.releaseLock(LOCK_KEY, "wrong-request-id");

        // Lock should still be held by request-1; a third party cannot acquire it
        Boolean wrongAcquire = redisLockUtil.tryLock(LOCK_KEY, "request-3", 30L);

        assertThat(wrongAcquire).isFalse();
    }
}
