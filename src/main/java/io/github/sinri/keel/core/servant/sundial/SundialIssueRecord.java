package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.logger.issue.record.AbstractIssueRecord;
import io.github.sinri.keel.utils.time.cron.ParsedCalenderElements;

import javax.annotation.Nonnull;
import java.util.Calendar;

/**
 * @since 4.0.0
 */
public class SundialIssueRecord extends AbstractIssueRecord<SundialIssueRecord> {
    public static final String TopicSundial = "Sundial";

    public SundialIssueRecord() {
        super();
        this.classification("Scheduler");
    }

    public SundialIssueRecord(
            @Nonnull KeelSundialPlan sundialPlan,
            @Nonnull Calendar now,
            @Nonnull String deploymentId
    ) {
        super();
        this.classification("Plan");
        this.context("plan", sundialPlan.key())
                .context("cron", sundialPlan.cronExpression().getRawCronExpression())
                .context("time", new ParsedCalenderElements(now).toString())
                .context("deploymentId", deploymentId);
    }

    @Nonnull
    @Override
    public SundialIssueRecord getImplementation() {
        return this;
    }
}
