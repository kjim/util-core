package org.gleamy.util;

import java.util.Date;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.gleamy.util.concurrent.NamedPoolThreadFactory;

public class ScheduledThreadPoolTimer extends Timer {
    private ScheduledThreadPoolExecutor underlying;

    public ScheduledThreadPoolTimer() {
        this(2, "timer");
    }

    public ScheduledThreadPoolTimer(int poolSize, String name) {
        this(poolSize, new NamedPoolThreadFactory(name));
    }

    public ScheduledThreadPoolTimer(int poolSize, ThreadFactory threadFactory) {
        this(poolSize, threadFactory, null);
    }

    public ScheduledThreadPoolTimer(int poolSize, ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
        ScheduledThreadPoolExecutor scheduler = handler == null
            ? new ScheduledThreadPoolExecutor(poolSize, threadFactory)
            : new ScheduledThreadPoolExecutor(poolSize, threadFactory, handler);

        this.underlying = scheduler;
    }

    @Override
    public Cancellable schedule(Date when, Function f) {
        final long fromNow = when.getTime() - System.currentTimeMillis();
        final ScheduledFuture<?> scheduled = underlying.schedule(new TimerTask(f), fromNow, TimeUnit.MILLISECONDS);
        return new CancellableSink(new Function() {
            public void apply() {
                scheduled.cancel(true);
            }
        });
    }

    @Override
    public Cancellable schedule(Date when, long period, TimeUnit unit, Function f) {
        long fromNow = when.getTime() - System.currentTimeMillis();
        return schedule(fromNow, TimeUnit.MILLISECONDS, period, unit, f);
    }

    public Cancellable schedule(long wait, TimeUnit waitUnit, long period, TimeUnit periodUnit,
            Function f) {
        final ScheduledFuture<?> scheduled = underlying.scheduleAtFixedRate(
                new TimerTask(f),
                waitUnit.toMillis(wait), periodUnit.toMillis(period), TimeUnit.MILLISECONDS);
        return new CancellableSink(new Function() {
            public void apply() {
                scheduled.cancel(true);
            }
        });
    }

    @Override
    public void stop() {
        underlying.shutdown();
    }

    private static class TimerTask implements Runnable {
        private Function f;

        public TimerTask(Function f) {
            this.f = f;
        }

        @Override
        public void run() {
            f.apply();
        }
    }
}
