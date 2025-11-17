package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.log.SpecificLog;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @since 3.2.0
 */
public final class QueueTaskIssueRecord extends SpecificLog<QueueTaskIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueTaskIssueRecord(@NotNull String taskReference, @NotNull String taskCategory) {
        super();
        this.classification(List.of("task", "reference:" + taskReference, "category:" + taskCategory));
    }

    //    @NotNull
    //    @Override
    //    public QueueTaskIssueRecord getImplementation() {
    //        return this;
    //    }
}
