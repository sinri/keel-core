package io.github.sinri.keel.core.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
@NullMarked
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
    public void save(K key, @Nullable V value, long lifeInSeconds) {

    }

    @Override
    public V read(K key) throws NotCached {
        throw new NotCached(key.toString());
    }

    @Override
    public V computeIfAbsent(K key, Function<K, V> computation, long lifeInSeconds) {
        return computation.apply(key);
    }

    @Override
    public V read(K key, @Nullable V fallbackValue) {
        return fallbackValue;
    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void cleanUp() {

    }

    @Override
    public Set<K> getCachedKeySet() {
        return Set.of();
    }
}
