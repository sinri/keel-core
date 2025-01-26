package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.matrix.ResultRow;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.2.21
 */
public interface ReadStatementMixin extends AnyStatement {
    /**
     * @param namedMySQLConnection NamedMySQLConnection
     * @param classT               class of type of result object
     * @param <T>                  type of result object
     * @return 查询到数据，异步返回第一行数据封装的指定类实例；查询不到时异步返回null。
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default <T extends ResultRow> Future<T> queryForOneRow(@Nonnull NamedMySQLConnection namedMySQLConnection, @Nonnull Class<T> classT) {
        return ResultRow.fetchResultRow(namedMySQLConnection, this, classT);
    }

    /**
     * @param classT class of type of result object
     * @param <T>    type of result object
     * @return 查询到数据，异步返回所有行数据封装的指定类实例；查询不到时异步返回null。
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default <T extends ResultRow> Future<List<T>> queryForRowList(@Nonnull NamedMySQLConnection namedMySQLConnection, @Nonnull Class<T> classT) {
        return ResultRow.fetchResultRows(namedMySQLConnection, this, classT);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            @Nonnull NamedMySQLConnection namedMySQLConnection,
            @Nonnull Class<T> classT,
            @Nonnull Function<T, K> categoryGenerator
    ) {
        return ResultRow.fetchResultRowsToCategorizedMap(namedMySQLConnection, this, classT, categoryGenerator);
    }


    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            @Nonnull NamedMySQLConnection namedMySQLConnection,
            @Nonnull Class<T> classT,
            @Nonnull Function<T, K> uniqueKeyGenerator
    ) {
        return ResultRow.fetchResultRowsToUniqueKeyBoundMap(namedMySQLConnection, this, classT, uniqueKeyGenerator);
    }

    /**
     * @since 3.3.0
     */
    default Future<Void> stream(
            @Nonnull NamedMySQLConnection namedMySQLConnection,
            Function<Row, Future<Void>> readRowFunction
    ) {
        return namedMySQLConnection.getSqlConnection()
                .prepare(toString())
                .compose(preparedStatement -> {
                    Cursor cursor = preparedStatement.cursor();

                    return Keel.asyncCallRepeatedly(routineResult -> {
                                if (!cursor.hasMore()) {
                                    routineResult.stop();
                                    return Future.succeededFuture();
                                }

                                return cursor.read(1)
                                        .compose(rows -> {
                                            return Keel.asyncCallIteratively(rows, readRowFunction);
                                        });
                            })
                            .eventually(() -> cursor.close())
                            .eventually(() -> preparedStatement.close());
                });
    }


}
