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
import org.jspecify.annotations.NullMarked;

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
@NullMarked
public class CronWatchman extends WatchmanImpl {
    private final WatchmanEventHandler handler;
    private final Function<String, Future<Void>> cronTabUpdateStartup;
    private final LoggerFactory loggerFactory;

    protected CronWatchman(
            Keel keel,
            String watchmanName,
            Function<String, Future<Void>> cronTabUpdateStartup,
            LoggerFactory loggerFactory
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

    public static Future<String> deploy(
            Keel keel,
            String watchmanName,
            Function<String, Future<Void>> cronTabUpdateStartup,
            LoggerFactory loggerFactory
    ) {
        CronWatchman keelCronWatchman = new CronWatchman(keel, watchmanName, cronTabUpdateStartup, loggerFactory);
        return keel.getVertx()
                   .deployVerticle(keelCronWatchman, new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }


    private static Future<Void> operateCronTab(Keel keel, String asyncMapName, Supplier<Future<Void>> supplier) {
        return keel.getVertx().sharedData().getLock(asyncMapName)
                   .compose(lock -> supplier.get()
                                            .andThen(ar -> lock.release()));
    }


    public static Future<Void> addCronJobToAsyncMap(
            Keel keel,
            String asyncMapName,
            KeelCronExpression keelCronExpression,
            Class<? extends WatchmanEventHandler> eventHandlerClass
    ) {
        return addCronJobToAsyncMap(keel,
                asyncMapName, keelCronExpression.getRawCronExpression(),
                eventHandlerClass.getName());
    }


    public static Future<Void> addCronJobToAsyncMap(
            Keel keel,
            String asyncMapName,
            String keelCronExpression,
            String eventHandlerClassName
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


    public static Future<Void> replaceAllCronJobToAsyncMap(Keel keel, String asyncMapName, Map<String, List<String>> newMap) {
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


    public static Future<Void> removeCronJobFromAsyncMap(
            Keel keel,
            String asyncMapName,
            KeelCronExpression keelCronExpression,
            Class<? extends WatchmanEventHandler> eventHandlerClass
    ) {
        return removeCronJobFromAsyncMap(keel, asyncMapName, keelCronExpression.getRawCronExpression(),
                eventHandlerClass.getName());
    }


    public static Future<Void> removeCronJobFromAsyncMap(
            Keel keel,
            String asyncMapName,
            String keelCronExpression,
            String eventHandlerClassName
    ) {
        return removeCronJobFromAsyncMap(keel, asyncMapName, keelCronExpression + "@" + eventHandlerClassName);
    }


    public static Future<Void> removeCronJobFromAsyncMap(Keel keel, String asyncMapName, String hash) {
        return operateCronTab(
                keel,
                asyncMapName,
                () -> keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                          .compose(asyncMap -> asyncMap.remove(hash)
                                                       .compose(v -> Future.succeededFuture()))
        );
    }


    public static Future<Void> removeAllCronJobsFromAsyncMap(Keel keel, String asyncMapName) {
        return operateCronTab(
                keel,
                asyncMapName,
                () -> keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                          .compose(AsyncMap::clear));
    }


    public static Future<Map<String, List<String>>> getAllCronJobsFromAsyncMap(Keel keel, String asyncMapName) {
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


    public static Future<List<WatchmanEventHandler>> readAsyncMapForEventHandlers(
            Keel keel,
            String asyncMapName,
            Calendar calendar
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


    private Future<List<WatchmanEventHandler>> readAsyncMapForEventHandlers(Keel keel, Calendar calendar) {
        return readAsyncMapForEventHandlers(keel, eventBusAddress(), calendar);
    }

    @Override
    public final long interval() {
        return 60_000L;
    }

    @Override
    public final WatchmanEventHandler regularHandler() {
        return handler;
    }

    @Override
    protected Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> cronTabUpdateStartup.apply(eventBusAddress()))
              .onFailure(throwable -> {
                  getWatchmanLogger().error(log -> log.exception(throwable));
                  undeployMe();
              });
        return Future.succeededFuture();
    }

    @Override
    protected LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }
}
