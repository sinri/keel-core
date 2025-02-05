package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 * @since 4.0.0
 */
public final class KeelEventLog extends KeelIssueRecord<KeelEventLog> {

    public KeelEventLog() {
        super();
    }

    /**
     * @since 4.0.0
     */
    public KeelEventLog(KeelIssueRecord<?> baseIssueRecord) {
        super(baseIssueRecord);
    }

    @Nonnull
    @Override
    public KeelEventLog getImplementation() {
        return this;
    }
}

