package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.core.cron.KeelCronExpression;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;

import java.util.Calendar;

/**
 * @since 3.0.0
 * @since 3.2.4 change sync method `execute` to be async.
 */
public interface KeelSundialPlan {
    String key();

    KeelCronExpression cronExpression();

    /**
     * @param sundialIssueRecorder as of 4.0.3
     */
    Future<Void> execute(Calendar now, KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder);

    /**
     * For some easy work, run with event pool might be an open choice.
     *
     * @return whether this sundial plan should be executed in Worker Thread. By default, it returns true.
     * @since 3.2.21
     */
    default boolean isWorkerThreadRequired() {
        return true;
    }
}
