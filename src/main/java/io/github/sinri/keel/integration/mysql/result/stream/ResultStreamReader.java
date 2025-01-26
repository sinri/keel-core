package io.github.sinri.keel.integration.mysql.result.stream;

import io.vertx.core.Future;
import io.vertx.sqlclient.Row;

/**
 * @since 3.3.0
 */
public interface ResultStreamReader {
    static <T> T mapRowToEntity(Row row, Class<T> clazz) {
        return row.toJson().mapTo(clazz);
    }

    Future<Void> read(Row row);
}
