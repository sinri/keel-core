package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.core.cache.impl.KeelCacheAlef;
import io.github.sinri.keel.core.cache.impl.KeelCacheDummy;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @param <K> class for key
 * @param <V> class for key
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
    @Override
    default void save(@Nonnull K key, V value) {
        save(key, value, getDefaultLifeInSeconds());
    }

    /**
     * Seek cached value with key, generate one with generator if not cached.
     *
     * @since 4.0.0
     */
    V read(@Nonnull K key, Supplier<V> generator, long lifeInSeconds);

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
    @Nonnull
    Map<K, V> getSnapshotMap();

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
