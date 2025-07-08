package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelAsyncEverlastingCacheInterface;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.9
 */
public class KeelCacheGimel<K, V> implements KeelAsyncEverlastingCacheInterface<K, V> {
    private final ReadWriteLock lock;
    private final Map<K, V> map;
    private long lockWaitMs = 100;

    public KeelCacheGimel() {
        lock = new ReentrantReadWriteLock();
        map = new HashMap<>();
    }

    public long getLockWaitMs() {
        return lockWaitMs;
    }

    public KeelCacheGimel<K, V> setLockWaitMs(long lockWaitMs) {
        this.lockWaitMs = lockWaitMs;
        return this;
    }

    @Override
    public Future<Void> save(@Nonnull K k, V v) {
        return Keel.getVertx().executeBlocking(() -> {
            lock.writeLock().lock();
            try {
                map.put(k, v);
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        });
    }

    @Override
    public Future<Void> save(@Nonnull Map<K, V> appendEntries) {
        return Keel.getVertx().executeBlocking(() -> {
            lock.writeLock().lock();
            try {
                map.putAll(appendEntries);
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        });
    }

    @Override
    public Future<V> read(@Nonnull K k, V v) {
        return Keel.getVertx().executeBlocking(() -> {
            lock.readLock().lock();
            try {
                var cachedValue = map.get(k);
                if (cachedValue != null) {
                    return cachedValue;
                }
            } finally {
                lock.readLock().unlock();
            }
            return v;
        });
    }

    @Override
    public Future<Void> remove(@Nonnull K key) {
        return Keel.getVertx().executeBlocking(() -> {
            lock.writeLock().lock();
            try {
                map.remove(key);
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        });
    }

    @Override
    public Future<Void> remove(@Nonnull Collection<K> keys) {
        return Keel.getVertx().executeBlocking(() -> {
            lock.writeLock().lock();
            try {
                keys.forEach(map::remove);
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        });
    }

    @Override
    public Future<Void> removeAll() {
        return Keel.getVertx().executeBlocking(() -> {
            lock.writeLock().lock();
            try {
                map.clear();
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        });
    }

    /**
     * @param newEntries new map of entries
     * @since 2.9.4 no longer implemented by replace map
     */
    @Override
    public Future<Void> replaceAll(@Nonnull Map<K, V> newEntries) {
        return Keel.getVertx().executeBlocking(() -> {
            lock.writeLock().lock();
            try {
                Set<K> ks = newEntries.keySet();
                map.putAll(newEntries);
                map.keySet().forEach(k -> {
                    if (!ks.contains(k)) {
                        map.remove(k);
                    }
                });
            } finally {
                lock.writeLock().unlock();
            }
            return null;
        });
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
