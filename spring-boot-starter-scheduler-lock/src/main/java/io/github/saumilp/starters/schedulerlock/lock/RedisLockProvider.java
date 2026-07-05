/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.schedulerlock.lock;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Cluster-safe {@link LockProvider} backed by Redis.
 *
 * <p>Acquisition uses an atomic {@code SET key value NX PX ttl}: only the first instance to set the
 * key wins, and the key auto-expires after {@code atMostFor} so a crashed holder cannot deadlock the
 * task. Keys are namespaced with the configured prefix.
 *
 * @since 1.0.0
 */
public class RedisLockProvider implements LockProvider {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;

    /**
     * Constructs the provider.
     *
     * @param redisTemplate the Redis string template; must not be {@code null}
     * @param keyPrefix     the key namespace prefix; must not be {@code null}
     */
    public RedisLockProvider(StringRedisTemplate redisTemplate, String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
    }

    /**
     * {@inheritDoc}
     *
     * @param name     the lock name; must not be {@code null}
     * @param atMostFor the auto-expiry duration; must not be {@code null}
     * @return {@code true} if the key was set by this caller; {@code false} otherwise
     */
    @Override
    public boolean tryAcquire(String name, Duration atMostFor) {
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(keyPrefix + name, "locked", atMostFor);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * {@inheritDoc}
     *
     * @param name the lock name; must not be {@code null}
     */
    @Override
    public void release(String name) {
        redisTemplate.delete(keyPrefix + name);
    }
}
