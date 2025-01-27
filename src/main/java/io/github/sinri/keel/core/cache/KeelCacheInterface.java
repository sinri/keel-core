package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.core.cache.impl.KeelCacheAlef;
import io.github.sinri.keel.core.cache.impl.KeelCacheDummy;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @param <K> class for key
 * @param <V> class for key
 * @since 1.9
 */
public interface KeelCacheInterface<K, V> {
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
     * Save an item (as key and value pair) into cache, keep it available for a certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    void save(@Nonnull K key, V value, long lifeInSeconds);

    /**
     * @since 2.8
     */
    default void save(@Nonnull K key, V value) {
        save(key, value, getDefaultLifeInSeconds());
    }

    /**
     * Read an available cached item with key, or return `fallbackValue` when not found.
     *
     * @param key           key
     * @param fallbackValue the certain value returned when not found
     * @return value of found available cached item, or `fallbackValue`
     */
    V read(@Nonnull K key, V fallbackValue);

    /**
     * Read an available cached item with key, or return `null` when not found.
     *
     * @param key key
     * @return value of found available cached item, or `null`
     * @throws NotCached when the key is not mapped with cached value.
     */
    @Nonnull
    default V read(@Nonnull K key) throws NotCached {
        var v = this.read(key, null);
        if (v == null) {
            throw new NotCached(key.toString());
        }
        return v;
    }

    /**
     * Seek cached value with key, generate one with generator if not cached.
     *
     * @since 4.0.0
     */
    V read(@Nonnull K key, Supplier<V> generator, long lifeInSeconds);

    /**
     * Remove the cached item with key.
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
    @Nonnull
    Map<K, V> getSnapshotMap();

    /**
     * Start an endless for cleaning up.
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
