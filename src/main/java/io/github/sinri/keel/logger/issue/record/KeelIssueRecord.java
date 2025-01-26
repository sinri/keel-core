package io.github.sinri.keel.logger.issue.record;

/**
 * @param <T> the final implementation type.
 * @since 3.1.10
 */
public interface KeelIssueRecord<T> extends KeelIssueRecordCore<T>, IssueRecordMessageMixin<T>, IssueRecordContextMixin<T> {

}
