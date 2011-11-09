package org.gleamy.util.concurrent;

public class ImmutableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ImmutableException(String message) {
        super(message);
    }
}
