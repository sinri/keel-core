package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;

/**
 * @param <T>
 * @since 4.0.1
 */
public interface KeelIssueRecorderCommonMixin<T extends KeelIssueRecord<T>> extends KeelIssueRecorderCore<T> {
    default void exception(@Nonnull Throwable throwable, @Nonnull String message) {
        exception(throwable, t -> t.message(message));
    }

    default void exception(@Nonnull Throwable throwable) {
        exception(throwable, t -> {
        });
    }

    default void debug(@Nonnull String message) {
        debug(t -> t.message(message));
    }

    default void info(@Nonnull String message) {
        info(t -> t.message(message));
    }

    default void notice(@Nonnull String message) {
        notice(t -> t.message(message));
    }

    default void warning(@Nonnull String message) {
        warning(t -> t.message(message));
    }

    default void error(@Nonnull String message) {
        error(t -> t.message(message));
    }

    default void fatal(@Nonnull String message) {
        fatal(t -> t.message(message));
    }
}
