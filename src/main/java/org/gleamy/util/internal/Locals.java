package org.gleamy.util.internal;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Locals are more flexible thread-locals. They allow for saving &
 * restoring the state of *all* Locals. This is useful for threading
 * Locals through execution contexts. In this manner they are
 * propagated in delayed computations in {@link Promise}.
 */
public final class Locals {
    private static final SavedLocals emptyLocals =
        new SavedLocals(new SavedLocal[0]);

    private static ReentrantLock localsLock = new ReentrantLock();
    private volatile static Local<?>[] locals = new Local<?>[0];

    public static void add(Local<?> local) {
        final ReentrantLock lock = localsLock;
        lock.lock();
        try {
            Local<?>[] locals0 = locals;
            int len = locals0.length;
            Local<?>[] newLocals = Arrays.copyOf(locals, len);
            newLocals[len] = local;
            locals = newLocals;
        }
        finally {
            lock.unlock();
        }
    }

    public static SavedLocals save() {
        Local<?>[] locals0 = locals;
        if (locals0.length == 0) {
            return emptyLocals;
        }
        else {
            final int locals0Len = locals0.length;
            SavedLocal<?>[] saved = new SavedLocal<?>[locals0.length];
            for (int i = 0; i < locals0Len; i++) {
                saved[i] = locals0[i].save();
            }

            return new SavedLocals(saved);
        }
    }
}
