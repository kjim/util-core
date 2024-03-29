package org.gleamy.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NativeTimer extends Timer {
    private java.util.Timer underlying;

    public NativeTimer() {
        this(false);
    }

    public NativeTimer(boolean isDaemon) {
        this.underlying = new java.util.Timer(isDaemon);
    }

    @Override
    public Cancellable schedule(Date when, Function f) {
        java.util.TimerTask task = toNativeTimerTask(f);
        underlying.schedule(task, when);
        return fromNativeTimerTask(task);
    }

    @Override
    public Cancellable schedule(Date when, long period, TimeUnit unit, Function f) {
        java.util.TimerTask task = toNativeTimerTask(f);
        underlying.schedule(task, when, unit.toMillis(period));
        return fromNativeTimerTask(task);
    }

    @Override
    public void stop() {
        underlying.cancel();
    }

    static java.util.TimerTask toNativeTimerTask(final Function f) {
        return new java.util.TimerTask() {
            private final AtomicBoolean cancelled = new AtomicBoolean(false);
            public void run() {
                if (cancelled.get()) {
                    return;
                }
                f.apply();
            }
            public boolean cancel() {
                if (cancelled.compareAndSet(false, true)) {
                    return super.cancel();
                }
                else {
                    return false;
                }
            }
        };
    }

    static  Cancellable fromNativeTimerTask(final java.util.TimerTask task) {
        return new CancellableSink(new Function() {
            public void apply() {
                task.cancel();
            }
        });
    }
}
