package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;

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
    public V computeIfAbsent(@Nonnull K key, @Nonnull Function<K, V> computation, long lifeInSeconds) {
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
    public Set<K> getCachedKeySet() {
        return Set.of();
    }
}
