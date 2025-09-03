package io.github.sinri.keel.logger.issue.log4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.function.Supplier;

@TechnicalPreview(since = "4.1.3")
public final class KeelLog4jLoggerContextFactory implements LoggerContextFactory {

    private final KeelLog4jLoggerContext loggerContext;

    public KeelLog4jLoggerContextFactory(@Nonnull Supplier<KeelIssueRecorderAdapter> adapterSupplier,
                                         @Nonnull KeelLogLevel visibleBaseLevel) {
        this.loggerContext = new KeelLog4jLoggerContext(adapterSupplier, visibleBaseLevel);
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext) {
        return this.loggerContext;
    }

    @Override
    public LoggerContext getContext(String fqcn, ClassLoader loader, Object externalContext, boolean currentContext, URI configLocation, String name) {
        return this.loggerContext;
    }

    @Override
    public void removeContext(LoggerContext context) {
        // do nothing
    }
}
