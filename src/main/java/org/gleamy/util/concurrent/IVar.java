package org.gleamy.util.concurrent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.gleamy.util.Function;
import org.gleamy.util.Function1;
import org.gleamy.util.Unit;

public class IVar<A> {
    private static interface State<A> {
    }
    private static class Waiting<A> implements State<A> {
        private List<Function1<A, Unit>> waitq;
        Waiting(List<Function1<A, Unit>> waitq) {
            this.waitq = waitq;
        }
    }
    private static class Done<A> implements State<A> {
        private A value;
        Done(A value) {
            this.value = value;
        }
    }

    private static class Schedule {
        private Function w0;
        private Function w1;
        private Function w2;
        private BlockingQueue<Function> ws = new LinkedBlockingQueue<Function>();
        private boolean running = false;

        void apply(Function waiter) {
            if (w0 == null)      w0 = waiter;
            else if (w1 == null) w1 = waiter;
            else if (w2 == null) w2 = waiter;
            else {
                try {
                    ws.put(waiter);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (!running) {
                run();
            }
        }

        public void flush() {
            if (running) {
                run();
            }
        }

        private void run() {
            boolean save = running;
            running = true;
            try {
                while (w0 != null) {
                    final Function w = w0;
                    w0 = w1;
                    w1 = w2;
                    w2 = ws.isEmpty() ? null : ws.poll();
                }
            }
            finally {
                running = save;
            }
        }
    }

    private static final ThreadLocal<Schedule> _schedule = new ThreadLocal<Schedule>() {
        protected Schedule initialValue() {
            return new Schedule();
        }
    };

    private static Schedule schedule() {
        return _schedule.get();
    }


    private Queue<Function1<A, Unit>> waitq = new LinkedBlockingQueue<Function1<A, Unit>>();
    volatile private A result = null;

    public A apply() {
        return apply(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public A apply(long timeout, TimeUnit unit) {
        if (result != null) {
            return result;
        }

        final ArrayBlockingQueue<A> q = new ArrayBlockingQueue<A>(1);
        get(new Function1<A, Unit>() {
            public Unit apply(A value) {
                q.offer(value);
                return Unit.getInstance();
            }
        });

        try {
            return q.poll(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

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
