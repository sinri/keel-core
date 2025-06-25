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

    T context(@Nonnull JsonObject context);

    default T context(@Nonnull Handler<JsonObject> contextHandler) {
        JsonObject context = new JsonObject();
        contextHandler.handle(context);
        return context(context);
    }

    default T context(@Nonnull String name, @Nullable Object item) {
        var context = attributes().readJsonObject(AttributeContext);
        if (context == null) {
            context = new JsonObject();
        }
        context.put(name, item);
        return context(context);
    }
}
