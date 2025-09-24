package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelCacheInterface;
import io.github.sinri.keel.core.cache.ValueWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * An implementation of KeelCacheInterface, using ConcurrentHashMap.
 *
 * @since 2.5
 */
public class KeelCacheAlef<K, V> implements KeelCacheInterface<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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

    private void saveImpl(@Nonnull K key, @Nullable V value, long lifeInSeconds) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, new ValueWrapper<>(value, lifeInSeconds));
        }
    }

    @Nullable
    private V readImpl(@Nonnull K key) {
        ValueWrapper<V> vValueWrapper = map.get(key);
        if (vValueWrapper == null) {
            return null;
        } else {
            return vValueWrapper.getValue();
        }
    }

    @Override
    public void save(@Nonnull K key, V value, long lifeInSeconds) {
        lock.writeLock().lock();
        try {
            saveImpl(key, value, lifeInSeconds);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public V read(@Nonnull K key, @Nullable V fallbackValue) {
        lock.readLock().lock();
        try {
            V v = readImpl(key);
            if (v == null) {
                return fallbackValue;
            }
            return v;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public V computeIfAbsent(@Nonnull K key, @Nonnull Function<K, V> computation, long lifeInSeconds) {
        this.lock.writeLock().lock();
        try {
            V v = readImpl(key);
            if (v != null) return v;
            V r = computation.apply(key);
            saveImpl(key, r, lifeInSeconds);
            return r;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void removeImpl(@Nonnull K key) {
        map.remove(key);
    }

    private void removeAllImpl() {
        map.clear();
    }

    @Override
    public void remove(@Nonnull K key) {
        this.lock.writeLock().lock();
        try {
            removeImpl(key);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void removeAll() {
        this.lock.writeLock().lock();
        try {
            removeAllImpl();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void cleanUpImpl() {
        this.map.keySet().forEach(key -> {
            var cached = readImpl(key);
            if (cached == null) {
                removeImpl(key);
            }
        });
    }

    @Override
    public void cleanUp() {
        this.lock.writeLock().lock();
        try {
            cleanUpImpl();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * @since 4.1.5
     */
    @Nonnull
    @Override
    public Set<K> getCachedKeySet() {
        this.lock.writeLock().lock();
        try {
            cleanUpImpl();
            Set<K> keySet = new HashSet<>();
            for (Map.Entry<K, ValueWrapper<V>> entry : map.entrySet()) {
                V value = entry.getValue().getValue();
                if (value != null) {
                    keySet.add(entry.getKey());
                }
            }
            return Collections.unmodifiableSet(keySet);
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
