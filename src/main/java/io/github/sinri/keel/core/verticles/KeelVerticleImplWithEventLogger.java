package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
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
        this.logger = KeelEventLogger.from(KeelIssueRecorder.buildSilentIssueRecorder());
    }

    @Nonnull
    public final KeelEventLogger getLogger() {
        return logger;
    }

    abstract protected KeelEventLogger buildEventLogger();

    @Override
    public final void start(Promise<Void> startPromise) {
        start();
        startAsKeelVerticle(startPromise);
    }

    /**
     * Just do one thing: build event logger.
     */
    @Override
    public final void start() {
        this.logger = buildEventLogger();
    }

    abstract protected void startAsKeelVerticle(Promise<Void> startPromise);

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
     * @since 3.2.19
     */
    protected void stopAsKeelVerticle(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}
