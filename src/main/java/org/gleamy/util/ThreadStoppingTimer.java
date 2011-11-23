package org.gleamy.util;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadStoppingTimer extends Timer {
    private Timer underlying;
    private ExecutorService executor;

    public ThreadStoppingTimer(Timer underlying, ExecutorService executor) {
        this.underlying = underlying;
        this.executor = executor;
    }

    @Override
    public Cancellable schedule(Date when, Function f) {
        return underlying.schedule(when, f);
    }

    @Override
    public Cancellable schedule(Date when, long period, TimeUnit unit, Function f) {
        return underlying.schedule(when, period, unit, f);
    }

    @Override
    public void stop() {
        executor.submit(new Runnable() {
            public void run() {
                underlying.stop();
            }
        });
    }
}
