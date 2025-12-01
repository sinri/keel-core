package io.github.sinri.keel.core.maids.pleiades;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import org.jetbrains.annotations.NotNull;


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

    public Pleiades(@NotNull Keel keel) {
        super(keel);
    }

    @NotNull
    public static <T> MessageProducer<T> generateMessageProducer(@NotNull Vertx vertx, @NotNull String address) {
        return generateMessageProducer(vertx, address, new DeliveryOptions());
    }

    @NotNull
    public static <T> MessageProducer<T> generateMessageProducer(@NotNull Vertx vertx, @NotNull String address, @NotNull DeliveryOptions deliveryOptions) {
        return vertx.eventBus().sender(address, deliveryOptions);
    }

    @NotNull
    abstract public String getAddress();

    abstract protected void handleMessage(@NotNull Message<T> message);

    @NotNull
    abstract protected Logger buildPleiadesLogger();

    @NotNull
    public final Logger getPleiadesLogger() {
        return pleiadesLogger;
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        this.pleiadesLogger = buildPleiadesLogger();
        consumer = getVertx().eventBus().consumer(getAddress(), this::handleMessage);
        return Future.succeededFuture();
    }

    @Override
    protected @NotNull Future<Void> stopVerticle() {
        if (consumer != null) {
            return consumer.unregister();
        }
        return Future.succeededFuture();
    }
}
