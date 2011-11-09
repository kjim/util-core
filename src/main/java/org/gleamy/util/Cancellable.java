package org.gleamy.util;

public interface Cancellable {
    boolean isCancelled();

    /**
     * Cancel the computation.  The cancellation is propagated to linked
     * cancellable objects.
     */
    void cancel();

    /**
     * Link this cancellable computation to 'other'.  This means
     * cancellation of 'this' computation will propagate to 'other'.
     */
    void linkTo(Cancellable other);
}
