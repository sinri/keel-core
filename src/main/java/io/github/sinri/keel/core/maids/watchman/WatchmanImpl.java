package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.shareddata.Lock;
import org.jspecify.annotations.NullMarked;


/**
 * 更夫的基础实现，基于Verticle。
 *
 * @since 5.0.0
 */
@NullMarked
abstract class WatchmanImpl extends KeelVerticleBase implements Watchman {
    private final String watchmanName;
    private final LateObject<Logger> lateWatchmanLogger = new LateObject<>();
    private final LateObject<MessageConsumer<Long>> lateConsumer = new LateObject<>();

    public WatchmanImpl(String watchmanName) {
        super();
        this.watchmanName = watchmanName;
    }

    @Override
    public String watchmanName() {
        return this.watchmanName;
    }

    protected String eventBusAddress() {
        return "%s:%s".formatted(this.getClass().getName(), watchmanName());
    }

    @Override
    protected Future<Void> startVerticle() {
        this.lateWatchmanLogger.set(this.buildWatchmanLogger());

        this.lateConsumer.ensure(() -> getVertx().eventBus().consumer(eventBusAddress()))
                         .handler(this::consumeHandleMassage)
                         .exceptionHandler(throwable -> getWatchmanLogger()
                                 .error(log -> log.message(watchmanName() + " ERROR").exception(throwable))
                         );

        try {
            // 强行拟合HH:MM:SS.000-200
            long x = 1000 - System.currentTimeMillis() % 1_000;
            if (x < 800) {
                Thread.sleep(x);
            }
        } catch (Exception ignore) {
            // 拟合不了拉倒
        }
        getVertx().setPeriodic(
                interval(),
                timerID -> getVertx().eventBus()
                                     .send(eventBusAddress(), System.currentTimeMillis())
        );

        return Future.succeededFuture();
    }

    protected void consumeHandleMassage(Message<Long> message) {
        Long timestamp = message.body();
        getWatchmanLogger().debug(r -> r.message(watchmanName() + " TRIGGERED FOR " + timestamp));

        long x = timestamp / interval();
        getVertx().sharedData().getLockWithTimeout(eventBusAddress() + "@" + x, Math.min(3_000L, interval() - 1))
                  .onComplete(lockAR -> {
                      if (lockAR.failed()) {
                          getWatchmanLogger().warning(r -> r.message("LOCK ACQUIRE FAILED FOR " + timestamp + " i.e. " + x));
                      } else {
                          Lock lock = lockAR.result();
                          getWatchmanLogger().info(r -> r.message("LOCK ACQUIRED FOR " + timestamp + " i.e. " + x));
                          regularHandler().handle(timestamp);
                          getVertx().setTimer(interval(), timerID -> {
                              lock.release();
                              getWatchmanLogger().info(r -> r.message("LOCK RELEASED FOR " + timestamp + " i.e. " + x));
                          });
                      }
                  });
    }

    @Override
    protected Future<Void> stopVerticle() {
        if (lateConsumer.isInitialized()) {
            lateConsumer.get().unregister();
        }
        return Future.succeededFuture();
    }

    protected abstract LoggerFactory getLoggerFactory();

    protected Logger buildWatchmanLogger() {
        return getLoggerFactory().createLogger("Watchman");
    }

    public final Logger getWatchmanLogger() {
        return lateWatchmanLogger.get();
    }
}
