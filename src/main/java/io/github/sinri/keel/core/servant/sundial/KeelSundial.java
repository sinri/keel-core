package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.utils.time.cron.KeelCronExpression;
import io.github.sinri.keel.utils.time.cron.ParsedCalenderElements;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.0
 * @since 3.2.4 use verticle to handle the sundial plan executing.
 * @since 4.0.0 changed to use issue recorder
 */
public abstract class KeelSundial extends KeelVerticleImpl {
    private final Map<String, KeelSundialPlan> planMap = new ConcurrentHashMap<>();
    private Long timerID;
    /**
     * @since 4.0.2
     */
    private KeelIssueRecorder<SundialIssueRecord> sundialIssueRecorder;

    /**
     * @since 4.0.0
     */
    abstract protected KeelIssueRecordCenter getIssueRecordCenter();

    /**
     * @since 4.0.0
     */
    @Nonnull
    protected KeelIssueRecorder<SundialIssueRecord> buildIssueRecorder() {
        return getIssueRecordCenter().generateIssueRecorder(SundialIssueRecord.TopicSundial, SundialIssueRecord::new);
    }

    /**
     * @since 4.0.2
     */
    public KeelIssueRecorder<SundialIssueRecord> getSundialIssueRecorder() {
        return sundialIssueRecorder;
    }

    @Override
    protected Future<Void> startVerticle() {
        this.sundialIssueRecorder = buildIssueRecorder();
        int delaySeconds = 61 - KeelCronExpression.parseCalenderToElements(Calendar.getInstance()).second;
        this.timerID = Keel.getVertx().setPeriodic(delaySeconds * 1000L, 60_000L, timerID -> {
            handleEveryMinute(Calendar.getInstance());
            refreshPlans();
        });
        return Future.succeededFuture();
    }

    private void handleEveryMinute(Calendar now) {
        ParsedCalenderElements parsedCalenderElements = new ParsedCalenderElements(now);
        planMap.forEach((key, plan) -> {
            if (plan.cronExpression().match(parsedCalenderElements)) {
                getSundialIssueRecorder().debug(x -> x
                        .message("Sundial Plan Matched")
                        .context("plan_key", plan.key())
                        .context("plan_cron", plan.cronExpression().getRawCronExpression())
                        .context("now", parsedCalenderElements.toString())
                );
                new KeelSundialVerticle(plan, now, getSundialIssueRecorder()).deployMe();
            } else {
                getSundialIssueRecorder().debug(x -> x
                        .message("Sundial Plan Not Match")
                        .context("plan_key", plan.key())
                        .context("plan_cron", plan.cronExpression().getRawCronExpression())
                        .context("now", parsedCalenderElements.toString())
                );
            }
        });
    }

    /**
     * @since 3.2.4
     */
    private void refreshPlans() {
        Keel.asyncCallExclusively(
                    "io.github.sinri.keel.servant.sundial.KeelSundial.refreshPlans",
                    1000L,
                    () -> fetchPlans()
                            .compose(plans -> {
                                // treat null as NOT MODIFIED
                                if (plans != null) {
                                    Set<String> toDelete = new HashSet<>(planMap.keySet());
                                    plans.forEach(plan -> {
                                        toDelete.remove(plan.key());
                                        planMap.put(plan.key(), plan);
                                    });
                                    if (!toDelete.isEmpty()) {
                                        toDelete.forEach(planMap::remove);
                                    }
                                }
                                return Future.succeededFuture();
                            })
            )
            .onFailure(throwable -> getSundialIssueRecorder().exception(throwable, "io.github.sinri.keel.core.servant.sundial.KeelSundial" +
                    ".refreshPlans exception"));
    }

    /**
     * @since 3.0.1 Before plansSupplier is removed, when plansSupplier returns non-null supplier, use that and ignore
     *         this. If future as null, means `NOT MODIFIED`.
     */
    abstract protected Future<Collection<KeelSundialPlan>> fetchPlans();

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (this.timerID != null) {
            Keel.getVertx().cancelTimer(this.timerID);
        }
        stopPromise.complete();
    }

    /**
     * Deploys the current verticle using a deployment option configured with a worker threading model.
     *
     * @return a future representing the result of the deployment. The future completes with the deployment ID if
     *         the deployment is successful or fails with an exception if an error occurs during deployment.
     * @since 4.1.3
     */
    public final Future<String> deployMe() {
        return super.deployMe(new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER));
    }
}
