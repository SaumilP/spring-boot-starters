package io.github.saumilp.starters.exceptions;

/**
 * Checked exception thrown when a MinIO storage operation fails.
 *
 * @since 1.0.0
 */
public class MinioException extends Exception {

    /**
     * Creates a new exception with the given message.
     *
     * @param message the detail message
     */
    public MinioException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause
     */
    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
}
