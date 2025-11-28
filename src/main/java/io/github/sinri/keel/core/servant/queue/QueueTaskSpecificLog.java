package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.log.SpecificLog;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 面向队列任务的特定日志。
 *
 * @since 5.0.0
 */
public final class QueueTaskSpecificLog extends SpecificLog<QueueTaskSpecificLog> {
    @NotNull
    public static final String TopicQueue = "Queue";

    public QueueTaskSpecificLog(@NotNull String taskReference, @NotNull String taskCategory) {
        super();
        this.classification(List.of("task", "reference:" + taskReference, "category:" + taskCategory));
    }
}
