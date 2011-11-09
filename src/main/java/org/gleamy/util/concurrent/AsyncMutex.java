package org.gleamy.util.concurrent;

public class AsyncMutex extends AsyncSemaphore {

    public AsyncMutex() {
        super(1);
    }
}
