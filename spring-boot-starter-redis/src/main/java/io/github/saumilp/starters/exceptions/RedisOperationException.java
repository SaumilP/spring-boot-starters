/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.exceptions;

import io.github.saumilp.starters.common.exception.StarterException;

/**
 * Signals that a Redis operation could not be completed due to a connectivity failure,
 * a serialisation error, or an unexpected response from the Redis server.
 *
 * <p>This exception wraps the underlying Spring Data Redis exception hierarchy so that
 * consuming application code does not need to import Spring Data Redis-specific exception types
 * directly.
 *
 * <p>Example:
 * <pre>{@code
 * try {
 *     redisUtil.set("key", value, 300);
 * } catch (RedisOperationException ex) {
 *     log.error("Failed to cache value: {}", ex.getMessage(), ex);
 * }
 * }</pre>
 *
 * @since 1.0.0
 * @see StarterException
 */
public class RedisOperationException extends StarterException {

    /**
     * Constructs a {@code RedisOperationException} with the specified detail message.
     *
     * @param message a human-readable description of the Redis error; must not be {@code null}
     */
    public RedisOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code RedisOperationException} with a detail message and the underlying cause.
     *
     * @param message a human-readable description of the Redis error; must not be {@code null}
     * @param cause   the Spring Data Redis or IO exception that triggered this error; may be {@code null}
     */
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
