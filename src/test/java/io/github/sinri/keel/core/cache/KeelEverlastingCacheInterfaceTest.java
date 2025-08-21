package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith(VertxExtension.class)
class KeelEverlastingCacheInterfaceTest extends KeelJUnit5Test {
    private static KeelEverlastingCacheInterface<String, Integer> cache;

    KeelEverlastingCacheInterfaceTest(Vertx vertx) {
        super(vertx);
        cache = KeelEverlastingCacheInterface.createDefaultInstance();
    }

    @BeforeEach
    public void setUp() {
        cache.removeAll();
    }

    @Test
    public void test1() {
        cache.save("a", 1);
        cache.save(Map.of(
                "b", 2,
                "c", 3
        ));
        Assertions.assertDoesNotThrow(() -> {
            var a = cache.read("a");
            Assertions.assertEquals(1, a);
        });
        Assertions.assertDoesNotThrow(() -> {
            var d = cache.read("d", 4);
            Assertions.assertEquals(4, d);
        });

        Assertions.assertThrows(NotCached.class, () -> {
            cache.read("e");
        });

    }
}