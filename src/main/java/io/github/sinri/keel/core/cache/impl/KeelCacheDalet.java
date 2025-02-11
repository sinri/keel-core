package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelEverlastingCacheInterface;
import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * This implement is to provide: 1. initialized cache data; 2. everlasting cache till modified; 3. regular updating.
 *
 * @since 3.2.11
 */
abstract public class KeelCacheDalet extends KeelVerticleImpl implements KeelEverlastingCacheInterface<String, String> {
    private final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

    /**
     * Save the item to cache.
     *
     * @param key   key
     * @param value value
     */
    @Override
    public void save(@Nonnull String key, String value) {
        this.map.put(key, value);
    }

    @Override
    public void save(@Nonnull Map<String, String> appendEntries) {
        this.map.putAll(appendEntries);
    }

    /**
     * @param key   key
     * @param value default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     */
    @Override
    public String read(@Nonnull String key, String value) {
        return map.getOrDefault(key, value);
    }

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    @Override
    public void remove(@Nonnull String key) {
        this.map.remove(key);
    }

    @Override
    public void remove(@Nonnull Collection<String> keys) {
        keys.forEach(this.map::remove);
    }

    /**
     * Remove all the cached items.
     */
    @Override
    public void removeAll() {
        map.clear();
    }

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    @Override
    public void replaceAll(@Nonnull Map<String, String> newEntries) {
        newEntries.forEach(map::replace);
    }

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Nonnull
    @Override
    public Map<String, String> getSnapshotMap() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    protected Future<Void> startVerticle() {
        return fullyUpdate()
                .compose(updated -> {
                    if (regularUpdatePeriod() >= 0) {
                        Keel.asyncCallEndlessly(() -> {
                            return Future.succeededFuture()
                                         .compose(v -> {
                                             if (regularUpdatePeriod() == 0) return Future.succeededFuture();
                                             else return Keel.asyncSleep(regularUpdatePeriod());
                                         })
                                         .compose(v -> {
                                             return fullyUpdate();
                                         });
                        });
                    }
                    return Future.succeededFuture();
                });
    }

    abstract public Future<Void> fullyUpdate();

    /**
     * @return a time period to sleep between regular updates. Use minus number to disable regular update.
     */
    protected long regularUpdatePeriod() {
        return 600_000L;// by default, 10min.
    }

}
