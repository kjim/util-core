package org.gleamy.util.concurrent;

public class ImmutableRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ImmutableRuntimeException(String message) {
        super(message);
    }
}
