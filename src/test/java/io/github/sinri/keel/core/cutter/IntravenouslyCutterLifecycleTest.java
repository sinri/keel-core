package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Vertx;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class IntravenouslyCutterLifecycleTest {
    private Vertx vertx;
    private Keel keel;

    @BeforeEach
    void startVertx() {
        vertx = Vertx.vertx();
        keel = Keel.create(vertx);
    }

    @AfterEach
    void closeVertx() throws Exception {
        await(vertx.close());
    }

    private static <T> T await(Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    @Test
    void waitsForSerialProcessingAndReportsFirstFailure() throws Exception {
        List<String> calls = new CopyOnWriteArrayList<>();
        Promise<Void> firstEntered = Promise.promise();
        Promise<Void> releaseFirst = Promise.promise();
        RuntimeException firstFailure = new RuntimeException("first");
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(value -> {
            calls.add(value);
            if (value.equals("first")) {
                firstEntered.complete();
                return releaseFirst.future();
            }
            if (value.equals("second")) throw new IllegalArgumentException("second");
            return Future.succeededFuture();
        });
        await(cutter.deployMe(keel, new DeploymentOptions()));
        try {
            cutter.acceptFromStream(Buffer.buffer("first\r\n\r\nsecond\r\rthird\n\nunfinished"));
            cutter.stopHere();
            Future<Void> completion = cutter.waitForAllHandled();
            await(firstEntered.future());
            assertEquals(List.of("first"), calls);
            assertFalse(completion.isComplete());
            releaseFirst.fail(firstFailure);
            ExecutionException error = assertThrows(ExecutionException.class, () -> await(completion));
            assertSame(firstFailure, error.getCause());
            assertEquals(List.of("first", "second", "third"), calls);
            assertEquals("unfinished", cutter.getBufferRef().get().toString());
        } finally {
            releaseFirst.tryComplete();
            await(cutter.undeployMe());
        }
    }

    @Test
    void reportsSynchronousThrowAndContinues() throws Exception {
        RuntimeException failure = new RuntimeException("synchronous");
        List<String> calls = new CopyOnWriteArrayList<>();
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(value -> {
            calls.add(value);
            if (value.equals("bad")) throw failure;
            return Future.succeededFuture();
        });
        await(cutter.deployMe(keel, new DeploymentOptions()));
        try {
            cutter.acceptFromStream(Buffer.buffer("bad\n\ngood\n\n"));
            cutter.stopHere();
            ExecutionException error = assertThrows(ExecutionException.class, () -> await(cutter.waitForAllHandled()));
            assertSame(failure, error.getCause());
            assertEquals(List.of("bad", "good"), calls);
        } finally {
            await(cutter.undeployMe());
        }
    }

    @Test
    void successfulCompletionDeliversOnlyCompleteEvents() throws Exception {
        List<String> calls = new CopyOnWriteArrayList<>();
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(value -> {
            calls.add(value);
            return Future.succeededFuture();
        });
        await(cutter.deployMe(keel, new DeploymentOptions()));
        try {
            Buffer input = Buffer.buffer("data: 中文😀\r\n\r\ndata: next\r\runfinished\r\n");
            for (int i = 0; i < input.length(); i++) cutter.acceptFromStream(input.getBuffer(i, i + 1));
            cutter.stopHere();
            await(cutter.waitForAllHandled());
            assertEquals(List.of("data: 中文😀", "data: next"), calls);
            assertEquals("unfinished\r\n", cutter.getBufferRef().get().toString());
        } finally {
            await(cutter.undeployMe());
        }
    }

    @Test
    void explicitStopCauseTakesPrecedenceOverProcessingFailure() throws Exception {
        RuntimeException stopCause = new RuntimeException("stream failed");
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(value ->
                Future.failedFuture(new RuntimeException("processor failed")));
        await(cutter.deployMe(keel, new DeploymentOptions()));
        try {
            cutter.acceptFromStream(Buffer.buffer("data: test\n\n"));
            cutter.stopHere(stopCause);
            ExecutionException error = assertThrows(ExecutionException.class, () -> await(cutter.waitForAllHandled()));
            assertSame(stopCause, error.getCause());
        } finally {
            await(cutter.undeployMe());
        }
    }

    @Test
    void timeoutReportsFailureAndRetainsIncompleteBytes() throws Exception {
        IntravenouslyCutterOnString cutter = new IntravenouslyCutterOnString(value -> Future.succeededFuture(), 100);
        await(cutter.deployMe(keel, new DeploymentOptions()));
        try {
            cutter.acceptFromStream(Buffer.buffer("unfinished"));
            ExecutionException error = assertThrows(ExecutionException.class, () -> await(cutter.waitForAllHandled()));
            assertInstanceOf(CutterTimeout.class, error.getCause());
            assertEquals("unfinished", cutter.getBufferRef().get().toString());
        } finally {
            await(cutter.undeployMe());
        }
    }
}
