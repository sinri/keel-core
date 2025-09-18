package io.github.sinri.keel.core.cache;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * A simple in-memory implementation of KeelAsyncCacheInterface for testing purposes.
 */
public class TestKeelAsyncCache<K, V> implements KeelAsyncCacheInterface<K, V> {
    private final ConcurrentMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();

    @Override
    public Future<Void> save(@Nonnull K key, V value, long lifeInSeconds) {
        cache.put(key, new CacheEntry<>(value, lifeInSeconds));
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    public Future<V> read(@Nonnull K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry == null || entry.isExpired()) {
            if (entry != null && entry.isExpired()) {
                cache.remove(key);
            }
            return Future.failedFuture(new NotCached(key.toString()));
        }
        return Future.succeededFuture(entry.getValue());
    }

    @Nonnull
    @Override
    public Future<Void> save(@Nonnull K k, V v) {
        return save(k, v, 10);
    }

    @Nonnull
    @Override
    public Future<V> read(@Nonnull K key, V fallbackValue) {
        return read(key)
                .compose(value -> Future.succeededFuture(value),
                        throwable -> Future.succeededFuture(fallbackValue));
    }

    @Override
    public Future<V> read(@Nonnull K key, Function<K, Future<V>> generator, long lifeInSeconds) {
        return read(key)
                .compose(value -> Future.succeededFuture(value),
                        throwable -> generator.apply(key)
                                              .compose(generatedValue -> save(key, generatedValue, lifeInSeconds)
                                                      .compose(v -> Future.succeededFuture(generatedValue))));
    }

    @Override
    public Future<Void> remove(@Nonnull K key) {
        cache.remove(key);
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> removeAll() {
        cache.clear();
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> cleanUp() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return Future.succeededFuture();
    }

    @Override
    public Future<Map<K, V>> getSnapshotMap() {
        Map<K, V> snapshot = new ConcurrentHashMap<>();
        cache.entrySet().stream()
             .filter(entry -> !entry.getValue().isExpired())
             .forEach(entry -> snapshot.put(entry.getKey(), entry.getValue().getValue()));
        return Future.succeededFuture(snapshot);
    }

    private static class CacheEntry<V> {
        private final V value;
        private final long expireTime;

        public CacheEntry(V value, long lifeInSeconds) {
            this.value = value;
            this.expireTime = System.currentTimeMillis() + (lifeInSeconds * 1000);
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public V getValue() {
            return value;
        }
    }
}
