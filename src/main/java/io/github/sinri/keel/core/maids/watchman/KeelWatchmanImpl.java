package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.shareddata.Lock;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 更夫的基础实现，基于Verticle。
 *
 * @since 5.0.0
 */
abstract class KeelWatchmanImpl extends AbstractKeelVerticle implements KeelWatchman {
    @NotNull
    private final String watchmanName;
    private MessageConsumer<Long> consumer;
    private Logger watchmanLogger;

    public KeelWatchmanImpl(@NotNull String watchmanName) {
        this.watchmanName = watchmanName;
    }

    @Override
    @NotNull
    public String watchmanName() {
        return this.watchmanName;
    }

    protected String eventBusAddress() {
        return "%s:%s".formatted(this.getClass().getName(), watchmanName());
    }

    @Override
    protected Future<Void> startVerticle() {
        this.watchmanLogger = this.buildWatchmanLogger();

        this.consumer = Keel.getVertx().eventBus().consumer(eventBusAddress());
        this.consumer.handler(this::consumeHandleMassage);
        this.consumer.exceptionHandler(throwable -> getWatchmanLogger()
                .exception(throwable, watchmanName() + " ERROR")
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
        Keel.getVertx().setPeriodic(interval(), timerID -> Keel.getVertx().eventBus()
                                                               .send(eventBusAddress(), System.currentTimeMillis()));

        return Future.succeededFuture();
    }

    protected void consumeHandleMassage(Message<Long> message) {
        Long timestamp = message.body();
        getWatchmanLogger().debug(r -> r.message(watchmanName() + " TRIGGERED FOR " + timestamp));

        long x = timestamp / interval();
        Keel.getVertx().sharedData().getLockWithTimeout(eventBusAddress() + "@" + x, Math.min(3_000L, interval() - 1))
            .onComplete(lockAR -> {
                if (lockAR.failed()) {
                    getWatchmanLogger().warning(r -> r.message("LOCK ACQUIRE FAILED FOR " + timestamp + " i.e. " + x));
                } else {
                    Lock lock = lockAR.result();
                    getWatchmanLogger().info(r -> r.message("LOCK ACQUIRED FOR " + timestamp + " i.e. " + x));
                    regularHandler().handle(timestamp);
                    Keel.getVertx().setTimer(interval(), timerID -> {
                        lock.release();
                        getWatchmanLogger().info(r -> r.message("LOCK RELEASED FOR " + timestamp + " i.e. " + x));
                    });
                }
            });
    }

    @Override
    protected Future<Void> stopVerticle() {
        consumer.unregister();
        return Future.succeededFuture();
    }

    @NotNull
    protected abstract LoggerFactory getLoggerFactory();

    @NotNull
    protected Logger buildWatchmanLogger() {
        return getLoggerFactory().createLogger("Watchman");
    }

    @NotNull
    public Logger getWatchmanLogger() {
        return Objects.requireNonNull(watchmanLogger);
    }
}
