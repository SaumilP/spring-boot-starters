package io.github.saumilp.starters.exceptions;

/**
 * Unchecked exception thrown when fetching or parsing MinIO object listings fails.
 *
 * @since 1.0.0
 */
public class MinioFetchException extends RuntimeException {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the detail message
     */
    public MinioFetchException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public MinioFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
