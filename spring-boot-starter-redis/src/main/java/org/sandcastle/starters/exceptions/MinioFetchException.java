package org.sandcastle.starters.exceptions;

public class MinioFetchException extends RuntimeException {

    public MinioFetchException(String message) {
        super(message);
    }

    public MinioFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
