package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.core.cache.impl.KeelCacheVet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * An interface for an everlasting cache mechanism that supports non-blocking operations
 * and provides functionalities to manage cached key-value pairs.
 *
 * @param <K> the type of key
 * @param <V> the type of value
 * @since 2.9
 */
public interface KeelEverlastingCacheInterface<K, V> extends KeelSyncCacheAlike<K, V> {
    static <K, V> KeelEverlastingCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheVet<>();
    }

    void save(@Nonnull Map<K, V> appendEntries);

    /**
     * Computes and retrieves a value associated with the provided key. If the key is not already
     * cached, the computation function is executed to produce the value, which is then stored in the cache.
     *
     * @param key         the key for which the value is being computed or retrieved; must not be null
     * @param computation the function to compute the value if it is not already cached; must not be null
     * @return the computed or cached value associated with the specified key
     * @since 4.1.5
     */
    V computeIfAbsent(@Nonnull K key, @Nonnull Function<K, V> computation);

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
     * Replace all entries in the cache with new entries, i.e., remove all and save the provided.
     *
     * @param newEntries new map of entries
     */
    void replaceAll(@Nonnull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Deprecated(since = "4.1.5", forRemoval = true)
    @Nonnull
    default Map<K, V> getSnapshotMap() {
        Map<K, V> map = new HashMap<>();
        getCachedKeySet().forEach(k -> {
            try {
                map.put(k, read(k));
            } catch (NotCached e) {
                // do nothing
            }
        });
        return map;
    }

    /**
     * Retrieves the set of all keys currently cached in the everlasting cache.
     *
     * @return a non-null set of keys representing all the items currently stored in the cache
     * @since 4.1.5
     */
    @Nonnull
    Set<K> getCachedKeySet();

}
