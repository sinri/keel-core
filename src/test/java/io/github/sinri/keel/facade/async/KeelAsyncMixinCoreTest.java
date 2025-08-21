package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class KeelAsyncMixinCoreTest extends KeelJUnit5Test implements KeelAsyncMixinCore {

    public KeelAsyncMixinCoreTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void testAsyncSleep(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        long sleepTime = 1000L;

        Future<Void> future = asyncSleep(sleepTime);
        future.onComplete(ar -> {
            if (ar.succeeded()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                assertTrue(duration >= sleepTime, "Sleep duration should be at least " + sleepTime + "ms");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncSleepWithInterrupter(VertxTestContext testContext) {
        long sleepTime = 5000L;
        long interruptTime = 1000L;
        long startTime = System.currentTimeMillis();

        Promise<Void> interrupter = Promise.promise();
        Future<Void> future = asyncSleep(sleepTime, interrupter);

        // 在1秒后中断睡眠
        Keel.getVertx().setTimer(interruptTime, id -> {
            interrupter.complete();
        });

        future.onComplete(ar -> {
            if (ar.succeeded()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                assertTrue(duration >= interruptTime, "Sleep should be interrupted after at least " + interruptTime + "ms");
                assertTrue(duration < sleepTime, "Sleep should be interrupted before " + sleepTime + "ms");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncSleepWithNegativeTime(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        Future<Void> future = asyncSleep(-1000L);

        future.onComplete(ar -> {
            if (ar.succeeded()) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                assertTrue(duration >= 1L, "Sleep duration should be at least 1ms for negative input");
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}