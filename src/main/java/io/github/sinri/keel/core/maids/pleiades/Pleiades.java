package io.github.sinri.keel.core.maids.pleiades;

import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import org.jspecify.annotations.NullMarked;


/**
 * 基于 Vert.x 的 EventBus 和消息机制运作的集群队列实现。
 *
 * @see <a href="https://overlordmaruyama.fandom.com/wiki/Pleiades">Pleiades, of the Great Tomb of Nazarick, in
 *         Overload</a>
 * @since 5.0.0
 */
@NullMarked
public abstract class Pleiades<T> extends KeelVerticleBase {
    private final LateObject<Logger> latePleiadesLogger = new LateObject<>();
    private final LateObject<MessageConsumer<T>> lateConsumer = new LateObject<>();

    public Pleiades() {
        super();
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
        //return Objects.requireNonNull(pleiadesLogger);
        return latePleiadesLogger.get();
    }

    @Override
    protected Future<Void> startVerticle() {
        this.latePleiadesLogger.set(buildPleiadesLogger());
        lateConsumer.set(getKeel().eventBus().consumer(getAddress(), this::handleMessage));
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> stopVerticle() {
        if (lateConsumer.isInitialized()) {
            return lateConsumer.get().unregister();
        }
        return Future.succeededFuture();
    }
}
