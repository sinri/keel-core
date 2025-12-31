package io.github.sinri.keel.core.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

/**
 * 伪同步缓存实现。
 * <p>
 * 本类实现不会缓存任何记录，每次读取时都不会命中缓存记录。
 *
 * @param <K> 键的类型
 * @param <V> 值的类型
 * @since 5.0.0
 */
public class KeelCacheDummy<K, V> implements KeelCacheInterface<K, V> {

    @Override
    public long getDefaultLifeInSeconds() {
        return 0;
    }

    @Override
    public @NotNull KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        return this;
    }

    @Override
    public void save(@NotNull K key, @Nullable V value, long lifeInSeconds) {

    }

    @Override
    public @NotNull V read(@NotNull K key) throws NotCached {
        throw new NotCached(key.toString());
    }

    @Override
    public @NotNull V computeIfAbsent(@NotNull K key, @NotNull Function<@NotNull K, @NotNull V> computation, long lifeInSeconds) {
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

    @Override
    public @NotNull Set<@NotNull K> getCachedKeySet() {
        return Set.of();
    }
}
