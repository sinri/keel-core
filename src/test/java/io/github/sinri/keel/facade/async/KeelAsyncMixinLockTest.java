package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
class KeelAsyncMixinLockTest extends KeelJUnit5Test {

    public KeelAsyncMixinLockTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void testAsyncCallExclusively(VertxTestContext testContext) {
        String lockName = "test_lock";
        AtomicInteger counter = new AtomicInteger(0);

        // Test basic lock functionality
        Future<Integer> result = Keel.asyncCallExclusively(lockName, () -> {
            counter.incrementAndGet();
            return Future.succeededFuture(counter.get());
        });

        result.onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, ar.result());
                assertEquals(1, counter.get());

                // Test lock with timeout
                long waitTime = 2000L;
                Future<Integer> resultWithTimeout = Keel.asyncCallExclusively(lockName, waitTime, () -> {
                    counter.incrementAndGet();
                    return Future.succeededFuture(counter.get());
                });

                resultWithTimeout.onComplete(ar2 -> {
                    if (ar2.succeeded()) {
                        assertEquals(2, ar2.result());
                        assertEquals(2, counter.get());
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar2.cause());
                    }
                });
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallExclusivelyWithConcurrency(VertxTestContext testContext) {
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
        Future.all(futures).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(numOperations, sharedCounter.get(), "All operations should have completed exactly once");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallExclusivelyWithFailure(VertxTestContext testContext) {
        String lockName = "failure_test_lock";
        AtomicInteger counter = new AtomicInteger(0);

        // Test lock with a failing operation
        Future<Integer> failingResult = Keel.asyncCallExclusively(lockName, () -> {
            counter.incrementAndGet();
            return Future.failedFuture(new RuntimeException("Test failure"));
        });

        failingResult.otherwiseEmpty().onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, counter.get(), "Counter should be incremented even if operation fails");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}