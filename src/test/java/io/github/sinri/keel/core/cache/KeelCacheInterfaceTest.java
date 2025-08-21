package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class KeelCacheInterfaceTest extends KeelJUnit5Test {
    static KeelCacheInterface<String, Integer> cache;

    KeelCacheInterfaceTest(Vertx vertx) {
        super(vertx);
        cache = KeelCacheInterface.createDefaultInstance();
        cache.startEndlessCleanUp(3);
    }

    @BeforeEach
    public void setUp() {
        cache.removeAll();
    }

    @Test
    public void test1() {
        cache.save("a", 1);
        cache.save("b", 2, 2);
        cache.save("c", 3, 7);

        try {
            var a = cache.read("a");
            Assertions.assertEquals(1, a);
            var b = cache.read("b");
            Assertions.assertEquals(2, b);
        } catch (NotCached e) {
            Assertions.fail(e);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assertions.fail("Sleep interrupted", e);
        }

        Assertions.assertDoesNotThrow(() -> {
            var a = cache.read("a");
            Assertions.assertEquals(1, a);
        });

        Assertions.assertThrows(NotCached.class, () -> cache.read("b"));

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assertions.fail("Sleep interrupted", e);
        }
        Assertions.assertThrows(NotCached.class, () -> cache.read("c"));
    }
}