package io.github.sinri.keel.facade.launcher;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Adapter for the KeelLauncher, providing lifecycle hooks and a method to launch the application.
 * This interface is designed to be implemented by classes that need to customize the behavior of the KeelLauncher.
 *
 * @since 3.0.10 Technical Preview
 */
public interface KeelLauncherAdapter extends VertxLifecycleHooks {

    /**
     * Run this in main. Do not override this.
     *
     * @param args refer to the main.
     */
    default void launch(String[] args) {
        this.launcher().dispatch(args);
    }

    /**
     * Create a launcher. Do not override this.
     */
    default @Nonnull KeelLauncher launcher() {
        return new KeelLauncher(this);
    }

    /**
     * @since 4.0.2
     */
    default @Nonnull KeelIssueRecorder<KeelEventLog> buildIssueRecorderForLauncher() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder(getClass().getName(), KeelEventLog::new);
    }

    void beforeStoppingVertx();

    void afterStoppingVertx();

    @Override
    default void handleDeployFailed(
            Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause
    ) {
        vertx.close();
    }

    default @Nullable String getDefaultCommand() {
        return null;
    }

    default @Nullable String getMainVerticle() {
        return null;
    }

}
