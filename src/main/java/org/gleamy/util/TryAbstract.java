package org.gleamy.util;

public abstract class TryAbstract<R> implements Try<R> {

    @Override
    public R call() throws Exception {
        return get();
    }
}
