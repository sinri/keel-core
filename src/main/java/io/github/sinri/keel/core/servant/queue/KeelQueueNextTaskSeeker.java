package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;

/**
 * @since 2.7 moved here
 * @since 2.8 extends Supplier{Future{KeelQueueTask}}
 * @since 4.0.0 not extends Supplier{Future{KeelQueueTask}}
 */
public interface KeelQueueNextTaskSeeker {
    /**
     * @since 4.0.0
     */
    SpecificLogger<QueueManageIssueRecord> getQueueManageIssueRecorder();

    /**
     * 找出一个task且保证其完成锁定。
     *
     * @return Future为成功时，如内容为空，则说明已经找不到任务；如非空，则为准备好的任务。Future为失败时表示获取任务过程失败。
     * @since 3.0.9 锁定功能不再要求以完成lockTaskBeforeDeployment方法的调用来实现。
     * @since 2.8 follow Supplier
     * @since 4.0.0 rename
     */
    Future<KeelQueueTask> seekNextTask();

    /**
     * As of 4.0.4, renamed to current method name.
     *
     * @return the sleep time (in ms) when no task sought to deal with.
     */
    default long getWaitingPeriodInMsWhenTaskFree() {
        return 1000L * 10;
    }

}
