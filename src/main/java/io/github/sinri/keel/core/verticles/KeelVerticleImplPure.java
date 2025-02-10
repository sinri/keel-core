package io.github.sinri.keel.core.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

/**
 * @since 3.2.0
 * @deprecated use {@link KeelVerticleImpl} or {@link KeelInstantVerticle} instead.
 */
@Deprecated(since = "4.0.0")
public abstract class KeelVerticleImplPure extends AbstractVerticle implements KeelVerticle {
    @Override
    public final void start(Promise<Void> startPromise) {
        this.startAsPureKeelVerticle(startPromise);
    }

    @Override
    public final void start() {
    }

    abstract protected void startAsPureKeelVerticle(Promise<Void> startPromise);

    /**
     * Just do nothing.
     */
    @Override
    public final void stop() {
    }

    @Override
    public final void stop(Promise<Void> stopPromise) {
        stop();
        stopAsKeelVerticle(stopPromise);
    }

    /**
     * @since 4.0.0
     */
    protected void stopAsKeelVerticle(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}
