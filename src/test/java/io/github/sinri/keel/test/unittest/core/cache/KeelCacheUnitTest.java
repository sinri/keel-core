package io.github.sinri.keel.test.unittest.core.cache;


import io.github.sinri.keel.core.cache.KeelAsyncCacheInterface;
import io.github.sinri.keel.core.cache.KeelCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.core.cache.ValueWrapper;
import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelCacheUnitTest extends KeelUnitTest {
    @Test
    public void testForValueWrapper() throws Throwable {
        this.async(promise -> {
            long t1 = System.currentTimeMillis();
            getUnitTestLogger().info("value: " + t1);
            var vw = new ValueWrapper<Long>(t1, 3);

            Keel.asyncCallStepwise(4, i -> {
                    getUnitTestLogger().info("i=" + i + " start, to sleep 1s");
                    return Keel.asyncSleep(1_000L)
                               .compose(v -> {
                                   Long value = vw.getValue();
                                   getUnitTestLogger().info("after " + (i + 1) + "s (" + System.currentTimeMillis() + "), vw = " + value);
                                   return Future.succeededFuture();
                               });
                })
                .andThen(ar -> {
                    if (ar.succeeded()) {
                        promise.complete();
                    } else {
                        promise.fail(ar.cause());
                    }
                });
        });
    }

    @Test
    public void testForSyncCache() throws Throwable {
        async(() -> {
            KeelCacheInterface<String, String> cache = KeelCacheInterface.createDefaultInstance();
            cache.save("a", "apple", 2);
            return Keel.asyncSleep(1_000L)
                       .compose(v -> {
                           try {
                               getUnitTestLogger().info("read a and got " + cache.read("a"));
                           } catch (NotCached e) {
                               throw new RuntimeException(e);
                           }
                           return Keel.asyncSleep(1_000L);
                       })
                       .compose(v -> {
                           try {
                               getUnitTestLogger().info("read a and got " + cache.read("a"));
                               Assertions.fail();
                           } catch (NotCached e) {
                               getUnitTestLogger().info("read a and not cached now.");
                           }
                           return Future.succeededFuture();
                       });
        });
    }

    @Test
    public void testForAsyncCache() throws Throwable {
        async(() -> {
            KeelAsyncCacheInterface<String, String> cache = KeelAsyncCacheInterface.createDefaultInstance();
            return cache.save("a", "apple", 2)
                        .compose(v -> {
                            return Keel.asyncSleep(1_000L);
                        })
                        .compose(v -> {
                            return cache.read("a");
                        })
                        .compose(v -> {
                            getUnitTestLogger().info("read a and got " + v);
                            return Keel.asyncSleep(1_000L);
                        })
                        .compose(v -> {
                            return cache.read("a");
                        })
                        .compose(v -> {
                            Assertions.fail();
                            return Future.succeededFuture();
                        }, throwable -> {
                            getUnitTestLogger().info("read a and not cached now.");
                            return Future.succeededFuture();
                        });
        });
    }
}
