package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * An interface for an asynchronous, everlasting cache mechanism that supports non-blocking operations
 * and provides functionalities to manage cached key-value pairs.
 *
 * @param <K> the type of keys used for the cache
 * @param <V> the type of values stored in the cache
 * @since 2.9
 */
public interface KeelAsyncEverlastingCacheInterface<K, V> extends KeelAsyncCacheAlike<K, V> {

    Future<Void> save(@NotNull Map<K, V> appendEntries);

    /**
     * Remove the cached item with the key.
     *
     * @param key key
     */
    Future<Void> remove(@NotNull K key);

    Future<Void> remove(@NotNull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * Replace all entries in the cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    Future<Void> replaceAll(@NotNull Map<K, V> newEntries);

    /**
     * Retrieves the current set of keys present in the cache.
     * The set represents a snapshot of all keys corresponding to cached items at the time of invocation.
     *
     * @return a non-null set containing the keys of currently cached items
     * @since 4.1.5
     */
    @NotNull
    Future<Set<K>> getCachedKeySet();
}
