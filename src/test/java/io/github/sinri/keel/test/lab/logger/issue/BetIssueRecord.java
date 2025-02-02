package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;

public final class BetIssueRecord extends KeelIssueRecord<BetIssueRecord> {
    public BetIssueRecord(@Nonnull String caller) {
        super("Bet");
        this.classification("Bet", caller);
    }

    @Nonnull
    @Override
    public BetIssueRecord getImplementation() {
        return this;
    }

    public BetIssueRecord setData(int x) {
        this.attribute("data", x);
        return this;
    }
}
