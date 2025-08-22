package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.cache.KeelEverlastingCacheInterface;
import io.github.sinri.keel.core.cache.NotCached;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelLoggerFactory implements ILoggerFactory {
    private final @Nonnull KeelIssueRecordCenter issueRecordCenter;
    private final KeelEverlastingCacheInterface<String, Logger> loggerCache = KeelEverlastingCacheInterface.createDefaultInstance();

    public KeelLoggerFactory(@Nonnull KeelIssueRecordCenter issueRecordCenter) {
        this.issueRecordCenter = issueRecordCenter;
    }

    @Override
    public Logger getLogger(String name) {
        try {
            return loggerCache.read(name);
        } catch (NotCached e) {
            synchronized (loggerCache) {
                var logger = new KeelSlf4jLogger(this.issueRecordCenter, KeelLogLevel.DEBUG, name);
                loggerCache.save(name, logger);
                return logger;
            }
        }
    }
}
