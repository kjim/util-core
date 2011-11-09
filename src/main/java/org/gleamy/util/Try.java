package org.gleamy.util;

import java.util.concurrent.Callable;

public interface Try<R> extends Callable<R> {
    /**
     * Returns true if the Try is a Throw, false otherwise.
     */
    boolean isThrow();

    /**
     * Returns true if the Try is a Return, false otherwise.
     */
    boolean isReturn();

    R get();
}
