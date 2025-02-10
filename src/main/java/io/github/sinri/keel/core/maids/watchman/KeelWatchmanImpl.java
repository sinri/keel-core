package io.github.sinri.keel.core.maids.watchman;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.shareddata.Lock;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.9.3
 */
abstract class KeelWatchmanImpl extends KeelVerticleImpl<KeelEventLog> implements KeelWatchman<KeelEventLog> {
    private final String watchmanName;
    private final KeelIssueRecordCenter issueRecordCenter;
    private MessageConsumer<Long> consumer;

    public KeelWatchmanImpl(String watchmanName, KeelIssueRecordCenter issueRecordCenter) {
        this.watchmanName = watchmanName;
        this.issueRecordCenter = issueRecordCenter;
    }

    @Override
    public String watchmanName() {
        return this.watchmanName;
    }

    protected String eventBusAddress() {
        return this.getClass().getName() + ":" + watchmanName();
    }

    @Override
    protected Future<Void> startVerticle() {
        this.consumer = Keel.getVertx().eventBus().consumer(eventBusAddress());
        this.consumer.handler(this::consumeHandleMassage);
        this.consumer.exceptionHandler(throwable -> getIssueRecorder()
                .exception(throwable, r -> r.message(watchmanName() + " ERROR")));

        try {
            // @since 2.9.3 强行拟合HH:MM:SS.000-200
            long x = 1000 - System.currentTimeMillis() % 1_000;
            if (x < 800) {
                Thread.sleep(x);
            }
        } catch (Exception ignore) {
            // 拟合不了拉倒
        }
        Keel.getVertx().setPeriodic(
                interval(),
                timerID -> Keel.getVertx().eventBus()
                               .send(eventBusAddress(), System.currentTimeMillis()));

        return Future.succeededFuture();
    }

    protected void consumeHandleMassage(Message<Long> message) {
        Long timestamp = message.body();
        getIssueRecorder().debug(r -> r.message(watchmanName() + " TRIGGERED FOR " + timestamp));

        long x = timestamp / interval();
        Keel.getVertx().sharedData()
            .getLockWithTimeout(eventBusAddress() + "@" + x, Math.min(3_000L, interval() - 1), lockAR -> {
                if (lockAR.failed()) {
                    getIssueRecorder().warning(r -> r.message("LOCK ACQUIRE FAILED FOR " + timestamp + " i.e. " + x));
                } else {
                    Lock lock = lockAR.result();
                    getIssueRecorder().info(r -> r.message("LOCK ACQUIRED FOR " + timestamp + " i.e. " + x));
                    regularHandler().handle(timestamp);
                    Keel.getVertx().setTimer(interval(), timerID -> {
                        lock.release();
                        getIssueRecorder().info(r -> r.message("LOCK RELEASED FOR " + timestamp + " i.e. " + x));
                    });
                }
            });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        consumer.unregister();
        stopPromise.complete();
    }

    /**
     * @since 4.0.2
     */
    protected final KeelIssueRecordCenter getIssueRecordCenter() {
        return issueRecordCenter;
    }

    /**
     * @since 4.0.2
     */
    @Nonnull
    @Override
    protected KeelIssueRecorder<KeelEventLog> buildIssueRecorder() {
        return getIssueRecordCenter().generateIssueRecorder("Watchman", KeelEventLog::new);
    }
}
