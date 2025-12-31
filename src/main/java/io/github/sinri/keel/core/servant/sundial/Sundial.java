package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.github.sinri.keel.core.utils.cron.KeelCronExpression;
import io.github.sinri.keel.core.utils.cron.ParsedCalenderElements;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 日晷。
 * <p>
 * 单节点下的定时任务调度器。
 *
 * @since 5.0.0
 */
public abstract class Sundial extends AbstractKeelVerticle {

    private final @NotNull Map<@NotNull String, @NotNull SundialPlan> planMap = new ConcurrentHashMap<>();
    private @Nullable Long timerID;
    private @Nullable SpecificLogger<SundialSpecificLog> logger;

    public Sundial(@NotNull Keel keel) {
        super(keel);
    }

    abstract protected @NotNull LoggerFactory getLoggerFactory();

    protected @NotNull SpecificLogger<SundialSpecificLog> buildLogger() {
        return getLoggerFactory().createLogger(SundialSpecificLog.TopicSundial, SundialSpecificLog::new);
    }

    public final @NotNull SpecificLogger<SundialSpecificLog> getLogger() {
        return Objects.requireNonNull(logger);
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        this.logger = buildLogger();
        int delaySeconds = 61 - KeelCronExpression.parseCalenderToElements(Calendar.getInstance()).second;
        this.timerID = getVertx().setPeriodic(delaySeconds * 1000L, 60_000L, timerID -> {
            handleEveryMinute(Calendar.getInstance());
            refreshPlans();
        });
        return Future.succeededFuture();
    }

    private void handleEveryMinute(@NotNull Calendar now) {
        ParsedCalenderElements parsedCalenderElements = new ParsedCalenderElements(now);
        planMap.forEach((key, plan) -> {
            if (plan.cronExpression().match(parsedCalenderElements)) {
                getLogger().debug(x -> x
                        .message("Sundial Plan Matched")
                        .context("plan_key", plan.key())
                        .context("plan_cron", plan.cronExpression().getRawCronExpression())
                        .context("now", parsedCalenderElements.toString())
                );

                KeelVerticle.instant(
                                    getKeel(),
                                    keelVerticle -> plan.execute(
                                            keelVerticle.getKeel(),
                                            now,
                                            getLogger()
                                    )
                            )
                            .deployMe(new DeploymentOptions()
                                    .setThreadingModel(plan.threadingModel())
                            )
                            .onComplete(ar -> {
                                if (ar.failed()) {
                                    getLogger().error(log -> log
                                            .exception(ar.cause())
                                            .message("Failed to deploy verticle for " + plan.key())
                                    );
                                } else {
                                    getLogger().info(log -> log
                                            .message("Deployed verticle for " + plan.key() + " as " + ar.result())
                                    );
                                }
                            });
            } else {
                getLogger().debug(x -> x
                        .message("Sundial Plan Not Match")
                        .context("plan_key", plan.key())
                        .context("plan_cron", plan.cronExpression().getRawCronExpression())
                        .context("now", parsedCalenderElements.toString())
                );
            }
        });
    }

    private void refreshPlans() {
        getKeel().asyncCallExclusively(
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
                                     return Future.succeededFuture(null);
                                 })
                 )
                 .onFailure(throwable -> getLogger().error(log -> log
                         .exception(throwable)
                         .message("io.github.sinri.keel.core.servant.sundial.KeelSundial.refreshPlans exception"))
                 );
    }

    /**
     * 异步获取最新定时任务计划集，根据结果进行全量覆盖或保持不动。
     *
     * @return 异步返回的定时任务计划集，用于覆盖更新当前的计划快照；如果异步返回了 null，则表示不更新计划快照。
     */
    abstract protected @NotNull Future<@Nullable Collection<@NotNull SundialPlan>> fetchPlans();

    @Override
    protected @NotNull Future<Void> stopVerticle() {
        if (this.timerID != null) {
            getVertx().cancelTimer(this.timerID);
        }
        return Future.succeededFuture();
    }

    /**
     * 以 WORKER 模式部署。
     *
     * @return 部署结果
     */
    public final @NotNull Future<String> deployMe() {
        return super.deployMe(new DeploymentOptions()
                .setThreadingModel(ThreadingModel.WORKER));
    }
}
