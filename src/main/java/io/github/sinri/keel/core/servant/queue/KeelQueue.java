package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.base.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.api.factory.RecorderFactory;
import io.github.sinri.keel.logger.api.issue.IssueRecorder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * 标准的队列服务实现。
 * <p>
 * 仅用于单节点模式。
 *
 * @since 2.1
 */
public abstract class KeelQueue extends KeelVerticleImpl
        implements KeelQueueNextTaskSeeker, KeelQueueSignalReader {
    //private KeelQueueNextTaskSeeker nextTaskSeeker;
    private QueueWorkerPoolManager queueWorkerPoolManager;
    //private KeelQueueSignalReader signalReader;
    private KeelQueueStatus queueStatus = KeelQueueStatus.INIT;
    private IssueRecorder<QueueManageIssueRecord> queueManageIssueRecorder;

    /**
     * @since 4.0.0
     */
    protected abstract RecorderFactory getIssueRecordCenter();

    /**
     * @since 4.0.2
     */
    @Nonnull
    protected final IssueRecorder<QueueManageIssueRecord> buildQueueManageIssueRecorder() {
        return getIssueRecordCenter().createIssueRecorder(
                QueueManageIssueRecord.TopicQueue,
                QueueManageIssueRecord::new
        );
    }

    /**
     * @since 4.0.2
     */
    public IssueRecorder<QueueManageIssueRecord> getQueueManageIssueRecorder() {
        return queueManageIssueRecorder;
    }

    public KeelQueueStatus getQueueStatus() {
        return queueStatus;
    }

    protected KeelQueue setQueueStatus(KeelQueueStatus queueStatus) {
        this.queueStatus = queueStatus;
        return this;
    }

    /**
     * Create a new instance of QueueWorkerPoolManager when routine starts. By default, it uses an unlimited pool, this
     * could be override if needed.
     *
     * @since 3.0.9
     */
    protected @Nonnull QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(0);
    }

    /**
     * Callback method invoked before the queue starts its processing routine.
     * This method can be overridden to define any preparatory logic or initialization
     * steps required before the queue begins processing tasks.
     *
     * @return a Future that completes successfully when the preparatory logic is completed.
     * @since 4.1.3
     */
    protected Future<Void> beforeQueueStart() {
        return Future.succeededFuture();
    }

    @Override
    protected Future<Void> startVerticle() {
        this.queueStatus = KeelQueueStatus.RUNNING;
        return beforeQueueStart()
                .compose(v -> {
                    routine();
                    return Future.succeededFuture();
                });
    }

    protected final void routine() {
        queueManageIssueRecorder = this.buildQueueManageIssueRecorder();

        getQueueManageIssueRecorder().debug(r -> r.message("KeelQueue::routine start"));
        this.queueWorkerPoolManager = getQueueWorkerPoolManager();

        Future.succeededFuture()
              .compose(v -> this.readSignal())
              .recover(throwable -> {
                  getQueueManageIssueRecorder().debug(r -> r.message("AS IS. Failed to read signal: " + throwable.getMessage()));
                  if (getQueueStatus() == KeelQueueStatus.STOPPED) {
                      return Future.succeededFuture(KeelQueueSignal.STOP);
                  } else {
                      return Future.succeededFuture(KeelQueueSignal.RUN);
                  }
              })
              .compose(signal -> {
                  if (signal == KeelQueueSignal.STOP) {
                      return this.whenSignalStopCame();
                  } else if (signal == KeelQueueSignal.RUN) {
                      return this.whenSignalRunCame();
                  } else {
                      return Future.failedFuture("Unknown Signal");
                  }
              })
              .eventually(() -> {
                  long waitingMs = this.getWaitingPeriodInMsWhenTaskFree();
                  getQueueManageIssueRecorder().debug(r -> r.message("set timer for next routine after " + waitingMs + " ms"));
                  Keel.getVertx().setTimer(waitingMs, timerID -> routine());
                  return Future.succeededFuture();
              })
        ;
    }

    private Future<Void> whenSignalStopCame() {
        if (getQueueStatus() == KeelQueueStatus.RUNNING) {
            this.queueStatus = KeelQueueStatus.STOPPED;
            getQueueManageIssueRecorder().notice(r -> r.message("Signal Stop Received"));
        }
        return Future.succeededFuture();
    }

    private Future<Void> whenSignalRunCame() {
        this.queueStatus = KeelQueueStatus.RUNNING;

        return Keel.asyncCallRepeatedly(routineResult -> {
                       if (this.queueWorkerPoolManager.isBusy()) {
                           return Keel.asyncSleep(1_000L);
                       }

                       return Future.succeededFuture()
                                    .compose(v -> this.seekNextTask())
                                    .compose(task -> {
                                        if (task == null) {
                                            // 队列里已经空了，不必再找
                                            getQueueManageIssueRecorder().debug(r -> r
                                                    .message("No more task todo"));
                                            // 通知 FutureUntil 结束
                                            routineResult.stop();
                                            return Future.succeededFuture();
                                        }

                                        // 队列里找出来一个task, deploy it (至于能不能跑起来有没有锁就不管了)
                                        getQueueManageIssueRecorder().info(r -> r
                                                .message("To run task: " + task.getTaskReference()));
                                        getQueueManageIssueRecorder().info(r -> r
                                                .message("Trusted that task  is already locked by seeker: " + task.getTaskReference()));

                                        // since 3.0.9
                                        task.setQueueWorkerPoolManager(this.queueWorkerPoolManager);

                                        return Future.succeededFuture()
                                                     .compose(v -> {
                                                         return task.deployMe();
                                                     })
                                                     .compose(
                                                             deploymentID -> {
                                                                 getQueueManageIssueRecorder().info(r -> r.message(
                                                                         "TASK [" + task.getTaskReference() + "] " +
                                                                                 "VERTICLE DEPLOYED: " + deploymentID));
                                                                 // 通知 FutureUntil 继续下一轮
                                                                 return Future.succeededFuture();
                                                             },
                                                             throwable -> {
                                                                 getQueueManageIssueRecorder().exception(throwable,
                                                                         "CANNOT DEPLOY TASK [%s] VERTICLE".formatted(task.getTaskReference())
                                                                 );
                                                                 // 通知 FutureUntil 继续下一轮
                                                                 return Future.succeededFuture();
                                                             }
                                                     );
                                    });
                   })
                   .recover(throwable -> {
                       getQueueManageIssueRecorder().exception(throwable, "KeelQueue 递归找活干里出现了奇怪的故障");
                       return Future.succeededFuture();
                   });
    }

    @Override
    protected Future<Void> stopVerticle() {
        this.queueStatus = KeelQueueStatus.STOPPED;
        return Future.succeededFuture();
    }

    /**
     * Deploys the current queue verticle on the worker threading model.
     *
     * @return a future that completes with the deployment ID if the deployment is successful,
     *         or fails with an exception if the deployment fails
     * @since 4.1.3
     */
    public final Future<String> deployMe() {
        return super.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }
}
