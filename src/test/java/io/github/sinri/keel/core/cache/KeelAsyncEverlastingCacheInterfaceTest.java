package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

class KeelAsyncEverlastingCacheInterfaceTest extends KeelUnitTest {
    private static KeelAsyncEverlastingCacheInterface<String, Integer> cache;

    @BeforeAll
    public static void beforeAll() {
        cache = KeelAsyncEverlastingCacheInterface.createDefaultInstance();
    }

    @BeforeEach
    public void beforeEach() {
        cache.removeAll();
    }

    @Test
    public void test() {
        cache.save("a", 1);
        cache.save("b", 2);

        CompositeFuture f = Future.all(
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
        );
        Keel.blockAwait(f);
    }
}