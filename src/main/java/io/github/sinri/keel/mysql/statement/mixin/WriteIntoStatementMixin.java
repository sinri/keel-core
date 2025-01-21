package io.github.sinri.keel.mysql.statement.mixin;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 3.2.21
 */
public interface WriteIntoStatementMixin extends ModifyStatementMixin {
    /**
     * @param sqlConnection get from pool
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 1.7
     * @since 1.10, removed the recover block
     */
    default Future<Long> executeForLastInsertedID(@Nonnull SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<Long> executeForLastInsertedID(@Nonnull NamedMySQLConnection namedMySQLConnection) {
        return executeForLastInsertedID(namedMySQLConnection.getSqlConnection());
    }

    /**
     * 按照最大块尺寸分裂！
     *
     * @param chunkSize an integer
     * @return a list of WriteIntoStatement
     * @since 2.3
     */
    List<WriteIntoStatementMixin> divide(int chunkSize);
}
