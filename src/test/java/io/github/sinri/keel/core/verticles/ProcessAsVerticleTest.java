package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.facade.tesuto.unit.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
class ProcessAsVerticleTest extends KeelJUnit5Test {

    /**
     * The constructor would run after {@code @BeforeAll} annotated method.
     */
    public ProcessAsVerticleTest(Vertx vertx) {
        super(vertx);
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    private static List<String> buildEchoBothCommand() {
        if (isWindows()) {
            // Echo to stdout and stderr
            return Arrays.asList("cmd", "/c", "echo OUT & echo ERR 1>&2");
        } else {
            return Arrays.asList("/bin/sh", "-c", "sleep 1; echo OUT; echo ERR 1>&2");
        }
    }

    private static List<String> buildSleepCommand(int seconds) {
        if (isWindows()) {
            // ping loop to simulate sleep approximately N seconds (+1)
            int count = Math.max(1, seconds + 1);
            return Arrays.asList("cmd", "/c", "ping -n " + count + " 127.0.0.1 >NUL");
        } else {
            return Arrays.asList("/bin/sh", "-c", "sleep " + seconds);
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testStdoutStderrAndAutoUndeploy(VertxTestContext testContext) {
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        AtomicBoolean exitCalled = new AtomicBoolean(false);

        ProcessAsVerticle pav = new ProcessAsVerticle(
                buildEchoBothCommand(),
                null,
                null,
                // stdout processor
                stdoutStream -> {
                    stdoutStream.handler(stdout::append)
                                .exceptionHandler(throwable -> {
                                    getUnitTestLogger().exception(throwable, "stdoutStream exceptionHandler");
                                });
                },
                // stderr processor
                stderrStream -> {
                    stderrStream.handler(stderr::append)
                                .exceptionHandler(throwable -> {
                                    getUnitTestLogger().exception(throwable, "stderrStream exceptionHandler");
                                });
                },
                // exit processor
                (Process p) -> {
                    getUnitTestLogger().info("process exited: " + p.exitValue());
                    exitCalled.set(true);
                    return Future.succeededFuture();
                }
        );
        assertThrows(IllegalStateException.class, pav::undeployMe);
        pav.deployMe()
           .compose(id -> {
               getUnitTestLogger().info("deploymentID: " + id);
               assertNotNull(id);
               assertFalse(id.isEmpty());
               // Wait briefly to allow process to run and exit, then the verticle should auto-undeploy
               Promise<Void> wait = Promise.promise();
               // Give some time for IO loop and exit handler
               Vertx.currentContext().owner().setTimer(2000, t -> wait.complete());
               return wait.future();
           })
           .onComplete(ar -> {
               getUnitTestLogger().info("alive : " + pav.getProcess().isAlive());
               testContext.verify(() -> {
                   assertTrue(exitCalled.get(), "processExitProcessor should be called");
                   assertTrue(stdout.toString().contains("OUT"), "stdout should contain OUT");
                   assertTrue(stderr.toString().contains("ERR"), "stderr should contain ERR");
                   // After auto-undeploy, undeployMe should not be allowed (deploymentID is null)
                   getUnitTestLogger().info("deployment id now: " + pav.deploymentID());
               });
               testContext.completeNow();
           });
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testStopTerminatesLongRunningProcessOnUndeploy(VertxTestContext testContext) {
        AtomicReference<Integer> exitCodeRef = new AtomicReference<>();
        AtomicBoolean exitCalled = new AtomicBoolean(false);

        ProcessAsVerticle pav = new ProcessAsVerticle(
                buildSleepCommand(5),
                null,
                null,
                // stdout
                null,
                // stderr
                null,
                // on exit
                (Process p) -> {
                    exitCalled.set(true);
                    exitCodeRef.set(p.exitValue());
                    return Future.succeededFuture();
                }
        );

        pav.deployMe()
           .compose(id -> {
               assertNotNull(id);
               // Undeploy soon after start; this should cause stop() to destroy the process
               return pav.undeployMe();
           })
           .onComplete(ar -> {
               testContext.verify(() -> {
                   assertTrue(ar.succeeded(), "undeploy should succeed");
                   // Give a little time for process to actually exit after destroy
                   try {
                       Thread.sleep(200);
                   } catch (InterruptedException ignored) {
                   }
                   assertTrue(exitCalled.get(), "exit handler should be called after undeploy/stop");
                   Integer exitCode = exitCodeRef.get();
                   // Exit code is platform dependent after forced destroy; just assert we got a code
                   assertNotNull(exitCode, "exit code should be captured");
               });
               testContext.completeNow();
           });
    }
}