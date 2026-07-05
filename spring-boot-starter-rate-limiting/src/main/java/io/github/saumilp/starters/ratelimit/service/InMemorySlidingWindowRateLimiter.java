/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding window rate limiter intended for single-node deployments and tests.
 *
 * <p>Maintains a per-key {@link Deque} of request timestamps. On each {@link #tryConsume}
 * call, entries older than the window are evicted before the count is checked. This provides
 * accurate sliding-window semantics without requiring Redis.
 *
 * <p><strong>Warning:</strong> This implementation is not suitable for multi-instance
 * deployments because state is local to the JVM. Use
 * {@link RedisTokenBucketRateLimiter} in distributed environments.
 *
 * <p>The implementation is thread-safe: all mutations on a given bucket are synchronised on
 * the bucket's own deque instance, avoiding contention between independent keys while
 * preventing race conditions within the same key.
 *
 * @since 1.0.0
 * @see RateLimiter
 */
public class InMemorySlidingWindowRateLimiter implements RateLimiter {

    /** Creates a new in-memory sliding-window rate limiter. */
    public InMemorySlidingWindowRateLimiter() {
    }

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * <p>Evicts expired timestamps from the bucket, then accepts or rejects the request
     * based on the current count. All operations on a single bucket are synchronised.
     *
     * @param key           the rate-limit bucket key; must not be {@code null}
     * @param maxRequests   maximum requests per window; must be positive
     * @param windowSeconds window duration in seconds; must be positive
     * @return {@code true} if the request was accepted; {@code false} if rejected
     */
    @Override
    public boolean tryConsume(String key, int maxRequests, long windowSeconds) {
        Deque<Long> bucket = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (bucket) {
            long now    = System.currentTimeMillis();
            long cutoff = now - (windowSeconds * 1000L);
            while (!bucket.isEmpty() && bucket.peekFirst() <= cutoff) {
                bucket.pollFirst();
            }
            if (bucket.size() < maxRequests) {
                bucket.addLast(now);
                return true;
            }
            return false;
        }
    }
}
