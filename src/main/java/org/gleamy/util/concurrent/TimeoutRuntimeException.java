package org.gleamy.util.concurrent;

import java.util.concurrent.TimeoutException;

public class TimeoutRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TimeoutRuntimeException() {
        super(new TimeoutException());
    }
}
