package com.featureflags.exception;

public class FlagNotFoundException extends RuntimeException {

    public FlagNotFoundException(String name) {
        super("Feature flag not found: " + name);
    }
}
