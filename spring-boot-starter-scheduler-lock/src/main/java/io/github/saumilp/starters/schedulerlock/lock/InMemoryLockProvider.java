/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.lock;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single-JVM {@link LockProvider} backed by a concurrent map.
 *
 * <p>Suitable for development and single-instance deployments. It is <strong>not</strong>
 * cluster-safe — use {@link RedisLockProvider} across multiple instances.
 *
 * @since 1.0.0
 */
public class InMemoryLockProvider implements LockProvider {

    private final ConcurrentHashMap<String, Long> locks = new ConcurrentHashMap<>();

    /** Creates a new in-memory lock provider. */
    public InMemoryLockProvider() {
    }

    /**
     * {@inheritDoc}
     *
     * @param name     the lock name; must not be {@code null}
     * @param atMostFor the auto-expiry duration; must not be {@code null}
     * @return {@code true} if acquired; {@code false} if a non-expired lock is already held
     */
    @Override
    public boolean tryAcquire(String name, Duration atMostFor) {
        long now = System.currentTimeMillis();
        long expiry = now + atMostFor.toMillis();
        AtomicBoolean acquired = new AtomicBoolean(false);
        locks.compute(name, (key, existingExpiry) -> {
            if (existingExpiry == null || existingExpiry <= now) {
                acquired.set(true);
                return expiry;
            }
            return existingExpiry;
        });
        return acquired.get();
    }

    /**
     * {@inheritDoc}
     *
     * @param name the lock name; must not be {@code null}
     */
    @Override
    public void release(String name) {
        locks.remove(name);
    }
}
