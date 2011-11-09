package org.gleamy.util;

public class Throw<R> extends TryAbstract<R> {
    private RuntimeException e;

    public Throw(RuntimeException e) {
        this.e = e;
    }

    @Override
    public boolean isThrow() {
        return true;
    }

    @Override
    public boolean isReturn() {
        return false;
    }

    @Override
    public R get() {
        throw e;
    }
}
