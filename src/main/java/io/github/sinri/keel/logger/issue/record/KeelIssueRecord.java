package io.github.sinri.keel.logger.issue.record;

/**
 * @param <T> the final implementation type.
 * @since 3.1.10
 * @since 4.0.0
 */
public interface KeelIssueRecord<T> extends KeelIssueRecordCore<T>, IssueRecordMessageMixin<T>, IssueRecordContextMixin<T> {

}
