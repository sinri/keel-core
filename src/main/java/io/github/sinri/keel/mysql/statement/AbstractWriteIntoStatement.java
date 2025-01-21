package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.statement.mixin.WriteIntoStatementMixin;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.2.21
 */
public class AbstractWriteIntoStatement extends AbstractModifyStatement implements WriteIntoStatementMixin {
    /**
     * @param sqlConnection get from pool
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 1.7
     * @since 1.10, removed the recover block
     */
    @Override
    public Future<Long> executeForLastInsertedID(@Nonnull SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }

}
