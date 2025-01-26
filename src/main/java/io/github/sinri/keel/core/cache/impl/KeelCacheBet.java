package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.cache.KeelAsyncCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.core.cache.ValueWrapper;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelCacheBet<K, V> implements KeelAsyncCacheInterface<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;

    public KeelCacheBet() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public Future<Void> save(@Nonnull K key, V value, long lifeInSeconds) {
        this.map.put(key, new ValueWrapper<>(value, lifeInSeconds));
        return Future.succeededFuture();
    }

    @Override
    public Future<V> read(@Nonnull K key) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw != null) {
            var v = vw.getValue();
            if (v != null) {
                return Future.succeededFuture(v);
            }
        }
        return Future.failedFuture(new NotCached(key.toString()));
    }

    @Override
    public Future<V> read(@Nonnull K key, V fallbackValue) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw != null) {
            var v = vw.getValue();
            if (v != null) {
                return Future.succeededFuture(v);
            }
        }
        return Future.succeededFuture(fallbackValue);
    }

    @Override
    public Future<V> read(@Nonnull K key, Function<K, Future<V>> generator, long lifeInSeconds) {
        // i.e. computeIfAbsent
        ValueWrapper<V> vw = this.map.get(key);
        if (vw != null) {
            var v = vw.getValue();
            if (v != null) {
                return Future.succeededFuture(v);
            }
        }

        return generator.apply(key)
                .compose(v -> {
                    return save(key, v, lifeInSeconds)
                            .compose(saved -> {
                                return Future.succeededFuture(v);
                            });
                });
    }

    @Override
    public Future<Void> remove(@Nonnull K key) {
        this.map.remove(key);
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> removeAll() {
        this.map.clear();
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> cleanUp() {
        return Keel.getVertx().executeBlocking(() -> {
            synchronized (this.map) {
                this.map.keySet().forEach(key -> {
                    ValueWrapper<V> vw = this.map.get(key);
                    if (vw == null || !vw.isAvailable()) {
                        this.map.remove(key, vw);
                    }
                });
            }
            return null;
        });
    }

    @Override
    public Future<Map<K, V>> getSnapshotMap() {
        return Keel.getVertx().executeBlocking(() -> {
            Map<K, V> snapshot = new HashMap<>();
            synchronized (this.map) {
                this.map.keySet().forEach(k -> {
                    V v = this.map.get(k).getValue();
                    if (v != null) {
                        snapshot.put(k, v);
                    }
                });
            }
            return Collections.unmodifiableMap(snapshot);
        });
    }
}
