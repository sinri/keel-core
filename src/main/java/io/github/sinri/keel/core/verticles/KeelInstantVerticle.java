package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @since 4.0.2
 */
public final class KeelInstantVerticle extends KeelVerticleImpl<KeelEventLog> {

    @Nonnull
    private final Supplier<Future<Void>> startFutureSupplier;

    KeelInstantVerticle(
            @Nonnull Supplier<Future<Void>> startFutureSupplier,
            @Nonnull KeelIssueRecorder<KeelEventLog> logger
    ) {
        this.startFutureSupplier = startFutureSupplier;
        issueRecorder = logger;
    }

    @Nonnull
    @Override
    protected KeelIssueRecorder<KeelEventLog> buildIssueRecorder() {
        return issueRecorder;
    }

    @Override
    protected Future<Void> startVerticle() {
        return startFutureSupplier.get();
    }
}
