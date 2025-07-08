package io.github.sinri.keel.facade.async;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
/**
 * @since 4.1.0
 */
interface KeelAsyncMixinCore {
    /**
     * Asynchronously sleeps for a specified amount of time.
     *
     * @param time the duration to sleep in milliseconds
     * @return a Future that completes after the specified time has elapsed
     */
    default Future<Void> asyncSleep(long time) {
        return asyncSleep(time, null);
    }

    /**
     * Asynchronously sleeps for a specified amount of time, with an optional
     * interrupter.
     *
     * @param time        the duration to sleep in milliseconds. If less than 1, it
     *                    will be set to 1.
     * @param interrupter an optional Promise that, when completed, will cancel the
     *                    sleep and complete the returned
     *                    Future.
     * @return a Future that completes after the specified time has elapsed, or is
     *         interrupted by the interrupter.
     */
    default Future<Void> asyncSleep(long time, @Nullable Promise<Void> interrupter) {
        Context currentContext = Vertx.currentContext();
        Keel.getLogger().info("<" + Thread.currentThread().getId() + "> asyncSleep start",ctx->ctx
                .put("onCurrentContext",currentContext!=null)
                .put("isOnWorkerContext",currentContext!=null&&currentContext.isWorkerContext())
                .put("isOnEventLoop",currentContext!=null&&currentContext.isEventLoopContext())
        );
        Promise<Void> promise = Promise.promise();
        time = Math.max(1, time);
        long timer_id = Keel.getVertx().setTimer(time, timerID -> {
            Keel.getLogger().info("<" + Thread.currentThread().getId() + "> asyncSleep time up");
            promise.complete();
        });
        Keel.getLogger().info("timer_id:"+timer_id);
        if (interrupter != null) {
            interrupter.future().onSuccess(interrupted -> {
                Keel.getVertx().cancelTimer(timer_id);
                promise.tryComplete();
            });
        }
        var f= promise.future();
        return f;
    }
}
