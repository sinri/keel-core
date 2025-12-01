package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.verticles.AbstractKeelVerticle;
import io.github.sinri.keel.core.utils.ReflectionUtils;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * 定时任务计划的封装运行 Verticle 类。
 *
 * @since 5.0.0
 */
final class KeelSundialVerticle extends AbstractKeelVerticle {
    @NotNull
    private final KeelSundialPlan sundialPlan;
    @NotNull
    private final Calendar now;
    @NotNull
    private final SpecificLogger<SundialSpecificLog> sundialSpecificLogger;

    public KeelSundialVerticle(
            @NotNull Keel keel,
            @NotNull KeelSundialPlan sundialPlan,
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
              .compose(v -> sundialPlan.execute(now, sundialSpecificLogger))
              .onComplete(ar -> undeployMe());
        return Future.succeededFuture();
    }

    /**
     * 如果定时任务计划指定在 WORKER 线程模型下运行，则以此部署；否则，先尝试以虚拟线程模型部署，不行就用事件循环模式部署。
     *
     * @return 部署结果
     */
    @NotNull
    public Future<String> deployMe() {
        var deploymentOptions = new DeploymentOptions();
        if (sundialPlan.isWorkerThreadRequired()) {
            deploymentOptions.setThreadingModel(ThreadingModel.WORKER);
        } else if (ReflectionUtils.isVirtualThreadsAvailable()) {
            deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
        }
        return super.deployMe(deploymentOptions);
    }
}
