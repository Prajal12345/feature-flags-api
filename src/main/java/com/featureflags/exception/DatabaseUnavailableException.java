package com.featureflags.exception;

public class DatabaseUnavailableException extends RuntimeException {

    public DatabaseUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
