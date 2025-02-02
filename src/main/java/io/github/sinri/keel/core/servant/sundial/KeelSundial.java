package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.core.cron.KeelCronExpression;
import io.github.sinri.keel.core.cron.ParsedCalenderElements;
import io.github.sinri.keel.core.verticles.KeelVerticleImplWithIssueRecorder;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
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
public abstract class KeelSundial extends KeelVerticleImplWithIssueRecorder<SundialIssueRecord> {
    private final Map<String, KeelSundialPlan> planMap = new ConcurrentHashMap<>();
    private Long timerID;

    /**
     * @since 4.0.0
     */
    abstract protected KeelIssueRecordCenter getIssueRecordCenter();

    /**
     * @since 4.0.0
     */
    @Nonnull
    @Override
    protected KeelIssueRecorder<SundialIssueRecord> buildIssueRecorder() {
        return getIssueRecordCenter().generateIssueRecorder(SundialIssueRecord.TopicSundial, SundialIssueRecord::new);
    }

    @Override
    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        int delaySeconds = 61 - KeelCronExpression.parseCalenderToElements(Calendar.getInstance()).second;
        this.timerID = Keel.getVertx().setPeriodic(delaySeconds * 1000L, 60_000L, timerID -> {
            handleEveryMinute(Calendar.getInstance());
            refreshPlans();
        });
        startPromise.complete();
    }

    private void handleEveryMinute(Calendar now) {
        ParsedCalenderElements parsedCalenderElements = new ParsedCalenderElements(now);
        planMap.forEach((key, plan) -> {
            if (plan.cronExpression().match(parsedCalenderElements)) {
                getIssueRecorder().debug(x -> x
                        .message("Sundial Plan Matched")
                        .context("plan_key", plan.key())
                        .context("plan_cron", plan.cronExpression().getRawCronExpression())
                        .context("now", parsedCalenderElements.toString())
                );

                // since 3.2.5
                var deploymentOptions = new DeploymentOptions();
                if (plan.isWorkerThreadRequired()) {
                    deploymentOptions.setThreadingModel(ThreadingModel.WORKER);
                }
                new KeelSundialVerticle(plan, now, getIssueRecordCenter())
                        .deployMe(deploymentOptions);
            } else {
                getIssueRecorder().debug(x -> x
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
                        () -> {
                            return fetchPlans()
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
                                    });
                        }
                )
                .onFailure(throwable -> {
                    getIssueRecorder().exception(throwable, "io.github.sinri.keel.core.servant.sundial.KeelSundial.refreshPlans exception");
                });
    }

    /**
     * @since 3.0.1
     * Before plansSupplier is removed, when plansSupplier returns non-null supplier, use that and ignore this.
     * If future as null, means `NOT MODIFIED`.
     */
    abstract protected Future<Collection<KeelSundialPlan>> fetchPlans();

    @Override
    protected void stopAsKeelVerticle(Promise<Void> stopPromise) {
        if (this.timerID != null) {
            Keel.getVertx().cancelTimer(this.timerID);
        }
        stopPromise.complete();
    }
}
