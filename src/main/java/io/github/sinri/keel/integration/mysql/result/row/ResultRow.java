package io.github.sinri.keel.integration.mysql.result.row;

import io.github.sinri.keel.core.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.function.Function;

/**
 * Dump an instance of class {@link Row}  to {@link JsonObject},
 * and then wrap it to a {@link ResultRow} instance.
 *
 * @since 2.7
 */
public interface ResultRow extends JsonifiableDataUnit {
    /**
     * @since 4.0.0
     */
    static <R extends ResultRow> R of(@Nonnull JsonObject tableRow, Class<R> clazz) {
        try {
            Constructor<R> constructor = clazz.getConstructor(JsonObject.class);
            return constructor.newInstance(tableRow);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 4.0.0
     */
    static <R extends ResultRow> R of(@Nonnull Row tableRow, Class<R> clazz) {
        return of(tableRow.toJson(), clazz);
    }

    static JsonArray batchToJsonArray(@Nonnull Collection<? extends ResultRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    static JsonArray batchToJsonArray(@Nonnull Collection<? extends ResultRow> rows, @Nonnull Function<ResultRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    default JsonObject getRow() {
        return toJsonObject();
    }

    /**
     * @since 2.8
     * @since 2.9.4 fix null field error
     */
    @Nullable
    default String readDateTime(@Nonnull String field) {
        String s = readString(field);
        if (s == null) return null;
        return LocalDateTime.parse(s)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Nullable
    default String readDate(@Nonnull String field) {
        return readString(field);
    }

    /**
     * @since 2.9.4 fix null field error
     */
    @Nullable
    default String readTime(@Nonnull String field) {
        var s = readString(field);
        if (s == null) return null;
        return s
                .replaceAll("[PTS]+", "")
                .replaceAll("[HM]", ":");
    }

    @Nullable
    default String readTimestamp(@Nonnull String field) {
        return readDateTime(field);
    }
}
