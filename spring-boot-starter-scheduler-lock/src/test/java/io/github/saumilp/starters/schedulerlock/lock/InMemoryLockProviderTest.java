/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.lock;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link InMemoryLockProvider}.
 */
class InMemoryLockProviderTest {

    private final InMemoryLockProvider provider = new InMemoryLockProvider();

    @Test
    void should_acquireOnce_thenRejectUntilReleased() {
        assertThat(provider.tryAcquire("job", Duration.ofSeconds(30))).isTrue();
        assertThat(provider.tryAcquire("job", Duration.ofSeconds(30))).isFalse();

        provider.release("job");

        assertThat(provider.tryAcquire("job", Duration.ofSeconds(30))).isTrue();
    }

    @Test
    void should_treatDifferentNamesIndependently() {
        assertThat(provider.tryAcquire("a", Duration.ofSeconds(30))).isTrue();
        assertThat(provider.tryAcquire("b", Duration.ofSeconds(30))).isTrue();
    }

    @Test
    void should_reacquire_afterExpiry() throws InterruptedException {
        assertThat(provider.tryAcquire("short", Duration.ofMillis(1))).isTrue();
        Thread.sleep(15);
        assertThat(provider.tryAcquire("short", Duration.ofSeconds(30))).isTrue();
    }
}
