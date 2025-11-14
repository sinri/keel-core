package io.github.sinri.keel.core.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void save(@NotNull K key, V value, long lifeInSeconds) {

    }

    @NotNull
    @Override
    public V read(@NotNull K key) throws NotCached {
        throw new NotCached(key.toString());
    }

    @Override
    public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computation, long lifeInSeconds) {
        return computation.apply(key);
    }

    @Override
    public V read(@NotNull K key, @Nullable V fallbackValue) {
        return fallbackValue;
    }

    @Override
    public void remove(@NotNull K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void cleanUp() {

    }

    @NotNull
    @Override
    public Set<K> getCachedKeySet() {
        return Set.of();
    }
}
