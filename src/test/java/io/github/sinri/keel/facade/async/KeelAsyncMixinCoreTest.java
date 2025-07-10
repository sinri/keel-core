package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

class KeelAsyncMixinCoreTest extends KeelUnitTest implements KeelAsyncMixinCore {

    @Test
    void testAsyncSleep() {
        long startTime = System.currentTimeMillis();
        long sleepTime = 1000L;

        Future<Void> future = asyncSleep(sleepTime);
        future.onComplete(ar -> {
            assertTrue(ar.succeeded());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            assertTrue(duration >= sleepTime, "Sleep duration should be at least " + sleepTime + "ms");
        });
    }

    @Test
    void testAsyncSleepWithInterrupter() {
        long sleepTime = 5000L;
        long interruptTime = 1000L;
        AtomicLong endTime = new AtomicLong();
        long startTime = System.currentTimeMillis();

        Promise<Void> interrupter = Promise.promise();
        Future<Void> future = asyncSleep(sleepTime, interrupter);

        // 在1秒后中断睡眠
        Keel.getVertx().setTimer(interruptTime, id -> {
            interrupter.complete();
            endTime.set(System.currentTimeMillis());
        });

        future.onComplete(ar -> {
            assertTrue(ar.succeeded());
            long duration = endTime.get() - startTime;
            assertTrue(duration >= interruptTime, "Sleep should be interrupted after at least " + interruptTime + "ms");
            assertTrue(duration < sleepTime, "Sleep should be interrupted before " + sleepTime + "ms");
        });
    }

    @Test
    void testAsyncSleepWithNegativeTime() {
        long startTime = System.currentTimeMillis();
        Future<Void> future = asyncSleep(-1000L);

        future.onComplete(ar -> {
            assertTrue(ar.succeeded());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            assertTrue(duration >= 1L, "Sleep duration should be at least 1ms for negative input");
        });
    }
}