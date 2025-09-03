package io.github.sinri.keel.logger.issue.record;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.2.0
 * @since 4.0.0 package protected
 */
interface IssueRecordContextMixin<T> extends KeelIssueRecordCore<T> {
    String AttributeContext = "context";

    /**
     * @deprecated as of 4.1.3, it should not be used in production, as fully rewrite context is dangerous and not
     *         commended.
     */
    @Deprecated(since = "4.1.3", forRemoval = true)
    default T context(@Nonnull JsonObject context) {
        return context(j -> j.mergeIn(context));
    }

    T context(@Nonnull Handler<JsonObject> contextHandler);

    default T context(@Nonnull String name, @Nullable Object item) {
        return context(j -> j.put(name, item));
    }
}
