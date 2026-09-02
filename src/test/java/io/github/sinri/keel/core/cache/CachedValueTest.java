package io.github.sinri.keel.core.cache;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CachedValueTest {
    @Test
    void storesReplacesAndClearsValue() {
        CachedValue<String> cache = new CachedValue<>();

        assertNull(cache.get());
        assertEquals("fallback", cache.getOrElse("fallback"));
        assertFalse(cache.isAvailable());

        assertEquals(cache, cache.set("first"));
        assertEquals("first", cache.get());
        assertTrue(cache.isAvailable());

        cache.set("second");
        assertEquals("second", cache.get());

        cache.clear();
        assertNull(cache.get());
        assertFalse(cache.isAvailable());
    }

    @Test
    void expiresAndAcceptsANewValue() throws InterruptedException {
        CachedValue<String> cache = new CachedValue<>();
        cache.set("old", 1);

        Thread.sleep(1_100L);

        assertNull(cache.get());
        cache.set("new", 1);
        assertEquals("new", cache.get());
    }

    @Test
    void validatesLifetime() {
        CachedValue<String> cache = new CachedValue<>();

        assertThrows(IllegalArgumentException.class, () -> cache.setDefaultLifetimeInSeconds(0));
        assertThrows(IllegalArgumentException.class, () -> cache.setDefaultLifetimeInSeconds(-1));
        assertThrows(IllegalArgumentException.class, () -> cache.setDefaultLifetimeInSeconds(Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> cache.set("value", 0));
        assertThrows(IllegalArgumentException.class, () -> cache.set("value", Long.MAX_VALUE));

        assertEquals(cache, cache.setDefaultLifetimeInSeconds(2));
        assertEquals(2, cache.getDefaultLifetimeInSeconds());
    }

    @Test
    void reloadsOnlyWhenValueIsAbsent() {
        CachedValue<String> cache = new CachedValue<>();
        AtomicInteger loads = new AtomicInteger();

        assertEquals("loaded", cache.getOrReload(() -> {
            loads.incrementAndGet();
            return "loaded";
        }));
        assertEquals("loaded", cache.getOrReload(() -> {
            loads.incrementAndGet();
            return "other";
        }));
        assertEquals(1, loads.get());

        cache.clear();
        assertThrows(NullPointerException.class, () -> cache.getOrReload(() -> null));
        assertNull(cache.get());
    }

    @Test
    void coalescesConcurrentReloads() throws Exception {
        CachedValue<String> cache = new CachedValue<>();
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> results = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    return cache.getOrReload(() -> {
                        loads.incrementAndGet();
                        return "loaded";
                    });
                }));
            }

            start.countDown();
            for (Future<String> result : results) {
                assertEquals("loaded", result.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, loads.get());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
