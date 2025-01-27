package io.github.sinri.keel.integration.mysql.result.stream;

import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;

/**
 * @since 4.0.0
 */
public interface ResultStreamReader {
    static <T> T mapRowToEntity(Row row, Class<T> clazz) {
        return row.toJson().mapTo(clazz);
    }

    static <R extends ResultRow> R mapRowToResultRow(Row row, Class<R> clazz) {
        return ResultRow.of(row, clazz);
    }

    Future<Void> read(Row row);
}
