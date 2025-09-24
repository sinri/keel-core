package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.github.sinri.keel.facade.KeelInstance.Keel;


/**
 * An interface for an asynchronous, everlasting cache mechanism that supports non-blocking operations
 * and provides functionalities to manage cached key-value pairs.
 *
 * @param <K> the type of keys used for the cache
 * @param <V> the type of values stored in the cache
 * @since 2.9
 */
public interface KeelAsyncEverlastingCacheInterface<K, V> extends KeelAsyncCacheAlike<K, V> {

    Future<Void> save(@Nonnull Map<K, V> appendEntries);

    /**
     * Remove the cached item with the key.
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
     * Replace all entries in the cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    Future<Void> replaceAll(@Nonnull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Deprecated(since = "4.1.5", forRemoval = true)
    @Nonnull
    default Future<Map<K, V>> getSnapshotMap() {
        return getCachedKeySet()
                .compose(ks -> {
                    if (ks.isEmpty()) {
                        return Future.succeededFuture(Map.of());
                    }
                    Map<K, V> map = new HashMap<>();
                    return Keel.parallelForAllComplete(ks, k -> {
                                   return read(k)
                                           .compose(v -> {
                                               map.put(k, v);
                                               return Future.succeededFuture();
                                           }, throwable -> {
                                               return Future.succeededFuture();
                                           });
                               })
                               .compose(v -> Future.succeededFuture(map));
                });
    }

    /**
     * Retrieves the current set of keys present in the cache.
     * The set represents a snapshot of all keys corresponding to cached items at the time of invocation.
     *
     * @return a non-null set containing the keys of currently cached items
     * @since 4.1.5
     */
    @Nonnull
    Future<Set<K>> getCachedKeySet();
}
