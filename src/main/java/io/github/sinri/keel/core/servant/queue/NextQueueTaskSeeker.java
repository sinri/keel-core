package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 队列任务寻求者
 *
 * @since 5.0.0
 */
public interface NextQueueTaskSeeker {
    @NotNull SpecificLogger<QueueManageSpecificLog> getQueueManageLogger();

    /**
     * 找出一个task且保证其完成锁定。
     *
     * @return Future为成功时，如内容为空，则说明已经找不到任务；如非空，则为准备好的任务。Future为失败时表示获取任务过程失败。
     */
    @NotNull Future<@Nullable QueueTask> seekNextTask();

    /**
     * 找不到任务时的等待时间，以毫秒计。
     * <p>
     * 默认 10 秒。
     *
     * @return 找不到任务时的等待时间，以毫秒计。
     */
    default long getWaitingPeriodInMsWhenTaskFree() {
        return 1000L * 10;
    }

}
