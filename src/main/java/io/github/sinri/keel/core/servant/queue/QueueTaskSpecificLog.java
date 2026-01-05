package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.log.SpecificLog;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * 面向队列任务的特定日志。
 *
 * @since 5.0.0
 */
@NullMarked
public final class QueueTaskSpecificLog extends SpecificLog<QueueTaskSpecificLog> {
    public static final String TopicQueue = "Queue";

    public QueueTaskSpecificLog(String taskReference, String taskCategory) {
        super();
        this.classification(List.of("task", "reference:" + taskReference, "category:" + taskCategory));
    }
}
