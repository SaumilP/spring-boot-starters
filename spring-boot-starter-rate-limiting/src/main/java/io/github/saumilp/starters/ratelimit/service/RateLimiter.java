/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.service;

/**
 * Contract for rate-limiting implementations.
 *
 * <p>Implementations must be thread-safe and may be backed by Redis (for distributed systems)
 * or an in-process data structure (for single-node deployments or tests). The caller is
 * responsible for constructing a meaningful key that uniquely identifies the operation and
 * the entity being limited (e.g., a combination of user ID and endpoint path).
 *
 * @since 1.0.0
 */
public interface RateLimiter {

    /**
     * Attempts to consume one request token for the given key within the specified window.
     *
     * <p>Implementations must atomically check the current request count and increment it
     * if the limit has not been reached. This operation must be safe for concurrent callers
     * using the same key.
     *
     * @param key           a unique identifier for the rate-limit bucket (e.g., "user:42:search");
     *                      must not be {@code null} or blank
     * @param maxRequests   the maximum number of requests permitted within the window;
     *                      must be a positive integer
     * @param windowSeconds the duration of the sliding window in seconds; must be a positive value
     * @return {@code true} if the request is within the limit and was accepted;
     *         {@code false} if the limit has been reached and the request should be rejected
     */
    boolean tryConsume(String key, int maxRequests, long windowSeconds);
}
