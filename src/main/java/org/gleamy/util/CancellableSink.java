package org.gleamy.util;

import java.util.concurrent.atomic.AtomicBoolean;

public class CancellableSink implements Cancellable {
    private AtomicBoolean wasCancelled = new AtomicBoolean(false);
    private Function f;

    public CancellableSink(Function f) {
        this.f = f;
    }

    @Override
    public boolean isCancelled() {
        return wasCancelled.get();
    }

    @Override
    public void cancel() {
        if (wasCancelled.compareAndSet(false, true)) {
            f.apply();
        }
    }

    @Override
    public void linkTo(Cancellable other) {
        throw new UnsupportedOperationException("linking not supported in CancellableSink");
    }
}
