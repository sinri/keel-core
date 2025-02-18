package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.helper.KeelHelpersInterface;
import io.github.sinri.keel.facade.async.KeelAsyncMixin;
import io.github.sinri.keel.facade.cluster.KeelClusterKit;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import io.github.sinri.keel.facade.launcher.KeelLauncher;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.requester.KeelWebRequestMixin;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @since 3.1.0
 * @since 4.0.0 make it final and implement KeelAsyncMixin.
 */
public final class KeelInstance implements KeelHelpersInterface, KeelClusterKit, KeelAsyncMixin, KeelWebRequestMixin {
    public final static KeelInstance Keel = new KeelInstance();

    /**
     * @since 3.2.3
     */
    private final @Nonnull KeelConfigElement configuration;
    /**
     * @since 4.0.0
     */
    private final KeelIssueRecorder<KeelEventLog> logger;
    private @Nullable Vertx vertx;
    private @Nullable ClusterManager clusterManager;

    private KeelInstance() {
        this.configuration = new KeelConfigElement("");
        this.logger = KeelIssueRecordCenter.outputCenter().generateIssueRecorder("Keel", KeelEventLog::new);
        this.logger.setVisibleLevel(KeelLogLevel.WARNING);
    }

    @Nonnull
    public KeelConfigElement getConfiguration() {
        return configuration;
    }

    public @Nullable String config(@Nonnull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        KeelConfigElement keelConfigElement = this.configuration.extract(split);
        if (keelConfigElement == null) {
            return null;
        }
        return keelConfigElement.getValueAsString();
    }

    public @Nonnull Vertx getVertx() {
        Objects.requireNonNull(vertx);
        return vertx;
    }

    /**
     * Used in {@link KeelLauncher#afterStartingVertx(Vertx)}. Do not use in other way without completely tests.
     */
    public void setVertx(@Nonnull Vertx outsideVertx) {
        getLogger().debug(r -> r
                .message("KeelInstance::setVertx is called with outsideVertx " + outsideVertx + " while currently " +
                        "vertx is " + vertx));
        if (vertx == null) {
            vertx = outsideVertx;
        } else {
            throw new IllegalStateException("Vertx Already Initialized");
        }
    }

    @Nullable
    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public Future<Void> initializeVertx(@Nonnull VertxOptions vertxOptions) {
        return initializeVertx(vertxOptions, null);
    }

    public Future<Void> initializeVertx(
            @Nonnull VertxOptions vertxOptions,
            @Nullable ClusterManager clusterManager
    ) {
        this.clusterManager = clusterManager;
        if (clusterManager == null && vertxOptions.getClusterManager() != null) {
            this.clusterManager = vertxOptions.getClusterManager();
        }
        if (this.clusterManager == null) {
            this.vertx = Vertx.builder().with(vertxOptions).withClusterManager(null).build();
            return Future.succeededFuture();
        } else {
            return Vertx.builder().with(vertxOptions).withClusterManager(clusterManager).buildClustered()
                        .compose(x -> {
                            this.vertx = x;
                            return Future.succeededFuture();
                        });
        }
    }

    public void initializeVertxStandalone(@Nonnull VertxOptions vertxOptions) {
        this.clusterManager = null;
        this.vertx = Vertx.builder().with(vertxOptions).build();
    }

    public boolean isVertxInitialized() {
        return vertx != null;
    }

    public boolean isRunningInVertxCluster() {
        return isVertxInitialized() && getVertx().isClustered();
    }

    /**
     * @since 4.0.2 To acquire an instant logger for those logs without designed topic. By default, it is print to
     *         stdout and only WARNING and above may be recorded. If you want to debug locally, just get it and reset
     *         its visible level.
     */
    public KeelIssueRecorder<KeelEventLog> getLogger() {
        return logger;
    }


    public Future<Void> gracefullyClose(@Nonnull io.vertx.core.Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future()
                      .compose(v -> getVertx().close());
    }

    public Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }
}
