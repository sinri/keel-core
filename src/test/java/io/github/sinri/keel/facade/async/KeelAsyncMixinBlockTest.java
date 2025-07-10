package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static org.junit.jupiter.api.Assertions.*;

public class KeelAsyncMixinBlockTest extends KeelUnitTest {


    @Test
    void testAsyncTransformCompletableFuture_Success() {
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
    }

    @Test
    void testAsyncTransformCompletableFuture_Failure() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            throw new IllegalArgumentException("Test Exception");
        });
        Future<String> future = Keel.asyncTransformCompletableFuture(completableFuture);

        Assertions.assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
    }

    @Test
    void testAsyncTransformRawFuture_Success() throws Exception {
        FutureTask<String> rawFuture = new FutureTask<>(() -> "Raw Success");
        rawFuture.run();
        Future<String> future = Keel.asyncTransformRawFuture(rawFuture);

        String result = Keel.blockAwait(future);
        assertEquals("Raw Success", result);
    }

    @Test
    void testAsyncTransformRawFuture_Failure() throws Exception {
        FutureTask<String> rawFuture = new FutureTask<>(() -> {
            throw new RuntimeException("Raw Failure");
        });
        rawFuture.run();
        Future<String> future = Keel.asyncTransformRawFuture(rawFuture);

        assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
    }

    @Test
    void testAsyncTransformRawFutureWithSleep_Success(){
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
    }

    @Test
    void testBlockAwait_Success() {
        Future<String> future = Future.succeededFuture("Immediate Success");
        String result = Keel.blockAwait(future);
        assertEquals("Immediate Success", result);
    }

    @Test
    void testBlockAwait_Failure() {
        Future<String> future = Future.failedFuture(new RuntimeException("Immediate Failure"));
        assertThrows(RuntimeException.class, () -> Keel.blockAwait(future));
    }

    @Test
    void testBlockAwait_Interrupted() throws InterruptedException {
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
    }
}