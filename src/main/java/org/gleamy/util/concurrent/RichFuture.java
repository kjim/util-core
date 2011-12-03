package org.gleamy.util.concurrent;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gleamy.util.Cancellable;
import org.gleamy.util.Function;
import org.gleamy.util.Function0;
import org.gleamy.util.Function1;
import org.gleamy.util.Return;
import org.gleamy.util.Throw;
import org.gleamy.util.Timer;
import org.gleamy.util.Try;
import org.gleamy.util.TryAbstract;
import org.gleamy.util.Unit;

public abstract class RichFuture<A> {

    /**
     * Make a Future with a constant value. E.g., Future.value(1) is a Future[Int].
     */
    public static <A> RichFuture<A> value(A a) {
        return new Promise<A>(new Return<A>(a));
    }

    /**
     * Make a Future with an error. E.g., Future.exception(new Exception("boo"))
     */
    public static <A> RichFuture<A> exception(RuntimeException e) {
        return new Promise<A>(new Throw<A>(e));
    }

    /**
     * A factory function to "lift" computations into the Future monad. It will catch
     * exceptions and wrap them in the Throw[_] type. Non-exceptional values are wrapped
     * in the Return[_] type.
     */
    public static <A> RichFuture<A> newInstance(Function0<A> a) {
        return new Promise<A>(TryAbstract.newInstance(a));
    }

    /**
     * When the computation completes, invoke the given callback function. Respond()
     * yields a Try (either a Return or a Throw). This method is most useful for
     * very generic code (like libraries). Otherwise, it is a best practice to use
     * one of the alternatives (onSuccess(), onFailure(), etc.). Note that almost
     * all methods on Future[_] are written in terms of respond(), so this is
     * the essential template method for use in concrete subclasses.
     */
    public abstract RichFuture<A> respond(Function1<Try<A>, Unit> k);

    /**
     * Block indefinitely, wait for the result of the Future to be available.
     */
    public A apply() {
        return get().get();
    }

    /**
     * Block, but only as long as the given Timeout.
     */
    public A apply(long timeout, TimeUnit unit) throws TimeoutException {
        return get(timeout, unit).get();
    }

    /**
     * Is the result of the Future available yet?
     */
    public abstract boolean isDefined();

    public abstract boolean isCancelled();

    public abstract void cancel();

    public abstract Try<A> get();

    /**
     * Returns a new Future that will error if this Future does not return in time.
     *
     * @param timeout indicates how long you are willing to wait for the result to be available.
     */
    public RichFuture<A> within(Timer timer, long timeout, TimeUnit unit) {
        Date timeoutAt = new Date(System.currentTimeMillis() + unit.toMillis(timeout));

        final Promise<A> promise = new Promise<A>();
        final Cancellable timeoutMonitor = timer.schedule(timeoutAt, new Function() {
            public void apply() {
                promise.updateIfEmpty(new Throw<A>(new TimeoutRuntimeException()));
            }
        });
        respond(new Function1<Try<A>, Unit>() {
            public Unit apply(Try<A> r) {
                timeoutMonitor.cancel();
                promise.updateIfEmpty(r);
                return Unit.getInstance();
            }
        });
        return promise;
    }

    /**
     * Demands that the result of the future be available within `timeout`. The result
     * is a Return[_] or Throw[_] depending upon whether the computation finished in
     * time.
     */
    public abstract Try<A> get(long timeout, TimeUnit unit) throws TimeoutException;

    public Future<A> toNativeFuture() {
        final RichFuture<A> f = this;
        return new Future<A>() {
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (isDone() || isCancelled()) {
                    return false;
                }
                else {
                    f.cancel();
                    return true;
                }
            }

            public boolean isCancelled() {
                return f.isCancelled();
            }

            public boolean isDone() {
                return f.isCancelled() || f.isDefined();
            }

            public A get() throws InterruptedException, ExecutionException {
                if (isCancelled()) {
                    throw new CancellationException();
                }
                return f.apply();
            }

            public A get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                if (isCancelled()) {
                    throw new CancellationException();
                }
                return f.apply(timeout, unit);
            }
        };
    }
}
