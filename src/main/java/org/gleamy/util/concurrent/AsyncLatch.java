package org.gleamy.util.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gleamy.util.Function;

/**
 * The AsyncLatch is an asynchronous latch.
 */
public class AsyncLatch {
    volatile private int count = 0;
    private List<Function> waiters = new ArrayList<Function>();

    public AsyncLatch() {
    }

    public AsyncLatch(int initialCount) {
        if (initialCount < 0) {
            throw new IllegalArgumentException("initialCount must be natural number.");
        }
        this.count = initialCount;
    }

    /**
     * Execute the given computation when the count of this latch has
     * reached 0.
     */
    public synchronized void await(Function f) {
        if (count == 0) {
            f.apply();
        }
        else {
            waiters.add(f);
        }
    }

    /**
     * Increment the latch.
     */
    public synchronized int incr() {
        return count += 1;
    }

    /**
     * Decrement the latch. If the latch value reaches 0, awaiting
     * computations are executed inline.
     */
    public int decr() {
        final List<Function> pendingTasks;
        synchronized (this) {
            if (count <= 0) {
                throw new IllegalStateException("count is negative value: " + count);
            }
            count -= 1;
            if (count == 0) {
                List<Function> pending = waiters;
                waiters = new ArrayList<Function>();
                pendingTasks = pending;
            }
            else {
                return count;
            }
        }

        for (Function f : pendingTasks) {
            f.apply();
        }

        return 0;
    }

    public int getCount() {
        return count;
    }
}
