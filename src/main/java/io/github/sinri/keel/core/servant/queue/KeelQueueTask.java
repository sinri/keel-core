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
 * 队列任务类
 *
 * @since 5.0.0
 */
public abstract class KeelQueueTask extends AbstractKeelVerticle {
    private QueueWorkerPoolManager queueWorkerPoolManager;
    private SpecificLogger<QueueTaskSpecificLog> queueTaskIssueRecorder;

    final void setQueueWorkerPoolManager(QueueWorkerPoolManager queueWorkerPoolManager) {
        this.queueWorkerPoolManager = queueWorkerPoolManager;
    }

    @NotNull
    abstract public String getTaskReference();

    @NotNull
    abstract public String getTaskCategory();

    abstract protected LoggerFactory getIssueRecordCenter();

    @NotNull
    protected final SpecificLogger<QueueTaskSpecificLog> buildQueueTaskIssueRecorder() {
        return getIssueRecordCenter().createLogger(
                QueueTaskSpecificLog.TopicQueue,
                () -> new QueueTaskSpecificLog(getTaskReference(), getTaskCategory())
        );
    }

    public SpecificLogger<QueueTaskSpecificLog> getQueueTaskIssueRecorder() {
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
     *
     *
     * @return 指定本任务是否需要在 WORKER 线程模型下运行
     */
    public boolean isWorkerThreadRequired() {
        return true;
    }

    /**
     * 如果本任务类指定在 WORKER 线程模型下运行，则以此部署；否则，先尝试以虚拟线程模型部署，不行就用事件循环模式部署。
     *
     * @return 部署结果
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
