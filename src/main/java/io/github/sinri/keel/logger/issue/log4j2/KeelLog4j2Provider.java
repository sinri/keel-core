package io.github.sinri.keel.logger.issue.log4j2;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.Provider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

@TechnicalPreview(since = "4.1.3")
public class KeelLog4j2Provider extends Provider {
    public static final int DEFAULT_PRIORITY = 50;
    public static final String DEFAULT_VERSIONS = "2.x";
    private volatile KeelLog4j2LoggerContextFactory loggerContextFactory;

    public KeelLog4j2Provider() {
        this(DEFAULT_PRIORITY, DEFAULT_VERSIONS);
    }

    protected KeelLog4j2Provider(int priority, String versions) {
        super(priority, versions);
    }

    @Nonnull
    @Override
    public LoggerContextFactory getLoggerContextFactory() {
        if (loggerContextFactory == null) {
            synchronized (this) {
                if (loggerContextFactory == null) {
                    loggerContextFactory = new KeelLog4j2LoggerContextFactory(
                            getAdapterSupplier(),
                            getVisibleBaseLevel(),
                            getIssueRecordInitializer()
                    );
                }
            }
        }
        return loggerContextFactory;
    }

    @Override
    public @Nullable Class<? extends LoggerContextFactory> loadLoggerContextFactory() {
        // 按照 Log4j SPI 的设计原意，此方法只负责返回工厂类型
        // 实际的实例化工作由 getLoggerContextFactory() 负责
        return KeelLog4j2LoggerContextFactory.class;
    }

    /**
     * Provides the visible base level for logging.
     * <p>
     * Override this method to customize the minimum log level that will be
     * processed.
     *
     * @return the minimum {@link KeelLogLevel} that will be processed
     */
    @Nonnull
    protected KeelLogLevel getVisibleBaseLevel() {
        return KeelLogLevel.INFO;
    }

    /**
     * Provides a {@link Supplier} that supplies the
     * {@link KeelIssueRecorderAdapter} instance
     * associated with the output center of the {@link KeelIssueRecordCenter}.
     *
     * <p>
     * Override this method to use another {@link KeelIssueRecorderAdapter} to
     * record issues.
     *
     * @return a {@link Supplier} that retrieves the
     *         {@link KeelIssueRecorderAdapter} from the
     *         {@link KeelIssueRecordCenter#outputCenter()}.
     */
    @Nonnull
    protected Supplier<KeelIssueRecorderAdapter> getAdapterSupplier() {
        return () -> KeelIssueRecordCenter.outputCenter().getAdapter();
    }

    @Nullable
    protected Handler<KeelEventLog> getIssueRecordInitializer() {
        return null;
    }
}
