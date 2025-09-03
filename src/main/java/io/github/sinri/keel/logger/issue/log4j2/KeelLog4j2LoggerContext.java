package io.github.sinri.keel.logger.issue.log4j2;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

@TechnicalPreview(since = "4.1.3")
public final class KeelLog4j2LoggerContext implements LoggerContext {
    private final Map<String, KeelLog4j2Logger> loggerMap;
    @Nonnull
    private final Supplier<KeelIssueRecorderAdapter> adapterSupplier;
    @Nonnull
    private final KeelLogLevel visibleBaseLevel;
    @Nullable
    private final Handler<KeelEventLog> issueRecordInitializer;

    public KeelLog4j2LoggerContext(
            @Nonnull Supplier<KeelIssueRecorderAdapter> adapterSupplier,
            @Nonnull KeelLogLevel visibleBaseLevel,
            @Nullable Handler<KeelEventLog> issueRecordInitializer
    ) {
        this.loggerMap = new ConcurrentHashMap<>();
        this.adapterSupplier = adapterSupplier;
        this.visibleBaseLevel = visibleBaseLevel;
        this.issueRecordInitializer = issueRecordInitializer;
    }

    @Override
    public Object getExternalContext() {
        return null;
    }

    @Override
    public ExtendedLogger getLogger(String name) {
        if (loggerMap.containsKey(name)) {
            return loggerMap.get(name);
        } else {
            synchronized (loggerMap) {
                KeelLog4j2Logger existed = loggerMap.get(name);
                if (existed == null) {
                    var logger = new KeelLog4j2Logger(this.adapterSupplier, visibleBaseLevel, name, issueRecordInitializer);
                    loggerMap.put(name, logger);
                    Keel.getLogger().notice("Keel Logging for log4j built logger for [" + name + "]");
                    return logger;
                } else {
                    return existed;
                }
            }
        }
    }

    @Override
    public ExtendedLogger getLogger(String name, MessageFactory messageFactory) {
        return getLogger(name);
    }

    @Override
    public boolean hasLogger(String name) {
        return this.loggerMap.containsKey(name);
    }

    @Override
    public boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass) {
        return hasLogger(name);
    }

    @Override
    public boolean hasLogger(String name, MessageFactory messageFactory) {
        return hasLogger(name);
    }
}
