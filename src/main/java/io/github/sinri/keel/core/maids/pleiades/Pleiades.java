package io.github.sinri.keel.core.maids.pleiades;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * A queue impl based on EventBus and Messages. Pleiades (戦闘メイド) is the combat maid squad of the Great Tomb of Nazarick
 * [REF: Overlord].
 *
 * @since 3.2.19
 */
@TechnicalPreview(since = "3.2.19")
public abstract class Pleiades<T> extends AbstractKeelVerticle {
    private MessageConsumer<T> consumer;
    private Logger pleiadesLogger;

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
    abstract protected Logger buildPleiadesLogger();

    /**
     * @since 4.0.2
     */
    public Logger getPleiadesLogger() {
        return pleiadesLogger;
    }

    @Override
    protected Future<Void> startVerticle() {
        this.pleiadesLogger = buildPleiadesLogger();
        consumer = Keel.getVertx().eventBus().consumer(getAddress(), this::handleMessage);
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> stopVerticle() {
        if (consumer != null) {
            return consumer.unregister();
        }
        return Future.succeededFuture();
    }
}
