package org.gleamy.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A NullTimer is not a timer at all: it invokes all tasks immediately
 * and inline.
 */
public class NullTimer extends Timer {

    @Override
    public Cancellable schedule(Date when, Function f) {
        f.apply();
        return NULL;
    }

    @Override
    public Cancellable schedule(Date when, long period, TimeUnit unit, Function f) {
        f.apply();
        return NULL;
    }

    @Override
    public void stop() {
        // NOP
    }

    private static final Cancellable NULL = new CancellableSink(new Function() {
        public void apply() {
            // NOP
        }
    });
}
