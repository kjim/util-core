package org.gleamy.util.internal;

public final class SavedLocals {
    private SavedLocal<?>[] locals;

    public SavedLocals(SavedLocal<?>[] locals) {
        this.locals = locals;
    }

    public void restore() {
        int length = locals.length;
        for (int i = 0; i < length; i++) {
            locals[i].restore();
        }
    }
}
