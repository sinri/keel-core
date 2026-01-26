package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.base.verticles.KeelVerticleBase;
import io.github.sinri.keel.logger.api.LateObject;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jspecify.annotations.NullMarked;


/**
 * 队列任务类
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class QueueTask extends KeelVerticleBase {
    private final LateObject<QueueWorkerPoolManager> lateQueueWorkerPoolManager = new LateObject<>();
    private final LateObject<SpecificLogger<QueueTaskSpecificLog>> lateQueueTaskLogger = new LateObject<>();

    public QueueTask() {
        super();
    }

    protected final QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return lateQueueWorkerPoolManager.get();
    }

    final void setQueueWorkerPoolManager(QueueWorkerPoolManager queueWorkerPoolManager) {
        this.lateQueueWorkerPoolManager.set(queueWorkerPoolManager);
    }


    abstract public String getTaskReference();


    abstract public String getTaskCategory();

    protected final SpecificLogger<QueueTaskSpecificLog> buildQueueTaskLogger() {
        return LoggerFactory.getShared().createLogger(
                QueueTaskSpecificLog.TopicQueue,
                () -> new QueueTaskSpecificLog(getTaskReference(), getTaskCategory())
        );
    }


    protected final SpecificLogger<QueueTaskSpecificLog> getQueueTaskLogger() {
        return lateQueueTaskLogger.get();
    }

    /**
     * As of 4.1.3, if a worker thread is required, the threading model is set to WORKER;
     * otherwise, the threading model is set to VIRTUAL_THREAD if possible.
     */
    @Override
    protected Future<Void> startVerticle() {
        this.lateQueueTaskLogger.set(buildQueueTaskLogger());

        this.getQueueWorkerPoolManager().whenOneWorkerStarts();

        Future.succeededFuture()
              .compose(v -> {
                  notifyAfterDeployed();
                  return Future.succeededFuture();
              })
              .compose(v -> run())
              .recover(throwable -> {
                  getQueueTaskLogger().error(log -> log
                          .exception(throwable)
                          .message("KeelQueueTask Caught throwable from Method run")
                  );
                  return Future.succeededFuture();
              })
              .eventually(() -> {
                  getQueueTaskLogger().info(r -> r.message("KeelQueueTask to undeploy"));
                  notifyBeforeUndeploy();
                  return undeployMe().onSuccess(done -> this.getQueueWorkerPoolManager().whenOneWorkerEnds());
              });

        return Future.succeededFuture();
    }


    abstract protected Future<Void> run();

    protected void notifyAfterDeployed() {
        getQueueTaskLogger().debug("KeelQueueTask.notifyAfterDeployed");
    }

    protected void notifyBeforeUndeploy() {
        getQueueTaskLogger().debug("KeelQueueTask.notifyBeforeUndeploy");
    }

    /**
     * @return 部署结果
     */

    public Future<String> deployMe(Keel keel) {
        var deploymentOptions = new DeploymentOptions();
        deploymentOptions.setThreadingModel(expectedThreadingModel());
        return super.deployMe(keel, deploymentOptions);
    }


    protected ThreadingModel expectedThreadingModel() {
        return ThreadingModel.WORKER;
    }

}
