package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;

abstract public class KeelVerticleImplWithIssueRecorder<T extends KeelIssueRecord<T>> extends AbstractVerticle implements KeelVerticle {
    private @Nonnull KeelIssueRecorder<T> issueRecorder;

    public KeelVerticleImplWithIssueRecorder() {
        this.issueRecorder = KeelIssueRecorder.buildSilentIssueRecorder();
    }

    @Nonnull
    public final KeelIssueRecorder<T> getIssueRecorder() {
        return issueRecorder;
    }

    abstract protected @Nonnull KeelIssueRecorder<T> buildIssueRecorder();

    @Override
    public final void start(Promise<Void> startPromise) {
        start();
        startAsKeelVerticle(startPromise);
    }

    @Override
    public final void start() {
        this.issueRecorder = buildIssueRecorder();
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
