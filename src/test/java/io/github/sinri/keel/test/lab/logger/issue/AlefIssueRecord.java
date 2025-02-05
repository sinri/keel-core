package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;

public class AlefIssueRecord extends KeelIssueRecord<AlefIssueRecord> {

    public AlefIssueRecord() {
        super();
    }

    @Nonnull
    @Override
    public AlefIssueRecord getImplementation() {
        return this;
    }
}
