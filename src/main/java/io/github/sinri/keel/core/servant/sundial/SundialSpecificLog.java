package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.core.utils.cron.ParsedCalenderElements;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;

/**
 * 日晷定时任务特定日志记录
 *
 * @since 5.0.0
 */
public final class SundialSpecificLog extends SpecificLog<SundialSpecificLog> {
    public static final String TopicSundial = "Sundial";

    public SundialSpecificLog() {
        super();
        this.classification(List.of("Scheduler"));
    }

    public SundialSpecificLog(
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
}
