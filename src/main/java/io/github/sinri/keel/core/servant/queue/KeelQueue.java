package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * 队列服务实现。
 * <p>
 * 仅用于单节点模式。
 *
 * @since 5.0.0
 */
public abstract class KeelQueue extends AbstractKeelVerticle
        implements KeelQueueNextTaskSeeker, KeelQueueSignalReader {
    @Nullable
    private QueueWorkerPoolManager queueWorkerPoolManager;
    @NotNull
    private KeelQueueStatus queueStatus = KeelQueueStatus.INIT;
    @Nullable
    private SpecificLogger<QueueManageSpecificLog> queueManageLogger;

    public KeelQueue(@NotNull Keel keel) {
        super(keel);
    }

    @NotNull
    protected abstract LoggerFactory getLoggerFactory();

    @NotNull
    private SpecificLogger<QueueManageSpecificLog> buildQueueManageLogger() {
        return getLoggerFactory().createLogger(
                QueueManageSpecificLog.TopicQueue,
                QueueManageSpecificLog::new
        );
    }

    public final @NotNull SpecificLogger<QueueManageSpecificLog> getQueueManageLogger() {
        return Objects.requireNonNull(queueManageLogger);
    }

    public final @NotNull KeelQueueStatus getQueueStatus() {
        return queueStatus;
    }

    @NotNull
    protected KeelQueue setQueueStatus(@NotNull KeelQueueStatus queueStatus) {
        this.queueStatus = queueStatus;
        return this;
    }

    /**
     * 创建队列并发工作管理器。
     * <p>
     * 默认实现为一个不限制并发的实例，可以重写本方法修改。
     */
    @NotNull
    protected QueueWorkerPoolManager buildQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(0);
    }

    public @NotNull QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return Objects.requireNonNull(queueWorkerPoolManager);
    }

    /**
     * 队列运行前的清理整备逻辑。
     *
     */
    @NotNull
    protected Future<Void> beforeQueueStart() {
        return Future.succeededFuture();
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        queueManageLogger = this.buildQueueManageLogger();
        this.queueWorkerPoolManager = buildQueueWorkerPoolManager();
        this.queueStatus = KeelQueueStatus.RUNNING;
        return beforeQueueStart()
                .compose(v -> {
                    routine();
                    return Future.succeededFuture();
                });
    }

    private void routine() {
        this.getQueueManageLogger().debug(r -> r.message("KeelQueue::routine start"));

        Future.succeededFuture()
              .compose(v -> this.readSignal())
              .recover(throwable -> {
                  this.getQueueManageLogger()
                      .debug(r -> r.message("AS IS. Failed to read signal: " + throwable.getMessage()));
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
                  this.getQueueManageLogger()
                      .debug(r -> r.message("set timer for next routine after " + waitingMs + " ms"));
                  getVertx().setTimer(waitingMs, timerID -> routine());
                  return Future.succeededFuture();
              })
        ;
    }

    @NotNull
    private Future<Void> whenSignalStopCame() {
        if (getQueueStatus() == KeelQueueStatus.RUNNING) {
            this.queueStatus = KeelQueueStatus.STOPPED;
            this.getQueueManageLogger().notice(r -> r.message("Signal Stop Received"));
        }
        return Future.succeededFuture();
    }

    @NotNull
    private Future<Void> whenSignalRunCame() {
        this.queueStatus = KeelQueueStatus.RUNNING;

        return getKeel().asyncCallRepeatedly(routineResult -> {
                       if (this.getQueueWorkerPoolManager().isBusy()) {
                           return getKeel().asyncSleep(1_000L);
                       }

                       return Future.succeededFuture()
                                    .compose(v -> this.seekNextTask())
                                    .compose(task -> {
                                        if (task == null) {
                                            // 队列里已经空了，不必再找
                                            this.getQueueManageLogger().debug(r -> r
                                                    .message("No more task todo"));
                                            // 通知 FutureUntil 结束
                                            routineResult.stop();
                                            return Future.succeededFuture();
                                        }

                                        // 队列里找出来一个task, deploy it (至于能不能跑起来有没有锁就不管了)
                                        this.getQueueManageLogger().info(r -> r
                                                .message("To run task: " + task.getTaskReference()));
                                        this.getQueueManageLogger().info(r -> r
                                                .message("Trusted that task  is already locked by seeker: " + task.getTaskReference()));

                                        task.setQueueWorkerPoolManager(this.getQueueWorkerPoolManager());

                                        return Future.succeededFuture()
                                                     .compose(v -> {
                                                         return task.deployMe();
                                                     })
                                                     .compose(
                                                             deploymentID -> {
                                                                 this.getQueueManageLogger().info(r -> r.message(
                                                                         "TASK [" + task.getTaskReference() + "] " +
                                                                                 "VERTICLE DEPLOYED: " + deploymentID));
                                                                 // 通知 FutureUntil 继续下一轮
                                                                 return Future.succeededFuture();
                                                             },
                                                             throwable -> {
                                                                 this.getQueueManageLogger().error(log -> log
                                                                         .exception(throwable)
                                                                         .message("CANNOT DEPLOY TASK [%s] VERTICLE".formatted(task.getTaskReference()))
                                                                 );
                                                                 // 通知 FutureUntil 继续下一轮
                                                                 return Future.succeededFuture();
                                                             }
                                                     );
                                    });
                   })
                   .recover(throwable -> {
                       this.getQueueManageLogger().error(log -> log
                               .exception(throwable)
                               .message("KeelQueue 递归找活干里出现了奇怪的故障")
                       );
                       return Future.succeededFuture();
                   });
    }

    @Override
    protected @NotNull Future<Void> stopVerticle() {
        this.queueStatus = KeelQueueStatus.STOPPED;
        return Future.succeededFuture();
    }

    /**
     * 将本队列实例以 WORKER 线程模型部署。
     */
    @NotNull
    public final Future<String> deployMe() {
        return super.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }
}
