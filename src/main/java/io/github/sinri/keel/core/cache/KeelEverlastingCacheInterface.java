package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.core.cache.impl.KeelCacheVet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * @param <K>
 * @param <V>
 * @since 2.9
 */
public interface KeelEverlastingCacheInterface<K, V> {
    static <K, V> KeelEverlastingCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheVet<>();
    }

    /**
     * Save the item to cache.
     */
    void save(@Nonnull K k, V v);

    void save(@Nonnull Map<K, V> appendEntries);

    /**
     * @return found cached value for the provided key
     * @throws NotCached when the provided key not mapped to a cached value.
     * @since 3.3.0 throws NotCached
     */
    @Nonnull
    default V read(@Nonnull K key) throws NotCached {
        var v = read(key, null);
        if (v == null) {
            throw new NotCached(key.toString());
        }
        return v;
    }

    /**
     * @param key           key
     * @param fallbackValue default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     */
    V read(@Nonnull K key, V fallbackValue);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    void remove(@Nonnull K key);

    void remove(@Nonnull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    void replaceAll(@Nonnull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Nonnull
    Map<K, V> getSnapshotMap();

}
