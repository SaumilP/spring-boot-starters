/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.store;

import io.github.saumilp.starters.idempotency.model.CachedResponse;

import java.time.Duration;
import java.util.Optional;

/**
 * Contract for idempotency response stores.
 *
 * <p>Implementations are responsible for the atomic get-or-set behaviour required to
 * handle concurrent requests with the same idempotency key. The critical requirement is
 * that only the first writer wins: if two requests arrive simultaneously with the same key,
 * exactly one of them proceeds to the handler while the other waits and eventually receives
 * the cached result.
 *
 * <p>Implementations must be thread-safe.
 *
 * @since 1.0.0
 */
public interface IdempotencyStore {

    /**
     * Retrieves a previously cached response for the given idempotency key.
     *
     * @param key the idempotency key; must not be {@code null} or blank
     * @return an {@link Optional} containing the cached response, or empty if no cached
     *         response exists for the key
     */
    Optional<CachedResponse> get(String key);

    /**
     * Stores a response for the given idempotency key with the specified TTL.
     *
     * <p>If a response is already stored for the key (e.g., due to a race condition),
     * the existing entry must not be overwritten — the first writer wins.
     *
     * @param key      the idempotency key; must not be {@code null} or blank
     * @param response the response to cache; must not be {@code null}
     * @param ttl      how long to retain the cached entry; must be a positive duration
     */
    void put(String key, CachedResponse response, Duration ttl);

    /**
     * Marks a key as "in progress" to signal that a request is currently being processed.
     *
     * <p>This mechanism prevents a second concurrent request with the same key from
     * executing the handler simultaneously. The lock expires automatically after the given
     * timeout to prevent deadlocks if the first request fails before caching its response.
     *
     * @param key     the idempotency key; must not be {@code null} or blank
     * @param timeout how long to hold the in-progress marker before auto-releasing
     * @return {@code true} if the lock was acquired (this request should proceed);
     *         {@code false} if another request is already processing the same key
     */
    boolean tryLock(String key, Duration timeout);

    /**
     * Releases the in-progress marker for the given key.
     *
     * <p>This must be called after the handler completes (successfully or not) to free the
     * lock for future requests if no cached response was stored.
     *
     * @param key the idempotency key to unlock; must not be {@code null} or blank
     */
    void unlock(String key);
}
