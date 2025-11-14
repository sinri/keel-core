package io.github.sinri.keel.core.servant.queue;

import io.github.sinri.keel.logger.api.issue.IssueRecord;

import java.util.List;

public final class QueueManageIssueRecord extends IssueRecord<QueueManageIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueManageIssueRecord() {
        super();
        this.classification(List.of("manage"));
    }


    //    @NotNull
    //    @Override
    //    public QueueManageIssueRecord getImplementation() {
    //        return this;
    //    }
}
