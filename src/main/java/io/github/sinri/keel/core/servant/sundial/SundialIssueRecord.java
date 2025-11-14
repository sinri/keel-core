package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.utils.cron.ParsedCalenderElements;
import io.github.sinri.keel.logger.api.issue.IssueRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

/**
 * @since 4.0.0
 */
public class SundialIssueRecord extends IssueRecord<SundialIssueRecord> {
    public static final String TopicSundial = "Sundial";

    public SundialIssueRecord() {
        super();
        this.classification(List.of("Scheduler"));
    }

    public SundialIssueRecord(
            @NotNull KeelSundialPlan sundialPlan,
            @NotNull Calendar now,
            @NotNull String deploymentId
    ) {
        super();
        this.classification(List.of("Plan"));
        this.context("plan", sundialPlan.key())
            .context("cron", sundialPlan.cronExpression().getRawCronExpression())
            .context("time", new ParsedCalenderElements(now).toString())
            .context("deploymentId", deploymentId);
    }

    //    @NotNull
    //    @Override
    //    public SundialIssueRecord getImplementation() {
    //        return this;
    //    }
}
