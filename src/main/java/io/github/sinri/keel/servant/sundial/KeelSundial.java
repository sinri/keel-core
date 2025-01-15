package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.core.KeelCronExpression;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.verticles.KeelVerticleImplWithEventLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.0
 * @since 3.2.4 use verticle to handle the sundial plan executing.
 */
public abstract class KeelSundial extends KeelVerticleImplWithEventLogger {
    private final Map<String, KeelSundialPlan> planMap = new ConcurrentHashMap<>();
    private final AtomicInteger planFetchingSemaphore = new AtomicInteger(0);
    private Long timerID;

    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.outputCenter().generateEventLogger("Sundial");
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
        KeelCronExpression.ParsedCalenderElements parsedCalenderElements = new KeelCronExpression.ParsedCalenderElements(now);
        planMap.forEach((key, plan) -> {
            if (plan.cronExpression().match(parsedCalenderElements)) {
                getLogger().debug("Sundial Plan Matched", new JsonObject()
                        .put("plan_key", plan.key())
                        .put("plan_cron", plan.cronExpression())
                        .put("now", parsedCalenderElements.toString())
                );

                // since 3.2.5
                var deploymentOptions = new DeploymentOptions();
                if (plan.isWorkerThreadRequired()) {
                    deploymentOptions.setThreadingModel(ThreadingModel.WORKER);
                }
                new KeelSundialVerticle(plan, now).deployMe(deploymentOptions);
            } else {
                getLogger().debug("Sundial Plan Not Match", new JsonObject()
                        .put("plan_key", plan.key())
                        .put("plan_cron", plan.cronExpression())
                        .put("now", parsedCalenderElements.toString())
                );
            }
        });
    }

    /**
     * @since 3.2.4
     */
    private void refreshPlans() {
        KeelAsyncKit.exclusivelyCall(
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
        );
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
