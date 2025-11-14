package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.utils.cron.KeelCronExpression;
import io.github.sinri.keel.logger.api.issue.IssueRecorder;
import io.vertx.core.Future;

import java.util.Calendar;

/**
 * As of 3.2.4, change sync method `execute` to be async.
 *
 * @since 3.0.0
 */
public interface KeelSundialPlan {
    String key();

    KeelCronExpression cronExpression();

    /**
     * As of 4.1.3, if {@link KeelSundialPlan#isWorkerThreadRequired()} returns false,
     * this method would execute in virtual thread mode if possible.
     *
     * @param sundialIssueRecorder as of 4.0.3
     */
    Future<Void> execute(Calendar now, IssueRecorder<SundialIssueRecord> sundialIssueRecorder);

    /**
     * Determines whether the execution of a task or plan requires a worker thread.
     * <p>
     * Worker threads are typically needed for blocking or long-running operations to prevent
     * impacts on the event loop or main thread.
     *
     * @return {@code true} if a worker thread is required for execution, otherwise {@code false}.
     */
    default boolean isWorkerThreadRequired() {
        return true;
    }
}
