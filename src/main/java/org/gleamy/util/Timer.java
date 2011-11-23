package org.gleamy.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.gleamy.util.concurrent.Promise;
import org.gleamy.util.concurrent.RichFuture;

public abstract class Timer {

    public abstract Cancellable schedule(Date when, Function f);

    public abstract Cancellable schedule(Date when, long period, TimeUnit unit, Function f);

    /**
     * Performs an operation after the specified delay.  Cancelling the Future
     * will cancel the scheduled timer task, if not too late.
     */
    public <A> RichFuture<A> doLater(long delay, TimeUnit unit, Function0<A> f) {
        long timeDelay = System.currentTimeMillis() + unit.toMillis(delay);
        return doAt(new Date(timeDelay), f);
    }

    /**
     * Performs an operation at the specified time.  Cancelling the Future
     * will cancel the scheduled timer task, if not too late.
     */
    public <A> RichFuture<A> doAt(Date time, final Function0<A> f) {
        final Promise<A> promise = new Promise<A>();
        final Cancellable task = schedule(time, new Function() {
            public void apply() {
                promise.update(TryAbstract.<A>newInstance(f));
            }
        });
        promise.linkTo(new CancellableSink(new Function() {
            public void apply() {
                task.cancel();
            }
        }));
        return promise;
    }

    public abstract void stop();
}
