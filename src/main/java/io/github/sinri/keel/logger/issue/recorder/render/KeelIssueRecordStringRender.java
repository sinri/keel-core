package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.core.helper.KeelDateTimeHelper;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.10
 */
public class KeelIssueRecordStringRender implements KeelIssueRecordRender<String> {
    private static final KeelIssueRecordStringRender instance = new KeelIssueRecordStringRender();

    protected KeelIssueRecordStringRender() {

    }

    public static KeelIssueRecordStringRender getInstance() {
        return instance;
    }

    @Nonnull
    @Override
    public String renderIssueRecord(@Nonnull String topic, @Nonnull KeelIssueRecord<?> issueRecord) {
        StringBuilder s = new StringBuilder("㏒ ");
        s.append(Keel.datetimeHelper()
                     .getDateExpression(issueRecord.timestamp(), KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN));
        s.append(" [").append(issueRecord.level().name()).append("]");
        s.append(" ").append(topic).append(" (")
         .append(Keel.stringHelper().joinStringArray(issueRecord.classification(), ",")).append(")");
        s.append(" on ").append(issueRecord.getThreadInfo());
        if (!issueRecord.attributes().isEmpty()) {
            issueRecord.attributes().forEach(attribute -> s.append("\n ▪ ").append(attribute.getKey()).append(": ")
                                                           .append(attribute.getValue()));
        }
        Throwable exception = issueRecord.exception();
        if (exception != null) {
            s.append("\n ⊹ Exception Thrown:\n").append(renderThrowable(exception));
        }
        return s.toString();
    }

    @Nonnull
    @Override
    public String renderThrowable(@Nonnull Throwable throwable) {
        return Keel.stringHelper().renderThrowableChain(throwable, ignorableStackPackageSet());
    }
}
