package com.featureflags.exception;

public class FlagVersionConflictException extends RuntimeException {

    public FlagVersionConflictException(String name) {
        super("Feature flag '" + name + "' was modified by another request; re-fetch and retry");
    }
}
