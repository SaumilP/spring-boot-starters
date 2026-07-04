/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.ratelimit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * Redis-backed rate limiter using a sliding window algorithm implemented as an atomic Lua script.
 *
 * <p>The sliding window is maintained as a Redis sorted set where each member is a unique
 * timestamp representing a single request. On every call to {@link #tryConsume}, the script:
 * <ol>
 *   <li>Removes all members older than {@code now - windowMs} (expired requests)</li>
 *   <li>Counts the remaining members</li>
 *   <li>If the count is below {@code maxRequests}, adds the current timestamp and returns {@code 1}
 *       (accepted)</li>
 *   <li>Otherwise returns {@code 0} (rejected)</li>
 * </ol>
 *
 * <p>Because the entire operation executes atomically inside Redis via a Lua script, there are
 * no race conditions between the count-check and the member-add steps, even under high
 * concurrency across multiple application instances.
 *
 * <p>Keys are set to expire automatically after the window duration so that idle sorted sets
 * do not accumulate in Redis indefinitely.
 *
 * @since 1.0.0
 * @see RateLimiter
 */
public class RedisTokenBucketRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisTokenBucketRateLimiter.class);

    /**
     * Lua script implementing the atomic sliding window counter.
     * KEYS[1] = rate-limit key, ARGV[1] = window ms, ARGV[2] = max requests, ARGV[3] = now ms.
     */
    private static final String SLIDING_WINDOW_SCRIPT =
        "local key    = KEYS[1] " +
        "local window = tonumber(ARGV[1]) " +
        "local limit  = tonumber(ARGV[2]) " +
        "local now    = tonumber(ARGV[3]) " +
        "local cutoff = now - window " +
        "redis.call('ZREMRANGEBYSCORE', key, 0, cutoff) " +
        "local count = redis.call('ZCARD', key) " +
        "if count < limit then " +
        "  redis.call('ZADD', key, now, now .. ':' .. math.random(1, 1000000)) " +
        "  redis.call('PEXPIRE', key, window) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> script;

    /**
     * Constructs a {@code RedisTokenBucketRateLimiter} using the given Redis template.
     *
     * @param redisTemplate the Redis template used to execute the Lua script;
     *                      must not be {@code null}
     */
    public RedisTokenBucketRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>(SLIDING_WINDOW_SCRIPT, Long.class);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Executes the sliding-window Lua script atomically in Redis. If Redis is unavailable,
     * the exception is logged and the request is allowed through (fail-open behaviour) to
     * avoid blocking traffic during Redis outages.
     *
     * @param key           the rate-limit bucket key; must not be {@code null}
     * @param maxRequests   maximum requests per window; must be positive
     * @param windowSeconds window duration in seconds; must be positive
     * @return {@code true} if accepted; {@code false} if the limit has been reached
     */
    @Override
    public boolean tryConsume(String key, int maxRequests, long windowSeconds) {
        try {
            long windowMs = windowSeconds * 1000L;
            long now      = System.currentTimeMillis();
            Long result   = redisTemplate.execute(
                script,
                List.of(key),
                String.valueOf(windowMs),
                String.valueOf(maxRequests),
                String.valueOf(now)
            );
            return Long.valueOf(1L).equals(result);
        } catch (Exception ex) {
            log.warn("Rate limiter Redis call failed for key '{}'; allowing request (fail-open). Cause: {}",
                key, ex.getMessage());
            return true;
        }
    }
}
