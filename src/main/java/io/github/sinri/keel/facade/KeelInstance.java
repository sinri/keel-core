package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.helper.KeelHelpersInterface;
import io.github.sinri.keel.facade.async.KeelAsyncMixin;
import io.github.sinri.keel.facade.configuration.KeelConfigElement;
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
public final class KeelInstance implements KeelHelpersInterface, KeelAsyncMixin, KeelWebRequestMixin {
    public final static KeelInstance Keel;

    static {
        Keel = new KeelInstance();

        // As of 4.1.3
        String loggingProperty = System.getProperty("vertx.logger-delegate-factory-class-name");
        if (loggingProperty == null) {
            // 显式设置 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常
            // 必须在任何 Vert.x 类被加载之前设置此属性
            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.JULLogDelegateFactory");
        }
    }

    /**
     * @since 3.2.3
     */
    private final @Nonnull KeelConfigElement configuration;
    /**
     * @since 4.1.1
     */
    private @Nonnull KeelIssueRecordCenter issueRecordCenter;
    /**
     * As of 4.1.1, it is not final.
     *
     * @since 4.0.0
     */
    private KeelIssueRecorder<KeelEventLog> logger;
    private @Nullable Vertx vertx;
    private @Nullable ClusterManager clusterManager;

    private KeelInstance() {
        this.configuration = new KeelConfigElement("");
        setIssueRecordCenter(KeelIssueRecordCenter.outputCenter());
    }

    /**
     * @since 4.1.1
     */
    public void setIssueRecordCenter(@Nonnull KeelIssueRecordCenter issueRecordCenter) {
        this.issueRecordCenter = issueRecordCenter;
        this.resetLogger();
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
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
        this.clusterManager = clusterManager;
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
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
        this.clusterManager = null;
        this.vertx = Vertx.builder().with(vertxOptions).build();
    }

    /**
     * This method is designed for Unit Test with JUnit5, in {@code @BeforeEach} methods or constructor.
     * <p>
     * Do not call this method in your own code!
     *
     * @since 4.1.1
     */
    @TechnicalPreview(since = "4.1.1")
    public void initializeVertx(@Nonnull Vertx vertx) {
        if (isVertxInitialized() && this.vertx != vertx) {
            Keel.getLogger().info("Re-initialize Vertx from " + this.vertx + " to " + vertx + ".");
            this.vertx.close();
        }
        this.vertx = vertx;
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

    /**
     * @since 4.1.1
     */
    private void resetLogger() {
        this.logger = this.issueRecordCenter.generateIssueRecorder("Keel", KeelEventLog::new);

        String level = System.getProperty("keel.default-logger-level", "WARNING");
        KeelLogLevel keelLogLevel;
        try {
            keelLogLevel = KeelLogLevel.valueOf(level);
        } catch (IllegalArgumentException e) {
            keelLogLevel = KeelLogLevel.WARNING;
        }
        this.logger.setVisibleLevel(keelLogLevel);
    }

    public Future<Void> gracefullyClose(@Nonnull io.vertx.core.Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future()
                      .compose(v -> {
                          return getVertx().close();
                      })
                      .compose(closed -> {
                          this.vertx = null;
                          this.clusterManager = null;
                          return Future.succeededFuture();
                      });
    }

    public Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }
}
