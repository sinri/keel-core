package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * An interface representing an asynchronous cache system that allows storing,
 * retrieving, and managing key-value pairs with support for expiration, fallback logic,
 * and cleanup mechanisms. This interface is designed to be implemented by classes
 * utilizing various underlying storage mechanisms.
 *
 * @param <K> The type of keys maintained by this cache.
 * @param <V> The type of mapped values.
 * @since 1.14
 */
public interface KeelAsyncCacheInterface<K, V> extends KeelAsyncCacheAlike<K, V> {

    /**
     * Save an item (as key and value pair) into cache, keep it available for a
     * certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    Future<Void> save(@Nonnull K key, V value, long lifeInSeconds);

    /**
     * Read an available cached item with key;
     * if not found, try to generate one with key using `fallbackValueGenerator` to
     * save into cache, then return it in the future;
     * if failed to generate, failed future instead.
     *
     * @param key           key
     * @param generator     function to generate a value for given key, to be saved
     *                      into cache and return when no cached item found
     * @param lifeInSeconds cache available in this period, in seconds
     * @return async: the valued read from cache
     * @since 2.5
     */
    Future<V> read(@Nonnull K key, Function<K, Future<V>> generator, long lifeInSeconds);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    Future<Void> remove(@Nonnull K key);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * clean up the entries that is not alive (expired, etc.)
     */
    Future<Void> cleanUp();

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    Future<Map<K, V>> getSnapshotMap();

    /**
     * Start an endless for cleaning up.
     * Use it manually if needed.
     *
     * @since 3.0.4
     */
    default void startEndlessCleanUp(long sleepTime) {
        Keel.asyncCallEndlessly(() -> cleanUp().compose(cleaned -> Keel.asyncSleep(sleepTime)));
    }

}
