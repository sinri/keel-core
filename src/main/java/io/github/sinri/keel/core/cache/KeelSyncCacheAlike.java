package io.github.sinri.keel.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @param <K> the type of key
 * @param <V> the type of value
 * @since 4.1.5
 */
public interface KeelSyncCacheAlike<K, V> {
    /**
     * Save the item to the cache bound to the provided key.
     * <p>
     * If the value is null, the key will be removed from the cache,
     * as null is not a valid cached value.
     *
     * @param key   the key
     * @param value the value
     */
    void save(@Nonnull K key, @Nullable V value);

    /**
     * Read an available cached item with the provided key from the cache
     * or return the provided `fallbackValue` when not found.
     *
     * @param key           the provided key
     * @param fallbackValue the default value when the key is not mapped with a non-null cached value.
     * @return the value of the found available cached item,
     *         or the provided `fallbackValue` when not found, which might be null.
     */
    @Nullable
    V read(@Nonnull K key, @Nullable V fallbackValue);

    /**
     * Read an available cached item with the provided key from the cache, or throw {@link NotCached} exception.
     *
     * @param key the key
     * @return the value of found available cached item
     * @throws NotCached when the key is not mapped with a non-null cached value.
     */
    @Nonnull
    default V read(@Nonnull K key) throws NotCached {
        var v = read(key, null);
        if (v == null) {
            throw new NotCached(key.toString());
        }
        return v;
    }
}
