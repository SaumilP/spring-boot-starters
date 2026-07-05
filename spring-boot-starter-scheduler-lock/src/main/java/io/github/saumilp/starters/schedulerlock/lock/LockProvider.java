/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.lock;

import java.time.Duration;

/**
 * Strategy for acquiring and releasing a named, cluster-wide lock.
 *
 * @since 1.0.0
 */
public interface LockProvider {

    /**
     * Attempts to acquire the named lock.
     *
     * @param name     the lock name; must not be {@code null}
     * @param atMostFor the maximum time the lock should be held before it auto-expires; must not be {@code null}
     * @return {@code true} if the lock was acquired by this caller; {@code false} if already held
     */
    boolean tryAcquire(String name, Duration atMostFor);

    /**
     * Releases the named lock. A no-op if the lock is not held or has already expired.
     *
     * @param name the lock name; must not be {@code null}
     */
    void release(String name);
}
