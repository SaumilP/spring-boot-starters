/*
 * Copyright (c) 2024 SaumilP. Apache License 2.0.
 */
package io.github.saumilp.starters.llm.exception;

import io.github.saumilp.starters.common.exception.StarterException;

/**
 * Thrown when an LLM request fails — either because the remote endpoint returned a
 * non-2xx status, the connection timed out, or all configured retry attempts were
 * exhausted without a successful response.
 *
 * <p>The cause, when present, is typically a Spring {@code RestClientException} or an
 * {@code InterruptedException} from the retry back-off sleep.
 *
 * @since 1.0.0
 * @see StarterException
 */
public class LlmClientException extends StarterException {

    /**
     * Constructs an exception with the given detail message.
     *
     * @param message a human-readable description of the failure; must not be {@code null}
     */
    public LlmClientException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the given detail message and root cause.
     *
     * @param message a human-readable description of the failure; must not be {@code null}
     * @param cause   the underlying exception that triggered this failure; may be {@code null}
     */
    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception wrapping the given root cause. The detail message is derived
     * from {@link Throwable#getMessage()} of the cause.
     *
     * @param cause the underlying exception; may be {@code null}
     */
    public LlmClientException(Throwable cause) {
        super(cause);
    }
}
