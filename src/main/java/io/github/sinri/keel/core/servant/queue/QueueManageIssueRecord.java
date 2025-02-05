package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;

public final class QueueManageIssueRecord extends KeelIssueRecord<QueueManageIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueManageIssueRecord() {
        super();
        this.classification("manage");
    }


    @Nonnull
    @Override
    public QueueManageIssueRecord getImplementation() {
        return this;
    }
}
