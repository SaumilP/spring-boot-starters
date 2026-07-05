/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.resilientclient.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import java.util.function.Supplier;

/**
 * Façade that decorates an arbitrary call with Resilience4j retry and circuit-breaker semantics.
 *
 * <p>The supplied operation is wrapped so that the circuit breaker records each attempt and the
 * retry policy re-invokes it on failure. Typical use with the auto-configured
 * {@code resilientRestClient}:
 * <pre>{@code
 * String body = resilientClient.execute(() ->
 *     resilientRestClient.get().uri("https://api.example.com/things").retrieve().body(String.class));
 * }</pre>
 *
 * @since 1.0.0
 */
public class ResilientClient {

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    /**
     * Constructs the client.
     *
     * @param retry          the retry policy; must not be {@code null}
     * @param circuitBreaker the circuit breaker; must not be {@code null}
     */
    public ResilientClient(Retry retry, CircuitBreaker circuitBreaker) {
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Executes the supplied operation with circuit-breaker and retry protection.
     *
     * <p>The circuit breaker is the inner decorator, so each retry attempt is recorded by the
     * breaker; the retry policy is the outer decorator and re-invokes on failure until the
     * configured attempts are exhausted.
     *
     * @param supplier the operation to execute; must not be {@code null}
     * @param <T>      the result type
     * @return the operation result
     * @throws io.github.resilience4j.circuitbreaker.CallNotPermittedException if the breaker is open
     * @throws RuntimeException if the operation fails after all retry attempts
     */
    public <T> T execute(Supplier<T> supplier) {
        Supplier<T> decorated = Retry.decorateSupplier(retry,
            CircuitBreaker.decorateSupplier(circuitBreaker, supplier));
        return decorated.get();
    }

    /** {@return the underlying retry policy} */
    public Retry getRetry() {
        return retry;
    }

    /** {@return the underlying circuit breaker} */
    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }
}
