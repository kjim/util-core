package org.gleamy.util;

public class Return<R> extends TryAbstract<R> {
    private R r;

    public Return(R r) {
        this.r = r;
    }

    @Override
    public boolean isThrow() {
        return false;
    }

    @Override
    public boolean isReturn() {
        return true;
    }

    @Override
    public R get() {
        return r;
    }
}
