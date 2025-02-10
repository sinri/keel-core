package io.github.sinri.keel.core.maids.pleiades;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.event.KeelEventLog;
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
public abstract class Pleiades<T> extends KeelVerticleImpl<KeelEventLog> {
    private MessageConsumer<T> consumer;

    public static <T> MessageProducer<T> generateMessageProducer(String address) {
        return generateMessageProducer(address, new DeliveryOptions());
    }

    public static <T> MessageProducer<T> generateMessageProducer(String address, DeliveryOptions deliveryOptions) {
        return Keel.getVertx().eventBus().sender(address, deliveryOptions);
    }

    abstract public String getAddress();

    abstract protected void handleMessage(Message<T> message);

    @Override
    protected Future<Void> startVerticle() {
        consumer = Keel.getVertx().eventBus().consumer(getAddress(), this::handleMessage);
        return Future.succeededFuture();
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if (consumer != null) {
            consumer.unregister()
                    .onSuccess(unused -> stopPromise.complete())
                    .onFailure(stopPromise::fail);
        }
    }
}
