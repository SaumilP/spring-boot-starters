/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InMemorySlidingWindowRateLimiter}.
 *
 * <p>Verifies bucket isolation, limit enforcement, and allow-through behaviour for
 * requests within the configured threshold.
 */
class InMemorySlidingWindowRateLimiterTest {

    private InMemorySlidingWindowRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new InMemorySlidingWindowRateLimiter();
    }

    @Test
    void should_allowRequests_when_limitNotYetReached() {
        assertThat(limiter.tryConsume("key1", 3, 60)).isTrue();
        assertThat(limiter.tryConsume("key1", 3, 60)).isTrue();
        assertThat(limiter.tryConsume("key1", 3, 60)).isTrue();
    }

    @Test
    void should_rejectRequest_when_limitExceeded() {
        limiter.tryConsume("key2", 2, 60);
        limiter.tryConsume("key2", 2, 60);
        assertThat(limiter.tryConsume("key2", 2, 60)).isFalse();
    }

    @Test
    void should_isolateBuckets_when_differentKeysUsed() {
        limiter.tryConsume("keyA", 1, 60);
        assertThat(limiter.tryConsume("keyA", 1, 60)).isFalse();
        assertThat(limiter.tryConsume("keyB", 1, 60)).isTrue();
    }

    @Test
    void should_allowSingleRequest_when_limitIsOne() {
        assertThat(limiter.tryConsume("single", 1, 60)).isTrue();
        assertThat(limiter.tryConsume("single", 1, 60)).isFalse();
    }

    @Test
    void should_allowRequest_when_windowExpires() throws InterruptedException {
        assertThat(limiter.tryConsume("expiry", 1, 1)).isTrue();
        assertThat(limiter.tryConsume("expiry", 1, 1)).isFalse();
        Thread.sleep(1100);
        assertThat(limiter.tryConsume("expiry", 1, 1)).isTrue();
    }
}
