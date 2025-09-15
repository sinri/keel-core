package io.github.sinri.keel.integration.mysql.statement.mixin;

import io.github.sinri.keel.integration.mysql.NamedMySQLConnection;
import io.github.sinri.keel.integration.mysql.statement.AnyStatement;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * @since 3.2.21
 */
public interface ModifyStatementMixin extends AnyStatement {

    /**
     * As of 3.0.18 Finished Technical Preview.
     *
     * @return future with affected rows; failed future when failed
     * @since 3.0.11
     */
    default Future<Integer> executeForAffectedRows(@Nonnull NamedMySQLConnection namedMySQLConnection) {
        return execute(namedMySQLConnection)
                .compose(resultMatrix -> {
                    var afx = resultMatrix.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }
}
