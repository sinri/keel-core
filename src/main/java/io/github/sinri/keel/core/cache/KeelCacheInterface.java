package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.core.cache.impl.KeelCacheAlef;
import io.github.sinri.keel.core.cache.impl.KeelCacheDummy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * An interface for a cache mechanism that supports synchronous operations
 * and provides functionalities to manage cached key-value pairs.
 *
 * @param <K> class for key
 * @param <V> class for value
 * @since 1.9
 */
public interface KeelCacheInterface<K, V> extends KeelSyncCacheAlike<K, V> {
    /**
     * @param <K> class for key
     * @param <V> class for value
     * @return A new instance of KeelCacheInterface created.
     * @since 1.9 Use CaffeineCacheKit as implementation by default.
     * @since 2.5 changed to use KeelCacheAlef
     */
    static <K, V> KeelCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheAlef<>();
    }

    /**
     * @since 2.6
     */
    static <K, V> KeelCacheInterface<K, V> getDummyInstance() {
        return new KeelCacheDummy<>();
    }

    /**
     * @since 2.8
     */
    long getDefaultLifeInSeconds();

    /**
     * @since 2.8
     */
    KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds);

    /**
     * Save an item (as key and value pair) into cache, keep it available for a
     * certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    void save(@Nonnull K key, V value, long lifeInSeconds);

    /**
     * @since 2.8
     */
    @Override
    default void save(@Nonnull K key, @Nullable V value) {
        save(key, value, getDefaultLifeInSeconds());
    }

    /**
     * Seek cached value with the key, generate one with generator if not cached.
     * <p>
     * You must enclose the key with in the dynamic generator. It is not so convenient so deprecated.
     *
     * @since 4.0.0
     * @deprecated Use {@link KeelCacheInterface#computeIfAbsent(Object, Function, long)} instead as of 4.1.5
     */
    @Deprecated(since = "4.1.5", forRemoval = true)
    default V read(@Nonnull K key, Supplier<V> generator, long lifeInSeconds) {
        return computeIfAbsent(key, k -> generator.get(), lifeInSeconds);
    }

    /**
     * Computes a value for the given key if it is not already present in the cache.
     * If the key is already associated with a value, the existing value is
     * returned.
     * Otherwise, the provided computation function is used to compute the value,
     * associate it with the key, and return the computed value.
     *
     * @param key         the key whose associated value is to be returned or
     *                    computed
     * @param computation a function to compute a value for the key if it is not
     *                    already present
     * @return the existing or newly computed value associated with the key
     * @since 4.1.5
     */
    default V computeIfAbsent(@Nonnull K key, @Nonnull Function<K, V> computation) {
        return computeIfAbsent(key, computation, getDefaultLifeInSeconds());
    }

    /**
     * Computes a value for the given key if it is not already present in the cache.
     * If the key is already associated with a value, the existing value is returned.
     * Otherwise, the provided computation function is used to compute a value,
     * associate it with the key, and return the computed value. The computed value
     * will be stored with the specified lifetime in seconds.
     *
     * @param key           the key whose associated value is to be returned or computed.
     *                      This key must not be null.
     * @param computation   a function to compute a value for the key if it is not
     *                      already present. This function must not be null.
     * @param lifeInSeconds the lifetime in seconds for the computed value to remain
     *                      in the cache, after which it will expire.
     * @return the existing or newly computed value associated with the key.
     * @since 4.1.5
     */
    V computeIfAbsent(@Nonnull K key, @Nonnull Function<K, V> computation, long lifeInSeconds);

    /**
     * Remove the cached item with the key.
     *
     * @param key key
     */
    void remove(@Nonnull K key);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * clean up the entries that is not alive (expired, etc.)
     */
    void cleanUp();

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
     * Retrieves the set of keys currently stored in the cache.
     *
     * @return A non-null set containing all cached keys.
     */
    @Nonnull
    Set<K> getCachedKeySet();

    /**
     * Start an endless process for cleaning up.
     * Use it manually if needed.
     *
     * @since 3.0.4
     */
    default void startEndlessCleanUp(long sleepTime) {
        Keel.asyncCallEndlessly(() -> {
            cleanUp();
            return Keel.asyncSleep(sleepTime);
        });
    }
}
