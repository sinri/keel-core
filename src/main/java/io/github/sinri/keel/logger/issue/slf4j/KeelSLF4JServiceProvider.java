package io.github.sinri.keel.logger.issue.slf4j;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * The SLF4J Service Provider for Keel.
 * <p>
 * To autoload this service provider, put the class full name to
 * {@code src/main/resources/META-INF/services/org.slf4j.spi.SLF4JServiceProvider}.
 * <p>
 * Then you can create loggers by {@link org.slf4j.LoggerFactory#getLogger(String)}.
 *
 * @since 4.1.1
 */
@TechnicalPreview(since = "4.1.1")
public class KeelSLF4JServiceProvider implements SLF4JServiceProvider {
    private KeelIssueRecordCenter keelIssueRecordCenter;
    private KeelLoggerFactory keelLoggerFactory;

    @Override
    final public ILoggerFactory getLoggerFactory() {
        return keelLoggerFactory;
    }

    @Override
    final public IMarkerFactory getMarkerFactory() {
        return null;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return null;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.9";
    }

    @Override
    final public void initialize() {
        keelIssueRecordCenter = buildIssueRecordCenter();
        keelLoggerFactory = new KeelLoggerFactory(keelIssueRecordCenter);
    }

    /**
     * Override this method to customize the issue record center.
     *
     * @return the issue record center.
     */
    protected KeelIssueRecordCenter buildIssueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }
}
