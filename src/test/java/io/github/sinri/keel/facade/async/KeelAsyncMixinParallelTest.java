package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class KeelAsyncMixinParallelTest extends KeelJUnit5Test {

    public KeelAsyncMixinParallelTest(Vertx vertx) {
        super(vertx);
    }
    
    @Test
    void testParallelForAllSuccess(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        AtomicInteger sum = new AtomicInteger(0);

        Future<Void> future = Keel.parallelForAllSuccess(numbers, num -> {
            return Future.succeededFuture()
                    .map(v -> {
                        sum.addAndGet(num);
                        return null;
                    });
        });

        future.onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(15, sum.get()); // 1 + 2 + 3 + 4 + 5 = 15
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testParallelForAllSuccessWithFailure(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        Future<Void> future = Keel.parallelForAllSuccess(numbers, num -> {
            if (num == 3) {
                return Future.failedFuture("Failed on number 3");
            }
            return Future.succeededFuture();
        });

        future.onComplete(ar -> {
            if (ar.failed()) {
                assertEquals("Failed on number 3", ar.cause().getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow("Expected failure but got success");
            }
        });
    }

    @Test
    void testParallelForAnySuccess(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        AtomicInteger successCount = new AtomicInteger(0);

        Future<Void> future = Keel.parallelForAnySuccess(numbers, num -> {
            if (num % 2 == 0) {
                return Future.failedFuture("Failed on even number");
            }
            successCount.incrementAndGet();
            return Future.succeededFuture();
        });

        future.onComplete(ar -> {
            if (ar.succeeded()) {
                assertTrue(successCount.get() > 0);
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testParallelForAnySuccessAllFailed(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        Future<Void> future = Keel.parallelForAnySuccess(numbers, num -> 
            Future.failedFuture("All failed")
        );

        future.onComplete(ar -> {
            if (ar.failed()) {
                testContext.completeNow();
            } else {
                testContext.failNow("Expected failure but got success");
            }
        });
    }

    @Test
    void testParallelForAllComplete(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(0);

        Future<Void> future = Keel.parallelForAllComplete(numbers, num -> {
            return Future.future(promise -> {
                totalCount.incrementAndGet();
                if (num % 2 == 0) {
                    failureCount.incrementAndGet();
                    promise.fail("Failed on even number: " + num);
                } else {
                    successCount.incrementAndGet();
                    promise.complete();
                }
            });
        });

        future.onComplete(ar -> {
            // 验证所有任务都执行了
            assertEquals(5, totalCount.get(), "All tasks should be executed");
            // 验证成功和失败的任务数量
            assertEquals(3, successCount.get(), "Should have 3 successful tasks (1,3,5)");
            assertEquals(2, failureCount.get(), "Should have 2 failed tasks (2,4)");
            // 不需要验证 ar.succeeded()，因为 parallelForAllComplete 只关心任务是否完成，不关心成功失败
            testContext.completeNow();
        });
    }

    @Test
    void testEmptyCollection(VertxTestContext testContext) {
        List<Integer> emptyList = List.of();

        Future<Void> allSuccess = Keel.parallelForAllSuccess(emptyList, num -> Future.succeededFuture());
        Future<Void> anySuccess = Keel.parallelForAnySuccess(emptyList, num -> Future.succeededFuture());
        Future<Void> allComplete = Keel.parallelForAllComplete(emptyList, num -> Future.succeededFuture());

        allSuccess.onComplete(ar -> {
            if (ar.succeeded()) {
                anySuccess.onComplete(ar2 -> {
                    if (ar2.succeeded()) {
                        allComplete.onComplete(ar3 -> {
                            if (ar3.succeeded()) {
                                testContext.completeNow();
                            } else {
                                testContext.failNow(ar3.cause());
                            }
                        });
                    } else {
                        testContext.failNow(ar2.cause());
                    }
                });
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}