package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;


/**
 * An interface for an asynchronous, everlasting cache mechanism that supports non-blocking operations
 * and provides functionalities to manage cached key-value pairs.
 *
 * @param <K> the type of keys used for the cache
 * @param <V> the type of values stored in the cache
 * @since 2.9
 */
public interface KeelAsyncEverlastingCacheInterface<K, V> extends KeelAsyncCacheAlike<K, V> {

    default long getLockWaitMs() {
        return 100;
    }

    Future<Void> save(@Nonnull Map<K, V> appendEntries);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    Future<Void> remove(@Nonnull K key);

    Future<Void> remove(@Nonnull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    Future<Void> replaceAll(@Nonnull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Nonnull
    Map<K, V> getSnapshotMap();
}
