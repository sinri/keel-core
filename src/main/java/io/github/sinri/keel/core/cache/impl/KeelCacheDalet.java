package io.github.sinri.keel.core.cache.impl;

import io.github.sinri.keel.core.verticles.KeelVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * An extension of KeelCacheVet that provides an embedded verticle for periodic full updates and deployment management.
 * <p>
 * The class can be initialized with a callback to fetch the latest data and a time period for regular updates. It
 * deploys
 * a verticle that will periodically update the cache using the provided callback, if any.
 * </p>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
@Deprecated(since = "4.1.5")
public class KeelCacheDalet<K, V> extends KeelCacheVet<K, V> {
    @Nullable
    private final Supplier<Future<Map<K, V>>> fullyUpdateCallback;
    private final long regularUpdatePeriod;
    private String deploymentId;

    public KeelCacheDalet() {
        this(null, 0);
    }

    public KeelCacheDalet(@Nullable Supplier<Future<Map<K, V>>> fullyUpdateCallback, long regularUpdatePeriod) {
        this.fullyUpdateCallback = fullyUpdateCallback;
        this.regularUpdatePeriod = regularUpdatePeriod;
        KeelVerticle.instant(this::startVerticle)
                    .deployMe(new DeploymentOptions()
                            .setThreadingModel(ThreadingModel.WORKER))
                    .compose(deploymentId -> {
                        this.deploymentId = deploymentId;
                        return Future.succeededFuture();
                    });
    }

    public final Future<String> getDeploymentId() {
        return Keel.asyncCallRepeatedly(repeatedlyCallTask -> {
                       if (deploymentId == null) {
                           return Keel.asyncSleep(100L);
                       } else {
                           repeatedlyCallTask.stop();
                           return Future.succeededFuture();
                       }
                   })
                   .compose(done -> {
                       return Future.succeededFuture(deploymentId);
                   });
    }


    private Future<Void> startVerticle() {
        return fullyUpdate()
                .compose(updated -> {
                    if (regularUpdatePeriod() >= 0) {
                        Keel.asyncCallEndlessly(() -> Future.succeededFuture()
                                                            .compose(v -> {
                                                                if (regularUpdatePeriod() <= 0)
                                                                    return Future.succeededFuture();
                                                                else return Keel.asyncSleep(regularUpdatePeriod());
                                                            })
                                                            .compose(v -> fullyUpdate()));
                    }
                    return Future.succeededFuture();
                });
    }

    private Future<Void> fullyUpdate() {
        if (fullyUpdateCallback != null) {
            return fullyUpdateCallback.get()
                                      .compose(map -> {
                                          if (map != null) {
                                              this.replaceAll(map);
                                          }
                                          return Future.succeededFuture();
                                      });
        }
        return Future.succeededFuture();
    }

    /**
     * @return a time period to sleep between regular updates. Use minus number to disable regular update.
     */
    private long regularUpdatePeriod() {
        return regularUpdatePeriod;
    }

    /**
     * Undeploy the embedded verticle and remove all cached entries.
     * <p>After this method be called, the instance should not be used anymore to avoid more IO occurs.
     */
    public Future<Void> undeploy() {
        if (deploymentId != null) {
            return Keel.getVertx().undeploy(this.deploymentId)
                       .eventually(() -> {
                           this.removeAll();
                           return Future.succeededFuture();
                       });
        } else {
            return Future.succeededFuture();
        }
    }
}
