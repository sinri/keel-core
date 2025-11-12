package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.issue.IssueRecord;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 3.2.0
 */
public final class QueueTaskIssueRecord extends IssueRecord<QueueTaskIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueTaskIssueRecord(@Nonnull String taskReference, @Nonnull String taskCategory) {
        super();
        this.classification(List.of("task", "reference:" + taskReference, "category:" + taskCategory));
    }

    @Nonnull
    @Override
    public QueueTaskIssueRecord getImplementation() {
        return this;
    }
}
