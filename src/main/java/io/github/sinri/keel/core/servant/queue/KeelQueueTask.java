package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.core.utils.ReflectionUtils;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;


/**
 * @since 2.1
 */
public abstract class KeelQueueTask extends AbstractKeelVerticle {
    private QueueWorkerPoolManager queueWorkerPoolManager;
    private SpecificLogger<QueueTaskIssueRecord> queueTaskIssueRecorder;

    final void setQueueWorkerPoolManager(QueueWorkerPoolManager queueWorkerPoolManager) {
        this.queueWorkerPoolManager = queueWorkerPoolManager;
    }

    @NotNull
    abstract public String getTaskReference();

    @NotNull
    abstract public String getTaskCategory();

    /**
     * @since 4.0.0
     */
    abstract protected LoggerFactory getIssueRecordCenter();

    /**
     * @since 4.0.0
     */
    @NotNull
    protected final SpecificLogger<QueueTaskIssueRecord> buildQueueTaskIssueRecorder() {
        return getIssueRecordCenter().createLogger(
                QueueTaskIssueRecord.TopicQueue,
                () -> new QueueTaskIssueRecord(getTaskReference(), getTaskCategory())
        );
    }

    /**
     * @since 4.0.2
     */
    public SpecificLogger<QueueTaskIssueRecord> getQueueTaskIssueRecorder() {
        return queueTaskIssueRecorder;
    }

    /**
     * As of 4.1.3, if a worker thread is required, the threading model is set to WORKER;
     * otherwise, the threading model is set to VIRTUAL_THREAD if possible.
     */
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
                  getQueueTaskIssueRecorder().exception(throwable, "KeelQueueTask Caught throwable from Method run");
                  return Future.succeededFuture();
              })
              .eventually(() -> {
                  getQueueTaskIssueRecorder().info(r -> r.message("KeelQueueTask to undeploy"));
                  notifyBeforeUndeploy();
                  return undeployMe().onSuccess(done -> this.queueWorkerPoolManager.whenOneWorkerEnds());
              });

        return Future.succeededFuture();
    }

    abstract protected Future<Void> run();

    protected void notifyAfterDeployed() {
        getQueueTaskIssueRecorder().debug("KeelQueueTask.notifyAfterDeployed");
    }

    protected void notifyBeforeUndeploy() {
        getQueueTaskIssueRecorder().debug("KeelQueueTask.notifyBeforeUndeploy");
    }

    /**
     * Determines whether the current task requires execution on a worker thread.
     *
     * @return true if a worker thread is required for this task, false otherwise.
     * @since 4.1.3
     */
    public boolean isWorkerThreadRequired() {
        return true;
    }

    /**
     * Deploys the current task with proper threading model based on the configuration.
     * If a worker thread is required, the threading model is set to WORKER.
     * If virtual threads are available and no worker thread is required, the threading model is set to VIRTUAL_THREAD.
     * Delegates to the superclass deployMe method with configured DeploymentOptions.
     *
     * @return a future that completes with the deployment ID if the deployment is successful,
     *         or fails with an exception if the deployment fails
     * @since 4.1.3
     */
    public Future<String> deployMe() {
        var deploymentOptions = new DeploymentOptions();
        if (this.isWorkerThreadRequired()) {
            deploymentOptions.setThreadingModel(ThreadingModel.WORKER);
        } else if (ReflectionUtils.isVirtualThreadsAvailable()) {
            deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
        }
        return super.deployMe(deploymentOptions);
    }
}
