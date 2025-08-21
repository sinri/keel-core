package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class KeelAsyncMixinLogicTest extends KeelJUnit5Test {

    public KeelAsyncMixinLogicTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void testAsyncCallRepeatedly(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);
        Keel.asyncCallRepeatedly(task -> {
            int current = counter.incrementAndGet();
            if (current >= 3) {
                task.stop();
            }
            return Future.succeededFuture();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, counter.get(), "Task should be executed 3 times");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIteratively(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> processed = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (num) -> {
                    processed.add(num);
                    return Future.succeededFuture();
                }
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(numbers, processed, "All numbers should be processed in order");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatch(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        List<List<Integer>> batches = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                3
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, batches.size(), "Should have correct number of batches");
                assertEquals(Arrays.asList(1, 2, 3), batches.get(0));
                assertEquals(Arrays.asList(4, 5, 6), batches.get(1));
                assertEquals(List.of(7), batches.get(2));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallStepwise(VertxTestContext testContext) {
        List<Long> steps = new ArrayList<>();
        Keel.asyncCallStepwise(5, (step) -> {
            steps.add(step);
            return Future.succeededFuture();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(5, steps.size(), "Should execute 5 times");
                for (int i = 0; i < 5; i++) {
                    assertEquals(i, steps.get(i), "Steps should be in order");
                }
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallStepwiseWithRange(VertxTestContext testContext) {
        List<Long> steps = new ArrayList<>();
        Keel.asyncCallStepwise(2, 8, 2, (step, task) -> {
            steps.add(step);
            return Future.succeededFuture();
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(Arrays.asList(2L, 4L, 6L), steps, "Should execute with correct step increments");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallEndlessly(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);
        Future<Void> interrupter = Future.future(promise -> {
            Keel.getVertx().setTimer(100, id -> promise.complete());
        });

        Keel.asyncCallEndlessly(() -> {
            counter.incrementAndGet();
            return interrupter.succeeded() ? interrupter : Future.succeededFuture();
        });

        interrupter.onComplete(ar -> {
            if (ar.succeeded()) {
                assertTrue(counter.get() > 0, "Task should be executed at least once");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithEmptyIterator(VertxTestContext testContext) {
        List<Integer> emptyList = new ArrayList<>();
        List<Integer> processed = new ArrayList<>();

        Keel.asyncCallIteratively(
                emptyList,
                (num) -> {
                    processed.add(num);
                    return Future.succeededFuture();
                }
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertTrue(processed.isEmpty(), "Empty iterator should not process any items");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatchEmptyIterator(VertxTestContext testContext) {
        List<Integer> emptyList = new ArrayList<>();
        List<List<Integer>> batches = new ArrayList<>();

        Keel.asyncCallIteratively(
                emptyList,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                3
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertTrue(batches.isEmpty(), "Empty iterator should not create any batches");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatchSizeLargerThanElements(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2);
        List<List<Integer>> batches = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                5  // batchSize larger than total elements
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, batches.size(), "Should have only one batch when batchSize > total elements");
                assertEquals(Arrays.asList(1, 2), batches.get(0));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatchSizeEqualToElements(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        List<List<Integer>> batches = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                3  // batchSize equals total elements
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, batches.size(), "Should have only one batch when batchSize equals total elements");
                assertEquals(Arrays.asList(1, 2, 3), batches.get(0));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithSingleElement(VertxTestContext testContext) {
        List<Integer> numbers = List.of(42);
        List<Integer> processed = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (num) -> {
                    processed.add(num);
                    return Future.succeededFuture();
                }
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, processed.size(), "Should process single element");
                assertEquals(42, processed.get(0));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatchSingleElement(VertxTestContext testContext) {
        List<Integer> numbers = List.of(42);
        List<List<Integer>> batches = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                3
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, batches.size(), "Should have one batch for single element");
                assertEquals(List.of(42), batches.get(0));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallIterativelyWithBatchExactMultiple(VertxTestContext testContext) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<List<Integer>> batches = new ArrayList<>();

        Keel.asyncCallIteratively(
                numbers,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                2  // batchSize divides total elements exactly
        ).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, batches.size(), "Should have correct number of batches for exact multiple");
                assertEquals(Arrays.asList(1, 2), batches.get(0));
                assertEquals(Arrays.asList(3, 4), batches.get(1));
                assertEquals(Arrays.asList(5, 6), batches.get(2));
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}