package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class CronWatchmanTest extends KeelJUnit5Test {
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
}
