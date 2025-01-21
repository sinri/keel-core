package io.github.sinri.keel.mysql.statement.mixin;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.statement.AnyStatement;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.2.21
 */
public interface ModifyStatementMixin extends AnyStatement {
    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; failed future when failed
     * @since 1.7
     * @since 1.10 removed recover
     */
    Future<Integer> executeForAffectedRows(@Nonnull SqlConnection sqlConnection);

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<Integer> executeForAffectedRows(@Nonnull NamedMySQLConnection namedMySQLConnection) {
        return executeForAffectedRows(namedMySQLConnection.getSqlConnection());
    }
}
