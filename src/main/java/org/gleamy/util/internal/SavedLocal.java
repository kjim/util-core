package org.gleamy.util.internal;

public final class SavedLocal<T> {
    private final Local<T> local;
    private final T savedValue;

    public SavedLocal(Local<T> local) {
        this.local = local;
        this.savedValue = local.get();
    }

    public void restore() {
        if (savedValue != null) {
            local.set(savedValue);
        }
        else {
            local.remove();
        }
    }
}
