/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.resilientclient.client;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ResilientClient} retry and circuit-breaker behaviour.
 */
class ResilientClientTest {

    @Test
    void should_retryUntilSuccess() {
        Retry retry = Retry.of("t", RetryConfig.custom()
            .maxAttempts(3).waitDuration(Duration.ofMillis(1)).build());
        ResilientClient client = new ResilientClient(retry, CircuitBreaker.ofDefaults("t"));
        AtomicInteger calls = new AtomicInteger();

        String result = client.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new IllegalStateException("transient");
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void should_propagateException_whenAllAttemptsFail() {
        Retry retry = Retry.of("t", RetryConfig.custom()
            .maxAttempts(2).waitDuration(Duration.ofMillis(1)).build());
        ResilientClient client = new ResilientClient(retry, CircuitBreaker.ofDefaults("t-fail"));
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> client.execute(() -> {
            calls.incrementAndGet();
            throw new IllegalStateException("always");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void should_openCircuit_afterFailureThresholdReached() {
        Retry noRetry = Retry.of("t2", RetryConfig.custom().maxAttempts(1).build());
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(3)
            .minimumNumberOfCalls(3)
            .failureRateThreshold(50f)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .build();
        ResilientClient client = new ResilientClient(noRetry, CircuitBreaker.of("t2", config));

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> client.execute(() -> {
                throw new IllegalStateException("boom");
            })).isInstanceOf(IllegalStateException.class);
        }

        assertThatThrownBy(() -> client.execute(() -> "unreachable"))
            .isInstanceOf(CallNotPermittedException.class);
    }
}
