package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.core.verticles.KeelVerticleImplWithIssueRecorder;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;

/**
 * @since 2.1
 */
public abstract class KeelQueueTask extends KeelVerticleImplWithIssueRecorder<QueueTaskIssueRecord> {
    private QueueWorkerPoolManager queueWorkerPoolManager;

    final void setQueueWorkerPoolManager(QueueWorkerPoolManager queueWorkerPoolManager) {
        this.queueWorkerPoolManager = queueWorkerPoolManager;
    }

    @Nonnull
    abstract public String getTaskReference();

    @Nonnull
    abstract public String getTaskCategory();

    /**
     * @since 4.0.0
     */
    abstract protected KeelIssueRecordCenter getIssueRecordCenter();

    /**
     * @since 4.0.0
     */
    @Nonnull
    @Override
    protected final KeelIssueRecorder<QueueTaskIssueRecord> buildIssueRecorder() {
        return getIssueRecordCenter().generateIssueRecorder(QueueTaskIssueRecord.TopicQueue, () -> new QueueTaskIssueRecord(getTaskReference(), getTaskCategory()));
    }

    @Override
    protected final void startAsKeelVerticle(Promise<Void> startPromise) {
        this.queueWorkerPoolManager.whenOneWorkerStarts();

        Future.succeededFuture()
                .compose(v -> {
                    notifyAfterDeployed();
                    return Future.succeededFuture();
                })
                .compose(v -> run())
                .recover(throwable -> {
                    getIssueRecorder().exception(throwable, r -> r.message("KeelQueueTask Caught throwable from Method run"));
                    return Future.succeededFuture();
                })
                .eventually(() -> {
                    getIssueRecorder().info(r -> r.message("KeelQueueTask to undeploy"));
                    notifyBeforeUndeploy();
                    return undeployMe().onSuccess(done -> {
                        this.queueWorkerPoolManager.whenOneWorkerEnds();
                    });
                });

        startPromise.complete();
    }

    abstract protected Future<Void> run();

    protected void notifyAfterDeployed() {
        // do nothing by default
    }

    protected void notifyBeforeUndeploy() {
        // do nothing by default
    }
}
