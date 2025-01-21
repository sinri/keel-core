package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.statement.mixin.ModifyStatementMixin;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 1.10
 */
public abstract class AbstractModifyStatement extends AbstractStatement implements ModifyStatementMixin {

    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; failed future when failed
     * @since 1.7
     * @since 1.10 removed recover
     */
    @Override
    public Future<Integer> executeForAffectedRows(@Nonnull SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> {
                    var afx = resultMatrix.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }

    /**
     * @since 3.0.0
     */
    public static AbstractModifyStatement buildWithRawSQL(@Nonnull String sql) {
        return new AbstractModifyStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }
}
