/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.s3.exception;

import io.github.saumilp.starters.common.exception.StarterException;

/**
 * Thrown when an AWS S3 operation fails due to a client error, network issue, or
 * service-side exception.
 *
 * <p>This exception wraps the underlying AWS SDK exception so that callers are
 * insulated from SDK-specific types and can handle storage failures uniformly,
 * regardless of whether the backing store is S3, MinIO, or another provider.
 *
 * <p>The original SDK exception is always available via {@link #getCause()} for
 * diagnostic purposes.
 *
 * @since 1.0.0
 * @see StarterException
 */
public class S3OperationException extends StarterException {

    /**
     * Constructs an {@code S3OperationException} with the given detail message.
     *
     * @param message a human-readable description of the failure; must not be {@code null}
     */
    public S3OperationException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code S3OperationException} with the given detail message and root cause.
     *
     * @param message a human-readable description of the failure; must not be {@code null}
     * @param cause   the underlying exception from the AWS SDK or network layer;
     *                may be {@code null}
     */
    public S3OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
