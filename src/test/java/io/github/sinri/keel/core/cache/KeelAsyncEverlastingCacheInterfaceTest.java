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

@ExtendWith(VertxExtension.class)
class KeelAsyncEverlastingCacheInterfaceTest extends KeelJUnit5Test {
    @RegisterExtension
    static RunTestOnContext runTestOnContext = new RunTestOnContext();
    private static KeelAsyncEverlastingCacheInterface<String, Integer> cache;

    KeelAsyncEverlastingCacheInterfaceTest(Vertx vertx) {
        super(vertx);
        cache = KeelAsyncEverlastingCacheInterface.createDefaultInstance();
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