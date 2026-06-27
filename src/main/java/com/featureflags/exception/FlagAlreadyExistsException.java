package com.featureflags.exception;

public class FlagAlreadyExistsException extends RuntimeException {

    public FlagAlreadyExistsException(String name) {
        super("Feature flag already exists: " + name);
    }
}
