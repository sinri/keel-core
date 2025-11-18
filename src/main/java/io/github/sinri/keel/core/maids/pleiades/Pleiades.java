package io.github.sinri.keel.core.maids.pleiades;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;

import static io.github.sinri.keel.base.KeelInstance.Keel;


/**
 * 基于 Vert.x 的 EventBus 和消息机制运作的集群队列实现。
 *
 * @see <a href="https://overlordmaruyama.fandom.com/wiki/Pleiades">Pleiades, of the Great Tomb of Nazarick, in
 *         Overload</a>
 * @since 5.0.0
 */
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

    abstract protected Logger buildPleiadesLogger();

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
