package org.gleamy.util.concurrent;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.gleamy.util.Function;

/**
 * An AsyncSemaphore is a traditional semaphore but with asynchronous
 * execution. Grabbing a permit returns a Future[Permit]
 */
public class AsyncSemaphore {
    private Object lock = new Object();

    private Queue<Function> waiters = new LinkedBlockingQueue<Function>();
    private int availablePermits;

    public AsyncSemaphore(int initialPermits) {
        this.availablePermits = initialPermits;
    }

    private class SemaphorePermit implements Permit {
        /**
         * Indicate that you are done with your Permit.
         */
        public void release() {
            Function run;
            synchronized (lock) {
                availablePermits += 1;
                if (availablePermits > 0 && !waiters.isEmpty()) {
                    availablePermits -= 1;
                    run = waiters.poll();
                }
                else {
                    run = null;
                }
            }

            if (run != null) {
                run.apply();
            }
        }
    }

    public int getNumWaiters() {
        synchronized (lock) {
            return waiters.size();
        }
    }

    public int getNumPermitsAvailable() {
        return availablePermits;
    }

    /**
     * Acquire a Permit, asynchronously. Be sure to permit.release() in a 'finally'
     * block of your onSuccess() callback.
     *
     * @return a Future[Permit] when the Future is satisfied, computation can proceed.
     */
    public RichFuture<Permit> acquire() {
        final Promise<Permit> result = new Promise<Permit>();

        Function setAcquired = new Function() {
            public void apply() {
                result.setValue(new SemaphorePermit());
            }
        };

        boolean runNow;
        synchronized (lock) {
            if (availablePermits > 0) {
                availablePermits -= 1;
                runNow = true;
            }
            else {
                waiters.add(setAcquired);
                runNow = false;
            }
        }

        if (runNow) {
            setAcquired.apply();
        }

        return result;
    }
}
