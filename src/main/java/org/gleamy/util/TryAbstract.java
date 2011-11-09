package org.gleamy.util;

public abstract class TryAbstract<R> implements Try<R> {

    @Override
    public R call() throws Exception {
        return get();
    }

    @Override
    public R getOrElse(R defaultVal) {
        return isReturn() ? get() : defaultVal;
    }

    public static <R> Try<R> newInstance(Function0<R> r) {
        try {
            return new Return<R>(r.apply());
        }
        catch (RuntimeException e) {
            return new Throw<R>(e);
        }
        catch (Exception e) {
            return new Throw<R>(new RuntimeException(e));
        }
    }
}
