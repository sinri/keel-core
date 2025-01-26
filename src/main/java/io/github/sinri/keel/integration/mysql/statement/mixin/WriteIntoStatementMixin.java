package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @since 3.2.21
 */
public interface WriteIntoStatementMixin extends ModifyStatementMixin {
    /**
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<Long> executeForLastInsertedID(@Nonnull NamedMySQLConnection namedMySQLConnection) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
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
