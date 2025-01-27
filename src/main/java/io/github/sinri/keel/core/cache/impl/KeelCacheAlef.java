package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelCacheInterface;
import io.github.sinri.keel.core.cache.ValueWrapper;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * An implementation of KeelCacheInterface, using ConcurrentHashMap.
 *
 * @since 2.5
 */
public class KeelCacheAlef<K, V> implements KeelCacheInterface<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;
    private long defaultLifeInSeconds = 1000L;

    public KeelCacheAlef() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public long getDefaultLifeInSeconds() {
        return defaultLifeInSeconds;
    }

    @Override
    public KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        defaultLifeInSeconds = lifeInSeconds;
        return this;
    }

    @Override
    public void save(@Nonnull K key, V value, long lifeInSeconds) {
        this.map.put(key, new ValueWrapper<>(value, lifeInSeconds));
    }

    @Override
    public V read(@Nonnull K key, V fallbackValue) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw == null) {
            return fallbackValue;
        }
        var v = vw.getValue();
        if (v == null) {
            return fallbackValue;
        }
        return v;
    }

    /**
     * @since 4.0.0
     */
    @Override
    public V read(@Nonnull K key, Supplier<V> generator, long lifeInSeconds) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw != null) {
            V v = vw.getValue();
            if (v != null) {
                return v;
            }
        }

        V v = generator.get();
        save(key, v, lifeInSeconds);
        return v;
    }

    @Override
    public void remove(@Nonnull K key) {
        this.map.remove(key);
    }

    @Override
    public void removeAll() {
        this.map.clear();
    }

    @Override
    public void cleanUp() {
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            if (vw != null) {
                if (!vw.isAvailable()) {
                    this.map.remove(key, vw);
                }
            }
        });
    }

    @Override
    @Nonnull
    public synchronized Map<K, V> getSnapshotMap() {
        Map<K, V> snapshot = new HashMap<>();
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            var v = vw.getValue();
            if (v != null) {
                snapshot.put(key, vw.getValue());
            }
        });
        return snapshot;
    }
}
