/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.common.exception;

/**
 * Signals that a starter cannot be initialised due to missing or invalid configuration.
 *
 * <p>Thrown during application context startup — typically from an {@code @AutoConfiguration}
 * class or a {@code @Bean} factory method — when a required configuration property is absent
 * or has an invalid value. This exception is intentionally unchecked so that it propagates
 * cleanly through the Spring application context lifecycle without forcing callers to declare
 * checked exceptions.
 *
 * <p>Example:
 * <pre>{@code
 * if (!StringUtils.hasText(props.getUrl())) {
 *     throw new StarterConfigurationException(
 *         "spring.minio.url must be configured. " +
 *         "Set it to the MinIO server endpoint (e.g., http://localhost:9000).");
 * }
 * }</pre>
 *
 * @since 1.0.0
 * @see StarterException
 */
public class StarterConfigurationException extends StarterException {

    /**
     * Constructs a {@code StarterConfigurationException} with the specified detail message.
     *
     * <p>The message should describe which property is missing or invalid and include an
     * example of a valid value to help the developer resolve the issue quickly.
     *
     * @param message a human-readable description of the configuration error; must not be {@code null}
     */
    public StarterConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code StarterConfigurationException} with a detail message and cause.
     *
     * @param message a human-readable description of the configuration error; must not be {@code null}
     * @param cause   the underlying exception (e.g., a parse or type-conversion failure); may be {@code null}
     */
    public StarterConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
