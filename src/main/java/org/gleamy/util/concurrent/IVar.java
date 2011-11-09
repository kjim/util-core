package org.gleamy.util.concurrent;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.gleamy.util.Function1;
import org.gleamy.util.Unit;

public class IVar<A> {
    private Queue<Function1<A, Unit>> waitq = new LinkedBlockingQueue<Function1<A, Unit>>();
    volatile private A result = null;

    public boolean isDefined() {
        return result != null;
    }

    public boolean set(A value) {
        boolean didSet = false;
        synchronized (this) {
            if (result != null) {
                didSet = false;
            }
            else {
                result = value;
                didSet = true;
            }
        }

        if (didSet) {
            Function1<A, Unit> f;
            while ((f = waitq.poll()) != null) {
                f.apply(value);
            }
        }

        return didSet;
    }

    public void get(Function1<A, Unit> k) {
        boolean isSet = false;
        synchronized (this) {
            if (result != null) {
                isSet = true;
            }
            else {
                waitq.add(k);
                isSet = false;
            }
        }

        if (isSet) {
            k.apply(result);
        }
    }
}
