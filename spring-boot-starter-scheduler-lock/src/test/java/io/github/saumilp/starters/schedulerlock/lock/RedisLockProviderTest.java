/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RedisLockProvider}.
 */
class RedisLockProviderTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private RedisLockProvider provider;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        provider = new RedisLockProvider(redisTemplate, "lock:");
    }

    @Test
    void should_returnTrue_when_setIfAbsentSucceeds() {
        when(valueOps.setIfAbsent(eq("lock:job"), eq("locked"), any(Duration.class))).thenReturn(true);
        assertThat(provider.tryAcquire("job", Duration.ofSeconds(30))).isTrue();
    }

    @Test
    void should_returnFalse_when_setIfAbsentFailsOrNull() {
        when(valueOps.setIfAbsent(eq("lock:job"), eq("locked"), any(Duration.class))).thenReturn(false);
        assertThat(provider.tryAcquire("job", Duration.ofSeconds(30))).isFalse();

        when(valueOps.setIfAbsent(eq("lock:job"), eq("locked"), any(Duration.class))).thenReturn(null);
        assertThat(provider.tryAcquire("job", Duration.ofSeconds(30))).isFalse();
    }

    @Test
    void should_deletePrefixedKey_onRelease() {
        provider.release("job");
        verify(redisTemplate).delete("lock:job");
    }
}
