package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

class KeelAsyncMixinLogicTest extends KeelUnitTest {

    @Test
    void testAsyncCallRepeatedly() {
        AtomicInteger counter = new AtomicInteger(0);
        async(() -> Keel.asyncCallRepeatedly(task -> {
            int current = counter.incrementAndGet();
            if (current >= 3) {
                task.stop();
            }
            return Future.succeededFuture();
        }));

        assertEquals(3, counter.get(), "Task should be executed 3 times");
    }

    @Test
    void testAsyncCallIteratively() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> processed = new ArrayList<>();

        async(() -> Keel.asyncCallIteratively(
                numbers,
                (num) -> {
                    processed.add(num);
                    return Future.succeededFuture();
                }
        ));

        assertEquals(numbers, processed, "All numbers should be processed in order");
    }

    @Test
    void testAsyncCallIterativelyWithBatch() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        List<List<Integer>> batches = new ArrayList<>();

        async(() -> Keel.asyncCallIteratively(
                numbers,
                (batch, task) -> {
                    batches.add(new ArrayList<>(batch));
                    return Future.succeededFuture();
                },
                3
        ));

        assertEquals(3, batches.size(), "Should have correct number of batches");
        assertEquals(Arrays.asList(1, 2, 3), batches.get(0));
        assertEquals(Arrays.asList(4, 5, 6), batches.get(1));
        assertEquals(Arrays.asList(7), batches.get(2));
    }

    @Test
    void testAsyncCallStepwise() {
        List<Long> steps = new ArrayList<>();
        async(() -> Keel.asyncCallStepwise(5, (step) -> {
            steps.add(step);
            return Future.succeededFuture();
        }));

        assertEquals(5, steps.size(), "Should execute 5 times");
        for (int i = 0; i < 5; i++) {
            assertEquals((long) i, steps.get(i), "Steps should be in order");
        }
    }

    @Test
    void testAsyncCallStepwiseWithRange() {
        List<Long> steps = new ArrayList<>();
        async(() -> Keel.asyncCallStepwise(2, 8, 2, (step, task) -> {
            steps.add(step);
            return Future.succeededFuture();
        }));

        assertEquals(Arrays.asList(2L, 4L, 6L), steps, "Should execute with correct step increments");
    }

    @Test
    void testAsyncCallEndlessly() {
        AtomicInteger counter = new AtomicInteger(0);
        Future<Void> interrupter = Future.future(promise -> {
            Keel.getVertx().setTimer(100, id -> promise.complete());
        });

        Keel.asyncCallEndlessly(() -> {
            counter.incrementAndGet();
            return interrupter.succeeded() ? interrupter : Future.succeededFuture();
        });

        async(() -> interrupter);
        assertTrue(counter.get() > 0, "Task should be executed at least once");
    }
}