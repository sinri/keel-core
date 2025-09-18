package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.core.cache.impl.KeelCacheVet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 2.9
 */
public interface KeelEverlastingCacheInterface<K, V> extends KeelSyncCacheAlike<K, V> {
    static <K, V> KeelEverlastingCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheVet<>();
    }

    void save(@Nonnull Map<K, V> appendEntries);

    /**
     * Atomically, read the cached nullable value with the given key, then call the given compute function to generate a
     * new value to save back and return.
     * <p>The given compute function should take the value read by the key as input, compute for a
     * result, save it to map the key, and finally outputs it.
     * <p> Fix the computation definition bug as of version 4.1.5.
     *
     * @param key         the target key
     * @param computation a compute function takes a nullable cached value as input, and returns a nullable value.
     * @since 4.1.1
     */
    V computed(@Nonnull K key, @Nonnull Function<K, V> computation);

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
    @Nonnull
    Map<K, V> getSnapshotMap();

}
