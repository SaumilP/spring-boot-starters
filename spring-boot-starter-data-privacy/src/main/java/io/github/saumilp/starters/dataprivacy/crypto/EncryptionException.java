/*
 * Copyright (c) 2010-2026 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.dataprivacy.crypto;

/**
 * Thrown when a field encryption or decryption operation fails.
 *
 * @since 1.0.0
 */
public class EncryptionException extends RuntimeException {

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
