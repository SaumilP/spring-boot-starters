/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.idempotency.store;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import io.github.saumilp.starters.idempotency.model.CachedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed implementation of {@link IdempotencyStore}.
 *
 * <p>Each idempotency key maps to two Redis entries:
 * <ul>
 *   <li>{@code <prefix><key>} — the serialised {@link CachedResponse}, set with the
 *       configured TTL once the handler completes</li>
 *   <li>{@code <prefix><key>:lock} — an ephemeral lock entry set via
 *       {@code SET NX PX} to serialise concurrent requests</li>
 * </ul>
 *
 * <p>Jackson serialises {@link CachedResponse} to JSON. Java time types such as
 * {@link java.time.Instant} are handled natively by Jackson 3.x.
 *
 * @since 1.0.0
 */
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyStore.class);
    private static final String LOCK_SUFFIX = ":lock";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;

    /**
     * Constructs a {@code RedisIdempotencyStore} with the given Redis template and key prefix.
     *
     * @param redisTemplate the Spring Data Redis string template; must not be {@code null}
     * @param keyPrefix     the namespace prefix prepended to all Redis keys; must not be {@code null}
     */
    public RedisIdempotencyStore(StringRedisTemplate redisTemplate, String keyPrefix) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix     = keyPrefix;
        this.objectMapper  = new ObjectMapper();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Deserialises the JSON value stored at {@code <prefix><key>} into a
     * {@link CachedResponse}. Returns {@link Optional#empty()} if the key does not exist
     * or if deserialisation fails.
     */
    @Override
    public Optional<CachedResponse> get(String key) {
        try {
            String json = redisTemplate.opsForValue().get(prefixed(key));
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, CachedResponse.class));
        } catch (JacksonException ex) {
            log.warn("Failed to deserialise cached response for key '{}': {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Serialises the {@link CachedResponse} to JSON and stores it with {@code SET NX PX}
     * semantics so that the first writer wins in race conditions.
     */
    @Override
    public void put(String key, CachedResponse response, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().setIfAbsent(prefixed(key), json, ttl);
        } catch (JacksonException ex) {
            log.error("Failed to serialise cached response for key '{}': {}", key, ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses {@code SET NX PX} to atomically acquire the lock. Returns {@code true} only if
     * this call created the key, guaranteeing mutual exclusion.
     */
    @Override
    public boolean tryLock(String key, Duration timeout) {
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(prefixed(key) + LOCK_SUFFIX, "1", timeout);
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Deletes the lock key unconditionally. If the lock has already expired or was never
     * held, this is a no-op.
     */
    @Override
    public void unlock(String key) {
        redisTemplate.delete(prefixed(key) + LOCK_SUFFIX);
    }

    private String prefixed(String key) {
        return keyPrefix + key;
    }
}
