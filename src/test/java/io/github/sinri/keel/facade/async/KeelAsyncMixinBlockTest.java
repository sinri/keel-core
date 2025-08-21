package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class KeelAsyncMixinBlockTest extends KeelJUnit5Test {

    public KeelAsyncMixinBlockTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void testAsyncTransformCompletableFuture_Success(VertxTestContext testContext) {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            getUnitTestLogger().info("async task start");
            try {
                Thread.sleep(1000L);
                return "Success";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                getUnitTestLogger().info("async task end");
            }
        });
        getUnitTestLogger().info("completableFuture generated");
        Future<String> future = Keel.asyncTransformCompletableFuture(completableFuture);
        getUnitTestLogger().info("to await");
        String result = Keel.blockAwait(future);
        getUnitTestLogger().info("result awaited is " + result);
        assertEquals("Success", result);
        testContext.completeNow();
    }

    @Test
    void testAsyncTransformCompletableFuture_Failure(VertxTestContext testContext) {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            throw new IllegalArgumentException("Test Exception");
        });
        Future<String> future = Keel.asyncTransformCompletableFuture(completableFuture);

        Assertions.assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
        testContext.completeNow();
    }

    @Test
    void testAsyncTransformRawFuture_Success(VertxTestContext testContext) throws Exception {
        FutureTask<String> rawFuture = new FutureTask<>(() -> "Raw Success");
        rawFuture.run();
        Future<String> future = Keel.asyncTransformRawFuture(rawFuture);

        String result = Keel.blockAwait(future);
        assertEquals("Raw Success", result);
        testContext.completeNow();
    }

    @Test
    void testAsyncTransformRawFuture_Failure(VertxTestContext testContext) throws Exception {
        FutureTask<String> rawFuture = new FutureTask<>(() -> {
            throw new RuntimeException("Raw Failure");
        });
        rawFuture.run();
        Future<String> future = Keel.asyncTransformRawFuture(rawFuture);

        assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
        testContext.completeNow();
    }

    @Test
    void testAsyncTransformRawFutureWithSleep_Success(VertxTestContext testContext) {
        FutureTask<String> rawFuture = new FutureTask<>(() -> {
            getUnitTestLogger().info("rawFuture start");
            Thread.sleep(100);
            getUnitTestLogger().info("rawFuture to end");
            return "Delayed Success";
        });
        getUnitTestLogger().info("task of raw future defined");
        new Thread(rawFuture).start();
        getUnitTestLogger().info("thread of raw future started");
        Future<String> future = Keel.asyncTransformRawFuture(rawFuture, 50);
        getUnitTestLogger().info("future defined");
        String result = Keel.blockAwait(future);
        getUnitTestLogger().info("future awaited: "+result);
        assertEquals("Delayed Success", result);
        testContext.completeNow();
    }

    @Test
    void testBlockAwait_Success(VertxTestContext testContext) {
        Future<String> future = Future.succeededFuture("Immediate Success");
        String result = Keel.blockAwait(future);
        assertEquals("Immediate Success", result);
        testContext.completeNow();
    }

    @Test
    void testBlockAwait_Failure(VertxTestContext testContext) {
        Future<String> future = Future.failedFuture(new RuntimeException("Immediate Failure"));
        assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
        testContext.completeNow();
    }

    @Test
    void testBlockAwait_Interrupted(VertxTestContext testContext) throws InterruptedException {
        Promise<String> promise = Promise.promise();
        Future<String> future = promise.future();

        Thread testThread = new Thread(() -> {
            try {
                Thread.sleep(100);
                promise.complete("Should be interrupted");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        testThread.start();

        Thread.currentThread().interrupt();
        Exception exception = assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
        assertTrue(exception.getMessage().contains("Interrupted while waiting"));
        testThread.interrupt();
        testContext.completeNow();
    }
}