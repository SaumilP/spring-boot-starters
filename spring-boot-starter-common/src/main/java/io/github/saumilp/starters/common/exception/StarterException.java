/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.common.exception;

/**
 * Base unchecked exception for all Spring Boot starters in this project.
 *
 * <p>All starter-specific exceptions extend this class, allowing consuming applications to
 * catch the full hierarchy from a single catch block when fine-grained exception distinction
 * is not required.
 *
 * <p>Example:
 * <pre>{@code
 * try {
 *     storageService.upload(path, inputStream);
 * } catch (StarterException ex) {
 *     log.error("Starter operation failed: {}", ex.getMessage(), ex);
 * }
 * }</pre>
 *
 * @since 1.0.0
 */
public class StarterException extends RuntimeException {

    /**
     * Constructs a {@code StarterException} with the specified detail message.
     *
     * @param message a human-readable description of the error condition; must not be {@code null}
     */
    public StarterException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code StarterException} with the specified detail message and cause.
     *
     * @param message a human-readable description of the error condition; must not be {@code null}
     * @param cause   the underlying exception that triggered this error; may be {@code null}
     */
    public StarterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code StarterException} wrapping the given cause.
     * The detail message is derived from the cause's own message.
     *
     * @param cause the underlying exception; must not be {@code null}
     */
    public StarterException(Throwable cause) {
        super(cause);
    }
}
