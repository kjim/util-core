package org.gleamy.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.gleamy.util.Cancellable;
import org.gleamy.util.Function1;
import org.gleamy.util.Return;
import org.gleamy.util.Throw;
import org.gleamy.util.Try;
import org.gleamy.util.Unit;
import org.gleamy.util.internal.Locals;
import org.gleamy.util.internal.SavedLocals;

public class Promise<A> extends RichFuture<A> {
    private final IVar<Try<A>> ivar;
    private final IVar<Unit> cancelled;

    private Promise(IVar<Try<A>> ivar, IVar<Unit> cancelled) {
        this.ivar = ivar;
        this.cancelled = cancelled;
    }

    public Promise() {
        this(new IVar<Try<A>>(), new IVar<Unit>());
    }

    public Promise(Try<A> result) {
        this();
        this.ivar.set(result);
    }

    @Override
    public boolean isCancelled() {
        return cancelled.isDefined();
    }

    @Override
    public void cancel() {
        cancelled.set(Unit.getInstance());
    }

    public void linkTo(final Cancellable other) {
        cancelled.get(new Function1<Unit, Unit>() {
            public Unit apply(Unit _) {
                other.cancel();
                return _;
            }
        });
    }

    @Override
    public Try<A> get() {
        return ivar.apply();
    }

    @Override
    public Try<A> get(long timeout, TimeUnit unit) throws TimeoutException {
        Try<A> var = ivar.apply(timeout, unit);
        if (var != null) {
            return var;
        }
        throw new TimeoutException("Duration(time=" + timeout + ", unit=" + unit + ")");
    }

    @Override
    public boolean isDefined() {
        return ivar.isDefined();
    }

    /**
     * Populate the Promise with the given result.
     *
     * @throws ImmutableResult if the Promise is already populated
     */
    public void setValue(A result) throws ImmutableRuntimeException {
        update(new Return<A>(result));
    }

    /**
     * Populate the Promise with the given exception.
     *
     * @throws ImmutableResult if the Promise is already populated
     */
    public void setException(RuntimeException exception) throws ImmutableRuntimeException {
        update(new Throw<A>(exception));
    }

    /**
     * Populate the Promise with the given Try. The try can either be a value
     * or an exception. setValue and setException are generally more readable
     * methods to use.
     *
     * @throws ImmutableResult if the Promise is already populated
     */
    public void update(Try<A> result) throws ImmutableRuntimeException {
        if (!updateIfEmpty(result)) {
            throw new ImmutableRuntimeException("Result set multiple times: " + result);
        }
    }

    /**
     * Populate the Promise with the given Try. The try can either be a value
     * or an exception. setValue and setException are generally more readable
     * methods to use.
     *
     * @return true only if the result is updated, false if it was already set.
     */
    public boolean updateIfEmpty(Try<A> result) {
        return ivar.set(result);
    }

    @Override
    public RichFuture<A> respond(Function1<Try<A>, Unit> k) {
        respond0(k);

        // FIXME: return the instance of RichFuture
        return null;
    }

    private void respond0(final Function1<Try<A>, Unit> k) {
        final SavedLocals saved = Locals.save();
        ivar.get(new Function1<Try<A>, Unit>() {
            public Unit apply(Try<A> result) {
                SavedLocals current = Locals.save();
                saved.restore();
                try {
                    return k.apply(result);
                }
                finally {
                    current.restore();
                }
            }
        });
    }
}
