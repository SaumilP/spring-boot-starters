package org.sandcastle.starters.exceptions;

public class MinioException extends Exception {

    public MinioException(String message) {
        super(message);
    }

    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
}
