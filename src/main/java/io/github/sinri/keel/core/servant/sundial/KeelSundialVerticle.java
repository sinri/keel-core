package io.github.sinri.keel.core.servant.sundial;

import io.github.sinri.keel.base.verticles.KeelVerticleImpl;
import io.github.sinri.keel.logger.api.issue.IssueRecorder;
import io.github.sinri.keel.utils.ReflectionUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import java.util.Calendar;

/**
 * @since 3.2.4
 * @since 3.2.5 Used in KeelSundial
 * @since 4.0.0 become abstract
 */
final class KeelSundialVerticle extends KeelVerticleImpl {
    private final KeelSundialPlan sundialPlan;
    private final Calendar now;
    private final IssueRecorder<SundialIssueRecord> sundialIssueRecorder;

    public KeelSundialVerticle(
            @Nonnull KeelSundialPlan sundialPlan,
            @Nonnull Calendar now,
            @Nonnull IssueRecorder<SundialIssueRecord> sundialIssueRecorder
    ) {
        this.sundialPlan = sundialPlan;
        this.now = now;
        this.sundialIssueRecorder = sundialIssueRecorder;
    }

    @Override
    protected Future<Void> startVerticle() {
        Future.succeededFuture()
              .compose(v -> sundialPlan.execute(now, sundialIssueRecorder))
              .onComplete(ar -> undeployMe());
        return Future.succeededFuture();
    }

    /**
     * Deploys the current verticle with dynamic threading behavior based on the requirements
     * of the associated sundial plan and the availability of virtual threads.
     * <ul>
     * <li>If the sundial plan requires a worker thread, the verticle will be deployed
     * using a worker threading model.</li>
     * <li>If virtual threads are available and worker threads are not specifically required,
     * the verticle will be deployed using a virtual threading model.</li>
     * <li>If neither condition is met, the default threading model will be used.</li>
     * </ul>
     *
     * @return a future that completes with the deployment ID if the deployment is successful,
     *         or fails with an exception if the deployment fails.
     * @since 4.1.3
     */
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
