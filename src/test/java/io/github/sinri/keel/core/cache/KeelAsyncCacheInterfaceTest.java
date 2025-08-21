package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@ExtendWith(VertxExtension.class)
class KeelAsyncCacheInterfaceTest extends KeelJUnit5Test {
    private static KeelAsyncCacheInterface<String, Integer> cache;

    KeelAsyncCacheInterfaceTest(Vertx vertx) {
        super(vertx);
        System.out.println("KeelAsyncCacheInterfaceTest: " + vertx);

        cache = KeelAsyncCacheInterface.createDefaultInstance();
        cache.startEndlessCleanUp(3000);
    }

    @BeforeEach
    public void setUp() {
        cache.removeAll();
    }

    @Test
    public void test1(Vertx vertx, VertxTestContext testContext) {
        System.out.println("test1: " + vertx + " " + testContext);

        cache.save("a", 1, 2);
        cache.save("b", 2, 4);
        cache.save("c", 3, 7);

        Future.all(
                      Future.succeededFuture()
                            .compose(v -> Future.all(
                                    cache.read("a")
                                         .compose(a -> {
                                             Assertions.assertEquals(1, a);
                                             return Future.succeededFuture();
                                         }),
                                    cache.read("b")
                                         .compose(b -> {
                                             Assertions.assertEquals(2, b);
                                             return Future.succeededFuture();
                                         }),
                                    cache.read("c")
                                         .compose(c -> {
                                             Assertions.assertEquals(3, c);
                                             return Future.succeededFuture();
                                         })
                            )),
                      Keel.asyncSleep(2000)
                          .compose(after2s -> Future.all(
                                  cache.read("a")
                                       .compose(a -> {
                                           Assertions.fail();
                                           return Future.succeededFuture();
                                       }, throwable -> {
                                           Assertions.assertInstanceOf(NotCached.class, throwable);
                                           return Future.succeededFuture();
                                       }),
                                  cache.read("b")
                                       .compose(b -> {
                                           Assertions.assertEquals(2, b);
                                           return Future.succeededFuture();
                                       }, throwable -> {
                                           Assertions.fail(throwable);
                                           return Future.succeededFuture();
                                       }),
                                  cache.read("c")
                                       .compose(c -> {
                                           Assertions.assertEquals(3, c);
                                           return Future.succeededFuture();
                                       }, throwable -> {
                                           Assertions.fail(throwable);
                                           return Future.succeededFuture();
                                       })
                          )),
                      Keel.asyncSleep(6000)
                          .compose(v -> Future.all(
                                          cache.read("b")
                                               .compose(b -> {
                                                   Assertions.fail();
                                                   return Future.succeededFuture();
                                               }, throwable -> {
                                                   Assertions.assertInstanceOf(NotCached.class, throwable);
                                                   return Future.succeededFuture();
                                               }),
                                          cache.read("c")
                                               .compose(c -> {
                                                   Assertions.assertEquals(3, c);
                                                   return Future.succeededFuture();
                                               }, throwable -> {
                                                   Assertions.fail(throwable);
                                                   return Future.succeededFuture();
                                               })
                                  )
                          ),
                      Keel.asyncSleep(8000)
                          .compose(v -> Future.all(
                                          Future.succeededFuture(),
                                          cache.read("c")
                                               .compose(c -> {
                                                   Assertions.fail();
                                                   return Future.succeededFuture();
                                               }, throwable -> {
                                                   Assertions.assertInstanceOf(NotCached.class, throwable);
                                                   return Future.succeededFuture();
                                               })
                                  )
                          )
              )
              .onComplete(testContext.succeedingThenComplete());
    }
}