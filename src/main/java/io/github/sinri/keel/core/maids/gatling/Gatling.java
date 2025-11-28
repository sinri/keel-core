package io.github.sinri.keel.core.maids.gatling;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.Counter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 多管加特林。
 * <p>
 * 一个在 Vertx 集群中并行处理任务的服务。
 *
 * @since 5.0.0
 */
abstract public class Gatling extends AbstractKeelVerticle {
    @NotNull
    private final GatlingOptions options;
    @NotNull
    private final AtomicInteger barrelUsed = new AtomicInteger(0);
    @Nullable
    private Logger gatlingLogger;

    private Gatling(@NotNull GatlingOptions options) {
        this.options = options;
    }

    @NotNull
    protected Future<Void> rest() {
        long actualRestInterval = new Random().nextLong(Math.toIntExact(options.getAverageRestInterval() / 2));
        actualRestInterval += options.getAverageRestInterval();
        return Keel.asyncSleep(actualRestInterval);
    }

    @NotNull
    abstract protected Logger buildGatlingLogger();

    @NotNull
    public Logger getGatlingLogger() {
        return Objects.requireNonNull(gatlingLogger);
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        this.gatlingLogger = buildGatlingLogger();
        barrelUsed.set(0);
        Keel.asyncCallRepeatedly(routineResult -> fireOnce());
        return Future.succeededFuture();
    }

    @NotNull
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

                         return Keel.asyncSleep(10L);
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
    @NotNull
    private Future<Bullet> loadOneBullet() {
        return Keel.getVertx().sharedData()
                   .getLock("KeelGatling-%s-Load".formatted(this.options.getGatlingName()))
                   .compose(lock -> this.options.getBulletLoader().get().
                                                andThen(ar -> lock.release()));
    }

    @NotNull
    protected Future<Void> requireExclusiveLocksOfBullet(@NotNull Bullet bullet) {
        if (!bullet.exclusiveLockSet().isEmpty()) {
            AtomicBoolean blocked = new AtomicBoolean(false);
            return Keel.asyncCallIteratively(
                               bullet.exclusiveLockSet(),
                               (exclusiveLock, task) -> {
                                   String exclusiveLockName = "KeelGatling-Bullet-Exclusive-Lock-" + exclusiveLock;
                                   return Keel.getVertx().sharedData()
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

    @NotNull
    protected Future<Void> releaseExclusiveLocksOfBullet(@NotNull Bullet bullet) {
        bullet.exclusiveLockSet();
        if (!bullet.exclusiveLockSet().isEmpty()) {
            return Keel.asyncCallIteratively(bullet.exclusiveLockSet(), (exclusiveLock, task) -> {
                String exclusiveLockName = "KeelGatling-Bullet-Exclusive-Lock-" + exclusiveLock;
                return Keel.getVertx().sharedData().getCounter(exclusiveLockName)
                           .compose(counter -> counter.decrementAndGet()
                                                      .compose(x -> Future.succeededFuture()));
            });
        } else {
            return Future.succeededFuture();
        }
    }

    private void fireBullet(@NotNull Bullet bullet, @NotNull Handler<AsyncResult<Void>> handler) {
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
