package io.github.sinri.keel.core.servant.funnel;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


/**
 * 烟囱。
 * <p>
 * 接受各种异步任务逻辑，按照 FIFO 原则依次执行。
 *
 * @since 5.0.0
 */
public class Funnel extends AbstractKeelVerticle {
    /**
     * 休眠中出现新任务时，使用此内寄存的 Promise 唤醒。
     */
    @NotNull
    private final AtomicReference<Promise<Void>> interruptRef;
    @NotNull
    private final Queue<Supplier<Future<Void>>> queue;
    @NotNull
    private final AtomicLong sleepTimeRef;
    @NotNull
    private final Logger funnelLogger;

    public Funnel(@NotNull Keel keel) {
        super(keel);
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
        this.funnelLogger = buildFunnelLogger();
    }

    @NotNull
    private Logger buildFunnelLogger() {
        return getKeel().getLoggerFactory().createLogger("Funnel");
    }

    @NotNull
    protected final Logger getFunnelLogger() {
        return funnelLogger;
    }

    public void setSleepTime(long sleepTime) {
        if (sleepTime <= 1) {
            throw new IllegalArgumentException();
        }
        this.sleepTimeRef.set(sleepTime);
    }

    public void add(@NotNull Supplier<@NotNull Future<Void>> supplier) {
        queue.add(supplier);
        Promise<Void> currentInterrupt = getCurrentInterrupt();
        if (currentInterrupt != null) {
            currentInterrupt.tryComplete();
        }
    }

    @Nullable
    private Promise<Void> getCurrentInterrupt() {
        return this.interruptRef.get();
    }


    @Override
    protected @NotNull Future<Void> startVerticle() {
        getKeel().asyncCallEndlessly(this::executeCircle);
        return Future.succeededFuture();
    }

    @NotNull
    private Future<Void> executeCircle() {
        this.interruptRef.set(null);
        funnelLogger.debug("funnel one circle start");
        return getKeel().asyncCallRepeatedly(routineResult -> {
                         // got one job to do, no matter if done
                         return Future.succeededFuture()
                                      .compose(ready -> {
                                          Supplier<Future<Void>> supplier = queue.poll();
                                          if (supplier == null) {
                                              // no job to do
                                              routineResult.stop();
                                              Supplier<Future<Void>> supplierTemp = Future::succeededFuture;
                                              return Future.succeededFuture(supplierTemp);
                                          } else {
                                              return Future.succeededFuture(supplier);
                                          }
                                      })
                                      .compose(Supplier::get);
                     })
                     .recover(throwable -> {
                         funnelLogger.error(log -> log.exception(throwable));
                         return Future.succeededFuture();
                     })
                     .eventually(() -> {
                         this.interruptRef.set(Promise.promise());
                         return getKeel().asyncSleep(this.sleepTimeRef.get(), getCurrentInterrupt());
                     });
    }
}
