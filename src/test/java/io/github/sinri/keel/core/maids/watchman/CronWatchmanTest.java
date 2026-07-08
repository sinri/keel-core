package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CronWatchmanTest extends KeelJUnit5Test {
    private static final AtomicLong LAST_CRON_WATCHMAN_EVENT = new AtomicLong(-1L);

    @Test
    void addCronJobToAsyncMapCanBeReadBack(VertxTestContext testContext) {
        String asyncMapName = "CronWatchmanTest-" + UUID.randomUUID();
        Calendar calendar = Calendar.getInstance();

        CronWatchman.addCronJobToAsyncMap(
                            getKeel(),
                            asyncMapName,
                            "* * * * *",
                            FirstTestHandler.class.getName()
                    )
                    .compose(v -> CronWatchman.readAsyncMapForEventHandlers(getKeel(), asyncMapName, calendar))
                    .onComplete(testContext.succeeding(handlers -> {
                        testContext.verify(() -> {
                            assertEquals(1, handlers.size());
                            assertInstanceOf(FirstTestHandler.class, handlers.get(0));
                        });
                        testContext.completeNow();
                    }));
    }

    @Test
    void replaceAllCronJobToAsyncMapReplacesOldJobs(VertxTestContext testContext) {
        String asyncMapName = "CronWatchmanTest-" + UUID.randomUUID();
        Calendar calendar = Calendar.getInstance();

        CronWatchman.addCronJobToAsyncMap(
                            getKeel(),
                            asyncMapName,
                            "* * * * *",
                            FirstTestHandler.class.getName()
                    )
                    .compose(v -> CronWatchman.replaceAllCronJobToAsyncMap(
                            getKeel(),
                            asyncMapName,
                            Map.of("* * * * *", List.of(SecondTestHandler.class.getName()))
                    ))
                    .compose(v -> CronWatchman.readAsyncMapForEventHandlers(getKeel(), asyncMapName, calendar))
                    .onComplete(testContext.succeeding(handlers -> {
                        testContext.verify(() -> {
                            assertEquals(1, handlers.size());
                            assertInstanceOf(SecondTestHandler.class, handlers.get(0));
                        });
                        testContext.completeNow();
                    }));
    }

    @Test
    void readAsyncMapSkipsInvalidEntries(VertxTestContext testContext) {
        String asyncMapName = "CronWatchmanTest-" + UUID.randomUUID();
        Calendar calendar = Calendar.getInstance();

        getKeel().sharedData()
                 .getAsyncMap(asyncMapName)
                 .compose(asyncMap -> Future.all(
                         asyncMap.put(
                                 "bad-cron",
                                 new JsonObject()
                                         .put("cron", "bad-cron")
                                         .put("handler", FirstTestHandler.class.getName())
                         ),
                         asyncMap.put(
                                 "bad-handler",
                                 new JsonObject()
                                         .put("cron", "* * * * *")
                                         .put("handler", String.class.getName())
                         ),
                         asyncMap.put(
                                 "* * * * *@" + SecondTestHandler.class.getName(),
                                 new JsonObject()
                                         .put("cron", "* * * * *")
                                         .put("handler", SecondTestHandler.class.getName())
                         )
                 ))
                 .compose(v -> CronWatchman.readAsyncMapForEventHandlers(getKeel(), asyncMapName, calendar))
                 .onComplete(testContext.succeeding(handlers -> {
                     testContext.verify(() -> {
                         assertEquals(1, handlers.size());
                         assertInstanceOf(SecondTestHandler.class, handlers.get(0));
                     });
                     testContext.completeNow();
                 }));
    }

    @Test
    @Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
    void deployedCronWatchmanRunsScheduledCronJob(VertxTestContext testContext) {
        LAST_CRON_WATCHMAN_EVENT.set(-1L);
        String watchmanName = "CronWatchmanTest-" + UUID.randomUUID();

        CronWatchman cronWatchman = new CronWatchman(
                watchmanName,
                asyncMapName -> CronWatchman.addCronJobToAsyncMap(
                        getKeel(),
                        asyncMapName,
                        "* * * * *",
                        TriggeredTestHandler.class.getName()
                )
        );

        getKeel().deployVerticle(cronWatchman, new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                 .compose(deploymentId -> {
                     return waitUntilCronWatchmanEventHandled(130)
                             .eventually(() -> getKeel().undeploy(deploymentId));
                 })
                 .onComplete(testContext.succeeding(v -> {
                     testContext.verify(() -> assertNotEquals(-1L, LAST_CRON_WATCHMAN_EVENT.get()));
                     testContext.completeNow();
                 }));
    }

    public static class FirstTestHandler implements WatchmanEventHandler {
        @Override
        public void handle(Long event) {
        }
    }

    public static class SecondTestHandler implements WatchmanEventHandler {
        @Override
        public void handle(Long event) {
        }
    }

    public static class TriggeredTestHandler implements WatchmanEventHandler {
        @Override
        public void handle(Long event) {
            LAST_CRON_WATCHMAN_EVENT.set(event);
        }
    }

    private Future<Void> waitUntilCronWatchmanEventHandled(int attempts) {
        if (LAST_CRON_WATCHMAN_EVENT.get() != -1L) {
            return Future.succeededFuture();
        }
        if (attempts <= 0) {
            return Future.failedFuture("CronWatchman did not run the scheduled cron job");
        }
        return getKeel().asyncSleep(500L)
                        .compose(v -> waitUntilCronWatchmanEventHandled(attempts - 1));
    }
}
