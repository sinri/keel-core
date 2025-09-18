package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class KeelCacheDummy<K, V> implements KeelCacheInterface<K, V> {

    @Override
    public long getDefaultLifeInSeconds() {
        return 0;
    }

    @Override
    public KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        return this;
    }

    @Override
    public void save(@Nonnull K key, V value, long lifeInSeconds) {

    }

    @Nonnull
    @Override
    public V read(@Nonnull K key) throws NotCached {
        throw new NotCached(key.toString());
    }

    @Override
    public V read(@Nonnull K key, Supplier<V> generator, long lifeInSeconds) {
        return generator.get();
    }

    @Override
    public V computed(@Nonnull K key, @Nonnull Function<K, V> computation) {
        return computation.apply(key);
    }

    @Override
    public V read(@Nonnull K key, @Nullable V fallbackValue) {
        return fallbackValue;
    }

    @Override
    public void remove(@Nonnull K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void cleanUp() {

    }

    @Nonnull
    @Override
    public ConcurrentMap<K, V> getSnapshotMap() {
        return new ConcurrentHashMap<>();
    }
}
