package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelEverlastingCacheInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    private void saveImpl(@NotNull K key, @Nullable V value) {
        if (value == null) {
            map.remove(key);
        } else {
            map.put(key, value);
        }
    }

    @Override
    public void save(@NotNull K k, @Nullable V v) {
        lock.writeLock().lock();
        try {
            saveImpl(k, v);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void save(@NotNull Map<K, V> appendEntries) {
        lock.writeLock().lock();
        try {
            appendEntries.forEach(this::saveImpl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Nullable
    private V readImpl(@NotNull K key) {
        return map.get(key);
    }

    @Override
    @Nullable
    public V read(@NotNull K k, @Nullable V v) {
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
    public V computeIfAbsent(@NotNull K key, @NotNull Function<K, V> computation) {
        lock.writeLock().lock();
        try {
            var v = readImpl(key);
            // As of 4.1.5, the bug is fixed that the computation function would only be called when the key is not cached.
            if (v == null) {
                var r = computation.apply(key);
                saveImpl(key, r);
                return r;
            } else {
                return v;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void removeImpl(@NotNull K key) {
        map.remove(key);
    }

    private void removeAllImpl() {
        map.clear();
    }

    @Override
    public void remove(@NotNull K key) {
        lock.writeLock().lock();
        try {
            removeImpl(key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(@NotNull Collection<K> keys) {
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
    public void replaceAll(@NotNull Map<K, V> newEntries) {
        lock.writeLock().lock();
        try {
            removeAllImpl();
            newEntries.forEach(this::saveImpl);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @NotNull
    @Override
    public Set<K> getCachedKeySet() {
        lock.readLock().lock();
        try {
            HashSet<K> ks = new HashSet<>(map.keySet());
            return Collections.unmodifiableSet(ks);
        } finally {
            lock.readLock().unlock();
        }
    }
}
