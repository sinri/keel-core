package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * An interface for an asynchronous cache mechanism that supports non-blocking
 * operations
 * and provides functionalities to handle basic read and write operations for
 * cached key-value pairs.
 *
 * @param <K> the type of key
 * @param <V> the type of value
 * @since 4.1.5
 */
public interface KeelAsyncCacheAlike<K, V> {
    /**
     * Save the item to cache asynchronously.
     * <p>
     * If the value is null, the key will be removed from the cache.
     *
     * @param k the key
     * @param v the value
     * @return the future of save operation
     */
    @NotNull
    Future<Void> save(@NotNull K k, @Nullable V v);

    /**
     * Read an available cached item with the provided key asynchronously
     * and provide a fallback value when not cache.
     *
     * @param k the key
     * @param v the fallback value, nullable
     * @return the future of read operation, holding a non-null value when succeeded
     */
    @NotNull
    Future<V> read(@NotNull K k, @Nullable V v);

    /**
     * Read an available cached item with the provided key asynchronously
     *
     * @param k the key
     * @return the future of read operation, holding a non-null value when
     *         succeeded, otherwise failed with
     *         {@link NotCached}.
     */
    @NotNull
    default Future<V> read(@NotNull K k) {
        return read(k, null)
                .compose(v -> {
                    if (v == null) {
                        return Future.failedFuture(new NotCached(k.toString()));
                    }
                    return Future.succeededFuture(v);
                });
    }
}
