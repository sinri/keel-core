package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
abstract public class KeelVerticleImplWithEventLogger extends AbstractVerticle implements KeelVerticle {
    private @Nonnull KeelEventLogger logger;

    public KeelVerticleImplWithEventLogger() {
        super();
        this.logger = buildEventLogger();
    }

    @Nonnull
    public KeelEventLogger getLogger() {
        return logger;
    }

    abstract protected KeelEventLogger buildEventLogger();

    @Override
    public final void start(Promise<Void> startPromise) {
        this.logger = buildEventLogger();
        startAsKeelVerticle(startPromise);
    }

    /**
     * Just do one thing: build event logger.
     */
    @Override
    public final void start() {
        this.logger = buildEventLogger();
        this.startAsKeelVerticle();
    }

    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        startAsKeelVerticle();
        startPromise.complete();
    }

    @Deprecated(since = "3.2.19")
    protected void startAsKeelVerticle() {
        // do nothing
    }

    /**
     * Just do nothing.
     */
    @Override
    public final void stop() {
    }

    @Override
    public final void stop(Promise<Void> stopPromise) throws Exception {
        stop();
        stopAsKeelVerticle(stopPromise);
    }

    /**
     * @since 3.2.19
     */
    protected void stopAsKeelVerticle(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}
