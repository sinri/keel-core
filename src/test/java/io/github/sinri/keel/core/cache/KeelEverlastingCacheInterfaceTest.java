package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class KeelEverlastingCacheInterfaceTest extends KeelUnitTest {
    private static KeelEverlastingCacheInterface<String, Integer> cache;

    @BeforeAll
    public static void init() {
        cache = KeelEverlastingCacheInterface.createDefaultInstance();
    }

    @BeforeEach
    public void setUp() {
        cache.removeAll();
    }

    @Test
    public void test1(){
        cache.save("a", 1);
        cache.save(Map.of(
                "b",2,
                "c",3
        ));
        Assertions.assertDoesNotThrow(()->{
            var a=cache.read("a");
            Assertions.assertEquals(1,a);
        });
        Assertions.assertDoesNotThrow(()->{
            var d=cache.read("d",4);
            Assertions.assertEquals(4,d);
        });

        Assertions.assertThrows(NotCached.class,()->{
            cache.read("e");
        });

    }
}