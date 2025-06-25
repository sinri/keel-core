package io.github.sinri.keel.core.maids.pleiades;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * A queue impl based on EventBus and Messages. Pleiades (戦闘メイド) is the combat maid squad of the Great Tomb of Nazarick
 * [REF: Overlord].
 *
 * @since 3.2.19
 */
@TechnicalPreview(since = "3.2.19")
public abstract class Pleiades<T> extends KeelVerticleImpl {
    private MessageConsumer<T> consumer;
    private KeelIssueRecorder<KeelEventLog> pleiadesLogger;

    public static <T> MessageProducer<T> generateMessageProducer(String address) {
        return generateMessageProducer(address, new DeliveryOptions());
    }

    public static <T> MessageProducer<T> generateMessageProducer(String address, DeliveryOptions deliveryOptions) {
        return Keel.getVertx().eventBus().sender(address, deliveryOptions);
    }

    abstract public String getAddress();

    abstract protected void handleMessage(Message<T> message);

    /**
     * @since 4.0.2
     */
    abstract protected KeelIssueRecorder<KeelEventLog> buildPleiadesLogger();

    /**
     * @since 4.0.2
     */
    public KeelIssueRecorder<KeelEventLog> getPleiadesLogger() {
        return pleiadesLogger;
    }

    @Override
    protected Future<Void> startVerticle() {
        this.pleiadesLogger = buildPleiadesLogger();
        consumer = Keel.getVertx().eventBus().consumer(getAddress(), this::handleMessage);
        return Future.succeededFuture();
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (consumer != null) {
            consumer.unregister()
                    .onSuccess(unused -> stopPromise.complete())
                    .onFailure(stopPromise::fail);
        }
    }
}
