package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
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
    @Nonnull
    Future<Void> save(@Nonnull K k, @Nullable V v);

    /**
     * Read an available cached item with the provided key asynchronously
     * and provide a fallback value when not cache.
     *
     * @param k the key
     * @param v the fallback value, nullable
     * @return the future of read operation, holding a non-null value when succeeded
     */
    @Nonnull
    Future<V> read(@Nonnull K k, @Nullable V v);

    /**
     * Read an available cached item with the provided key asynchronously
     *
     * @param k the key
     * @return the future of read operation, holding a non-null value when succeeded, otherwise failed with
     *         {@link NotCached}.
     */
    @Nonnull
    default Future<V> read(@Nonnull K k) {
        return read(k, null)
                .compose(v -> {
                    if (v == null) {
                        return Future.failedFuture(new NotCached(k.toString()));
                    }
                    return Future.succeededFuture(v);
                });
    }
}
