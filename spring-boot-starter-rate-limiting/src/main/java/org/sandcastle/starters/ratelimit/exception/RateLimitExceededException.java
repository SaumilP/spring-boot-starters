/*
 * Copyright (c) 2024 Saumil Patel. Apache License 2.0.
 */
package org.sandcastle.starters.ratelimit.exception;

import org.sandcastle.starters.common.exception.StarterException;

/**
 * Thrown when an incoming request exceeds the configured rate limit for the targeted
 * operation or endpoint.
 *
 * <p>This exception is caught by the bundled
 * {@link org.sandcastle.starters.ratelimit.web.RateLimitExceptionHandler}, which translates
 * it into an HTTP {@code 429 Too Many Requests} response with a {@code Retry-After} header
 * indicating the number of seconds until the window resets.
 *
 * <p>If the consuming application defines its own {@code @ControllerAdvice} that handles
 * {@code RateLimitExceededException}, it takes precedence over the starter's handler.
 *
 * @since 1.0.0
 * @see StarterException
 */
public class RateLimitExceededException extends StarterException {

    /** The rate-limit key that was exhausted. */
    private final String limitKey;

    /** The configured maximum number of requests per window. */
    private final int maxRequests;

    /** The window duration in seconds after which the limit resets. */
    private final long windowSeconds;

    /**
     * Constructs a {@code RateLimitExceededException} with context about the violated limit.
     *
     * @param limitKey      the rate-limit key that was exhausted; must not be {@code null}
     * @param maxRequests   the maximum requests allowed per window; must be a positive integer
     * @param windowSeconds the window duration in seconds; must be a positive value
     */
    public RateLimitExceededException(String limitKey, int maxRequests, long windowSeconds) {
        super(String.format("Rate limit exceeded for key '%s': max %d requests per %d seconds.",
            limitKey, maxRequests, windowSeconds));
        this.limitKey      = limitKey;
        this.maxRequests   = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Returns the rate-limit key that was exhausted.
     *
     * @return the limit key; never {@code null}
     */
    public String getLimitKey() {
        return limitKey;
    }

    /**
     * Returns the maximum number of requests allowed per window for the exhausted key.
     *
     * @return the configured maximum; always a positive integer
     */
    public int getMaxRequests() {
        return maxRequests;
    }

    /**
     * Returns the window duration in seconds after which the limit counter resets.
     *
     * @return the window in seconds; always a positive value
     */
    public long getWindowSeconds() {
        return windowSeconds;
    }
}
