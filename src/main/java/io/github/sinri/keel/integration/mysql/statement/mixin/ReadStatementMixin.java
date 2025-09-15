package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.integration.mysql.result.row.ResultRow;
import io.github.sinri.keel.integration.mysql.result.stream.ResultStreamReader;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.Cursor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.2.21
 */
public interface ReadStatementMixin extends AnyStatement {
    /**
     * As of 3.0.18 Finished Technical Preview.
     *
     * @param namedMySQLConnection NamedMySQLConnection
     * @param classT               class of type of result object
     * @param <T>                  type of result object
     * @return 查询到数据，异步返回第一行数据封装的指定类实例；查询不到时异步返回null。
     * @since 3.0.11
     */
    default <T extends ResultRow> Future<T> queryForOneRow(@Nonnull NamedMySQLConnection namedMySQLConnection, @Nonnull Class<T> classT) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    try {
                        T t = resultMatrix.buildTableRowByIndex(0, classT);
                        return Future.succeededFuture(t);
                    } catch (KeelSQLResultRowIndexError e) {
                        return Future.succeededFuture(null);
                    }
                });
    }

    /**
     * @param classT class of type of result object
     * @param <T>    type of result object
     * @return 查询到数据，异步返回所有行数据封装的指定类实例；查询不到时异步返回null。
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default <T extends ResultRow> Future<List<T>> queryForRowList(@Nonnull NamedMySQLConnection namedMySQLConnection, @Nonnull Class<T> classT) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    List<T> ts = resultMatrix.buildTableRowList(classT);
                    return Future.succeededFuture(ts);
                });
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
        Map<K, List<T>> map = new HashMap<>();
        return queryForRowList(namedMySQLConnection, classT)
                .compose(list -> {
                    list.forEach(item -> {
                        K category = categoryGenerator.apply(item);
                        map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
                    });
                    return Future.succeededFuture(map);
                });
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
        Map<K, T> map = new HashMap<>();

        return queryForRowList(namedMySQLConnection, classT)
                .compose(list -> {
                    list.forEach(item -> {
                        K uniqueKey = uniqueKeyGenerator.apply(item);
                        map.put(uniqueKey, item);
                    });
                    return Future.succeededFuture(map);
                });
    }

    /**
     * @since 4.0.0
     */
    default Future<Void> stream(
            @Nonnull NamedMySQLConnection namedMySQLConnection,
            @Nonnull ResultStreamReader resultStreamReader
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
                                                                   .compose(rows -> Keel.asyncCallIteratively(rows, resultStreamReader::read));
                                                  })
                                                  .eventually(cursor::close)
                                                  .eventually(preparedStatement::close);
                                   });
    }


}
