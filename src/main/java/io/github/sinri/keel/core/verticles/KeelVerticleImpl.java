package io.github.sinri.keel.core.verticles;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;

/**
 * @since 4.0.2
 */
public abstract class KeelVerticleImpl<T extends KeelIssueRecord<T>> extends AbstractVerticle implements KeelVerticle {
    @Nonnull
    protected KeelIssueRecorder<T> issueRecorder;

    protected KeelVerticleImpl() {
        issueRecorder = KeelIssueRecordCenter.silentCenter().generateIssueRecorder("", () -> null);
    }

    @Nonnull
    protected abstract KeelIssueRecorder<T> buildIssueRecorder();

    @Nonnull
    public KeelIssueRecorder<T> getIssueRecorder() {
        return issueRecorder;
    }

    @Override
    public final void start() {
        issueRecorder = buildIssueRecorder();
    }

    @Override
    public final void start(Promise<Void> startPromise) {
        Future.succeededFuture()
              .compose(v -> {
                  start();
                  return startVerticle();
              })
              .andThen(ar -> {
                  if (ar.succeeded()) {
                      startPromise.complete();
                  } else {
                      startPromise.fail(ar.cause());
                  }
              });
    }

    protected abstract Future<Void> startVerticle();
}
