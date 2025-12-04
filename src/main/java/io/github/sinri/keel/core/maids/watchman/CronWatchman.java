package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.core.servant.sundial.Sundial;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * 基于Cron的更夫实现。
 * <p>
 * 本类实现类似单节点下的{@link Sundial}，在集群内实现 crontab 任务调度。
 *
 * @since 5.0.0
 */
public class CronWatchman extends WatchmanImpl {
    @NotNull
    private final WatchmanEventHandler handler;
    @NotNull
    private final Function<String, Future<Void>> cronTabUpdateStartup;
    @NotNull
    private final LoggerFactory loggerFactory;

    protected CronWatchman(
            @NotNull Keel keel,
            @NotNull String watchmanName,
            @NotNull Function<String, Future<Void>> cronTabUpdateStartup,
            @NotNull LoggerFactory loggerFactory
    ) {
        super(keel, watchmanName);
        this.loggerFactory = loggerFactory;
        this.handler = now -> {
            Calendar calendar = new Calendar
                    .Builder()
                    .setInstant(now)
                    .build();

            readAsyncMapForEventHandlers(keel, calendar)
                    .onSuccess(list -> list.forEach(x -> x.handle(now)))
                    .onFailure(throwable -> getWatchmanLogger().error(x -> x.exception(throwable)));
        };
        this.cronTabUpdateStartup = cronTabUpdateStartup;
    }

    @NotNull
    public static Future<String> deploy(
            @NotNull Keel keel,
            @NotNull String watchmanName,
            @NotNull Function<String, Future<Void>> cronTabUpdateStartup,
            @NotNull LoggerFactory loggerFactory
    ) {
        CronWatchman keelCronWatchman = new CronWatchman(keel, watchmanName, cronTabUpdateStartup, loggerFactory);
        return keel.getVertx()
                   .deployVerticle(keelCronWatchman, new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }

    @NotNull
    private static Future<Void> operateCronTab(@NotNull Keel keel, @NotNull String asyncMapName, @NotNull Supplier<Future<Void>> supplier) {
        return keel.getVertx().sharedData().getLock(asyncMapName)
                   .compose(lock -> supplier.get()
                                            .andThen(ar -> lock.release()));
    }

    @NotNull
    public static Future<Void> addCronJobToAsyncMap(
            @NotNull Keel keel,
            @NotNull String asyncMapName,
            @NotNull KeelCronExpression keelCronExpression,
            @NotNull Class<? extends WatchmanEventHandler> eventHandlerClass
    ) {
        return addCronJobToAsyncMap(keel,
                asyncMapName, keelCronExpression.getRawCronExpression(),
                eventHandlerClass.getName());
    }

    @NotNull
    public static Future<Void> addCronJobToAsyncMap(
            @NotNull Keel keel,
            @NotNull String asyncMapName,
            @NotNull String keelCronExpression,
            @NotNull String eventHandlerClassName
    ) {
        return operateCronTab(
                keel,
                asyncMapName,
                () -> keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                          .compose(asyncMap -> asyncMap.put(
                                  keelCronExpression + "@" + eventHandlerClassName,
                                  new JsonObject()
                                          .put("cron", keelCronExpression)
                                          .put("handler", eventHandlerClassName)
                          ))
        );
    }

    @NotNull
    public static Future<Void> replaceAllCronJobToAsyncMap(@NotNull Keel keel, @NotNull String asyncMapName, @NotNull Map<String, List<String>> newMap) {
        Map<Object, JsonObject> hashMap = new HashMap<>();
        newMap.forEach((cronExpression, classes) -> classes.forEach(classItem -> {
            String hash = cronExpression + "@" + classItem;

            hashMap.put(hash, new JsonObject()
                    .put("cron", cronExpression)
                    .put("handler", classItem));
        }));
        return operateCronTab(
                keel,
                asyncMapName,
                () -> keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                          .compose(asyncMap -> asyncMap.keys()
                                                       .compose(oldKeys -> {
                                                           Set<Object> newKeys = hashMap.keySet();

                                                           HashSet<Object> toDeleteHashSet =
                                                                   new HashSet<>(oldKeys);
                                                           toDeleteHashSet.removeAll(newKeys);

                                                           HashSet<Object> toAddHashSet =
                                                                   new HashSet<>(newKeys);
                                                           toAddHashSet.removeAll(oldKeys);

                                                           return Future.all(
                                                                   keel.asyncCallIteratively(
                                                                           toDeleteHashSet,
                                                                           (hash, task) -> asyncMap.remove(String.valueOf(hash))
                                                                                                   .compose(v -> Future.succeededFuture())),
                                                                   keel.asyncCallIteratively(
                                                                           toAddHashSet,
                                                                           (hash, task) -> asyncMap.put(hash, hashMap.get(hash))
                                                                                                   .compose(v -> Future.succeededFuture()))
                                                           );
                                                       })
                                                       .compose(v -> Future.succeededFuture()))
        );
    }

    @NotNull
    public static Future<Void> removeCronJobFromAsyncMap(
            @NotNull Keel keel,
            @NotNull String asyncMapName,
            @NotNull KeelCronExpression keelCronExpression,
            @NotNull Class<? extends WatchmanEventHandler> eventHandlerClass
    ) {
        return removeCronJobFromAsyncMap(keel, asyncMapName, keelCronExpression.getRawCronExpression(),
                eventHandlerClass.getName());
    }

    @NotNull
    public static Future<Void> removeCronJobFromAsyncMap(
            @NotNull Keel keel,
            @NotNull String asyncMapName,
            @NotNull String keelCronExpression,
            @NotNull String eventHandlerClassName
    ) {
        return removeCronJobFromAsyncMap(keel, asyncMapName, keelCronExpression + "@" + eventHandlerClassName);
    }

    @NotNull
    public static Future<Void> removeCronJobFromAsyncMap(@NotNull Keel keel, @NotNull String asyncMapName, @NotNull String hash) {
        return operateCronTab(
                keel,
                asyncMapName,
                () -> keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                          .compose(asyncMap -> asyncMap.remove(hash)
                                                       .compose(v -> Future.succeededFuture()))
        );
    }

    @NotNull
    public static Future<Void> removeAllCronJobsFromAsyncMap(@NotNull Keel keel, @NotNull String asyncMapName) {
        return operateCronTab(
                keel,
                asyncMapName,
                () -> keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                          .compose(AsyncMap::clear));
    }

    @NotNull
    public static Future<Map<String, List<String>>> getAllCronJobsFromAsyncMap(@NotNull Keel keel, @NotNull String asyncMapName) {
        return keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                   .compose(AsyncMap::entries)
                   .compose(entries -> {
                       Map<String, List<String>> map = new HashMap<>();
                       entries.forEach((hash, v) -> {
                           JsonObject jsonObject = (JsonObject) v;
                           String cron = jsonObject.getString("cron");
                           String handler = jsonObject.getString("handler");
                           map.computeIfAbsent(cron, s -> new ArrayList<>())
                              .add(handler);
                       });
                       return Future.succeededFuture(map);
                   });
    }

    @NotNull
    public static Future<List<WatchmanEventHandler>> readAsyncMapForEventHandlers(
            @NotNull Keel keel,
            @NotNull String asyncMapName,
            @NotNull Calendar calendar
    ) {
        List<WatchmanEventHandler> list = new ArrayList<>();
        return keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                   .compose(AsyncMap::entries)
                   .compose(entries -> {
                       entries.forEach((k, v) -> {
                           String cronExpression = String.valueOf(k);
                           if (new KeelCronExpression(cronExpression).match(calendar)) {
                               JsonArray eventHandlerClassNameArray = new JsonArray(String.valueOf(v));
                               eventHandlerClassNameArray.forEach(eventHandlerClassName -> {
                                   try {
                                       Class<?> aClass = Class.forName(String.valueOf(eventHandlerClassName));
                                       if (WatchmanEventHandler.class.isAssignableFrom(aClass)) {
                                           WatchmanEventHandler eventHandler =
                                                   (WatchmanEventHandler) aClass.getConstructor()
                                                                                .newInstance();
                                           list.add(eventHandler);
                                       }
                                   } catch (Throwable e) {
                                       //Keel.outputLogger().exception(e);
                                       System.out.println("EXCEPTION: " + e);
                                   }
                               });
                           }
                       });
                       return Future.succeededFuture(list);
                   });
    }

    @NotNull
    private Future<List<WatchmanEventHandler>> readAsyncMapForEventHandlers(@NotNull Keel keel, @NotNull Calendar calendar) {
        return readAsyncMapForEventHandlers(keel, eventBusAddress(), calendar);
    }

    @Override
    public final long interval() {
        return 60_000L;
    }

    @Override
    public final @NotNull WatchmanEventHandler regularHandler() {
        return handler;
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> cronTabUpdateStartup.apply(eventBusAddress()))
              .onFailure(throwable -> {
                  getWatchmanLogger().error(log -> log.exception(throwable));
                  undeployMe();
              });
        return Future.succeededFuture();
    }

    @Override
    @NotNull
    protected LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }
}
