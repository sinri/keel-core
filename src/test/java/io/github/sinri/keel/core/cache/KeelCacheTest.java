package io.github.sinri.keel.core.cache;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeelCacheTest extends KeelJUnit5Test {
    @Test
    void valueWrapperRejectsNonPositiveLifeInSeconds() {
        assertThrows(IllegalArgumentException.class, () -> new ValueWrapper<>("value", -1));
        assertThrows(IllegalArgumentException.class, () -> new ValueWrapper<>("value", 0));
    }

    @Test
    void valueWrapperRejectsOverflowingLifeInSeconds() {
        assertThrows(IllegalArgumentException.class, () -> new ValueWrapper<>("value", Long.MAX_VALUE));
        assertThrows(
                IllegalArgumentException.class,
                () -> ValueWrapper.computeDeath(Long.MAX_VALUE - 999, 1)
        );
    }

    @Test
    void valueWrapperAcceptsNormalLifeInSeconds() {
        ValueWrapper<String> wrapper = new ValueWrapper<>("value", 1);

        assertEquals("value", wrapper.getValue());
        assertTrue(wrapper.getDeath() > wrapper.getBirth());
        assertTrue(wrapper.getRemainingLifetime() > 0);
    }

    @Test
    void cacheRejectsInvalidLifeInSeconds() {
        KeelCacheInterface<String, String> cache = KeelCacheInterface.createDefaultInstance();

        assertThrows(IllegalArgumentException.class, () -> cache.setDefaultLifeInSeconds(-1));
        assertThrows(IllegalArgumentException.class, () -> cache.setDefaultLifeInSeconds(0));
        assertThrows(IllegalArgumentException.class, () -> cache.save("key", "value", Long.MAX_VALUE));
        assertThrows(
                IllegalArgumentException.class,
                () -> cache.computeIfAbsent("key", k -> "value", Long.MAX_VALUE)
        );
    }

    @Test
    void cacheAcceptsNormalLifeInSeconds() {
        KeelCacheInterface<String, String> cache = KeelCacheInterface.createDefaultInstance();

        cache.setDefaultLifeInSeconds(1);
        cache.save("key", "value");

        assertEquals("value", cache.read("key", null));
    }
}
