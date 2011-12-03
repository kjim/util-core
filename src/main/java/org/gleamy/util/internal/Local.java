package org.gleamy.util.internal;

public final class Local<T> {
    private ThreadLocal<T> threadLocal = new ThreadLocal<T>();

    public Local() {
        Locals.add(this);
    }

    public void set(T value) {
        threadLocal.set(value);
    }

    public void remove() {
        threadLocal.remove();
    }

    public T get() {
        return threadLocal.get();
    }

    public SavedLocal<T> save() {
        return new SavedLocal<T>(this);
    }
}
