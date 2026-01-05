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
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;


/**
 * 基于 Vert.x 的 EventBus 和消息机制运作的集群队列实现。
 *
 * @see <a href="https://overlordmaruyama.fandom.com/wiki/Pleiades">Pleiades, of the Great Tomb of Nazarick, in
 *         Overload</a>
 * @since 5.0.0
 */
@NullMarked
public abstract class Pleiades<T> extends AbstractKeelVerticle {
    private @Nullable MessageConsumer<T> consumer;
    private @Nullable Logger pleiadesLogger;

    public Pleiades(Keel keel) {
        super(keel);
    }

    public static <T> MessageProducer<T> generateMessageProducer(Vertx vertx, String address) {
        return generateMessageProducer(vertx, address, new DeliveryOptions());
    }

    public static <T> MessageProducer<T> generateMessageProducer(Vertx vertx, String address, DeliveryOptions deliveryOptions) {
        return vertx.eventBus().sender(address, deliveryOptions);
    }

    abstract public String getAddress();

    abstract protected void handleMessage(Message<T> message);

    abstract protected Logger buildPleiadesLogger();

    public final Logger getPleiadesLogger() {
        return Objects.requireNonNull(pleiadesLogger);
    }

    @Override
    protected Future<Void> startVerticle() {
        this.pleiadesLogger = buildPleiadesLogger();
        consumer = getVertx().eventBus().consumer(getAddress(), this::handleMessage);
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
