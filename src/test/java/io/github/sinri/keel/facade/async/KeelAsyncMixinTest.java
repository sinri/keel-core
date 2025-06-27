package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.tesuto.unit.KeelUnitTest;
import org.junit.jupiter.api.Test;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

class KeelAsyncMixinTest extends KeelUnitTest {

    @Test
    void parallelForAllSuccess() {
    }

    @Test
    void testParallelForAllSuccess() {
    }

    @Test
    void parallelForAnySuccess() {
    }

    @Test
    void testParallelForAnySuccess() {
    }

    @Test
    void parallelForAllComplete() {
    }

    @Test
    void testParallelForAllComplete() {
    }

    @Test
    void asyncCallRepeatedly() {
    }

    @Test
    void asyncCallIteratively() {
    }

    @Test
    void testAsyncCallIteratively() {
    }

    @Test
    void testAsyncCallIteratively1() {
    }

    @Test
    void testAsyncCallIteratively2() {
    }

    @Test
    void testAsyncCallIteratively3() {
    }

    @Test
    void testAsyncCallIteratively4() {
    }

    @Test
    void testAsyncCallIteratively5() {
    }

    @Test
    void asyncCallStepwise() {
    }

    @Test
    void testAsyncCallStepwise() {
    }

    @Test
    void testAsyncCallStepwise1() {
    }

    @Test
    void asyncCallEndlessly() {
    }

    @Test
    void asyncSleep() {
        getUnitTestLogger().info("start");
        Keel.asyncSleep(1000).await();
        getUnitTestLogger().info("end");
    }

    @Test
    void testAsyncSleep() {
        // Keel.asyncSleep(1000);
    }

    @Test
    void asyncCallExclusively() {
    }

    @Test
    void testAsyncCallExclusively() {
    }

    @Test
    void asyncTransfromCompletableFuture() {
    }

    @Test
    void asyncTransformCompletableFuture() {
    }

    @Test
    void asyncTransformRawFuture() {
    }

    @Test
    void executeBlocking() {
    }

    @Test
    void pseudoAwait() {
    }
}