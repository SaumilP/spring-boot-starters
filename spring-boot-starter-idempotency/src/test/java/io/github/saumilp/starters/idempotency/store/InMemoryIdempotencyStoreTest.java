/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.store;

import io.github.saumilp.starters.idempotency.model.CachedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link IdempotencyStore} contract using an in-memory test double.
 *
 * <p>These tests validate the expected behaviour of any {@link IdempotencyStore} implementation —
 * cache hit/miss semantics, lock acquisition and release, and first-writer-wins put semantics.
 */
class InMemoryIdempotencyStoreTest {

    private IdempotencyStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryIdempotencyStore();
    }

    @Test
    void should_returnEmpty_when_keyNotCached() {
        assertThat(store.get("missing-key")).isEmpty();
    }

    @Test
    void should_returnCachedResponse_when_keyExists() {
        CachedResponse response = cachedResponse("key1");
        store.put("key1", response, Duration.ofMinutes(1));
        assertThat(store.get("key1")).isPresent().contains(response);
    }

    @Test
    void should_notOverwrite_when_keyAlreadyExists() {
        CachedResponse first  = cachedResponse("dup");
        CachedResponse second = cachedResponse("dup");
        store.put("dup", first, Duration.ofMinutes(1));
        store.put("dup", second, Duration.ofMinutes(1));
        assertThat(store.get("dup")).isPresent().contains(first);
    }

    @Test
    void should_acquireLock_when_notAlreadyLocked() {
        assertThat(store.tryLock("lockKey", Duration.ofSeconds(10))).isTrue();
    }

    @Test
    void should_rejectLock_when_alreadyLocked() {
        store.tryLock("lockKey2", Duration.ofSeconds(10));
        assertThat(store.tryLock("lockKey2", Duration.ofSeconds(10))).isFalse();
    }

    @Test
    void should_allowLock_after_unlock() {
        store.tryLock("lockKey3", Duration.ofSeconds(10));
        store.unlock("lockKey3");
        assertThat(store.tryLock("lockKey3", Duration.ofSeconds(10))).isTrue();
    }

    @Test
    void should_isolateLocks_for_differentKeys() {
        store.tryLock("alpha", Duration.ofSeconds(10));
        assertThat(store.tryLock("beta", Duration.ofSeconds(10))).isTrue();
    }

    private CachedResponse cachedResponse(String key) {
        return new CachedResponse(
            200,
            Map.of("Content-Type", "application/json"),
            "{\"ok\":true}",
            Instant.now(),
            key
        );
    }

    /**
     * Simple in-memory {@link IdempotencyStore} implementation used as a test double.
     * Not suitable for production — no TTL enforcement, no distributed coordination.
     */
    static class InMemoryIdempotencyStore implements IdempotencyStore {

        private final ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Boolean> locks = new ConcurrentHashMap<>();

        @Override
        public Optional<CachedResponse> get(String key) {
            return Optional.ofNullable(cache.get(key));
        }

        @Override
        public void put(String key, CachedResponse response, Duration ttl) {
            cache.putIfAbsent(key, response);
        }

        @Override
        public boolean tryLock(String key, Duration timeout) {
            return locks.putIfAbsent(key, true) == null;
        }

        @Override
        public void unlock(String key) {
            locks.remove(key);
        }
    }
}
