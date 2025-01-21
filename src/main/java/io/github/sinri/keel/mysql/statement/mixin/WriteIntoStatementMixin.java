package io.github.sinri.keel.mysql.statement.mixin;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

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
    Future<Long> executeForLastInsertedID(@Nonnull SqlConnection sqlConnection);

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<Long> executeForLastInsertedID(@Nonnull NamedMySQLConnection namedMySQLConnection) {
        return executeForLastInsertedID(namedMySQLConnection.getSqlConnection());
    }
}
