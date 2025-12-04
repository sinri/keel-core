package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * 定时任务计划的封装运行 Verticle 类。
 *
 * @since 5.0.0
 */
final class SundialVerticle extends AbstractKeelVerticle {
    @NotNull
    private final SundialPlan sundialPlan;
    @NotNull
    private final Calendar now;
    @NotNull
    private final SpecificLogger<SundialSpecificLog> sundialSpecificLogger;

    public SundialVerticle(
            @NotNull Keel keel,
            @NotNull SundialPlan sundialPlan,
            @NotNull Calendar now,
            @NotNull SpecificLogger<SundialSpecificLog> sundialSpecificLogger
    ) {
        super(keel);
        this.sundialPlan = sundialPlan;
        this.now = now;
        this.sundialSpecificLogger = sundialSpecificLogger;
    }

    @Override
    protected @NotNull Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> sundialPlan.execute(getKeel(), now, sundialSpecificLogger))
              .onComplete(ar -> undeployMe());
        return Future.succeededFuture();
    }

    /**
     *
     * @return 部署结果
     */
    @NotNull
    public Future<String> deployMe() {
        var deploymentOptions = new DeploymentOptions();
        deploymentOptions.setThreadingModel(sundialPlan.threadingModel());
        return super.deployMe(deploymentOptions);
    }
}
