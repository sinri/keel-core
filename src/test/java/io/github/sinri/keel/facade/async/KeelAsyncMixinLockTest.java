package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

class KeelAsyncMixinLockTest extends KeelUnitTest {

    @Test
    void testAsyncCallExclusively() {
        String lockName = "test_lock";
        AtomicInteger counter = new AtomicInteger(0);

        // Test basic lock functionality
        Future<Integer> result = Keel.asyncCallExclusively(lockName, () -> {
            counter.incrementAndGet();
            return Future.succeededFuture(counter.get());
        });

        async(() -> result.compose(r -> {
            assertEquals(1, r);
            assertEquals(1, counter.get());
            return Future.succeededFuture();
        }));

        // Test lock with timeout
        long waitTime = 2000L;
        Future<Integer> resultWithTimeout = Keel.asyncCallExclusively(lockName, waitTime, () -> {
            counter.incrementAndGet();
            return Future.succeededFuture(counter.get());
        });

        async(() -> resultWithTimeout.compose(r -> {
            assertEquals(2, r);
            assertEquals(2, counter.get());
            return Future.succeededFuture();
        }));
    }

    @Test
    void testAsyncCallExclusivelyWithConcurrency() {
        String lockName = "concurrent_test_lock";
        AtomicInteger sharedCounter = new AtomicInteger(0);
        int numOperations = 10;

        // Create multiple concurrent operations
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < numOperations; i++) {
            futures.add(Keel.asyncCallExclusively(lockName, () -> {
                // Simulate some work
                return Keel.asyncSleep(100)
                        .compose(v -> {
                            sharedCounter.incrementAndGet();
                            return Future.succeededFuture();
                        });
            }));
        }

        // Wait for all operations to complete
        async(() -> Future.all(futures).compose(ar -> {
            assertEquals(numOperations, sharedCounter.get(), "All operations should have completed exactly once");
            return Future.succeededFuture();
        }));
    }

    @Test
    void testAsyncCallExclusivelyWithFailure() {
        String lockName = "failure_test_lock";
        AtomicInteger counter = new AtomicInteger(0);

        // Test lock with a failing operation
        Future<Integer> failingResult = Keel.asyncCallExclusively(lockName, () -> {
            counter.incrementAndGet();
            return Future.failedFuture(new RuntimeException("Test failure"));
        });

        async(() -> failingResult.otherwiseEmpty().compose(v -> {
            assertEquals(1, counter.get(), "Counter should be incremented even if operation fails");
            return Future.succeededFuture();
        }));
    }
}