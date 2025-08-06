package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelEverlastingCacheInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * @since 2.9
 */
public class KeelCacheVet<K, V> implements KeelEverlastingCacheInterface<K, V> {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<K, V> map;

    public KeelCacheVet() {
        map = new HashMap<>();
    }

    private void saveImpl(@Nonnull K key, @Nullable V value) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    @Override
    public void save(@Nonnull K k, @Nullable V v) {
        lock.writeLock().lock();
        try {
            saveImpl(k, v);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void save(@Nonnull Map<K, V> appendEntries) {
        lock.writeLock().lock();
        try {
            appendEntries.forEach(this::saveImpl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nullable
    private V readImpl(@Nonnull K key) {
        return map.get(key);
    }

    @Override
    public V read(@Nonnull K k, V v) {
        lock.readLock().lock();
        try {
            var cached = readImpl(k);
            if (cached != null) {
                return cached;
            } else {
                return v;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public synchronized V computed(@Nonnull K key, @Nonnull Function<V, V> computation) {
        lock.writeLock().lock();
        try {
            var v = readImpl(key);
            var r = computation.apply(v);
            saveImpl(key, r);
            return r;
        } finally {
            lock.writeLock().unlock();
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
        lock.writeLock().lock();
        try {
            removeImpl(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(@Nonnull Collection<K> keys) {
        lock.writeLock().lock();
        try {
            keys.forEach(this::removeImpl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeAll() {
        lock.writeLock().lock();
        try {
            removeAllImpl();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param newEntries new map of entries
     * @since 2.9.4 no longer implemented by replace map
     */
    @Override
    public void replaceAll(@Nonnull Map<K, V> newEntries) {
        lock.writeLock().lock();
        try {
            removeAllImpl();
            newEntries.forEach(this::saveImpl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    @Nonnull
    public Map<K, V> getSnapshotMap() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableMap(map);
        } finally {
            lock.readLock().unlock();
        }
    }
}
