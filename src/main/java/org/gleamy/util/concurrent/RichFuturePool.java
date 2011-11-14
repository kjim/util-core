package org.gleamy.util.concurrent;

import org.gleamy.util.Function0;

/**
 * A RichFuturePool executes tasks asynchronously, typically using a pool
 * of worker threads.
 */
public interface RichFuturePool {

    <T> RichFuture<T> apply(Function0<T> f);
}
