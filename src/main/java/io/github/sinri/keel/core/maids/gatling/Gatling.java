package io.github.sinri.keel.core.maids.gatling;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.Counter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


/**
 * 多管加特林。
 * <p>
 * 一个在 Vertx 集群中并行处理任务的服务。
 *
 * @since 5.0.0
 */
@NullMarked
abstract public class Gatling extends KeelVerticleBase {
    private final GatlingOptions options;
    private final AtomicInteger barrelUsed = new AtomicInteger(0);
    private final LateObject<Logger> lateGatlingLogger = new LateObject<>();

    private Gatling(GatlingOptions options) {
        super();
        this.options = options;
    }

    protected Future<Void> rest() {
        long actualRestInterval = new Random().nextLong(Math.toIntExact(options.getAverageRestInterval() / 2));
        actualRestInterval += options.getAverageRestInterval();
        return asyncSleep(actualRestInterval);
    }

    abstract protected Logger buildGatlingLogger();

    public Logger getGatlingLogger() {
        return lateGatlingLogger.get();
    }

    @Override
    protected Future<Void> startVerticle() {
        this.lateGatlingLogger.set(buildGatlingLogger());
        barrelUsed.set(0);
        asyncCallRepeatedly(routineResult -> fireOnce());
        return Future.succeededFuture();
    }

    private Future<Void> fireOnce() {
        if (barrelUsed.get() >= options.getBarrels()) {
            getGatlingLogger().debug(r -> r.message("BARREL FULL"));
            return rest();
        }
        return Future.succeededFuture()
                     .compose(v -> loadOneBullet())
                     .compose(bullet -> {
                         if (bullet == null) {
                             return rest();
                         }

                         barrelUsed.incrementAndGet();

                         fireBullet(bullet, firedAR -> {
                             if (firedAR.failed()) {
                                 getGatlingLogger().error(log -> log.exception(firedAR.cause())
                                                                    .message("BULLET FIRED ERROR"));
                             } else {
                                 getGatlingLogger().info(r -> r.message("BULLET FIRED DONE"));
                             }
                             barrelUsed.decrementAndGet();
                         });

                         return asyncSleep(10L);
                     })
                     .recover(throwable -> {
                         getGatlingLogger().error(log -> log.exception(throwable).message("FAILED TO LOAD BULLET"));
                         return rest();
                     });
    }

    /**
     * 基于 Vertx 集群共享锁机制为当前运行实例寻找可执行的任务。
     *
     * @return 异步找到的可执行任务；为 null 时表示当前没有可执行的任务
     */
    private Future<@Nullable Bullet> loadOneBullet() {
        return getVertx().sharedData()
                         .getLock("KeelGatling-%s-Load".formatted(this.options.getGatlingName()))
                         .compose(lock -> {
                             Supplier<Future<@Nullable Bullet>> bulletLoader = this.options.getBulletLoader();
                             return bulletLoader.get()
                                                .andThen(ar -> lock.release())
                                                .compose(Future::succeededFuture);
                         });
    }

    protected Future<Void> requireExclusiveLocksOfBullet(Bullet bullet) {
        if (!bullet.exclusiveLockSet().isEmpty()) {
            AtomicBoolean blocked = new AtomicBoolean(false);
            return asyncCallIteratively(
                    bullet.exclusiveLockSet(),
                    (exclusiveLock, task) -> {
                        String exclusiveLockName = "KeelGatling-Bullet-Exclusive-Lock-" + exclusiveLock;
                        return getVertx().sharedData()
                                         .getCounter(exclusiveLockName)
                                         .compose(Counter::incrementAndGet)
                                         .compose(increased -> {
                                             if (increased > 1) {
                                                 blocked.set(true);
                                             }
                                             return Future.succeededFuture();
                                         });
                    }
            )
                    .compose(v -> {
                        if (blocked.get()) {
                            return releaseExclusiveLocksOfBullet(bullet)
                                    .eventually(() -> Future.failedFuture(new Exception("This bullet met Exclusive" +
                                            " Lock Block.")));
                        }
                        return Future.succeededFuture();
                    });
        } else {
            return Future.succeededFuture();
        }
    }

    protected Future<Void> releaseExclusiveLocksOfBullet(Bullet bullet) {
        bullet.exclusiveLockSet();
        if (!bullet.exclusiveLockSet().isEmpty()) {
            return asyncCallIteratively(bullet.exclusiveLockSet(), (exclusiveLock, task) -> {
                String exclusiveLockName = "KeelGatling-Bullet-Exclusive-Lock-" + exclusiveLock;
                return getVertx().sharedData().getCounter(exclusiveLockName)
                                 .compose(counter -> counter.decrementAndGet()
                                                            .compose(x -> Future.succeededFuture()));
            });
        } else {
            return Future.succeededFuture();
        }
    }

    private void fireBullet(Bullet bullet, Handler<AsyncResult<Void>> handler) {
        Promise<Void> promise = Promise.promise();
        Future.succeededFuture()
              .compose(v -> requireExclusiveLocksOfBullet(bullet)
                      .compose(locked -> bullet.fire()
                                               .andThen(fired -> releaseExclusiveLocksOfBullet(bullet)))
              )
              .andThen(firedAR -> bullet.ejectShell(firedAR)
                                        .onComplete(ejected -> {
                                            if (firedAR.failed()) {
                                                promise.fail(firedAR.cause());
                                            } else {
                                                promise.complete();
                                            }
                                        })
              );

        promise.future().andThen(handler);
    }

}
