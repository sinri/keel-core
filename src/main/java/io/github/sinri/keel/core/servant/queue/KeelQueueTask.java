package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.core.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * @since 2.1
 */
public abstract class KeelQueueTask extends KeelVerticleImpl {
    private QueueWorkerPoolManager queueWorkerPoolManager;
    private KeelIssueRecorder<QueueTaskIssueRecord> queueTaskIssueRecorder;

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
    protected final KeelIssueRecorder<QueueTaskIssueRecord> buildQueueTaskIssueRecorder() {
        return getIssueRecordCenter().generateIssueRecorder(QueueTaskIssueRecord.TopicQueue,
                () -> new QueueTaskIssueRecord(getTaskReference(), getTaskCategory()));
    }

    /**
     * @since 4.0.2
     */
    public KeelIssueRecorder<QueueTaskIssueRecord> getQueueTaskIssueRecorder() {
        return queueTaskIssueRecorder;
    }

    @Override
    protected Future<Void> startVerticle() {
        this.queueTaskIssueRecorder = buildQueueTaskIssueRecorder();

        this.queueWorkerPoolManager.whenOneWorkerStarts();

        Future.succeededFuture()
              .compose(v -> {
                  notifyAfterDeployed();
                  return Future.succeededFuture();
              })
              .compose(v -> run())
              .recover(throwable -> {
                  getQueueTaskIssueRecorder().exception(throwable, r -> r.message("KeelQueueTask Caught throwable " +
                          "from Method " +
                          "run"));
                  return Future.succeededFuture();
              })
              .eventually(() -> {
                  getQueueTaskIssueRecorder().info(r -> r.message("KeelQueueTask to undeploy"));
                  notifyBeforeUndeploy();
                  return undeployMe().onSuccess(done -> {
                      this.queueWorkerPoolManager.whenOneWorkerEnds();
                  });
              });

        return Future.succeededFuture();
    }

    abstract protected Future<Void> run();

    protected void notifyAfterDeployed() {
        // do nothing by default
    }

    protected void notifyBeforeUndeploy() {
        // do nothing by default
    }
}
