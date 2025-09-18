package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.RunTestOnContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ExtendWith(VertxExtension.class)
class KeelAsyncEverlastingCacheInterfaceTest extends KeelJUnit5Test {
    @RegisterExtension
    static RunTestOnContext runTestOnContext = new RunTestOnContext();
    private static KeelAsyncEverlastingCacheInterface<String, Integer> cache;

    KeelAsyncEverlastingCacheInterfaceTest(Vertx vertx) {
        super(vertx);
        cache = new MockAsyncEverlastingCache<>();
    }

    /**
     * Mock implementation of KeelAsyncEverlastingCacheInterface for testing
     */
    private static class MockAsyncEverlastingCache<K, V> implements KeelAsyncEverlastingCacheInterface<K, V> {
        private final Map<K, V> map = new ConcurrentHashMap<>();

        @Nonnull
        @Override
        public Future<Void> save(@Nonnull K k, V v) {
            if (v == null) {
                map.remove(k);
            } else {
                map.put(k, v);
            }
            return Future.succeededFuture();
        }

        @Override
        public Future<Void> save(@Nonnull Map<K, V> appendEntries) {
            map.putAll(appendEntries);
            return Future.succeededFuture();
        }

        @Nonnull
        @Override
        public Future<V> read(@Nonnull K k, V v) {
            V result = map.get(k);
            if (result == null) {
                return Future.succeededFuture(v);
            }
            return Future.succeededFuture(result);
        }

        @Override
        public Future<Void> remove(@Nonnull K key) {
            map.remove(key);
            return Future.succeededFuture();
        }

        @Override
        public Future<Void> remove(@Nonnull Collection<K> keys) {
            keys.forEach(map::remove);
            return Future.succeededFuture();
        }

        @Override
        public Future<Void> removeAll() {
            map.clear();
            return Future.succeededFuture();
        }

        @Override
        public Future<Void> replaceAll(@Nonnull Map<K, V> newEntries) {
            map.clear();
            map.putAll(newEntries);
            return Future.succeededFuture();
        }

        @Override
        @Nonnull
        public Map<K, V> getSnapshotMap() {
            return new ConcurrentHashMap<>(map);
        }
    }

    @BeforeEach
    public void beforeEach(VertxTestContext testContext) {
        cache.removeAll();
        testContext.completeNow();
    }

    @Test
    public void test(VertxTestContext testContext) {
        cache.save("a", 1);
        cache.save("b", 2);

        Future.all(
                      cache.read("a")
                           .compose(a -> {
                               Assertions.assertEquals(1, a);
                               return Future.succeededFuture();
                           }, throwable -> {
                               Assertions.fail(throwable);
                               return Future.succeededFuture();
                           }),
                      cache.read("c")
                           .compose(c -> {
                               Assertions.fail();
                               return Future.succeededFuture();
                           }, throwable -> {
                               Assertions.assertInstanceOf(NotCached.class, throwable);
                               return Future.succeededFuture();
                           })
              )
              .onComplete(testContext.succeedingThenComplete());
    }
}